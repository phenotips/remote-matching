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
import org.phenotips.data.Feature;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.RequestEntity;
import org.phenotips.similarity.SimilarPatientsFinder;

import org.xwiki.model.reference.DocumentReference;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons. It is a combination of
 * a Patient interface, and a Request interface. Some functions, such as getId are ambiguous, because they can apply
 * both to the patient and the request. However, this seems to be the lesser evil at this time.
 *
 * @version $Id$
 */
@Entity
public class IncomingSearchRequest implements Patient, RequestEntity
{
    /*
    The only functions from the Patient implementation that are needed for the search to work are the getDocument and getFeatures()
     */
    @Id
    @GeneratedValue
    private long id;

//    @Type(type = "org.phenotips.remote.api.SearchRequestFeature")
    @OneToMany(mappedBy = "incomingsearchrequest")
    private Set<SearchRequestFeature> features = new HashSet<SearchRequestFeature>();

    public IncomingSearchRequest(JSONObject json, Session session)
    {
        JSONArray jsonFeatures = (JSONArray) json.get("features");
        for (Object jsonFeatureUncast : jsonFeatures) {
            JSONObject jsonFeature = (JSONObject) jsonFeatureUncast;
            SearchRequestFeature feature = new SearchRequestFeature();
            feature.setId(jsonFeature.getString("id"));
            feature.setPresent(convertTextToIntBool(jsonFeature.getString("observed")));
            features.add(feature);
        }
    }

    public List<PatientSimilarityView> getResults(SimilarPatientsFinder finder)
    {
        List<PatientSimilarityView> matches = finder.findSimilarPatients(this);
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

    public Set<? extends Feature> getFeatures() {
        return features;
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

    public JSONObject toJSON(Collection<String> onlyFieldNames)
    {
        throw new UnsupportedOperationException();
    }

    public void updateFromJSON(JSONObject json) {
        throw new UnsupportedOperationException();
    }
}
