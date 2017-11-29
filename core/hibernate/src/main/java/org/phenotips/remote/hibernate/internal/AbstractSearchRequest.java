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

import org.phenotips.remote.api.MatchRequest;

import java.sql.Timestamp;

import javax.persistence.Basic;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class combines shared functions between the different search request types (
 * {@link org.phenotips.remote.hibernate.internal.DefaultOutgoingMatchRequest},
 * {@link org.phenotips.remote.hibernate.internal.DefaultIncomingMatchRequest}). The children of this class are made
 * persistent by Hibernate.
 */
@MappedSuperclass()
public abstract class AbstractSearchRequest implements MatchRequest
{
    @Id
    @GeneratedValue
    private Long id;

    // Note on indexing:
    //  since this is a @MappedSuperclass a simple @Index(name=xxx) annotation can not be used here, since if used
    //  an index with the same name "xxx" will be created for all derived classes, causing a duplicate index name error

    @Basic
    private String remoteServerId;

    @Basic
    private Timestamp requestTime;

    @Type(type = "text")
    private String request;

    @Type(type = "text")
    private String response;

    @Basic
    private String apiVersionUsed;

    /**
     * Hibernate requires a no-args constructor
     */
    protected AbstractSearchRequest()
    {
    }

    public AbstractSearchRequest(String remoteServerId, String apiVersionUsed, String request, String response)
    {
        this.remoteServerId = remoteServerId;
        this.apiVersionUsed = apiVersionUsed;

        this.setRequest(request);
        this.setResponse(response);

        this.requestTime = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public String getRemoteServerId()
    {
        return this.remoteServerId;
    }

    @Override
    public String getApiVersionUsed()
    {
        return this.apiVersionUsed;
    }

    @Override
    public Timestamp getRequestTime()
    {
        return this.requestTime;
    }

    @Override
    public JSONObject getRequestJSON()
    {
        try {
            return new JSONObject(this.request);
        } catch (JSONException ex) {
            return null;
        }
    }

    @Override
    public JSONObject getResponseJSON()
    {
        try {
            return new JSONObject(this.response);
        } catch (JSONException | NullPointerException ex) {
            return null;
        }
    }

    protected void setRequest(String request)
    {
        this.request = request;
    }

    protected void setResponse(String response)
    {
        this.response = response;
    }
}
