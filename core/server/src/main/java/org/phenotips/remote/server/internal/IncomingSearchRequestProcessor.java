/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.remote.server.internal;

import org.phenotips.remote.adapters.XWikiAdapter;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.api.TypedWrapperInterface;
import org.phenotips.remote.api.WrapperInterface;
import org.phenotips.remote.server.RequestProcessorInterface;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.mailsender.MailSender;
import com.xpn.xwiki.plugin.mailsender.MailSenderPlugin;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

import net.sf.json.JSONObject;

/**
 * Takes a json string in the constructor and does all the request processing functionality.
 */
@Component
@Singleton
public class IncomingSearchRequestProcessor implements RequestProcessorInterface
{
    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Inject
    private Execution execution;

    @Inject
    @Named("json-patient")
    private WrapperInterface<JSONObject, HibernatePatientInterface> patientWrapper;

    @Inject
    @Named("json-meta")
    private WrapperInterface<JSONObject, IncomingSearchRequestInterface> metaWrapper;

    @Inject
    @Named("incoming-json")
    private TypedWrapperInterface<IncomingSearchRequestInterface, JSONObject> requestWrapper;

    public JSONObject processHTTPRequest(String stringJson, ExecutorService queue, HttpServletRequest httpRequest)
        throws Exception
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");

        /*
        First things first. Is the request authorized? If not, should not be able to continue at all. This is the
        first, and only line of defence.
        */
        BaseObject configurationObject = getConfigurationObject(context, httpRequest);
        Integer authorizationStatus = validateRequest(httpRequest, context, configurationObject);
        if (!authorizationStatus.equals(Configuration.HTTP_OK)) {
            JSONObject authorizationJSON = new JSONObject();
            authorizationJSON.put(Configuration.INTERNAL_JSON_STATUS, authorizationStatus);
            return authorizationJSON;
        }

        //FIXME. Should not be requested under Admin.
//        context.setUserReference(
//            new DocumentReference(context.getMainXWiki(), Configuration.REST_DEFAULT_USER_SPACE,
//                Configuration.REST_DEFAULT_USER_NAME));
        context.setUserReference(new DocumentReference(context.getMainXWiki(), "XWiki", "Admin"));

        //XWiki cannot find the context through (XWiki) Execution when called inside (Java) Executor.
        Callable<JSONObject> task = new ProcessingQueueTask(stringJson, queue, httpRequest, this, configurationObject);
        Future<JSONObject> responseFuture = queue.submit(task);
        return responseFuture.get();
    }

    private BaseObject getConfigurationObject(XWikiContext context, HttpServletRequest httpRequest)
        throws XWikiException
    {
        XWiki wiki = context.getWiki();
        String key = httpRequest.getParameter(Configuration.URL_KEY_PARAMETER);
        return XWikiAdapter.getRemoteConfigurationByKey(key, wiki, context);
    }

    private Integer validateRequest(HttpServletRequest httpRequest, XWikiContext context,
        BaseObject configurationObject) throws XWikiException, UnknownHostException, MalformedURLException
    {
        Boolean isAuthorized = false;
        JSONObject authorizationJSON = new JSONObject();

        String baseURL = configurationObject.getStringValue(Configuration.REMOTE_BASE_URL_FIELD);
        try {
            isAuthorized = validateIP(baseURL, httpRequest.getRemoteAddr());
        } catch (MalformedURLException ex) {
            return Configuration.HTTP_SERVER_ERROR;
        } catch (UnknownHostException ex) {
            return Configuration.HTTP_BAD_REQUEST;
        }
        if (!isAuthorized) {
            return Configuration.HTTP_UNAUTHORIZED;
        }
        return Configuration.HTTP_OK;
    }

    private boolean validateIP(String baseURL, String ip) throws UnknownHostException, MalformedURLException
    {
        URL url = new URL(baseURL);
        InetAddress address = InetAddress.getByName(url.getHost());
        String resolvedIP = address.getHostAddress();
        return StringUtils.equalsIgnoreCase(resolvedIP, ip);
    }

    public JSONObject internalProcessing(String stringJson, ExecutorService queue, BaseObject configurationObject) throws Exception
    {
        JSONObject json = JSONObject.fromObject(stringJson);
        Session session = this.sessionFactory.getSessionFactory().openSession();

        String format = configurationObject.getStringValue(Configuration.REMOTE_RESPONSE_FORMAT);
        RequestHandlerInterface<IncomingSearchRequestInterface> requestHandler =
            new IncomingRequestHandler(json, patientWrapper, metaWrapper, format);
        IncomingSearchRequestInterface request = requestHandler.createRequest();
        if (!request.getHTTPStatus().equals(Configuration.HTTP_OK)) {
            return requestWrapper.wrap(request);
        }

        String type = request.getResponseType();
        if (StringUtils.equalsIgnoreCase(type, Configuration.REQUEST_RESPONSE_TYPE_SYNCHRONOUS)) {
            return requestWrapper.inlineWrap(request, Configuration.REQUEST_RESPONSE_TYPE_SYNCHRONOUS);
        } else if (StringUtils.equalsIgnoreCase(type, Configuration.REQUEST_RESPONSE_TYPE_EMAIL)) {
            //FIXME. This is logically inconsistent.
            MailSenderPlugin mailSender = (MailSenderPlugin) xwiki.getPlugin(MAIL_SENDER, context);

        }

        /*
        TODO. For now all request are stored. However if for inline request it is not necessary to get a unique id,
        that should be changed. However, in case of periodic requests, the request should also be saved.
        */
        requestHandler.saveRequest(session);

        return requestWrapper.wrap(request);
    }
}
