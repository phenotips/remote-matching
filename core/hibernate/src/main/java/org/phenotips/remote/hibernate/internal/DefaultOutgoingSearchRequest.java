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

//import org.phenotips.data.Patient;
//import org.phenotips.data.similarity.PatientSimilarityView;
//import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.OutgoingSearchRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
//import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * TODO.
 *
 * @version $Id$
 */
@Entity
@DiscriminatorValue("outgoing")
public class DefaultOutgoingSearchRequest extends AbstractSearchRequest implements OutgoingSearchRequest
{
    /**
     * Query ID as reported by the remote server in the search request response
     */
    @Basic
    String remoteQueryId;

    @Basic
    private String localPatientId;

    // @JoinColumn(name="RESULT_HP_ID")
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "requestentity")
    @Cascade({ CascadeType.ALL })
    public Set<HibernatePatient> results = new HashSet<HibernatePatient>();

    public DefaultOutgoingSearchRequest(String localPatientId, String remoteServerId)
    {
        this.setRemoteServerId(remoteServerId);
        this.localPatientId = localPatientId;
        this.remoteQueryId = null;
    }

    @Override
    public String getQueryId()
    {
        return this.remoteQueryId;
    }

    @Override
    public void setQueryID(String id)
    {
        this.remoteQueryId = id;
    }

    @Override
    public Set<MatchingPatient> getResults()
    {
        Set<MatchingPatient> result = new HashSet<MatchingPatient>();
        for (HibernatePatient remotePatient : this.results) {
            result.add(remotePatient);
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public void setResults(Set<MatchingPatient> results)
    {
        this.results.clear();
        this.setLastResultsTimeToNow();
        if (results == null) {
            return;
        }
        for (MatchingPatient patient : results) {
            patient.setParent(this);
            this.results.add((HibernatePatient) patient);
        }
    }

    @Override
    public String getReferencePatientId()
    {
        return this.localPatientId;
    }
}
