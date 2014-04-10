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
package org.phenotips.remote.adapters;

import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.ontology.internal.solr.SolrOntologyTerm;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * The preferred way for this class to exist was to have only static functions and have an interface. Due to Java's
 * limitations, it was not possible to have both. If the code is being modified, please keep all the functions static,
 * even though they are not explicitly declared so.
 */
public class PatientToJSONConverter
{
    public static JSONArray features(Patient patient)
    {
        JSONArray features = new JSONArray();
        for (Feature patientFeature : patient.getFeatures()) {
            Map<String, ? extends FeatureMetadatum> metadata = patientFeature.getMetadata();
            String ageOfOnset = metadata.get("ageOfOnset").getId();

            JSONObject featureJson = new JSONObject();
            featureJson.put("id", patientFeature.getId());
            featureJson.put("observed", booleanToYesNo(patientFeature.isPresent()));

            if (ageOfOnset != null) {
                featureJson.put("age_of_onset", ageOfOnset.toString());
            }
            features.add(featureJson);
        }
        return features;
    }

    public static JSONArray disorders(Patient patient)
    {
        JSONArray disorders = new JSONArray();
        for (Disorder disease : patient.getDisorders()) {
            disorders.add(disease.toJSON().get("id"));
        }
        return disorders;
    }

    public static String gender(Patient patient)
    {
        return patient.<ImmutablePair<String, String>>getData("sex").get(0).getRight();
    }

    public static Map<String, String> globalQualifiers(Patient patient)
    {
        Map<String, String> globalQualifiers = new HashMap<String, String>();
        Map<String, String> remappedGlobalQualifierStrings = new HashMap<String, String>();
        remappedGlobalQualifierStrings.put("global_age_of_onset", "age_of_onset");
        remappedGlobalQualifierStrings.put("global_mode_of_inheritance", "mode_of_inheritance");

        JSONObject json = new JSONObject();
        JSONArray disorders = new JSONArray();
        JSONArray features = new JSONArray();

        //These are the actual qualifiers, that are remapped to have the keys compliant with the remote JSON standard.
        PatientData<ImmutablePair<String, SolrOntologyTerm>> existingQualifiers =
            patient.<ImmutablePair<String, SolrOntologyTerm>>getData("global-qualifiers");
        if (globalQualifiers != null) {
            for (ImmutablePair<String, SolrOntologyTerm> qualifierPair : existingQualifiers) {
                for (String key : remappedGlobalQualifierStrings.keySet()) {
                    //Could do contains, but is it safe?
                    if (StringUtils.equalsIgnoreCase(qualifierPair.getLeft(), key)) {
                        globalQualifiers
                            .put(remappedGlobalQualifierStrings.get(key), qualifierPair.getRight().getId());
                        break;
                    }
                }
            }
        }
        return globalQualifiers;
    }

    private static int yesNoToInt(String text)
    {
        if (StringUtils.equalsIgnoreCase(text, "yes")) {
            return 1;
        } else if (StringUtils.equalsIgnoreCase(text, "no")) {
            return -1;
        } else {
            return 0;
        }
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

    private static String booleanToYesNo(Boolean bool)
    {
        if (bool) {
            return "yes";
        }
        return "no";
    }

    //FIXME. REMOVE.
    public static JSONArray features_OLD(Patient patient)
    {
        JSONArray features = new JSONArray();
        for (Feature phenotype : patient.getFeatures()) {
            JSONObject phenotypeJson = phenotype.toJSON();
            JSONObject featureJson = new JSONObject();
            featureJson.put("id", phenotypeJson.get("id"));
            featureJson.put("observed", phenotypeJson.getString("observed"));
            Object ageOfOnset = phenotypeJson.get("age_of_onset");
            if (ageOfOnset != null) {
                featureJson.put("ageOfOnset", ageOfOnset.toString());
            }
            features.add(featureJson);
        }
        return features;
    }
}
