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
package org.phenotips.remote.hibernate;

import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.OutgoingMatchRequest;

import org.xwiki.component.annotation.Role;

@Role
public interface RemoteMatchingStorageManager
{
    /**
     * Stores the incoming request and the generated reply for audit purposes.
     *
     * @param request
     */
    void saveIncomingRequest(IncomingMatchRequest request);

    /**
     * Stores the incoming request and the generated reply for audit purposes.
     *
     * @param request
     */
    void saveOutgoingRequest(OutgoingMatchRequest request);

    /**
     * Returns the last request and response received form the given server when querying for the given patient.
     *
     * Returns null if no such requests exist in the database.
     *
     * @param remoteServerId
     * @param patientId
     * @return
     */
    OutgoingMatchRequest getLastOutgoingRequest(String remoteServerId, String patientId);

    /**
     * Returns the last successful request and response received form the given server when querying for
     * the given patient. A request is considered successful if it resulted in an HTTP OK status, even if no
     * matches were found.
     *
     * Returns null if no such requests exist in the database.
     *
     * @param remoteServerId
     * @param patientId
     * @return
     */
    OutgoingMatchRequest getLastSuccessfulOutgoingRequest(String remoteServerId, String patientId);

    /**
     * Removes all outgoing matches for the given patient (presumably because the patient is deleted)
     * @param patientId the local patient ID for whom the outgoing matches should be deleted
     */
    void deleteMatchesForLocalPatient(String patientId);
}
