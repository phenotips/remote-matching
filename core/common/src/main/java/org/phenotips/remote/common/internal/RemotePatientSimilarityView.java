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

import java.util.Iterator;
import java.util.Map.Entry;

import org.phenotips.data.ContactInfo;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.similarity.AccessType;
import org.phenotips.data.similarity.internal.DefaultPatientSimilarityView;
import org.json.JSONObject;

public class RemotePatientSimilarityView extends DefaultPatientSimilarityView
{
    /** Score as reposted by the remote server. */
    private Double remoteScore;

    public RemotePatientSimilarityView(Patient match, Patient reference, AccessType access, Double remoteScore)
        throws IllegalArgumentException
    {
        super(match, reference, access);

        this.remoteScore = remoteScore;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject result = super.toJSON();
        result.put("remoteScore", this.remoteScore);
        return result;
    }
}
