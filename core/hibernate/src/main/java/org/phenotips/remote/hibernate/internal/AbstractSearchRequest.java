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
package org.phenotips.remote.hibernate.internal;

import org.phenotips.remote.api.SearchRequest;
import org.phenotips.remote.api.ApiConfiguration;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import java.sql.Timestamp;

//import static javax.persistence.GenerationType.SEQUENCE;

/**
 * This class combines shared functions between the different search request types (
 * {@link org.phenotips.remote.hibernate.internal.DefaultOutgoingSearchRequest},
 * {@link org.phenotips.remote.hibernate.internal.DefaultIncomingSearchRequest}). The children of this class are made
 * persistent by Hibernate.
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "request_type")
@Table(name = "remote_matching_requests")
public abstract class AbstractSearchRequest implements SearchRequest
{
    @Id
    @GeneratedValue
    private Long id;

    @Basic
    private String submitterName;

    @Basic
    private String submitterEmail;

    @Basic
    private String submitterInstitution;

    /** Could be either inline, asynchronous, or email */
    @Basic
    private String responseType = ApiConfiguration.DEFAULT_REQUEST_RESPONSE_TYPE;

    /** Could be either once or periodic */
    @Basic
    private String queryType = ApiConfiguration.DEFAULT_REQUEST_QUERY_TYPE;

    @Basic
    private String remoteServerId;

    @Basic
    private Timestamp lastResultsTime;

    protected void setId(Long id)
    {
        this.id = id;
    }

    protected Long getId()
    {
        return this.id;
    }

    @Override
    public String getRemoteServerId()
    {
        return this.remoteServerId;
    }

    protected void setRemoteServerId(String serverId)
    {
        this.remoteServerId = serverId;
    }

    public Timestamp getLastResultTime()
    {
        return this.lastResultsTime;
    }

    public void setLastResultsTimeToNow()
    {
        this.lastResultsTime = new Timestamp(System.currentTimeMillis());
    }

    public void setSubmitterName(String submitterName)
    {
        this.submitterName = submitterName;
    }

    @Override
    public String getSubmitterName()
    {
        return this.submitterName;
    }

    public void setSubmitterEmail(String email)
    {
        this.submitterEmail = email;
    }

    @Override
    public String getSubmitterEmail()
    {
        return this.submitterEmail;
    }

    public void setSubmitterInstitution(String institution)
    {
        this.submitterInstitution = institution;
    }

    @Override
    public String getSubmitterInstitution()
    {
        return this.submitterInstitution;
    }

    @Override
    public String getQueryType()
    {
        return this.queryType;
    }

    public void setQueryType(String queryType)
    {
        if (queryType.equals(ApiConfiguration.REQUEST_QUERY_TYPE_ONCE) ||
            queryType.equals(ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC)) {
            this.queryType = queryType;
        }
        // TODO: else: throw ?
    }

    public void setResponseType(String responseType)
    {
        if (responseType.equals(ApiConfiguration.REQUEST_RESPONSE_TYPE_SYNCHRONOUS) ||
            responseType.equals(ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS)) {
            this.responseType = responseType;
        }
        // TODO: else: throw ?
    }

    @Override
    public String getResponseType()
    {
        return this.responseType;
    }
}
