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
package org.phenotips.remote.server.internal;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.RequestEntity;
import org.phenotips.similarity.SimilarPatientsFinder;

import org.xwiki.model.reference.DocumentReference;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons. It is a combination of
 * a Patient interface, and a Request interface. Some functions, such as getId are ambiguous, because they can apply
 * both to the patient and the request. However, this seems to be the lesser evil at this time.
 *
 * @version $Id$
 */
@Entity
public class IncomingSearchRequest implements RequestEntity
{
    @Id
    @GeneratedValue
    private long id;

    private HibernatePatientInterface referencePatient = null;

    private Integer httpStatus = 200;

    public IncomingSearchRequest()
    {
//        JSONArray jsonFeatures = (JSONArray) json.get("features");
//        for (Object jsonFeatureUncast : jsonFeatures) {
//            JSONObject jsonFeature = (JSONObject) jsonFeatureUncast;
//            HibernatePatientFeature feature = new HibernatePatientFeature();
//            feature.setId(jsonFeature.getString("id"));
//            feature.setPresent(convertTextToIntBool(jsonFeature.getString("observed")));
//            features.add(feature);
//        }
    }

    public String getResponseType() { return Configuration.IncomingRequestResponseType; }

    public void setReferencePatient(HibernatePatientInterface patient) { referencePatient = patient; }

    public HibernatePatientInterface getReferencePatient() throws IllegalArgumentException
    {
        if (referencePatient == null) {
            //FIXME. This should be custom.
            httpStatus = 400;
            throw new IllegalArgumentException("The reference patient for the incoming request has not been set");
        }
        return referencePatient;
    }

    public List<PatientSimilarityView> getResults(SimilarPatientsFinder finder) throws IllegalArgumentException
    {
        List<PatientSimilarityView> matches = finder.findSimilarPatients(getReferencePatient());
        return matches;
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
        throw new UnsupportedOperationException();
    }

    public Integer getResponseStatus() { return httpStatus; }

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
        return "RemoteRequest" + id;
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
}
