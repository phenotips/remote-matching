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
package org.phenotips.remote.adapters.script;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.RemoteMatchingClient;
import org.phenotips.remote.adapters.internal.OutgoingRequestHandler;
import org.phenotips.remote.adapters.jsonwrappers.OutgoingSearchRequestToJSONWrapper;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.api.WrapperInterface;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

import net.sf.json.JSONObject;

/**
 * Gives velocity access to the functions it needs to perform remote matching. There is a set of functions for sending
 * the request, and a set for retrieving the data.
 */
@Unstable
@Component
@Named("remoteMatching")
@Singleton
public class RemoteMatchingScriptService implements ScriptService
{
    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    @Named("restricted")
    private PatientSimilarityViewFactory viewFactory;

    @Inject
    private HibernateSessionFactory sessionFactory;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    /** Wrapped trusted API, doing the actual work. */
    @Inject
    private PatientRepository internalPatientService;

    public boolean sendRequest(com.xpn.xwiki.api.Object xwikiObject)
    {
        try {
            XWikiContext context = getContext();
            XWiki wiki = getWiki(context);

            BaseObject xwikiRequestObject = xwikiObject.getXWikiObject();
            RequestHandlerInterface<OutgoingSearchRequestInterface> requestHandler =
                new OutgoingRequestHandler(xwikiRequestObject, wiki, context, resolver);
            OutgoingSearchRequestInterface request = requestHandler.createRequest();

            Session session = this.sessionFactory.getSessionFactory().openSession();
            requestHandler.saveRequest(session);

            WrapperInterface<OutgoingSearchRequestInterface, JSONObject> requestWrapper =
                new OutgoingSearchRequestToJSONWrapper(wiki, context);
            String result = RemoteMatchingClient.sendRequest(request, requestWrapper);

            JSONObject json = JSONObject.fromObject(result);
            requestHandler = new OutgoingRequestHandler(request, json);
            requestHandler.saveRequest(session);

            return true;
        } catch (Exception ex) {
            logger.error("Failed to send request", ex);
        }
        return false;
    }

    public List<PatientSimilarityView> getSimilarityResults(Patient patient) throws XWikiException
    {
        List<PatientSimilarityView> resultsList = new LinkedList<PatientSimilarityView>();

        Session session = this.sessionFactory.getSessionFactory().openSession();
        RequestHandlerInterface<OutgoingSearchRequestInterface> requestHandler = new OutgoingRequestHandler(session);

        try {
            XWikiContext context = getContext();
            XWiki wiki = getWiki(context);
            XWikiDocument patientDoc = wiki.getDocument(patient.getDocument(), context);
            List<BaseObject> requestObjects = patientDoc.getXObjects(Configuration.REMOTE_REQUEST_REFERENCE);
            for (BaseObject requestObject : requestObjects) {
                String requestIdString = requestObject.getStringValue(Configuration.REMOTE_HIBERNATE_ID);
                if (StringUtils.isBlank(requestIdString)) {
                    return resultsList;
                }
                OutgoingSearchRequestInterface request =
                    requestHandler.loadRequest(Long.valueOf(requestIdString), internalPatientService);

                List<PatientSimilarityView> allResults = request.getResults(viewFactory);
                resultsList.addAll(allResults);
            }
            return resultsList;
        } catch (Exception ex) {
            return resultsList;
        }
    }

    private XWikiContext getContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }

    private XWiki getWiki(XWikiContext context)
    {
        return context.getWiki();
    }
}
