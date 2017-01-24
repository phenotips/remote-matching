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
package org.phenotips.remote.common.internal.api;

import org.phenotips.data.Patient;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.fromjson.JSONToMatchingPatientConverter;
import org.phenotips.remote.hibernate.internal.DefaultIncomingMatchRequest;
import org.phenotips.vocabulary.Vocabulary;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * TODO note: designed to be able to handle multiple (slightly different) versions of the Matching API
 */
public class DefaultIncomingJSONParser implements IncomingJSONParser
{
    private Logger logger;

    private JSONToMatchingPatientConverter patientConverter;

    private final String apiVersion;

    public DefaultIncomingJSONParser(String apiVersion, Logger logger, Vocabulary ontologyService)
    {
        this.apiVersion = apiVersion;
        this.logger = logger;

        this.patientConverter = new DefaultJSONToMatchingPatientConverter(apiVersion, logger, ontologyService);
    }

    @Override
    public IncomingMatchRequest parseIncomingRequest(JSONObject jsonRequest, String remoteServerId)
    {
        JSONObject patientJSON = jsonRequest.optJSONObject(ApiConfiguration.JSON_PATIENT);

        Patient requestPatient = this.patientConverter.convert(patientJSON);

        DefaultIncomingMatchRequest request =
            new DefaultIncomingMatchRequest(remoteServerId, this.apiVersion, jsonRequest.toString(), requestPatient);

        this.logger.debug("JSON->IncomingRequest done");

        return request;
    }
}
