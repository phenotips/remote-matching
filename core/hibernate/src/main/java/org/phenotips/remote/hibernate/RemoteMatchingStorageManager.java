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
package org.phenotips.remote.hibernate;

import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.OutgoingSearchRequest;

import org.xwiki.component.annotation.Role;

import java.util.Map;

@Role
public interface RemoteMatchingStorageManager
{
    // =======================================================================
    /**
     * Saves a new incoming periodic request or updates an existing request
     *
     * @param request
     * @return queryID assigned to this request
     */
    String saveIncomingPeriodicRequest(IncomingSearchRequest request);

    /**
     * Replaces existing periodic request with the given queryID with Throws iff request has no queryID field set
     *
     * @param request
     * @return true iff a request with the given ID was present in the system
     */
    boolean updateIncomingPeriodicRequest(IncomingSearchRequest request);

    boolean deleteIncomingPeriodicRequest(String queryID);

    //=======================================================================

    /**
     * Saves the request,
     *
     * @param request
     * @param patientID
     * @return
     */
    String saveOutgoingRequest(OutgoingSearchRequest request, String patientID);

    /**
     * @param patientID
     * @return last request or null if none found
     */
    OutgoingSearchRequest loadOutgoingRequest(String patientID, String remoteServerId);

    Map<OutgoingSearchRequest, String> loadAllOutgoingRequests(String patientID);

    void deleteAllOutgoingRequestsForPatient(String patientID);

    //=======================================================================
}
