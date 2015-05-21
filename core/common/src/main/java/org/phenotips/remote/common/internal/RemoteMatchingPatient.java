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
package org.phenotips.remote.common.internal;

import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.IndexedPatientData;
import org.phenotips.data.PatientData;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.MatchingPatientGene;
import org.phenotips.remote.api.ContactInfo;
import org.xwiki.model.reference.DocumentReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

/**
 * Implementation of {@link org.phenotips.data.Patient} based on the data obtained from the remote server for use in the
 * similarity search algorithm (which takes a model patient as one of the inputs)
 *
 * @version $Id$
 * @since 1.0M8
 */
public class RemoteMatchingPatient implements MatchingPatient
{
    final private String remotePatientId;

    final private String label;

    final private Set<? extends Feature> features;

    final private Set<? extends Disorder> disorders;

    final private Set<MatchingPatientGene> genes;

    final private ContactInfo contactInfo;

    final private Map<String, PatientData<?>> extraData = new HashMap<String, PatientData<?>>();

    public RemoteMatchingPatient(String remotePatientId, String label, Set<Feature> features,
                                 Set<Disorder> disorders, Set<MatchingPatientGene> genes, ContactInfo contactInfo)
    {
        this.remotePatientId = remotePatientId;
        this.label = label;
        this.features = (features != null) ? features : new HashSet<Feature>();
        this.disorders = (disorders != null) ? disorders : new HashSet<Disorder>();
        this.genes = (genes != null) ? genes : new HashSet<MatchingPatientGene>();
        this.contactInfo = contactInfo;
    }

    @Override
    public String getId()
    {
        return remotePatientId;
    }

    @Override
    public String getExternalId()
    {
        return remotePatientId;
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

    @Override
    public String getLabel()
    {
        return this.label;
    }

    @Override
    public ContactInfo getContactInfo()
    {
        return this.contactInfo;
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
            return (PatientData<T>) new IndexedPatientData<Map<String, String>>("genes", allGenes);
        }
        return (PatientData<T>) this.extraData.get(name);
    }

    @Override
    public JSONObject toJSON()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject toJSON(Collection<String> onlyFieldNames)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateFromJSON(JSONObject json)
    {
        throw new UnsupportedOperationException();
    }
}
