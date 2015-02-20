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

import org.phenotips.remote.api.ContactInfo;
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
    private String contactName;

    @Basic
    private String contactInstitution;

    @Basic
    private String contactHREF;

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

    public void setContact(String name, String institution, String href)
    {
        this.contactName = name;
        this.contactInstitution = institution;
        this.contactHREF = href;
    }

    public ContactInfo getContactInfo()
    {
        return null;
    }
}
