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
package org.phenotips.remote.api.internal;

import org.phenotips.data.Disorder;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.api.OutgoingRequestEntity;

import org.xwiki.model.reference.DocumentReference;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons. It is a combination of
 * a Patient interface, and a Request interface. Some functions, such as getId are ambiguous, because they can apply
 * both to the patient and the request. However, this seems to be the lesser evil at this time.
 *
 * @version $Id$
 */
//@Entity
public class OutgoingSearchRequest implements OutgoingRequestEntity
{
//    @Id
//    @GeneratedValue
    private long id;

//    @Basic
    private String externalId;
//
//    //FIXME Check is right cascade type
//    @OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name="HP_ID")
//    public Set<HibernatePatientInt> results = new HashSet<HibernatePatient>();

    //Don't bother saving the patient object. Just save the reference. That way if the patient is updated,
    //we are not missing out on the update.
    //Maybe there's not even a need to do that.

    public List<PatientSimilarityView> getResults(Patient referencePatient, PatientSimilarityViewFactory viewFactory)
    {
        List<PatientSimilarityView> patientSimilarityViews = new LinkedList<PatientSimilarityView>();
//        for (Patient patient : results) {
//            patientSimilarityViews.add(viewFactory.makeSimilarPatient(patient, referencePatient));
//        }
        return patientSimilarityViews;
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

    public String getResponseType()
    {
        throw new UnsupportedOperationException();
    }

    public boolean getResponseStatus()
    {
        throw new UnsupportedOperationException();
    }

    public String getResponseTargetURL()
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
