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

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.OutgoingMatchRequest;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.json.JSONObject;

/**
 * TODO.
 *
 * @version $Id$
 */
@Entity
@Table(name = "remote_matching_outgoing_requests")

// Can not add an @Index annotation in superclass, so have to add the index manually
@org.hibernate.annotations.Table(appliesTo = "remote_matching_outgoing_requests", indexes =
             { @Index(name = "outgoingmme_remoteServerIdIndex", columnNames = { "remoteServerId" }) })

public class DefaultOutgoingMatchRequest extends AbstractSearchRequest implements OutgoingMatchRequest
{
    @Basic
    @Index(name = "localRefPatientId")
    private String localReferencePatientId;

    @Basic
    private Integer replyHTTPStatus;

    /**
     * Hibernate requires a no-args constructor
     */
    protected DefaultOutgoingMatchRequest()
    {
    }

    public DefaultOutgoingMatchRequest(String remoteServerId, String apiVersionUsed, String localReferencePatientId)
    {
        super(remoteServerId, apiVersionUsed, null, null);

        this.localReferencePatientId = localReferencePatientId;

        this.replyHTTPStatus = 0;
    }

    @Override
    public String getLocalReferencePatientId()
    {
        return this.localReferencePatientId;
    }

    @Override
    public boolean wasSent()
    {
        if (this.getRequestJSON() == null) {
            return false;
        }
        if (this.getRequestJSON().has(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean errorContactingRemoteServer()
    {
        return (this.replyHTTPStatus == ApiConfiguration.ERROR_COMMUNICATION_PROBLEM);
    }

    @Override
    public boolean gotValidReply()
    {
        return (this.wasSent() && this.getResponseJSON() != null);
    }

    @Override
    public Integer getRequestStatusCode()
    {
        return this.replyHTTPStatus;
    }

    public void setReplayHTTPStatus(Integer replyHTTPStatus)
    {
        this.replyHTTPStatus = replyHTTPStatus;
    }

    public void addRequestJSON(JSONObject request)
    {
        this.setRequest(request.toString());
    }

    public void addResponseString(String responseString)
    {
        this.setResponse(responseString);
    }
}
