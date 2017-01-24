/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.remote.common.internal;

import org.phenotips.data.ContactInfo;
import org.phenotips.data.PatientContactsManager;
import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.IndexedPatientData;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.SimpleValuePatientData;
import org.phenotips.remote.api.MatchingPatientGene;
import org.xwiki.model.reference.DocumentReference;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

/**
 * Implementation of {@link org.phenotips.data.Patient} based on the data obtained from the remote server for use in the
 * similarity search algorithm (which takes a model patient as one of the inputs)
 *
 * @version $Id$
 * @since 1.0M8
 */
public class RemoteMatchingPatient implements Patient
{
    final private String remotePatientId;

    final private String label;

    final private Set<? extends Feature> features;

    final private Set<? extends Disorder> disorders;

    final private Set<MatchingPatientGene> genes;

    final private PatientContactsManager contacts;

    public RemoteMatchingPatient(String remotePatientId, String label, Set<Feature> features,
        Set<Disorder> disorders, Set<MatchingPatientGene> genes, ContactInfo contactInfo)
    {
        this.remotePatientId = remotePatientId;
        this.label = label;
        this.features = (features != null) ? features : new HashSet<Feature>();
        this.disorders = (disorders != null) ? disorders : new HashSet<Disorder>();
        this.genes = (genes != null) ? genes : new HashSet<MatchingPatientGene>();
        this.contacts = new RemotePatientContactsManager(contactInfo);
    }

    @Override
    public String getId()
    {
        return this.remotePatientId;
    }

    @Override
    public String getExternalId()
    {
        return this.label;
    }

    @Override
    public DocumentReference getDocument()
    {
        return null;
    }

    @Override
    public DocumentReference getReporter()
    {
        return null;
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> PatientData<T> getData(String name)
    {
        // TODO: somehow reuse GeneController.load()?
        if (name == "genes") {
            Set<? extends MatchingPatientGene> genes = this.genes;

            List<Map<String, String>> allGenes = new LinkedList<Map<String, String>>();

            for (MatchingPatientGene gene : genes) {
                Map<String, String> singleGene = new LinkedHashMap<String, String>();
                singleGene.put("gene", gene.getName());
                allGenes.add(singleGene);
            }
            return (PatientData<T>) new IndexedPatientData<>("genes", allGenes);
        }

        if (name == "contact") {
            return (PatientData<T>) new SimpleValuePatientData<>("contact", this.contacts);
        }

        return null;
    }

    @Override
    public JSONObject toJSON()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject toJSON(Collection<String> selectedFields)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFromJSON(JSONObject json)
    {
        throw new UnsupportedOperationException();
    }
}
