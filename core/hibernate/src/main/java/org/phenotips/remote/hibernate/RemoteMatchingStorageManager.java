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

import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.OutgoingMatchRequest;

import org.xwiki.component.annotation.Role;

@Role
public interface RemoteMatchingStorageManager
{
    /**
     * Stores the incoming request and the generated reply for audit purposes
     */
    void saveIncomingRequest(IncomingMatchRequest request);

    //=========================================================================

    /**
     * Stores the incoming request and the generated reply for audit purposes
     */
    void saveOutgoingRequest(OutgoingMatchRequest request);

    /**
     * Returns the last request+response received form the given server when quering for the given patient.
     * Returns null if no responses are currently cached.
     */
    OutgoingMatchRequest loadCachedOutgoingRequest(String remoteServerId, String patientId);
}
