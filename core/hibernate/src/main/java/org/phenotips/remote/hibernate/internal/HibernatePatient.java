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
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.MatchingPatientDisorder;
import org.phenotips.remote.api.MatchingPatientFeature;
import org.phenotips.remote.api.MatchingPatientGene;
import org.phenotips.remote.api.SearchRequest;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
public class HibernatePatient implements MatchingPatient
{
    @Id
    @GeneratedValue
    private long id;

    @Basic
    private String externalId;

    @Basic
    private String label;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requestentity_id", nullable = false)
    public AbstractSearchRequest requestentity;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hibernatepatient")
    @Cascade({ CascadeType.ALL })
    public Set<HibernatePatientFeature> features = new HashSet<HibernatePatientFeature>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hibernatepatient")
    @Cascade({ CascadeType.ALL })
    public Set<HibernatePatientDisorder> disorders = new HashSet<HibernatePatientDisorder>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hibernatepatient")
    @Cascade({ CascadeType.ALL })
    public Set<HibernatePatientGene> genes = new HashSet<HibernatePatientGene>();

    public HibernatePatient(String externalId, String label)
    {
        this.externalId = externalId;
        this.label = label;
    }

    @Override
    public void addFeatures(Set<MatchingPatientFeature> featureSet)
    {
        for (MatchingPatientFeature feature : featureSet) {
            feature.setParent(this);
            this.features.add((HibernatePatientFeature) feature);
        }
    }

    @Override
    public void addDisorders(Set<MatchingPatientDisorder> disorderSet)
    {
        for (MatchingPatientDisorder disorder : disorderSet) {
            disorder.setParent(this);
            this.disorders.add((HibernatePatientDisorder) disorder);
        }
    }

    @Override
    public void addGenes(Set<MatchingPatientGene> geneSet)
    {
        for (MatchingPatientGene gene : geneSet) {
            gene.setParent(this);
            this.genes.add((HibernatePatientGene) gene);
        }
    }

    @Override
    public void setParent(SearchRequest request)
    {
        this.requestentity = (AbstractSearchRequest) request;
    }

    @Override
    public Set<? extends Feature> getFeatures()
    {
        return this.features;
    }

    @Override
    public Set<? extends Disorder> getDisorders()
    {
        return this.disorders;
    }

    @Override
    public Set<? extends MatchingPatientGene> getGenes()
    {
        return this.genes;
    }

    @Override
    public String getExternalId()
    {
        return this.externalId;
    }

    @Override
    public String getLabel()
    {
        return this.label;
    }
}
