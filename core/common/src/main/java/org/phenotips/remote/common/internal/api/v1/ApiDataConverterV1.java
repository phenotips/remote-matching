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
package org.phenotips.remote.common.internal.api.v1;

import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;

import org.xwiki.component.phase.Initializable;
import org.phenotips.remote.common.internal.api.DefaultIncomingJSONParser;
import org.phenotips.remote.common.internal.api.DefaultPatientToJSONConverter;
import org.phenotips.remote.common.internal.api.DefaultOutgoingJSONGenerator;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.tojson.OutgoingJSONGenerator;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;
import org.phenotips.vocabulary.Vocabulary;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.bridge.DocumentAccessBridge;
import org.slf4j.Logger;

import javax.inject.Named;
import javax.inject.Inject;

import org.xwiki.component.annotation.Component;

@Component
@Named("api-data-converter-v1")
public class ApiDataConverterV1 implements ApiDataConverter, Initializable
{
    private final static String VERSION_STRING = "1.0";

    private final static Integer DEFAULT_NUMBER_OF_GENES_IN_REPLIES = 5;

    private IncomingJSONParser incomingJSONParser;

    private PatientToJSONConverter patientToJSONConverter;

    private OutgoingJSONGenerator outgoingJSONGenerator;

    @Inject
    private Logger logger;

    /** Provides access to patient data. */
    @Inject
    private PatientRepository patientRepository;

    /** Used for checking access rights. */
    @Inject
    private AuthorizationManager access;

    /** Used for obtaining the current user. */
    @Inject
    private DocumentAccessBridge bridge;

    /** Used for converting gene ID between different id schemas */
    @Inject
    @Named("hgnc")
    private Vocabulary ontologyService;

    public void initialize()
    {
        this.incomingJSONParser = new DefaultIncomingJSONParser(getApiVersion(), logger, ontologyService);

        this.patientToJSONConverter = new DefaultPatientToJSONConverter(getApiVersion(), logger);

        this.outgoingJSONGenerator = new DefaultOutgoingJSONGenerator(getApiVersion(), logger, patientRepository, access, bridge);
    }

    @Override
    public String getApiVersion()
    {
        return VERSION_STRING;
    }

    //================================================================

    @Override
    public JSONObject generateWrongInputDataResponse(String reasonMsg)
    {
        JSONObject reply = new JSONObject();
        reply.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, ApiConfiguration.HTTP_BAD_REQUEST);
        if (reasonMsg != null && !reasonMsg.isEmpty()) {
            reply.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, reasonMsg);
        }
        return reply;
    }

    @Override
    public JSONObject generateInternalServerErrorResponse(String reasonMsg)
    {
        JSONObject reply = new JSONObject();
        reply.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, ApiConfiguration.HTTP_SERVER_ERROR);
        if (reasonMsg != null && !reasonMsg.isEmpty()) {
            reply.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, reasonMsg);
        }
        return reply;
    }

    //================================================================

    @Override
    public IncomingJSONParser getIncomingJSONParser()
    {
        return this.incomingJSONParser;
    }

    @Override
    public JSONObject generateServerResponse(IncomingMatchRequest request, List<PatientSimilarityView> resultList)
    {
        JSONObject reply = new JSONObject();

        JSONArray matchList = new JSONArray();
        for (PatientSimilarityView patient : resultList) {
            try {
                JSONObject matchInfo = new JSONObject();

                JSONObject scoreJson = new JSONObject();
                scoreJson.put(ApiConfiguration.REPLY_JSON_RESULTS_SCORE_PATIENT, patient.getScore());

                matchInfo.put(ApiConfiguration.REPLY_JSON_RESULTS_SCORE, scoreJson);

                matchInfo.put(ApiConfiguration.REPLY_JSON_RESULTS_PATIENT,
                              this.patientToJSONConverter.convert(patient, true, DEFAULT_NUMBER_OF_GENES_IN_REPLIES));
                matchList.put(matchInfo);
            } catch (Exception ex) {
                this.logger.error("Error converting patient to JSON: [{}]", ex);
            }
        }
        reply.put(ApiConfiguration.REPLY_JSON_RESULTS, matchList);

        this.logger.debug("Inline reply: [{}]", reply.toString());

        reply.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, ApiConfiguration.HTTP_OK);

        return reply;
    }

    //================================================================

    @Override
    public OutgoingJSONGenerator getOutgoingJSONGenerator()
    {
        return this.outgoingJSONGenerator;
    }
}
