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
package org.phenotips.remote.client.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.similarity.AccessType;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.data.similarity.internal.DefaultAccessType;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.ApiFactory;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;
import org.phenotips.remote.common.internal.api.DefaultJSONToMatchingPatientConverter;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.remote.hibernate.internal.DefaultOutgoingMatchRequest;
import org.phenotips.vocabulary.Vocabulary;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.stability.Unstable;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

@Unstable
@Component
@Singleton
public class DefaultRemoteMatchingService implements RemoteMatchingService
{
    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    ApiFactory apiFactory;

    @Inject
    @Named("view")
    protected AccessLevel viewAccess;

    @Inject
    @Named("match")
    protected AccessLevel matchAccess;

    @Inject
    @Named("hgnc")
    private Vocabulary ontologyService;

    @Inject
    PatientRepository patientRepository;

    @Inject
    private RemoteMatchingStorageManager requestStorageManager;

    @Inject
    private PatientSimilarityViewFactory similarityViewFactory; // injected so that static data is initialized. TODO:
                                                                // review static data usage

    @Inject
    private RemoteConfigurationManager remoteConfigurationManager;

    @Override
    public OutgoingMatchRequest sendRequest(String patientId, String remoteServerId, int addTopNGenes)
    {
        DefaultOutgoingMatchRequest request =
            new DefaultOutgoingMatchRequest(remoteServerId, ApiConfiguration.LATEST_API_VERSION_STRING, patientId);

        XWikiContext context = this.getContext();

        BaseObject configurationObject =
            this.remoteConfigurationManager.getRemoteConfigurationGivenRemoteServerID(remoteServerId, context);

        if (configurationObject == null) {
            logger.error("Requested matching server is not configured: [{}]", remoteServerId);
            return this.generateErrorRequest(ApiConfiguration.ERROR_NOT_SENT,
                "requested matching server [" + remoteServerId + "] is not configured", request);
        }

        JSONObject requestJSON;
        try {
            ApiDataConverter apiVersionSpecificConverter =
                this.apiFactory.getApiVersion(ApiConfiguration.LATEST_API_VERSION_STRING);

            requestJSON = apiVersionSpecificConverter.getOutgoingJSONGenerator()
                .generateRequestJSON(remoteServerId, patientId, addTopNGenes);
        } catch (ApiViolationException ex) {
            return this.generateErrorRequest(ApiConfiguration.ERROR_NOT_SENT, ex.getMessage(), request);
        }
        if (requestJSON == null) {
            this.logger.error("Unable to convert patient to JSON: [{}]", patientId);
            return this.generateErrorRequest(ApiConfiguration.ERROR_NOT_SENT,
                "unable to convert patient with ID [" + patientId.toString() + "] to JSON", request);
        }

        CloseableHttpClient client = HttpClients.createDefault();

        StringEntity jsonEntity =
            new StringEntity(requestJSON.toString(), ContentType.create("application/json", "utf-8"));

        // TODO: hack to make charset lower-cased so that GeneMatcher accepts it
        // jsonEntity.setContentType("application/json; charset=utf-8");
        String mimeType = ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX +
            ApiConfiguration.LATEST_API_VERSION_STRING +
            ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX;
        jsonEntity.setContentType(mimeType + "; charset=utf-8");
        this.logger.error("Setting Content-Type: [{}]", jsonEntity.getContentType().toString());

        String key = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_KEY_FIELD);
        String baseURL = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD);
        if (baseURL.charAt(baseURL.length() - 1) != '/') {
            baseURL += "/";
        }
        String targetURL = baseURL + ApiConfiguration.REMOTE_URL_SEARCH_ENDPOINT;

        this.logger.error("Sending matching request to [" + targetURL + "]: " + requestJSON.toString());

        CloseableHttpResponse httpResponse;
        try {
            HttpPost httpRequest = new HttpPost(targetURL);
            httpRequest.setEntity(jsonEntity);
            httpRequest.setHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER, key);
            httpRequest.setHeader(ApiConfiguration.HTTPHEADER_API_VERSION, mimeType);
            this.logger.error("Setting {}: [{}]", ApiConfiguration.HTTPHEADER_API_VERSION, mimeType);
            httpResponse = client.execute(httpRequest);
        } catch (javax.net.ssl.SSLHandshakeException ex) {
            this.logger.error("Error sending matching request to [" + targetURL +
                "]: SSL handshake exception: [{}]", ex);
            return this.generateErrorRequest(ApiConfiguration.ERROR_NOT_SENT, "SSL handshake problem", request);
        } catch (Exception ex) {
            this.logger.error("Error sending matching request to [" + targetURL + "]: [{}]", ex);
            return this.generateErrorRequest(ApiConfiguration.ERROR_NOT_SENT, ex.getMessage(), request);
        }

        try {
            Integer httpStatus = (Integer) httpResponse.getStatusLine().getStatusCode();
            String stringReply = EntityUtils.toString(httpResponse.getEntity());

            logger.error("Reply to matching request: STATUS: [{}], DATA: [{}]", httpStatus, stringReply);

            // store sent request and recived response in the request object which will be stored for audit purposes
            // this will b stored even if reply is incorrect
            request.addRequestJSON(requestJSON);
            request.addResponseString(stringReply);
            request.setReplayHTTPStatus(httpStatus);
            requestStorageManager.saveOutgoingRequest(request);

            return request;
        } catch (Exception ex) {
            this.logger.error("Error processing matching request reply by [" + targetURL + "]: [{}]", ex);
            return this.generateErrorRequest(ApiConfiguration.ERROR_INTERNAL, null, request);
        }
    }

    private OutgoingMatchRequest generateErrorRequest(Integer statusCode, String errorMessage,
        DefaultOutgoingMatchRequest baseRequest)
    {
        baseRequest.setReplayHTTPStatus(statusCode);

        JSONObject errorJSON = new JSONObject();
        if (errorMessage != null) {
            errorJSON.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, errorMessage);
        }
        baseRequest.addRequestJSON(errorJSON);

        return baseRequest;
    }

    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    @Override
    public OutgoingMatchRequest getLastRequestSent(String patientId, String remoteServerId)
    {
        return requestStorageManager.loadCachedOutgoingRequest(patientId, remoteServerId);
    }

    @Override
    public List<RemotePatientSimilarityView> getSimilarityResults(OutgoingMatchRequest request)
    {
        List<RemotePatientSimilarityView> resultsList = new LinkedList<>();

        if (request == null) {
            return resultsList;
        }

        JSONObject replyJSON = request.getResponseJSON();

        if (replyJSON == null) {
            return resultsList;
        }

        // generate local scoring and feature-matching using the same algorithm we use for local similarity scores
        AccessType access = new DefaultAccessType(this.matchAccess, this.viewAccess, this.matchAccess);
        Patient reference = this.patientRepository.getPatientById(request.getLocalReferencePatientId());

        if (reference == null) {
            return resultsList;
        }

        DefaultJSONToMatchingPatientConverter patientConverter =
            new DefaultJSONToMatchingPatientConverter(ApiConfiguration.LATEST_API_VERSION_STRING, logger,
                ontologyService);

        // JSONArray processedResults = new JSONArray();

        JSONArray matches = replyJSON.optJSONArray("results");
        if (matches == null) {
            this.logger.error("No key 'results' in reply JSON");
        }

        String remoteServerId = request.getRemoteServerId();

        for (int i = 0; i < matches.length(); ++i) {
            try {
                JSONObject next = matches.getJSONObject(i);
                JSONObject nextPatient = next.getJSONObject("patient");

                MatchingPatient modelRemotePatient = patientConverter.convert(nextPatient);

                double patientScore = 0;
                try {
                    patientScore = next.getJSONObject("score").getDouble("patient");
                } catch (Exception ex) {
                    this.logger.error("Invalid score in JSON for patient [{}]", modelRemotePatient.getId());
                    throw new ApiViolationException(
                        "Invalid score in JSON for patient [" + modelRemotePatient.getId() + "]");
                }

                RemotePatientSimilarityView similarityView = new RemotePatientSimilarityView(modelRemotePatient,
                    reference, access, patientScore);

                resultsList.add(similarityView);
            } catch (ApiViolationException ex) {
                this.logger.error(
                    "Parsing incoming patients: one of the patients did not satisfy API requirements: [{}]",
                    ex.getMessage());
            } catch (Exception ex) {
                this.logger.error("Error parsing one of the patients from JSON: [{}]", ex);
            }
        }

        // replyJSON.element("results", processedResults);
        return resultsList;
    }
}
