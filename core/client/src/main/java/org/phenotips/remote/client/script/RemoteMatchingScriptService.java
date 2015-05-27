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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.phenotips.remote.client.script;

import java.util.List;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;

import net.sf.json.JSONArray;
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
    RemoteMatchingService matchingService;

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

        result.element("requestSent", request.wasSent());

        if (request.gotValidReply()) {
            result.element("remoteResponseReceived", true);
            result.element("queryJSON", request.getRequestJSON());
            result.element("responseJSON", request.getResponseJSON());
            result.element("responseHTTPCode", request.getRequestStatusCode());

            try {
                JSONArray matches = new JSONArray();

                List<PatientSimilarityView> parsedResults = this.matchingService.getSimilarityResults(request);

                for (PatientSimilarityView patient : parsedResults) {
                    matches.add(patient.toJSON());
                }

                result.element("matches", matches);
            } catch (Exception ex) {
                result.element("errorDetails", ex.getMessage());
            }
        } else {
            result.element("remoteResponseReceived", false);
            result.element("errorCode", request.getRequestStatusCode());
            result.element("errorDetails", request.getRequestJSON());
        }

        return result;
    }
}
