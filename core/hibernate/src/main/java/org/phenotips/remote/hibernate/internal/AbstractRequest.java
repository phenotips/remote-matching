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

import org.phenotips.remote.api.RequestInterface;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

/**
 * This class combines shared functions between the different search request types ({@link
 * org.phenotips.remote.hibernate.internal.OutgoingSearchRequest},
 * {@link org.phenotips.remote.hibernate.internal.IncomingSearchRequest}).
 * The children of this class are made persistent by Hibernate.
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "request_type")
@Table(name = "remote_matching_requests")
public abstract class AbstractRequest implements RequestInterface
{
    @Id
    @GeneratedValue
    protected long id;

    @Basic
    private String submitterName;

    @Basic
    private String submitterEmail;

    @Basic
    private String submitterInstitution;

    @Basic
    private String key;

    @Override
    public void setSubmitterName(String submitterName)
    {
        this.submitterName = submitterName;
    }

    @Override
    public String getSubmitterName()
    {
        return submitterName;
    }

    @Override
    public void setSubmitterEmail(String email)
    {
        this.submitterEmail = email;
    }

    @Override
    public String getSubmitterEmail()
    {
        return submitterEmail;
    }

    @Override
    public void setSubmitterInstitution(String institution)
    {
        this.submitterInstitution = institution;
    }

    @Override
    public String getSubmitterInstitution()
    {
        return submitterInstitution;
    }

    @Override
    public void setKey(String key)
    {
        this.key = key;
    }

    @Override
    public String getKey()
    {
        return key;
    }
}
