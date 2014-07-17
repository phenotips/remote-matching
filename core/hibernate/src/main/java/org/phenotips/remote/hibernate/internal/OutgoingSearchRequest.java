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

import org.phenotips.data.Patient;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * TODO.
 *
 * @version $Id$
 */
@Entity
@DiscriminatorValue("outgoing")
public class OutgoingSearchRequest extends AbstractRequest implements OutgoingSearchRequestInterface
{
    @Basic
    private String referencePatientId;

    @Transient
    private Patient referencePatient;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "requestentity")
    @Cascade({CascadeType.ALL})
    // @JoinColumn(name="RESULT_HP_ID")
    public Set<HibernatePatient> results = new HashSet<HibernatePatient>();

    public OutgoingSearchRequest()
    {

    }

    @Override
    protected String getURLSuffix() throws Exception
    {
        if (getKey() == null) {
            throw new Exception("No key is set");
        }
        return Configuration.REMOTE_URL_SEARCH_EXTENSION + getKey();
    }

    @Override
    public List<PatientSimilarityView> getResults(PatientSimilarityViewFactory viewFactory)
    {
        List<PatientSimilarityView> patientSimilarityViews = new LinkedList<PatientSimilarityView>();
        for (Patient patient : this.results) {
            patientSimilarityViews.add(viewFactory.makeSimilarPatient(patient, getReferencePatient()));
        }
        return patientSimilarityViews;
    }

    @Override
    public void setReferencePatient(Patient referencePatient)
    {
        this.referencePatient = referencePatient;
        this.referencePatientId = referencePatient.getId();
    }

    @Override
    public Patient getReferencePatient() throws NullPointerException
    {
        if (this.referencePatient != null) {
            return this.referencePatient;
        } else {
            throw new NullPointerException(
                "Reference patient is not set. Likely cause is this class instance being loaded from database");
        }
    }

    @Override
    public String getReferencePatientId()
    {
        return this.referencePatientId;
    }

    @Override
    public void addResults(Set<HibernatePatientInterface> results)
    {
        for (HibernatePatientInterface patient : results) {
            patient.setParent(this);
            this.results.add((HibernatePatient) patient);
        }
    }

    public Integer getResponseStatus()
    {
        throw new UnsupportedOperationException();
    }
}
