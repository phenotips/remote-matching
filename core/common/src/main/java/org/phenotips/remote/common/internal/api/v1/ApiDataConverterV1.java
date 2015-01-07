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
package org.phenotips.remote.common.internal.api.v1;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.tojson.OutgoingRequestToJSONConverter;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;
import org.phenotips.remote.common.internal.api.DefaultIncomingJSONParser;
import org.phenotips.remote.common.internal.api.DefaultPatientToJSONConverter;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component
@Named("api-data-converter-v1")
public class ApiDataConverterV1 implements ApiDataConverter, Initializable
{
    private final static String VERSION_STRING = "v1";

    private IncomingJSONParser incomingJSONParser;

    private PatientToJSONConverter patientToJSONConverter;

    @Inject
    private Logger logger;

    @Override
    public void initialize()
    {
        this.incomingJSONParser = new DefaultIncomingJSONParser(getApiVersion(), this.logger);

        this.patientToJSONConverter = new DefaultPatientToJSONConverter(getApiVersion(), this.logger);
    }

    @Override
    public String getApiVersion()
    {
        return VERSION_STRING;
    }

    //================================================================

    @Override
    public JSONObject generateWrongInputDataResponse()
    {
        JSONObject reply = new JSONObject();
        reply.put(ApiConfiguration.INTERNAL_JSON_STATUS, ApiConfiguration.HTTP_BAD_REQUEST);
        return reply;
    }

    @Override
    public JSONObject generateInternalServerErrorResponse()
    {
        JSONObject reply = new JSONObject();
        reply.put(ApiConfiguration.INTERNAL_JSON_STATUS, ApiConfiguration.HTTP_SERVER_ERROR);
        return reply;
    }

    //================================================================

    @Override
    public IncomingJSONParser getIncomingJSONParser()
    {
        return this.incomingJSONParser;
    }

    @Override
    public JSONObject generateInlineResponse(IncomingSearchRequest request, List<PatientSimilarityView> resultList)
    {
        JSONObject reply = new JSONObject();

        reply.put(ApiConfiguration.JSON_RESPONSE_TYPE, ApiConfiguration.REQUEST_RESPONSE_TYPE_SYNCHRONOUS);
        reply.put(ApiConfiguration.JSON_RESPONSE_ID, 0);

        reply.put("modelPatientLabel", request.getRemotePatient().getLabel());      // TODO: DEBUG field
        reply.put("modelPatientId",    request.getRemotePatient().getExternalId()); // TODO: DEBUG field

        JSONArray matchList = new JSONArray();
        for (PatientSimilarityView patient : resultList) {
            try {
                matchList.add(this.patientToJSONConverter.convert(patient, true));
            } catch (Exception ex) {
                this.logger.error("Error converting patient to JSON: [{}]", ex);
            }
        }
        reply.put(ApiConfiguration.JSON_RESULTS, matchList);

        reply.put(ApiConfiguration.INTERNAL_JSON_STATUS, ApiConfiguration.HTTP_OK);

        return reply;
    }

    @Override
    public JSONObject generateNonInlineResponse(IncomingSearchRequest request)
    {
        JSONObject reply = new JSONObject();

        reply.put(ApiConfiguration.JSON_RESPONSE_TYPE, request.getResponseType());

        // email responses are non-inloine responses, but there will be no query ID if the request is not periodic
        if (request.getQueryId() != null) {
            reply.put(ApiConfiguration.JSON_RESPONSE_ID, request.getQueryId());
        }

        reply.put("modelPatientLabel", request.getRemotePatient().getLabel());      // TODO: DEBUG field
        reply.put("modelPatientId",    request.getRemotePatient().getExternalId()); // TODO: DEBUG field

        reply.put(ApiConfiguration.INTERNAL_JSON_STATUS, ApiConfiguration.HTTP_OK);

        return reply;
    }

    @Override
    public JSONObject generateAsyncResult(Map<IncomingSearchRequest, List<PatientSimilarityView>> results)
    {
        JSONObject reply = new JSONObject();

        JSONArray resultsList = new JSONArray();

        for (Map.Entry<IncomingSearchRequest, List<PatientSimilarityView>> resultSet : results.entrySet()) {
            IncomingSearchRequest processedRequest = resultSet.getKey();
            List<PatientSimilarityView> resultList = resultSet.getValue();

            JSONObject oneResultsSet = new JSONObject();
            oneResultsSet.put(ApiConfiguration.JSON_RESPONSE_ID, processedRequest.getQueryId());

            JSONArray matchList = new JSONArray();
            for (PatientSimilarityView patient : resultList) {
                try {
                    matchList.add(this.patientToJSONConverter.convert(patient, true));
                } catch (Exception ex) {
                    this.logger.error("Error converting patient to JSON: [{}]", ex);
                }
            }
            oneResultsSet.put(ApiConfiguration.JSON_RESULTS, matchList);

            resultsList.add(oneResultsSet);
        }

        reply.put(ApiConfiguration.JSON_ASYNC_RESPONSES, resultsList);

        return reply;
    }

    //================================================================

    @Override
    public OutgoingRequestToJSONConverter getOutgoingRequestToJSONConverter()
    {
        return null;
    }
}
