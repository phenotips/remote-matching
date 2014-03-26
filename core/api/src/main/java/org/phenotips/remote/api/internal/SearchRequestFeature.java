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

import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.sf.json.JSONObject;

/**
 * Hibernate entity for storing patient features.
 */
@Entity
public class SearchRequestFeature implements Feature
{
    @Id
    @GeneratedValue
    private long hibernateId;

    @Basic
    private String id;

    @ManyToOne
    @JoinColumn(name="incomingsearchrequest_id")
    private IncomingSearchRequest incomingsearchrequest;

    /** 1 - true, -1 - false, 0 - NA */
    @Basic
    private int present = 0;

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public void setId(String newId) {
        this.id = newId;
    }

    public void setPresent(int isPresent)
    {
        present = isPresent;
    }
}
