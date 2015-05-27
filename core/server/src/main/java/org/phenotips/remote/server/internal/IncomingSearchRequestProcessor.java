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
package org.phenotips.remote.server.internal;

import org.phenotips.remote.server.MatchingPatientsFinder;
import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.remote.hibernate.internal.DefaultIncomingMatchRequest;
import org.phenotips.data.similarity.PatientSimilarityView;

import java.util.concurrent.ExecutorService;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.PropertyInterface;

import java.util.LinkedList;
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
    {
        this.logger.debug("Received JSON search request: <<{}>>", stringJson);

        XWikiContext context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        BaseObject configurationObject =
            XWikiAdapter.getRemoteConfigurationGivenRemoteIP(httpRequest.getRemoteAddr(), context);

        // Note: authorization hapens before this point, configurationObject should exist and be valid
        String remoteServerId =
                configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_NAME);

        try {
            // FIXME? Is there other way to access all the necessary patients/data?
            // context.setUserReference(new DocumentReference(context.getMainXWiki(), "XWiki", "Admin"));

            JSONObject json = JSONObject.fromObject(stringJson);

            this.logger.debug("...parsing input...");

            IncomingMatchRequest request =
                    apiVersionSpecificConverter.getIncomingJSONParser().parseIncomingRequest(json, remoteServerId);

            this.logger.debug("...handling...");

            List<PatientSimilarityView> matches = patientsFinder.findMatchingPatients(request.getModelPatient());

            // check consent level for each of the patient: exclude patients without explicit MME consent
            //
            // TODO: use CollectionUtils.filter once a) updated Apache Commons that support parametrized types are used
            //       and b) a workaround for anonymous classes only being able to use final local variables is testd

            List<PatientSimilarityView> filteredMatches = new LinkedList<PatientSimilarityView>();

            for (PatientSimilarityView match : matches) {
                XWikiDocument patientDoc = XWikiAdapter.getPatientDoc(match.getId(), context);
                if (patientDoc != null) {
                    BaseObject consent = patientDoc.getXObject(ApplicationConfiguration.PATIENT_CONSENT_OBJECT_REFERENCE);
                    if (consent != null) {
                        String consentValue = consent.getStringValue("matching");
                        logger.error("Consent value: [{}]", consentValue);
                        if (consentValue != null && consentValue.equals("1")) {
                            filteredMatches.add(match);
                        }
                    }
                }
            }

            JSONObject responseJSON = apiVersionSpecificConverter.generateServerResponse(request, filteredMatches);

            request.addResponse(responseJSON);

            // save for audit purposes only
            requestStorageManager.saveIncomingRequest(request);

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

        requestStorageManager.saveIncomingRequest(request);
    }
}
