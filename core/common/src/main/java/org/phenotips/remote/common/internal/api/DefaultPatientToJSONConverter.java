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
package org.phenotips.remote.common.internal.api;

import org.phenotips.remote.api.tojson.PatientToJSONConverter;
import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.ontology.internal.solr.SolrOntologyTerm;
import org.phenotips.data.similarity.internal.PatientGenotype;
import org.phenotips.remote.api.ApiConfiguration;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DefaultPatientToJSONConverter implements PatientToJSONConverter
{
    private final static String PATIENTMATCHING_JSON_FEATUREMATCHES = "featureMatches";
    private final static String PATIENTMATCHING_JSON_CATEGORY       = "category";
    private final static String PATIENTMATCHING_JSON_CATEGORY_ID    = "id";
    private final static String PATIENTMATCHING_JSON_MATCH          = "match";

    //private final static String ERROR_MESSAGE_UNSUPPORTED_JSON_FORMAT = "Unsupported JSON representation of matched features";

    private Logger logger;

    private final String apiVersion;

    public DefaultPatientToJSONConverter(String apiVersion, Logger logger)
    {
        this.apiVersion = apiVersion;
        this.logger     = logger;
    }

    public JSONObject convert(Patient patient, boolean removePrivateData)
    {
        return this.convert(patient, removePrivateData, 0);
    }

    public JSONObject convert(Patient patient, boolean removePrivateData, int includedTopGenes)
    {
        JSONObject json = new JSONObject();

        try {
            json.put(ApiConfiguration.JSON_GENDER, DefaultPatientToJSONConverter.gender(patient));
            json.putAll(DefaultPatientToJSONConverter.globalQualifiers(patient));
        } catch (Exception ex) {
            // Do nothing. These are optional.
        }
        JSONArray disorders = DefaultPatientToJSONConverter.disorders(patient);
        if (!disorders.isEmpty()) {
            json.put(ApiConfiguration.JSON_DISORDERS, disorders);
        }
        if (removePrivateData) {
            json.put(ApiConfiguration.JSON_FEATURES, DefaultPatientToJSONConverter.nonPersonalFeatures(patient));
        } else {
            json.put(ApiConfiguration.JSON_FEATURES, DefaultPatientToJSONConverter.features(patient));
        }
        JSONArray genes = DefaultPatientToJSONConverter.genes(patient, includedTopGenes, logger);
        if (!genes.isEmpty()) {
            json.put(ApiConfiguration.JSON_GENES, genes);
        }
        return json;
    }

    private static JSONArray features(Patient patient)
    {
        JSONArray features = new JSONArray();
        for (Feature patientFeature : patient.getFeatures()) {
            Map<String, ? extends FeatureMetadatum> metadata = patientFeature.getMetadata();
            FeatureMetadatum ageOfOnset = metadata.get(ApiConfiguration.FEATURE_AGE_OF_ONSET);

            JSONObject featureJson = new JSONObject();
            featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_ID,       patientFeature.getId());
            featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_OBSERVED, observedStatusToJSONString(patientFeature));

            if (ageOfOnset != null) {
                featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_AGE_OF_ONSET, ageOfOnset.getId());
            }
            features.add(featureJson);
        }
        return features;
    }

    private static String observedStatusToJSONString(Feature feature)
    {
        if (feature.isPresent()) {
            return ApiConfiguration.REPLY_JSON_FEATURE_OBSERVED_YES;
        }
        return ApiConfiguration.REPLY_JSON_FEATURE_OBSERVED_NO;
    }

    private static JSONArray nonPersonalFeatures(Patient patient)
    {
        /* Example of a expected reply which should be parsed for features:
         *
         * QUERY: "features": [ {"id": "HP:0000316", "observed": "yes"},
         *                      {"id": "HP:0004325", "observed": "yes"},
         *                      {"id": "HP:0001999", "observed": "yes"} ]
         *
         * REPLY:
         *  1) "matchabe" patient with the same set of symptoms {"HP:0000316", "HP:0004325", "HP:0001999"}
         *     and two other unmatched feature:
         *
         *   "features": [{"score":0.46,"category":{"id":"HP:0100886",...},"reference":["HP:0000316"],"match":[""]},
         *                {"score":0.44,"category":{"id":"HP:0004323",...},"reference":["HP:0004325"],"match":[""]},
         *                {"score":0.40,"category":{"id":"HP:0000271",...},"reference":["HP:0001999"],"match":[""]},
         *                {"score":0,"category":{"id":"","name":"Unmatched"},"match":["",""]}]
         *
         *  2) "public" patient with {"HP:0004325", "HP:0001999", "HP:0000479"} and two other unmatched features:
         *
         *   "features": [{"score":0.44,"category":{"id":"HP:0004325",...},"reference":["HP:0004325"],"match":["HP:0004325"]},
         *                {"score":0.40,"category":{"id":"HP:0001999",...},"reference":["HP:0001999"],"match":["HP:0001999"]},
         *                {"score":0.22,"category":{"id":"HP:0000478",...},"reference":["HP:0000316"],"match":["HP:0000479"]},
         *                {"score":0,"category":{"id":"","name":"Unmatched"},"match":["HP:0011276","HP:0000505"]}]
         *
         *  For now feature info returned by the patient-network component will be used, in order
         *  not to reinvent the "privacy" wheel. All non-matched features will be returned as
         *  HP:0000118 ("Phenotypic abnormality"), and (given how patient-netowrk component works)
         *  all non-observed features will be ignored.
         */

        Map<String, Integer> featureCounts      = new HashMap<String, Integer>();
        Set<String>          obfuscatedFeatures = new HashSet<String>();
        Set<String>          notMatchedFeatures = new HashSet<String>();

        JSONArray similarityFeaturesJson = patient.toJSON().getJSONArray(PATIENTMATCHING_JSON_FEATUREMATCHES);

        for (Object featureMatchUC : similarityFeaturesJson) {
            JSONObject featureMatch = (JSONObject) featureMatchUC;

            JSONObject featureCategory = featureMatch.optJSONObject(PATIENTMATCHING_JSON_CATEGORY);
            if (featureCategory == null) {
                //FIXME: throw new Exception(ERROR_MESSAGE_UNSUPPORTED_JSON_FORMAT);
                continue;
            }

            String catId = featureCategory.optString(PATIENTMATCHING_JSON_CATEGORY_ID, "");

            // an unmatched feature
            JSONArray featureMatches = featureMatch.optJSONArray(PATIENTMATCHING_JSON_MATCH);
            if (featureMatches == null) {
                // FIXME: need to throw to indicate unsuported format:throw new Exception(ERROR_MESSAGE_UNSUPPORTED_JSON_FORMAT);
                continue;
            }
            for (int i = 0; i < featureMatches.size(); i++)
            {
                String matchFeature = featureMatches.getString(i);

                // if feature id is obfuscated use category Id instead as the best available substitute
                String featureId = matchFeature.isEmpty() ? catId : matchFeature;

                // replace empty features by the most generic generic term,
                // and (possibly) re-format feature ID to the expected output format
                featureId = processFeatureID(featureId);

                if (catId.isEmpty()) {
                    notMatchedFeatures.add(featureId);
                }
                if (matchFeature.isEmpty()) {
                    obfuscatedFeatures.add(featureId);
                }

                Integer count = featureCounts.containsKey(featureId) ? featureCounts.get(featureId) : 0;
                featureCounts.put(featureId, count + 1);
            }
        }

        // convert featuresWithCounts to features
        // note: for now only observed features are supported, so "observed" is hardcoded to "yes" for now
        JSONArray features = new JSONArray();

        for (String featureId : featureCounts.keySet()) {

            JSONObject featureJson = new JSONObject();
            featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_ID,         featureId);
            featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_OBSERVED,   ApiConfiguration.REPLY_JSON_FEATURE_OBSERVED_YES);
            featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_MATCHED,    !notMatchedFeatures.contains(featureId));
            featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_OBFUSCATED, obfuscatedFeatures.contains(featureId));
            int count = featureCounts.get(featureId);
            if (count > 1) {
                featureJson.put(ApiConfiguration.REPLY_JSON_FEATURE_COUNT, count);
            }
            features.add(featureJson);
        }

        return features;
    }

    private static String processFeatureID(String featureIdFromMatching)
    {
        if (featureIdFromMatching.isEmpty()) {
            return ApiConfiguration.REPLY_JSON_FEATURE_HPO_MOST_GENERIC_TERM;
        }
        return featureIdFromMatching;
    }

    private static JSONArray disorders(Patient patient)
    {
        JSONArray disorders = new JSONArray();
        for (Disorder disease : patient.getDisorders()) {
            disorders.add(disease.getId());
        }
        return disorders;
    }

    private static JSONArray genes(Patient patient, int includedTopGenes, Logger logger)
    {
        PatientGenotype genotype = new PatientGenotype(patient);

        JSONArray genes = new JSONArray();
        try {
            Collection<String> candidateGeneNames;
            //Collection<String> candidateGeneNames = getPatientCandidateGeneNames(patient);
            if (includedTopGenes <= 0) {
                candidateGeneNames = genotype.getCandidateGenes();
            } else {
                final Map<String, Double> genesWithScore = new HashMap<String,Double>();
                Collection<String> allGenes = genotype.getGenes();
                for (String gene : allGenes) {
                    genesWithScore.put(gene, genotype.getGeneScore(gene));
                }
                Set<String> set = genesWithScore.keySet();
                List<String> keys = new ArrayList<String>(set);
                Collections.sort(keys, new Comparator<String>() {
                    public int compare(String s1, String s2) {
                        return Double.compare(genesWithScore.get(s1), genesWithScore.get(s2));
                    }
                });
                List<String> topGenes = keys.subList(0, includedTopGenes);
                candidateGeneNames = new HashSet<String>();
                for (String topGene : topGenes) {
                    candidateGeneNames.add(topGene);
                }
            }

            for (String geneName : candidateGeneNames) {
                JSONObject nextGene = new JSONObject();
                nextGene.put(ApiConfiguration.JSON_GENES_GENENAME, geneName);
                nextGene.put(ApiConfiguration.JSON_GENES_ASSEMBLY, "GRCh37"); // TODO: pull from candidate genes/patient/vcf?
                genes.add(nextGene);
            }
        } catch (Exception ex) {
            logger.error("Error getting candidate genes for patient [{}]: [{}]", patient.getId(), ex);
            return new JSONArray();
        }
        return genes;
    }

    /**
     * TODO: this is a rip from org.phenotips.data.similarity.internal.PatientGenotype
     *       that class was not intended to be used outside of its package, need
     *       to review and use PatientGenotype once it is updated
     *
     * Return a collection of the names of candidate genes listed for the given patient.
     * @return a (potentially-empty) unmodifiable collection of the names of candidate genes
     *
    private static Collection<String> getPatientCandidateGeneNames(Patient p)
    {
        PatientData<Map<String, String>> genesData = null;
        if (p != null) {
            genesData = p.getData("genes");
        }
        if (genesData != null) {
            Set<String> geneNames = new HashSet<String>();
            Iterator<Map<String, String>> iterator = genesData.iterator();
            while (iterator.hasNext()) {
                Map<String, String> geneInfo = iterator.next();
                String geneName = geneInfo.get("gene");
                if (geneName == null) {
                    continue;
                }
                geneName = geneName.trim();
                if (geneName.isEmpty()) {
                    continue;
                }
                geneNames.add(geneName);
            }
            return Collections.unmodifiableSet(geneNames);
        }
        return Collections.emptySet();
    }*/

    private static String gender(Patient patient)
    {
        return patient.<ImmutablePair<String, String>>getData("sex").get(0).getRight();
    }

    private static Map<String, String> globalQualifiers(Patient patient)
    {
        Map<String, String> globalQualifiers = new HashMap<String, String>();
        Map<String, String> remappedGlobalQualifierStrings = new HashMap<String, String>();
        remappedGlobalQualifierStrings.put("global_age_of_onset", "age_of_onset");
        remappedGlobalQualifierStrings.put("global_mode_of_inheritance", "mode_of_inheritance");

        // These are the actual qualifiers, that are remapped to have the keys compliant with the remote JSON standard.
        PatientData<ImmutablePair<String, SolrOntologyTerm>> existingQualifiers =
            patient.<ImmutablePair<String, SolrOntologyTerm>>getData("global-qualifiers");
        if (globalQualifiers != null) {
            for (ImmutablePair<String, SolrOntologyTerm> qualifierPair : existingQualifiers) {
                for (String key : remappedGlobalQualifierStrings.keySet()) {
                    // Could do contains, but is it safe?
                    if (StringUtils.equalsIgnoreCase(qualifierPair.getLeft(), key)) {
                        globalQualifiers.put(remappedGlobalQualifierStrings.get(key), qualifierPair.getRight().getId());
                        break;
                    }
                }
            }
        }
        return globalQualifiers;
    }
}
