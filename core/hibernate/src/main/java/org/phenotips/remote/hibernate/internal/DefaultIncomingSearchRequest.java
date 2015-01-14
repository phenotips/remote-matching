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

import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.MatchingPatient;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
//import javax.persistence.Transient;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons.
 *
 * @version $Id$
 */
@Entity
@DiscriminatorValue("incoming")
public class DefaultIncomingSearchRequest extends AbstractSearchRequest implements IncomingSearchRequest
{
    @OneToOne(mappedBy = "requestentity", cascade = CascadeType.ALL)
    private HibernatePatient referencePatient = null;

    public DefaultIncomingSearchRequest(MatchingPatient remoteModelPatient, String remoteServerId)
    {
        this(remoteModelPatient, remoteServerId, null);
    }

    /**
     * @param queryID when not null, the ID of the request that needs to be updated;
     *                iff null, an auto-generatred ID wil be assigned when the request is stored in the database
     */
    public DefaultIncomingSearchRequest(MatchingPatient remoteModelPatient, String remoteServerId, String queryId)
    {
        this.setRemoteServerId(remoteServerId);

        if (queryId != null) {
            try {
                this.setId(Long.parseLong(queryId));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("The provided queryID is not valid");
            }
        }

        if (remoteModelPatient == null) {
            throw new IllegalArgumentException("The reference patient for the incoming request has not been set");
        }
        this.referencePatient = (HibernatePatient) remoteModelPatient;
        this.referencePatient.setParent(this);
    }

    @Override
    public String getQueryId()
    {
        if (this.getId() == null) {
            return null;
        }
        return this.getId().toString();
    }

    @Override
    public MatchingPatient getRemotePatient()
    {
        return this.referencePatient;
    }
}
