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

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

/**
 * This is where the constants and other configurations are stored for easy global access.
 */
public interface Configuration
{
    EntityReference XWIKI_SPACE = new EntityReference("XWiki", EntityType.SPACE);
    EntityReference PHENOMECENTRAL_SPACE = new EntityReference("PhenomeCentral", EntityType.SPACE);

    EntityReference USER_OBJECT_REFERENCE = new EntityReference("XWikiUsers", EntityType.DOCUMENT);
    EntityReference REMOTE_CONFIGURATIONS_DOCUMENT_REFERENCE =
        new EntityReference("XWikiPreferences", EntityType.DOCUMENT, XWIKI_SPACE);
    EntityReference REMOTE_CONFIGURATION_OBJECT_REFERENCE =
        new EntityReference("RemoteMatchingServiceConfiguration", EntityType.DOCUMENT, PHENOMECENTRAL_SPACE);
    EntityReference REMOTE_REQUEST_REFERENCE = new EntityReference("RemoteRequest", EntityType.DOCUMENT,
        new EntityReference("PhenomeCentral", EntityType.SPACE));

    String MAIL_SENDER = "mailsender";
    //TODO. This should be changed to the actual address.
    String EMAIL_FROM_ADDRESS = "antonkats@gmail.com";
    //TODO. Make the subject include the name of the current installation.
    String EMAIL_SUBJECT = "PhenomeCentral has found matches to your request";

    /** Document which can be relied upon to exist at all times. Needed for the REST server to work */
    EntityReference ABSOLUTE_DOCUMENT_REFERENCE =
        new EntityReference("XWikiPreferences", EntityType.DOCUMENT, XWIKI_SPACE);
    String REST_DEFAULT_USER_SPACE = "PhenomeCentral";
    String REST_DEFAULT_USER_NAME = "DefaultRemoteUser";

    /** Must not contain the '/' at the beginning of the string */
    //FIXME remove media=json
    String REMOTE_URL_SEARCH_ENDPOINT = "match";
    String REMOTE_URL_ASYNCHRONOUS_RESULTS_ENDPOINT = "matchResults";
    String REMOTE_URL_SEARCH_EXTENSION = REMOTE_URL_SEARCH_ENDPOINT + "?media=json&key=";

    //XWiki remote request/config
    String REMOTE_KEY_FIELD = "remoteAuthToken";
    String REMOTE_BASE_URL_FIELD = "baseURL";
    String REMOTE_HIBERNATE_ID = "hibernateId";
    String REMOTE_RESPONSE_FORMAT = "responseFormat";

    //Patient
    String FEATURE_AGE_OF_ONSET = "age_of_onset";

    //JSON
    String JSON_FEATURE_AGE_OF_ONSET = "age_of_onset";
    String JSON_SUBMITTER = "submitter";
    String JSON_SUBMITTER_NAME = "name";
    String JSON_SUBMITTER_EMAIL = "email";
    String JSON_SUBMITTER_INSTITUTION = "institution";
    String JSON_QUERY_TYPE = "queryType";
    String JSON_REQUEST_ID = "id";
    String JSON_RESPONSE_ID = "queryId";
    String JSON_RESPONSE_TYPE = "responseType";
    String JSON_RESULTS = "results";
    String JSON_DISORDERS = "disorders";

    String REQUEST_RESPONSE_TYPE_SYNCHRONOUS = "inline";
    String REQUEST_RESPONSE_TYPE_ASYCHRONOUS = "asynchronous";
    String REQUEST_RESPONSE_TYPE_EMAIL = "email";

    String DEFAULT_REQUEST_QUERY_TYPE = "once";
    String DEFAULT_INCOMING_REQUEST_RESPONSE_TYPE = REQUEST_RESPONSE_TYPE_SYNCHRONOUS;
    //Used when the request loader could not find a response type in JSON.
    String DEFAULT_NULL_REQUEST_RESPONSE_TYPE = REQUEST_RESPONSE_TYPE_ASYCHRONOUS;

    String INTERNAL_JSON_STATUS = "status";

    //HTTP
    Integer HTTP_BAD_REQUEST = 400;
    Integer HTTP_UNAUTHORIZED = 401;
    Integer HTTP_OK = 200;
    Integer HTTP_SERVER_ERROR = 500;

    //REST
    String URL_KEY_PARAMETER = "key";
}
