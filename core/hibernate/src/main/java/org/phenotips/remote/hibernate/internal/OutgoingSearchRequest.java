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
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

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

    //FIXME Check is right cascade type
    @OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="HP_ID")
    public Set<HibernatePatient> results = new HashSet<HibernatePatient>();

    public OutgoingSearchRequest()
    {
        setQueryType(Configuration.DEFAULT_OUTGOING_REQUEST_QUERY_TYPE);
    }

    public List<PatientSimilarityView> getResults(PatientSimilarityViewFactory viewFactory)
    {
        List<PatientSimilarityView> patientSimilarityViews = new LinkedList<PatientSimilarityView>();
        for (Patient patient : results) {
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

    public Patient getReferencePatient() throws NullPointerException
    {
        if (referencePatient != null) {
            return referencePatient;
        } else {
            throw new NullPointerException(
                "Reference patient is not set. Likely cause is this class instance being loaded from database");
        }
    }

    public String getReferencePatientId() { return referencePatientId; }

    public void addResults(Set<HibernatePatientInterface> results)
    {
        for (HibernatePatientInterface patient: results) {
            this.results.add((HibernatePatient) patient);
        }
    }

    private int convertTextToIntBool(String text)
    {
        if (StringUtils.equalsIgnoreCase(text, "yes")) {
            return 1;
        } else if (StringUtils.equalsIgnoreCase(text, "no")) {
            return -1;
        } else {
            return 0;
        }
    }

    public String getResponseType()
    {
        throw new UnsupportedOperationException();
    }

    public Integer getResponseStatus()
    {
        throw new UnsupportedOperationException();
    }
}
