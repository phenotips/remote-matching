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

import org.phenotips.data.Disorder;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;

import org.xwiki.model.reference.DocumentReference;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;

/**
 * TODO.
 * @version $Id$
 */
@Entity
@DiscriminatorValue("outgoing")
public class OutgoingSearchRequest extends AbstractRequest implements OutgoingSearchRequestInterface
{
    @Basic
    private String externalId;

    @Basic
    private String referencePatientId;

    @Transient
    private Patient referencePatient;

    @Basic
    private String url;

//    //FIXME Check is right cascade type
//    @OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name="HP_ID")
//    public Set<HibernatePatientInt> results = new HashSet<HibernatePatient>();

    public List<PatientSimilarityView> getResults(PatientSimilarityViewFactory viewFactory)
    {
        List<PatientSimilarityView> patientSimilarityViews = new LinkedList<PatientSimilarityView>();
//        for (Patient patient : results) {
//            patientSimilarityViews.add(viewFactory.makeSimilarPatient(patient, referencePatient));
//        }
        return patientSimilarityViews;
    }

    @Override
    public void setReferencePatient(Patient referencePatient)
    {
        this.referencePatient = referencePatient;
        this.referencePatientId = referencePatient.getId();
    }

    public Patient getReferencePatient()
    {
        throw new UnsupportedOperationException();
    }

    public void addResult(JSONObject json)
    {
//        HibernatePatient resultPatient = new HibernatePatient();
//        resultPatient.populatePatient(json);
//        results.add(resultPatient);
    }

    public void setRequestExternalId(String id)
    {
        externalId = id;
    }

    public String getRequestExternalId()
    {
        return externalId;
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

    public long getRequestId()
    {
        return id;
    }

    public void setURL(String url)
    {
        this.url = url;
    }

    public String getURL()
    {
        return url;
    }

    public String getResponseType()
    {
        throw new UnsupportedOperationException();
    }

    public Integer getResponseStatus()
    {
        throw new UnsupportedOperationException();
    }

    public String getSubmitterEmail()
    {
        throw new UnsupportedOperationException();
    }

    public String getId()
    {
        throw new UnsupportedOperationException();
    }

    public String getExternalId()
    {
        throw new UnsupportedOperationException();
    }

    public DocumentReference getDocument()
    {
        //FIXME this is ugly. Can't leave empty.
        DocumentReference fakeReference = new DocumentReference("xwiki", "data", "0");
        return fakeReference;
    }

    public DocumentReference getReporter()
    {
        return new DocumentReference("xwiki", "XWiki", "Admin");
    }

    public Set<? extends Disorder> getDisorders()
    {
        return new HashSet<Disorder>();
    }

    public <T> PatientData<T> getData(String name)
    {
        throw new UnsupportedOperationException();
    }

    public JSONObject toJSON()
    {
        throw new UnsupportedOperationException();
    }
}
