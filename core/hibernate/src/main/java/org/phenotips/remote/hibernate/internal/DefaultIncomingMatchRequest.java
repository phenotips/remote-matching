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
package org.phenotips.remote.hibernate.internal;

import org.phenotips.data.Patient;
import org.phenotips.remote.api.IncomingMatchRequest;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.json.JSONObject;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons.
 *
 * @version $Id$
 */
@Entity
@Table(name = "remote_matching_incoming_requests")
public class DefaultIncomingMatchRequest extends AbstractSearchRequest implements IncomingMatchRequest
{
    @Transient
    private Patient remotePatient;

    /**
     * Hibernate requires a no-args constructor
     */
    protected DefaultIncomingMatchRequest()
    {
    }

    /**
     * @param queryID when not null, the ID of the request that needs to be updated; iff null, an auto-generated ID will
     *            be assigned when the request is stored in the database
     */
    public DefaultIncomingMatchRequest(String remoteServerId, String apiVersionUsed,
        String request, Patient remotePatient)
    {
        super(remoteServerId, apiVersionUsed, request, null);

        this.remotePatient = remotePatient;
    }

    @Override
    public void addResponse(JSONObject response)
    {
        this.setResponse(response.toString());
    }

    public Patient getModelPatient()
    {
        return this.remotePatient;
    }
}
