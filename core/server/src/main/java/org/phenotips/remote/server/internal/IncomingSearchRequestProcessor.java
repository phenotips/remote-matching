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
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.data.similarity.PatientSimilarityView;

import java.util.concurrent.ExecutorService;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JSONException;

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
    private MatchingPatientsFinder patientsFinder;

    @Inject
    private RemoteMatchingStorageManager requestStorageManager;

    @Override
    public JSONObject processHTTPSearchRequest(ApiDataConverter apiVersionSpecificConverter, String stringJson,
        ExecutorService queue, HttpServletRequest httpRequest)
        throws Exception
    {
        this.logger.debug("Received JSON search request: <<{}>>", stringJson);

        XWikiContext context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        BaseObject configurationObject =
            XWikiAdapter.getRemoteConfigurationGivenRemoteIP(httpRequest.getRemoteAddr(), context);

        // FIXME? Is there other way to access all the necessary patients/data?
        // context.setUserReference(new DocumentReference(context.getMainXWiki(), "XWiki", "Admin"));

        JSONObject json;
        try {
            json = JSONObject.fromObject(stringJson);
        } catch (JSONException ex) {
            this.logger.error("Incorrect incoming request: misformatted JSON");
            return apiVersionSpecificConverter.generateWrongInputDataResponse("misformatted JSON");
        }

        try {
            this.logger.debug("...parsing input...");

            IncomingSearchRequest request;
            try {
                request = this.createRequest(apiVersionSpecificConverter, json, configurationObject);
            } catch (ApiViolationException ex) {
                this.logger.error("Error converting JSON to incoming request");
                return apiVersionSpecificConverter.generateWrongInputDataResponse(ex.getMessage());
            }

            this.logger.debug("...handling...");

            //TODO: save for audit purposes
            //requestStorageManager.saveIncomingPeriodicRequest(request);
            //logger.error("Assigned request ID: {}", request.getQueryId());

            List<PatientSimilarityView> matches = patientsFinder.findMatchingPatients(request.getRemotePatient());

            return apiVersionSpecificConverter.generateInlineResponse(request, matches);
        } catch (Exception ex) {
            this.logger.error("CODE Error: {}", ex);
            return apiVersionSpecificConverter.generateInternalServerErrorResponse(null);
        }
    }

    private IncomingSearchRequest createRequest(ApiDataConverter apiDataConverter, JSONObject json,
        BaseObject configurationObject)
    {
        String remoteServerId =
            configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_NAME);

        IncomingSearchRequest request =
            apiDataConverter.getIncomingJSONParser().parseIncomingRequest(json, remoteServerId);

        return request;
    }
}
