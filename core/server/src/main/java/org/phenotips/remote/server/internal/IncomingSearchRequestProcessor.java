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
package org.phenotips.remote.server.internal;

import org.phenotips.consents.ConsentManager;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.matchingnotification.MatchingNotificationManager;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.remote.hibernate.internal.DefaultIncomingMatchRequest;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.phenotips.similarity.SimilarPatientsFinder;

import org.xwiki.component.annotation.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

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
    private SimilarPatientsFinder patientsFinder;

    @Inject
    private RemoteMatchingStorageManager requestStorageManager;

    @Inject
    private ConsentManager consentManager;

    @Inject
    private MatchingNotificationManager notificationManager;

    @Override
    public JSONObject processHTTPSearchRequest(ApiDataConverter apiVersionSpecificConverter, String stringJson,
        ExecutorService queue, String remoteServerId, HttpServletRequest httpRequest)
    {
        this.logger.debug("Received JSON search request: <<{}>>", stringJson);

        try {
            JSONObject json = new JSONObject(stringJson);

            this.logger.debug("...parsing input...");

            IncomingMatchRequest request =
                apiVersionSpecificConverter.getIncomingJSONParser().parseIncomingRequest(json, remoteServerId);

            this.logger.debug("...handling...");

            List<PatientSimilarityView> matches = this.patientsFinder.findSimilarPatients(request.getModelPatient());

            List<PatientSimilarityView> filteredMatches = filterMatches(matches);

            this.notificationManager.saveIncomingMatches(filteredMatches, remoteServerId);

            JSONObject responseJSON = apiVersionSpecificConverter.generateServerResponse(request, filteredMatches);

            request.addResponse(responseJSON);

            // save for audit purposes only
            this.requestStorageManager.saveIncomingRequest(request);

            return responseJSON;
        } catch (JSONException ex) {
            this.logger.error("Incorrect incoming request: misformatted JSON");
            return apiVersionSpecificConverter.generateWrongInputDataResponse("misformatted JSON");
        } catch (ApiViolationException ex) {
            this.logger.error("Error converting JSON to incoming request");
            return apiVersionSpecificConverter.generateWrongInputDataResponse(ex.getMessage());
        } catch (Exception ex) {
            this.logger.error("CODE Error: {}", ex);
            return apiVersionSpecificConverter.generateInternalServerErrorResponse(null);
        } finally {
            // save raw request data for audit purposes only
            this.saveUnprocessedRequest(stringJson, remoteServerId, apiVersionSpecificConverter.getApiVersion());
        }
    }

    public void saveUnprocessedRequest(String requestString, String remoteServerId, String apiVersion)
    {
        IncomingMatchRequest request =
            new DefaultIncomingMatchRequest(remoteServerId, apiVersion, requestString, null);

        this.requestStorageManager.saveIncomingRequest(request);
    }

    private List<PatientSimilarityView> filterMatches(List<PatientSimilarityView> matches)
    {
        // check consent level for each of the patient: exclude patients without explicit MME consent
        //
        // TODO: use CollectionUtils.filter once a) updated Apache Commons that support parametrized types are used
        // and b) a workaround for anonymous classes only being able to use final local variables is testd

        List<PatientSimilarityView> filteredMatches = new LinkedList<PatientSimilarityView>();

        for (PatientSimilarityView match : matches) {
            if (this.consentManager.hasConsent(match.getId(), "matching")) {
                // For now, only include results where the genotype score is high enough to indicate a candidate
                // gene matched (exome data gives max score of 0.5, and candidate genes have score of 1.0)
                //
                // FIXME: once PatientSimilarityView exposes candidate genes, use that to check if candidate genes
                // matched instead of this indirect test based on the score
                if (match.getGenotypeScore() >= 1.0) {
                    filteredMatches.add(match);
                }
            } else {
                this.logger.error("Patient [{}] is excluded form match results because match consent is unchecked",
                    match.getId());
            }
        }

        return filteredMatches;
    }
}
