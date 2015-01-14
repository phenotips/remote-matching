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
package org.phenotips.remote.api;

/**
 * TODO: make an interface so that different versions of the API can implement this differently
 *
 * This is where the constants and other configurations are stored for easy global access.
 */
public interface ApiConfiguration
{
    String API_VERSION_STRING = "v1";

    String HTTPHEADER_KEY_PARAMETER = "X-Auth-Token";
    String URL_KEY_PARAMETER        = "key";                // TODO: depricated

    // Must not contain the '/' at the beginning of the string
    String REMOTE_URL_SEARCH_ENDPOINT = "mmapi/" + API_VERSION_STRING + "/match";

    String REMOTE_URL_ASYNCHRONOUS_RESULTS_ENDPOINT = "mmapi/" + API_VERSION_STRING + "/matchResults";

    // TODO: get rid of "media=json"?
    //String REMOTE_URL_SEARCH_EXTENSION = REMOTE_URL_SEARCH_ENDPOINT + "?media=json&" + URL_KEY_PARAMETER + "=";

    String REMOTE_HIBERNATE_ID = "hibernateId";

    String REMOTE_RESPONSE_FORMAT = "responseFormat";

    // Patient
    String FEATURE_AGE_OF_ONSET = "age_of_onset";

    // used for non-matched non-disclosed featues to indicate there is an unmatched feature
    String REPLY_JSON_FEATURE_HPO_MOST_GENERIC_TERM = "HP:0000118";

    String JSON_ASYNC_RESPONSES = "responses";

    String JSON_SUBMITTER = "submitter";

    String JSON_SUBMITTER_NAME = "name";

    String JSON_SUBMITTER_EMAIL = "email";

    String JSON_SUBMITTER_INSTITUTION = "institution";

    String JSON_QUERY_TYPE = "queryType";

    String JSON_PATIENT_ID = "id";

    String JSON_PATIENT_LABEL = "label";

    String JSON_GENDER = "gender";

    String JSON_RESPONSE_ID = "queryID";

    String JSON_RESPONSE_TYPE = "responseType";

    String JSON_RESULTS = "results";

    String JSON_FEATURES = "features";

    // JSON Feature subfields
    String REPLY_JSON_FEATURE_AGE_OF_ONSET = "ageOfOnset";
    String REPLY_JSON_FEATURE_ID = "id";
    String REPLY_JSON_FEATURE_OBSERVED     = "observed";
    String REPLY_JSON_FEATURE_OBSERVED_YES = "yes";
    String REPLY_JSON_FEATURE_OBSERVED_NO  = "no";
    String REPLY_JSON_FEATURE_OBSERVED_UNK = "unknown";
    String REPLY_JSON_FEATURE_COUNT        = "count";
    String REPLY_JSON_FEATURE_MATCHED      = "matched";
    String REPLY_JSON_FEATURE_OBFUSCATED   = "obfuscated";
    String JSON_DISORDERS = "disorders";

    String JSON_GENES = "genes";
    // JSON genes subfields
    String JSON_GENES_GENENAME = "gene";
    String JSON_GENES_ASSEMBLY = "assembly";

    String REQUEST_RESPONSE_TYPE_SYNCHRONOUS  = "inline";
    String REQUEST_RESPONSE_TYPE_ASYNCHRONOUS = "asynchronous";
    // Used when the request does not explicitly specify response type
    String DEFAULT_REQUEST_RESPONSE_TYPE = REQUEST_RESPONSE_TYPE_SYNCHRONOUS;

    String REQUEST_QUERY_TYPE_ONCE     = "once";
    String REQUEST_QUERY_TYPE_PERIODIC = "periodic";
    String DEFAULT_REQUEST_QUERY_TYPE  = REQUEST_QUERY_TYPE_ONCE;

    String INTERNAL_JSON_STATUS = "status";

    // HTTP
    Integer HTTP_BAD_REQUEST  = 400;
    Integer HTTP_BAD_REQUEST1 = 409;  // debug
    Integer HTTP_BAD_REQUEST2 = 410;  // debug

    Integer HTTP_UNAUTHORIZED = 401;

    Integer HTTP_OK = 200;

    Integer HTTP_SERVER_ERROR = 500;
}
