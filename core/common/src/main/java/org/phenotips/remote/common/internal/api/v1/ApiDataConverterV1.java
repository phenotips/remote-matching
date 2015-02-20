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

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

import org.xwiki.component.phase.Initializable;
import org.phenotips.remote.common.internal.api.DefaultIncomingJSONParser;
import org.phenotips.remote.common.internal.api.DefaultPatientToJSONConverter;
import org.phenotips.remote.common.internal.api.DefaultOutgoingRequestToJSONConverter;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.tojson.OutgoingRequestToJSONConverter;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;
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

    private IncomingJSONParser incomingJSONParser;

    private PatientToJSONConverter patientToJSONConverter;

    private OutgoingRequestToJSONConverter outgoingJSONParser;

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

    public void initialize()
    {
        incomingJSONParser = new DefaultIncomingJSONParser(getApiVersion(), logger);

        patientToJSONConverter = new DefaultPatientToJSONConverter(getApiVersion(), logger);

        outgoingJSONParser = new DefaultOutgoingRequestToJSONConverter(getApiVersion(), logger, patientRepository, access, bridge);
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
    public JSONObject generateInlineResponse(IncomingSearchRequest request, List<PatientSimilarityView> resultList)
    {
        JSONObject reply = new JSONObject();

        //TODO:
        //reply.put("modelPatientLabel", request.getRemotePatient().getLabel());
        //reply.put("modelPatientId", request.getRemotePatient().getExternalId());

        JSONArray matchList = new JSONArray();
        for (PatientSimilarityView patient : resultList) {
            try {
                JSONObject matchInfo = new JSONObject();

                JSONObject scoreJson = new JSONObject();
                scoreJson.put(ApiConfiguration.REPLY_JSON_RESULTS_SCORE_PATIENT, patient.getScore());

                matchInfo.put(ApiConfiguration.REPLY_JSON_RESULTS_SCORE, scoreJson);

                matchInfo.put(ApiConfiguration.REPLY_JSON_RESULTS_PATIENT,
                              this.patientToJSONConverter.convert(patient, true));
                matchList.add(matchInfo);
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
    public OutgoingRequestToJSONConverter getOutgoingRequestToJSONConverter()
    {
        return this.outgoingJSONParser;
    }
}
