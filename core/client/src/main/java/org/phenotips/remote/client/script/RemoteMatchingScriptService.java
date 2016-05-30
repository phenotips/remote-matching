/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.remote.client.script;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

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
    private RemoteMatchingService matchingService;

    public JSONObject sendRequest(String patientId, String remoteServerId)
    {
        return this.sendRequest(patientId, remoteServerId, 0);
    }

    public JSONObject sendRequest(String patientId, String remoteServerId, int addTopNGenes)
    {
        this.logger.info("Sending outgoing request for patient [{}] to server [{}]", patientId, remoteServerId);

        OutgoingMatchRequest request =  this.matchingService.sendRequest(patientId, remoteServerId, addTopNGenes);

        return this.processRequest(request);
    }

    public JSONObject getLastRequest(String patientId, String remoteServerId)
    {
        this.logger.info("Getting processed response for the last request for patient [{}] to server [{}]", patientId, remoteServerId);

        OutgoingMatchRequest request = this.matchingService.getLastRequestSent(patientId, remoteServerId);

        return this.processRequest(request);
    }

    public OutgoingMatchRequest getLastRequestObject(String patientId, String remoteServerId)
    {
        return this.matchingService.getLastRequestSent(patientId, remoteServerId);
    }

    private JSONObject processRequest(OutgoingMatchRequest request)
    {
        if (request == null) {
            return null;
        }

        JSONObject result = new JSONObject();

        result.put("requestSent", request.wasSent());

        if (request.wasSent()) {
            result.put("responseHTTPCode", request.getRequestStatusCode());
        }

        if (request.gotValidReply()) {
            result.put("remoteResponseReceived", true);
            result.put("queryJSON", request.getRequestJSON());
            result.put("responseJSON", request.getResponseJSON());

            try {
                JSONArray matches = new JSONArray();

                List<RemotePatientSimilarityView> parsedResults = this.matchingService.getSimilarityResults(request);

                for (PatientSimilarityView patient : parsedResults) {
                    matches.put(patient.toJSON());
                }

                result.put("matches", matches);
            } catch (Exception ex) {
                result.put("errorDetails", ex.getMessage());
            }
        } else {
            result.put("remoteResponseReceived", false);
            result.put("errorCode", request.getRequestStatusCode());
            result.put("errorDetails", request.getRequestJSON());
        }

        return result;
    }
}
