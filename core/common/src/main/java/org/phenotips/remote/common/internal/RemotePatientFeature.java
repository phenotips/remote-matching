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

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.vocabulary.VocabularyManager;
import org.phenotips.vocabulary.VocabularyTerm;
import org.xwiki.component.manager.ComponentLookupException;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * FIXME. Must extends the AbstractPhenoTipsOntologyProperty
 */
public class RemotePatientFeature implements Feature
{
    private String id;

    private String observedStatus = "unknown";

    private String name;

    private Map<String, FeatureMetadatum> metadata = new TreeMap<String, FeatureMetadatum>();

    public RemotePatientFeature(String id, String observedStatus)
    {
        this.id = id;
        this.observedStatus = observedStatus;
        this.name = null;
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getName()
    {
        if (this.name != null) {
            return this.name;
        }
        try {
            VocabularyManager om =
                ComponentManagerRegistry.getContextComponentManager().getInstance(VocabularyManager.class);
            VocabularyTerm term = om.resolveTerm(this.id);
            if (term != null && StringUtils.isNotEmpty(term.getName())) {
                this.name = term.getName();
                return this.name;
            }
        } catch (ComponentLookupException ex) {
            // Shouldn't happen
        }
        return this.id;
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
        JSONObject result = new JSONObject();
        result.element("id", getId());
        result.element("observed", getObserved());
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
