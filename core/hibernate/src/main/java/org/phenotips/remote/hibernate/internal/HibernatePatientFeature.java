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

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.ontology.OntologyManager;
import org.phenotips.ontology.OntologyTerm;
import org.phenotips.remote.api.HibernatePatientFeatureInterface;
import org.phenotips.remote.api.HibernatePatientInterface;

import org.xwiki.component.manager.ComponentLookupException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Hibernate entity for storing patient features.
 *
 * FIXME. Must extends the AbstractPhenoTipsOntologyProperty
 */
@Entity
public class HibernatePatientFeature implements HibernatePatientFeatureInterface
{
    @Id
    @GeneratedValue
    private long hibernateId;

    @Basic
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hibernatepatient_id", nullable = false)
    public HibernatePatient hibernatepatient;

    /** 1 - true, -1 - false, 0 - NA */
    @Basic
    private int present = 0;

    @Basic
    private String name;

    @Transient
    private Map<String, FeatureMetadatum> metadata = new TreeMap<String, FeatureMetadatum>();

    @Override
    public void setParent(HibernatePatientInterface patient)
    {
        hibernatepatient = (HibernatePatient) patient;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        if (this.name != null) {
            return this.name;
        }
        try {
            OntologyManager om =
                ComponentManagerRegistry.getContextComponentManager().getInstance(OntologyManager.class);
            OntologyTerm term = om.resolveTerm(this.id);
            if (term != null && StringUtils.isNotEmpty(term.getName())) {
                this.name = term.getName();
                return this.name;
            }
        } catch (ComponentLookupException ex) {
            // Shouldn't happen
        }
        return this.id;
    }

    public String getType()
    {
        if (present == 1) {
            return "phenotype";
        } else if (present == -1) {
            return "negative_phenotype";
        } else {
            return "";
        }
    }

    public boolean isPresent()
    {
        return present == 1;
    }

    public Map<String, ? extends FeatureMetadatum> getMetadata()
    {
        return new HashMap<String, FeatureMetadatum>();
    }

    public JSONObject toJSON()
    {
        JSONObject result = new JSONObject();
        result.element("id", getId());
        result.element("name", getName());
        result.element("type", getType());
        result.element("isPresent", this.present);
        if (!this.metadata.isEmpty()) {
            JSONArray metadataList = new JSONArray();
            for (FeatureMetadatum metadatum : this.metadata.values()) {
                metadataList.add(metadatum.toJSON());
            }
            result.element("metadata", metadataList);
        }
        return result;
    }

    public void setId(String newId) {
        this.id = newId;
    }

    public void setPresent(Integer isPresent)
    {
        present = isPresent;
    }

    public String getValue()
    {
        throw new UnsupportedOperationException();
    }

    public String getNotes()
    {
        throw new UnsupportedOperationException();
    }
}
