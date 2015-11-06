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
package org.phenotips.remote.common;

import org.phenotips.Constants;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

/**
 * This is where the constants and other configurations are stored for easy global access.
 */
public interface ApplicationConfiguration
{
    EntityReference XWIKI_SPACE = new EntityReference("XWiki", EntityType.SPACE);

    EntityReference PHENOMECENTRAL_SPACE = new EntityReference("PhenomeCentral", EntityType.SPACE);

    EntityReference USER_OBJECT_REFERENCE = new EntityReference("XWikiUsers", EntityType.DOCUMENT);

    EntityReference XWIKI_PREFERENCES_DOCUMENT_REFERENCE =
        new EntityReference("XWikiPreferences", EntityType.DOCUMENT, XWIKI_SPACE);

    EntityReference REMOTE_CONFIGURATION_OBJECT_REFERENCE =
        new EntityReference("RemoteMatchingServiceConfiguration", EntityType.DOCUMENT, PHENOMECENTRAL_SPACE);

    EntityReference PATIENT_CONSENT_OBJECT_REFERENCE =
        new EntityReference("PatientConsent", EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    // XWiki remote request/config
    String CONFIGDOC_REMOTE_SERVER_NAME = "humanReadableName";
    String CONFIGDOC_REMOTE_SERVER_ID   = "serverId";

    String CONFIGDOC_REMOTE_KEY_FIELD = "remoteAuthToken";     // used for accessing remote server
    String CONFIGDOC_LOCAL_KEY_FIELD  = "localAuthToken";      // used for accessing this server

    String CONFIGDOC_REMOTE_BASE_URL_FIELD  = "baseURL";
    String CONFIGDOC_REMOTE_SERVER_LIMIT_IP = "limitAcceptedIPs";

    /** Document which can be relied upon to exist at all times. Needed for the REST server to work */
    EntityReference ABSOLUTE_DOCUMENT_REFERENCE =
        new EntityReference("XWikiPreferences", EntityType.DOCUMENT, XWIKI_SPACE);

    String REST_DEFAULT_USER_SPACE = "PhenomeCentral";

    String REST_DEFAULT_USER_NAME = "DefaultRemoteUser";

    String FEATURE_METADATA_AGEOFONSET = "age_of_onset";
}
