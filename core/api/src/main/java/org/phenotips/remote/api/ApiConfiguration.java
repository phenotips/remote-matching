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
package org.phenotips.remote.api;

/**
 * TODO: make an interface so that different versions of the API can implement this differently. This is where the
 * constants and other configurations are stored for easy global access.
 */
public interface ApiConfiguration
{
    String LATEST_API_VERSION_STRING = "1.0";

    String HTTPHEADER_CONTENT_TYPE_PREFIX = "application/vnd.ga4gh.matchmaker.v";

    String HTTPHEADER_CONTENT_TYPE_SUFFIX = "+json";

    String HTTPHEADER_CONTENT_TYPE_SIMPLE = "application/vnd.ga4gh.matchmaker" + HTTPHEADER_CONTENT_TYPE_SUFFIX;

    String HTTPHEADER_KEY_PARAMETER = "X-Auth-Token";

    String HTTPHEADER_API_VERSION = "Accept";

    // Must not contain the '/' at the beginning of the string
    String REMOTE_URL_SEARCH_ENDPOINT = "match";

    String REMOTE_URL_ASYNCHRONOUS_RESULTS_ENDPOINT = "matchResults";

    // used for non-matched non-disclosed featues to indicate there is an unmatched feature
    String REPLY_JSON_FEATURE_HPO_MOST_GENERIC_TERM = "HP:0000118";

    // Patient
    String JSON_PATIENT = "patient";

    String JSON_PATIENT_AGEOFONSET = "ageOfOnset";

    String JSON_PATIENT_SPECIES = "species";

    String SPECIES_HUMAN = "NCBITaxon:9606";

    String JSON_CONTACT = "contact";

    String JSON_CONTACT_NAME = "name";

    String JSON_CONTACT_INSTITUTION = "institution";

    String JSON_CONTACT_HREF = "href";

    String JSON_PATIENT_ID = "id";

    String JSON_PATIENT_TEST = "test";

    String JSON_PATIENT_LABEL = "label";

    String JSON_PATIENT_GENDER = "sex";

    String JSON_PATIENT_GENDER_MALE = "MALE";

    String JSON_PATIENT_GENDER_FEMALE = "FEMALE";

    String JSON_PATIENT_GENDER_OTHER = "OTHER";

    String JSON_FEATURES = "features";

    // JSON Feature subfields
    String JSON_FEATURE_ID = "id";

    String JSON_FEATURE_LABEL = "label";

    String JSON_FEATURE_AGE_OF_ONSET = "ageOfOnset";

    String JSON_FEATURE_OBSERVED = "observed";

    String JSON_FEATURE_OBSERVED_YES = "yes";

    String JSON_FEATURE_OBSERVED_NO = "no";

    String JSON_FEATURE_COUNT = "count";

    String JSON_FEATURE_MATCHED = "matched";

    String JSON_DISORDERS = "disorders";

    String JSON_DIAGNOSIS = "clinical_diagnosis";

    // JSON disorder subfields
    String JSON_DISORDER_ID = "id";

    String JSON_GENES = "genomicFeatures";

    // JSON genes subfields
    String JSON_GENES_GENE = "gene";

    String JSON_GENES_GENE_ID = "id";

    String JSON_GENES_VARIANT = "variant";

    String JSON_GENES_VARIANT_ASSEMBLY = "assembly";

    String REPLY_JSON_RESULTS = "results";

    String REPLY_JSON_RESULTS_PATIENT = "patient";

    String REPLY_JSON_RESULTS_SCORE = "score";

    String REPLY_JSON_RESULTS_SCORE_PATIENT = "patient";

    String REPLY_JSON_HTTP_STATUS = "status";

    String REPLY_JSON_ERROR_DESCRIPTION = "message";

    String REPLY_JSON_SUPPORTEDVERSIONS = "supportedVersions";

    // HTTP codes reported to the other side
    Integer HTTP_OK = 200;

    Integer HTTP_BAD_REQUEST = 400;

    Integer HTTP_UNAUTHORIZED = 401;

    Integer HTTP_SERVER_ERROR = 500;

    Integer HTTP_UNSUPPORTED_API_VERSION = 406;

    // Local error codes consumed only internally (TODO: review local error handling)
    Integer ERROR_NOT_SENT = -1;

    Integer ERROR_INTERNAL = -2;
}
