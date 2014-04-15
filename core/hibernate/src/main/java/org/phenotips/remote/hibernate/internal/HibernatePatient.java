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
import org.phenotips.data.Feature;
import org.phenotips.data.PatientData;
import org.phenotips.remote.api.HibernatePatientDisorderInterface;
import org.phenotips.remote.api.HibernatePatientFeatureInterface;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.RequestInterface;

import org.xwiki.model.reference.DocumentReference;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import net.sf.json.JSONObject;

@Entity
public class HibernatePatient implements HibernatePatientInterface
{
    @Id
    @GeneratedValue
    private long id;

    @Basic
    private String externalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requestentity_id", nullable = false)
    public AbstractRequest requestentity;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hibernatepatient")
    @Cascade({CascadeType.ALL})
    public Set<HibernatePatientFeature> features = new HashSet<HibernatePatientFeature>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hibernatepatient")
    @Cascade({CascadeType.ALL})
    public Set<HibernatePatientDisorder> disorders = new HashSet<HibernatePatientDisorder>();

    public HibernatePatient()
    {

    }

    public void addFeatures(Set<HibernatePatientFeatureInterface> featureSet)
    {
        for (HibernatePatientFeatureInterface feature : featureSet) {
            feature.setParent(this);
            features.add((HibernatePatientFeature) feature);
        }
    }

    public void addDisorders(Set<HibernatePatientDisorderInterface> disorderSet)
    {
        for (HibernatePatientDisorderInterface disorder : disorderSet) {
            disorder.setParent(this);
            disorders.add((HibernatePatientDisorder) disorder);
        }
    }

    public String getId()
    {
        return "RemotePatient" + id;
    }

    public String getExternalId()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(RequestInterface request)
    {
        requestentity = (AbstractRequest) request;
    }

    public DocumentReference getDocument()
    {
        //FIXME this is ugly. Can't leave empty.
        DocumentReference fakeReference = new DocumentReference("xwiki", "data", "0");
        return fakeReference;
    }

    public DocumentReference getReporter()
    {
        return new DocumentReference("xwiki", "XWiki", "Undisclosed");
    }

    public Set<? extends Feature> getFeatures()
    {
//        return new HashSet<Feature>();
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

    public void updateFromJSON(JSONObject json)
    {
        throw new UnsupportedOperationException();
    }
}
