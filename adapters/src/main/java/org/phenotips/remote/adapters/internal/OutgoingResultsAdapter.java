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
package org.phenotips.remote.adapters.internal;

import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.Patient;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Feature and disorder adapter.
 */
public class OutgoingResultsAdapter
{
    private Patient patient;

    private Boolean setPatientCalled = false;

    public void setPatient(Patient patientObject)
    {
        patient = patientObject;
        setPatientCalled = true;
    }

    public JSONObject patientJSON() throws Exception
    {
        /*
            The way this is implemented is that if when looking up a piece of data in the extraData of the patient,
            that piece is not found, this function will throw an exception.
            FIXME In the API there should be an interface class that would specify exactly how to hold this data.
            FIXME This way the API will be more reusable
        */
        if (!setPatientCalled) {
            //Fixme exception not specific enough
            throw new Exception("The patient object was never set");
        }

        JSONObject json = new JSONObject();
        JSONArray disorders = new JSONArray();
        JSONArray features = new JSONArray();

        //FIXME could still break
        for (Disorder disease : patient.getDisorders()) {
            JSONObject diseaseJson = disease.toJSON();
            String id = diseaseJson.getString("id");
            if (id == null) {
                id = diseaseJson.getString("queryId");
            }
            disorders.add(id);
        }
        for (Feature phenotype : patient.getFeatures()) {
            JSONObject phenotypeJson = phenotype.toJSON();
            JSONObject featureJson = new JSONObject();

            Object id = phenotypeJson.get("id");
            if (id == null) {
                id = phenotypeJson.get("queryId");
            }
            String idString = id.toString();
            featureJson.put("id", id);
            featureJson.put("observed", negativePositiveToYesNo(phenotypeJson.getString("queryType")));
            features.add(featureJson);
        }

        json.put("disorders", disorders);
        json.put("features", features);

        return json;
    }

    private static String negativePositiveToYesNo(String text)
    {
        if (StringUtils.equalsIgnoreCase(text, "negative_phenotype")) {
            return "no";
        } else if (StringUtils.equalsIgnoreCase(text, "phenotype")) {
            return "yes";
        } else {
            return "unknown";
        }
    }
}
