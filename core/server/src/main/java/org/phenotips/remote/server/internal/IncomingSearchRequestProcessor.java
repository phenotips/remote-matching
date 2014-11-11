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

import org.phenotips.remote.server.MatchingPatientsFinder;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.common.ApiFactory;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.phenotips.remote.server.internal.queuetasks.QueueTaskAsyncAnswer;
import org.phenotips.remote.server.internal.queuetasks.QueueTaskEmail;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.data.similarity.PatientSimilarityView;

import java.util.concurrent.ExecutorService;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import java.util.List;

import net.sf.json.JSONObject;

/**
 * Takes a json string in the constructor and does all the request processing functionality.
 */
@Component
@Singleton
public class IncomingSearchRequestProcessor implements SearchRequestProcessor
{
    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    ApiFactory apiFactory;

    @Inject
    private MatchingPatientsFinder patientsFinder;

    @Inject
    private RemoteMatchingStorageManager requestStorageManager;

    @Override
    public JSONObject processHTTPSearchRequest(String apiVersion, String stringJson, ExecutorService queue, HttpServletRequest httpRequest)
        throws Exception
    {
        XWikiContext context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");

        /*
         * Is the request authorized? If not, should not be able to continue at all. This is the first, and only line of
         * defence.
         */
        BaseObject configurationObject = XWikiAdapter.getRemoteConfigurationGivenRemoteIP(httpRequest.getRemoteAddr(), context);
        Integer authorizationStatus = validateRequest(httpRequest, configurationObject);
        if (!authorizationStatus.equals(ApiConfiguration.HTTP_OK)) {
            JSONObject authorizationJSON = new JSONObject();
            authorizationJSON.put(ApiConfiguration.INTERNAL_JSON_STATUS, authorizationStatus);
            return authorizationJSON;
        }

        // FIXME? Should not be requested under Admin.
        context.setUserReference(new DocumentReference(context.getMainXWiki(), "XWiki", "Admin"));

        logger.error("Received JSON search request: <<{}>>", stringJson);

        ApiDataConverter apiVersionSpecificConverter;
        try {
            apiVersionSpecificConverter = this.apiFactory.getApiVersion(apiVersion);
        } catch (Exception ex) {
            JSONObject reply = new JSONObject();
            reply.put("error", "unsupported API version");
            reply.put(ApiConfiguration.INTERNAL_JSON_STATUS, ApiConfiguration.HTTP_BAD_REQUEST);
            return reply;
        }

        JSONObject json = JSONObject.fromObject(stringJson);

        try {
            logger.error("...parsing input...");

            IncomingSearchRequest request = this.createRequest(apiVersionSpecificConverter, json, configurationObject);

            logger.error("...handling...");

            String type = request.getResponseType();

            if (request.getQueryType() == ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC ||
                StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS)) {
                // save to DB & assign unique ID
                requestStorageManager.saveIncomingPeriodicRequest(request);
            }

            if (StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_SYNCHRONOUS)) {
                logger.error("Request type: inline");

                List<PatientSimilarityView> matches = patientsFinder.findMatchingPatients(request.getRemotePatient());

                return apiVersionSpecificConverter.generateInlineResponse(request, matches);

            } else if (StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_EMAIL)) {
                logger.error("Request type: emial");

                Runnable task = new QueueTaskEmail(request, configurationObject, logger, this.execution.getContext());
                queue.submit(task);

                return apiVersionSpecificConverter.generateNonInlineResponse(request);

            } else if (StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS)) {
                logger.error("Request type: async");

                Runnable task = new QueueTaskAsyncAnswer(request, configurationObject, logger, this.execution.getContext(),
                                                         apiVersionSpecificConverter, patientsFinder);
                queue.submit(task);

                return apiVersionSpecificConverter.generateNonInlineResponse(request);
            }
        } catch (IllegalArgumentException ex) {
            logger.error("DATA Error: {}", ex);
            return apiVersionSpecificConverter.generateWrongInputDataResponse();
        } catch (Exception ex) {
            logger.error("CODE Error: {}", ex);
            return apiVersionSpecificConverter.generateInternalServerErrorResponse();
        }

        return null;
    }

    private Integer validateRequest(HttpServletRequest httpRequest, BaseObject configurationObject)
    {
        if (configurationObject == null) {
            return ApiConfiguration.HTTP_UNAUTHORIZED;  // this server is not listed as an accepted server, and has no key
        }
        // TODO: for a period of time support both URl and 'X-Auth-Token' HTTP header HTTP header, then remove URL" key param support
        String requestKey = httpRequest.getParameter(ApiConfiguration.URL_KEY_PARAMETER);
        if (requestKey == null) {
            requestKey = httpRequest.getHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER);
        }
        String configuredKey = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_LOCAL_KEY_FIELD);
        logger.error("VALIDATE: Key: {}, Configured: {}", requestKey, configuredKey);
        if (requestKey == null || configuredKey == null || !requestKey.equals(configuredKey)) {
             return ApiConfiguration.HTTP_UNAUTHORIZED;
        }
        return ApiConfiguration.HTTP_OK;
    }

    private IncomingSearchRequest createRequest(ApiDataConverter apiDataConverter, JSONObject json, BaseObject configurationObject) throws IllegalArgumentException
    {
        String remoteServerId = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_NAME);

        IncomingSearchRequest request = apiDataConverter.getIncomingJSONParser().parseIncomingRequest(json, remoteServerId);

        if (StringUtils.equals(request.getResponseType(), ApiConfiguration.REQUEST_RESPONSE_TYPE_EMAIL) &&
            StringUtils.isBlank(request.getSubmitterEmail()))
        {
            throw new IllegalArgumentException("Can not respond by email to a query with no email specified");
        }

        return request;
    }
}
