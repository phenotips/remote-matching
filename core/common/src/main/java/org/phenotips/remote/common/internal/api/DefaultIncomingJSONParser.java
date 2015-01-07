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
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.fromjson.JSONToMatchingPatientConverter;
import org.phenotips.remote.hibernate.internal.DefaultIncomingSearchRequest;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
        this.logger = logger;

        this.patientConverter = new DefaultJSONToMatchingPatientConverter(apiVersion, logger);
    }

    @Override
    public IncomingSearchRequest parseIncomingRequest(JSONObject jsonRequest, String remoteServerId)
    {

        MatchingPatient requestPatient = this.patientConverter.convert(jsonRequest);

        DefaultIncomingSearchRequest request = new DefaultIncomingSearchRequest(requestPatient, remoteServerId);

        try {
            request.setResponseType(jsonRequest.optString(ApiConfiguration.JSON_RESPONSE_TYPE,
                ApiConfiguration.DEFAULT_REQUEST_RESPONSE_TYPE));

            request.setQueryType(this.queryType(jsonRequest));

            Map<String, String> submitterMap = this.submitter(jsonRequest);
            if (submitterMap != null) {
                request.setSubmitterEmail(submitterMap.get(ApiConfiguration.JSON_SUBMITTER_EMAIL));
                request.setSubmitterName(submitterMap.get(ApiConfiguration.JSON_SUBMITTER_NAME));
                request.setSubmitterInstitution(submitterMap.get(ApiConfiguration.JSON_SUBMITTER_INSTITUTION));
            }

            // TODO: label
        } catch (Exception ex) {
            this.logger.error("Incoming request parsing error: {}", ex);
            // request.setHTTPStatus(AppConfiguration.HTTP_BAD_REQUEST);
            return null;
        }

        this.logger.debug("JSON->IncomingRequest done");
        return request;
    }

    private String queryType(JSONObject json)
    {
        String result = ApiConfiguration.DEFAULT_REQUEST_QUERY_TYPE;
        try {
            result = json.getString(ApiConfiguration.JSON_QUERY_TYPE);
            if (!result.equals(ApiConfiguration.REQUEST_QUERY_TYPE_ONCE) &&
                !result.equals(ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC)) {
                result = ApiConfiguration.DEFAULT_REQUEST_QUERY_TYPE;
            }
            return result;
        } catch (Exception ex) {
        }
        return result;
    }

    private Map<String, String> submitter(JSONObject json) throws Exception
    {
        JSONObject submitter = json.getJSONObject(ApiConfiguration.JSON_SUBMITTER);
        if (submitter.isEmpty()) {
            return null;
        }

        String[] keys = { ApiConfiguration.JSON_SUBMITTER_NAME,
                          ApiConfiguration.JSON_SUBMITTER_EMAIL,
                          ApiConfiguration.JSON_SUBMITTER_INSTITUTION };
        Map<String, String> submitterMap = new HashMap<String, String>();
        for (String key : keys) {
            Object valueObject = submitter.get(key);
            if (valueObject == null) {
                continue;
            }
            String value = valueObject.toString();
            if (StringUtils.isNotBlank(value)) {
                submitterMap.put(key, value);
            }
        }
        return submitterMap;
    }
}
