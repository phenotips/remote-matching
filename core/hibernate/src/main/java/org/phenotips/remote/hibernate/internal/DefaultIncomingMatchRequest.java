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

import org.hibernate.annotations.Index;
import org.json.JSONObject;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons.
 *
 * @version $Id$
 */
@Entity
@Table(name = "remote_matching_incoming_requests")

//Can not add an @Index annotation in superclass, so have to add the index manually
@org.hibernate.annotations.Table(appliesTo = "remote_matching_incoming_requests", indexes =
          { @Index(name = "incomingmme_remoteServerIdIndex", columnNames = { "remoteServerId" }) })

public class DefaultIncomingMatchRequest extends AbstractSearchRequest implements IncomingMatchRequest
{
    @Transient
    private Patient remotePatient;

    @Transient
    private boolean isTestRequest;

    /**
     * Hibernate requires a no-args constructor
     */
    protected DefaultIncomingMatchRequest()
    {
    }

    /**
     * @param remoteServerId
     * @param apiVersionUsed
     * @param request
     * @param remotePatient
     */
    public DefaultIncomingMatchRequest(String remoteServerId, String apiVersionUsed,
        String request, Patient remotePatient, boolean isTestRequest)
    {
        super(remoteServerId, apiVersionUsed, request, null);

        this.remotePatient = remotePatient;
        this.isTestRequest = isTestRequest;
    }

    @Override
    public void addResponse(JSONObject response)
    {
        this.setResponse(response.toString());
    }

    @Override
    public Patient getModelPatient()
    {
        return this.remotePatient;
    }

    @Override
    public boolean isTestRequest()
    {
        return this.isTestRequest;
    }
}
