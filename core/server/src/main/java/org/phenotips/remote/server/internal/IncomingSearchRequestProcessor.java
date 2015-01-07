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

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.common.ApiFactory;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.remote.server.MatchingPatientsFinder;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.phenotips.remote.server.internal.queuetasks.QueueTaskAsyncAnswer;
import org.phenotips.remote.server.internal.queuetasks.QueueTaskEmail;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.concurrent.ExecutionContextRunnable;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

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

    @Inject
    private ComponentManager componentManager;

    @Override
    public JSONObject processHTTPSearchRequest(String apiVersion, String stringJson, ExecutorService queue,
        HttpServletRequest httpRequest)
        throws Exception
    {
        XWikiContext context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        BaseObject configurationObject =
            XWikiAdapter.getRemoteConfigurationGivenRemoteIP(httpRequest.getRemoteAddr(), context);

        // FIXME? Is there other way to access all the necessary patients/data?
        // context.setUserReference(new DocumentReference(context.getMainXWiki(), "XWiki", "Admin"));

        this.logger.debug("Received JSON search request: <<{}>>", stringJson);

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
            this.logger.debug("...parsing input...");

            IncomingSearchRequest request = this.createRequest(apiVersionSpecificConverter, json, configurationObject);

            this.logger.debug("...handling...");

            String type = request.getResponseType();

            if (request.getQueryType() == ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC ||
                StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS)) {
                // save to DB & assign unique ID
                this.requestStorageManager.saveIncomingPeriodicRequest(request);
            }

            if (StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_SYNCHRONOUS)) {
                this.logger.debug("Request type: inline");

                List<PatientSimilarityView> matches =
                    this.patientsFinder.findMatchingPatients(request.getRemotePatient());

                return apiVersionSpecificConverter.generateInlineResponse(request, matches);

            } else if (StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_EMAIL)) {
                this.logger.debug("Request type: email");

                Runnable task =
                    new ExecutionContextRunnable(new QueueTaskEmail(request, configurationObject, this.logger),
                        this.componentManager);
                queue.submit(task);

                return apiVersionSpecificConverter.generateNonInlineResponse(request);

            } else if (StringUtils.equalsIgnoreCase(type, ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS)) {
                this.logger.debug("Request type: async");

                Runnable task =
                    new ExecutionContextRunnable(new QueueTaskAsyncAnswer(request, configurationObject, this.logger,
                        apiVersionSpecificConverter, this.patientsFinder), this.componentManager);
                queue.submit(task);

                return apiVersionSpecificConverter.generateNonInlineResponse(request);
            }
        } catch (IllegalArgumentException ex) {
            this.logger.error("DATA Error: {}", ex);
            return apiVersionSpecificConverter.generateWrongInputDataResponse();
        } catch (Exception ex) {
            this.logger.error("CODE Error: {}", ex);
            return apiVersionSpecificConverter.generateInternalServerErrorResponse();
        }

        return null;
    }

    private IncomingSearchRequest createRequest(ApiDataConverter apiDataConverter, JSONObject json,
        BaseObject configurationObject) throws IllegalArgumentException
    {
        String remoteServerId =
            configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_NAME);

        IncomingSearchRequest request =
            apiDataConverter.getIncomingJSONParser().parseIncomingRequest(json, remoteServerId);

        if (StringUtils.equals(request.getResponseType(), ApiConfiguration.REQUEST_RESPONSE_TYPE_EMAIL) &&
            StringUtils.isBlank(request.getSubmitterEmail()))
        {
            throw new IllegalArgumentException("Can not respond by email to a query with no email specified");
        }

        return request;
    }
}
