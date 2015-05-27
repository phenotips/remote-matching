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

import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.data.internal.AbstractPhenoTipsOntologyProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * FIXME. Must extends the AbstractPhenoTipsOntologyProperty
 */
public class RemotePatientFeature extends AbstractPhenoTipsOntologyProperty implements Feature
{
    private String observedStatus = "unknown";

    private Map<String, FeatureMetadatum> metadata = new TreeMap<String, FeatureMetadatum>();

    public RemotePatientFeature(String id, String observedStatus)
    {
        super(id);
        this.observedStatus = observedStatus;
    }

    @Override
    public String getType()
    {
        if (this.observedStatus.equals("yes")) {
            return "phenotype";
        } else if (this.observedStatus.equals("no")) {
            return "negative_phenotype";
        } else {
            return "";
        }
    }

    @Override
    public boolean isPresent()
    {
        return this.observedStatus.equals("yes");
    }

    @Override
    public Map<String, ? extends FeatureMetadatum> getMetadata()
    {
        return new HashMap<String, FeatureMetadatum>();
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject result = super.toJSON();
        result.element("observed", getObserved());
        result.element("type", "phenotype");
        // TODO: get ageOfOnset out of metadata
        if (!this.metadata.isEmpty()) {
            JSONArray metadataList = new JSONArray();
            for (FeatureMetadatum metadatum : this.metadata.values()) {
                metadataList.add(metadatum.toJSON());
            }
            result.element("metadata", metadataList);
        }
        return result;
    }

    public String getObserved()
    {
        return this.observedStatus;
    }

    @Override
    public String getValue()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNotes()
    {
        throw new UnsupportedOperationException();
    }
}
