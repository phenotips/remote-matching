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
package org.phenotips.remote.common.internal.api;

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.common.internal.api.DefaultJSONToMatchingPatientConverter;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.fromjson.JSONToMatchingPatientConverter;
import org.phenotips.remote.hibernate.internal.DefaultIncomingMatchRequest;

import org.slf4j.Logger;

import net.sf.json.JSONObject;
/**
 * TODO
 * note: designed to be able to handle multiple (slightly different) versions of the Matching API
 */
public class DefaultIncomingJSONParser implements IncomingJSONParser
{
    private Logger logger;

    private JSONToMatchingPatientConverter patientConverter;

    private final String apiVersion;

    public DefaultIncomingJSONParser(String apiVersion, Logger logger)
    {
        this.apiVersion = apiVersion;
        this.logger     = logger;

        this.patientConverter = new DefaultJSONToMatchingPatientConverter(apiVersion, logger);
    }

    @Override
    public IncomingMatchRequest parseIncomingRequest(JSONObject jsonRequest, String remoteServerId)
    {
        JSONObject patientJSON = jsonRequest.optJSONObject(ApiConfiguration.JSON_PATIENT);

        MatchingPatient requestPatient = this.patientConverter.convert(patientJSON);

        DefaultIncomingMatchRequest request =
               new DefaultIncomingMatchRequest(remoteServerId, this.apiVersion, jsonRequest.toString(), requestPatient);

        this.logger.debug("JSON->IncomingRequest done");

        return request;
    }
}
