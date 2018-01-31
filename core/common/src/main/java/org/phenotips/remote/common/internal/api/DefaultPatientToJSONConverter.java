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
package org.phenotips.remote.common.internal.api;

import org.phenotips.data.ContactInfo;
import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.similarity.PatientGenotype;
import org.phenotips.data.similarity.genotype.DefaultPatientGenotype;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;
import org.phenotips.remote.common.ApplicationConfiguration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 * Patient to MME JSON representation converter.
 *
 * See https://github.com/ga4gh/mme-apis/blob/master/search-api.md
 */
public class DefaultPatientToJSONConverter implements PatientToJSONConverter
{
    private Logger logger;

    private final Pattern hpoTerm; // not static: may be different from api version to api version

    public DefaultPatientToJSONConverter(String apiVersion, Logger logger)
    {
        this.logger = logger;

        this.hpoTerm = Pattern.compile("^HP:\\d+$");
    }

    @Override
    public JSONObject convert(Patient patient)
    {
        return this.convert(patient, 0);
    }

    @Override
    public JSONObject convert(Patient patient, int includedTopGenes)
    {
        JSONObject json = new JSONObject();

        json.put(ApiConfiguration.JSON_PATIENT_ID, patient.getId());

        // TODO
        json.put(ApiConfiguration.JSON_CONTACT, DefaultPatientToJSONConverter.contact(patient));
        try {
            json.put(ApiConfiguration.JSON_PATIENT_GENDER, DefaultPatientToJSONConverter.gender(patient));
            // TODO
            // json.putAll(DefaultPatientToJSONConverter.globalQualifiers(patient));
        } catch (Exception ex) {
            // Do nothing. These are optional.
        }

        JSONArray disorders = DefaultPatientToJSONConverter.disorders(patient);
        if (disorders.length() > 0) {
            json.put(ApiConfiguration.JSON_DISORDERS, disorders);
        }

        JSONArray clinicalDisorders = DefaultPatientToJSONConverter.clinicalDisorders(patient);
        if (disorders.length() > 0) {
            json.put(ApiConfiguration.JSON_DIAGNOSIS, clinicalDisorders);
        }

        json.put(ApiConfiguration.JSON_FEATURES, this.features(patient));

        JSONArray genes = DefaultPatientToJSONConverter.genes(patient, includedTopGenes, this.logger);
        if (genes.length() > 0) {
            json.put(ApiConfiguration.JSON_GENES, genes);
        }

        return json;
    }

    private static JSONObject contact(Patient patient)
    {
        // Default contact info
        String name = "PhenomeCentral Support";
        String institution = "PhenomeCentral";
        String href = "mailto:";

        PatientData<ContactInfo> data = patient.getData("contact");
        if (data != null && data.isIndexed() && data.size() > 0) {
            ContactInfo contact = data.get(0);
            String contactName = contact.getName();
            if (!StringUtils.isBlank(contactName)) {
                name = contactName;
            }
            // Replace institution, even if blank
            institution = contact.getInstitution();
            // TODO: replace this with a URL to a match/contact page
            List<String> email = contact.getEmails();
            if (!email.isEmpty() && !StringUtils.isBlank(email.get(0))) {
                href += email.get(0) + ",matchmaker@phenomecentral.org";
            } else {
                href += "matchmaker@phenomecentral.org";
            }
        }

        JSONObject contactJson = new JSONObject();
        contactJson.put(ApiConfiguration.JSON_CONTACT_NAME, name);
        // Institution is optional, so only include if non-blank
        if (!StringUtils.isBlank(institution)) {
            contactJson.put(ApiConfiguration.JSON_CONTACT_INSTITUTION, institution);
        }
        contactJson.put(ApiConfiguration.JSON_CONTACT_HREF, href);
        return contactJson;
    }

    private JSONArray features(Patient patient)
    {
        JSONArray features = new JSONArray();
        for (Feature patientFeature : patient.getFeatures()) {
            String featureId = patientFeature.getId();
            if (featureId.isEmpty() || !this.hpoTerm.matcher(featureId).matches()) {
                this.logger.error("Patient feature parser: ignoring term with non-HPO id [{}]", featureId);
                continue;
            }
            JSONObject featureJson = new JSONObject();
            featureJson.put(ApiConfiguration.JSON_FEATURE_ID, featureId);
            featureJson.put(ApiConfiguration.JSON_FEATURE_OBSERVED, observedStatusToJSONString(patientFeature));

            Map<String, ? extends FeatureMetadatum> metadata = patientFeature.getMetadata();
            FeatureMetadatum ageOfOnset = metadata.get(ApplicationConfiguration.FEATURE_METADATA_AGEOFONSET);
            if (ageOfOnset != null) {
                featureJson.put(ApiConfiguration.JSON_FEATURE_AGE_OF_ONSET, ageOfOnset.getId());
            }
            features.put(featureJson);
        }
        return features;
    }

    private static String observedStatusToJSONString(Feature feature)
    {
        if (feature.isPresent()) {
            return ApiConfiguration.JSON_FEATURE_OBSERVED_YES;
        }
        return ApiConfiguration.JSON_FEATURE_OBSERVED_NO;
    }

    private static JSONArray disorders(Patient patient)
    {
        JSONArray disorders = new JSONArray();
        for (Disorder disease : patient.getDisorders()) {
            if (!StringUtils.isBlank(disease.getId())) {
                JSONObject disorderJson = new JSONObject();
                disorderJson.put(ApiConfiguration.JSON_DISORDER_ID, disease.getId());
                disorders.put(disorderJson);
            }
        }
        return disorders;
    }

    private static JSONArray clinicalDisorders(Patient patient)
    {
        JSONArray disorders = new JSONArray();
        PatientData<Disorder> data = patient.getData("clinical-diagnosis");
        if (data != null) {
            Iterator<Disorder> iterator = data.iterator();
            while (iterator.hasNext()) {
              Disorder disorder = iterator.next();
              if (!StringUtils.isBlank(disorder.getId())) {
                  JSONObject disorderJson = new JSONObject();
                  disorderJson.put(ApiConfiguration.JSON_DISORDER_ID, disorder.getId());
                  disorders.put(disorderJson);
              }
            }
        }
        return disorders;
    }

    private static JSONArray genes(Patient patient, int includedTopGenes, Logger logger)
    {
        // logger.error("[request] Getting candidate genes for patient [{}]", patient.getId());

        JSONArray genes = new JSONArray();
        try {
            PatientGenotype genotype = new DefaultPatientGenotype(patient);

            Set<String> candidateGeneNames = new HashSet<>(genotype.getCandidateGenes());

            if (includedTopGenes > 0 && candidateGeneNames.size() < includedTopGenes) {
                // keep adding top exomiser genes until we have more than we want. Some of those may
                // overlap with explicit candidate genes, Set will take care of that, but we don't
                // know if the size of the collection grew or not without checking
                List<String> topGenes = genotype.getTopGenes(includedTopGenes);
                for (String topGene : topGenes) {
                    candidateGeneNames.add(topGene);
                    if (candidateGeneNames.size() >= includedTopGenes) {
                        break;
                    }
                }
            }

            for (String geneName : candidateGeneNames) {
                JSONObject gene = new JSONObject();
                gene.put(ApiConfiguration.JSON_GENES_GENE_ID, geneName);

                JSONObject nextGenomicFeature = new JSONObject();
                nextGenomicFeature.put(ApiConfiguration.JSON_GENES_GENE, gene);

                genes.put(nextGenomicFeature);
            }
        } catch (Exception ex) {
            logger.error("Error getting genes for patient [{}]: [{}]", patient.getId(), ex);
            return new JSONArray();
        }
        return genes;
    }

    private static String gender(Patient patient)
    {
        String rawSex = patient.<ImmutablePair<String, String>>getData("sex").get(0).getRight();
        if (rawSex.toUpperCase().equals("M")) {
            return ApiConfiguration.JSON_PATIENT_GENDER_MALE;
        }
        if (rawSex.toUpperCase().equals("F")) {
            return ApiConfiguration.JSON_PATIENT_GENDER_FEMALE;
        }
        return ApiConfiguration.JSON_PATIENT_GENDER_OTHER;
    }

//    private static Map<String, String> globalQualifiers(Patient patient)
//    {
//        Map<String, String> globalQualifiers = new HashMap<String, String>();
//        Map<String, String> remappedGlobalQualifierStrings = new HashMap<String, String>();
//        remappedGlobalQualifierStrings.put("global_age_of_onset", "age_of_onset");
//        remappedGlobalQualifierStrings.put("global_mode_of_inheritance", "mode_of_inheritance");
//        // These are the actual qualifiers, that are remapped to have the keys compliant with the remote JSON standard.
//        PatientData<ImmutablePair<String, SolrOntologyTerm>> existingQualifiers =
//            patient.<ImmutablePair<String, SolrOntologyTerm>>getData("global-qualifiers");
//        if (globalQualifiers != null) {
//            for (ImmutablePair<String, SolrOntologyTerm> qualifierPair : existingQualifiers) {
//                for (String key : remappedGlobalQualifierStrings.keySet()) { // Could do contains, but is it safe?
//                    if (StringUtils.equalsIgnoreCase(qualifierPair.getLeft(), key)) {
//                        globalQualifiers.put(remappedGlobalQualifierStrings.get(key), qualifierPair.getRight().getId());
//                        break;
//                    }
//                }
//            }
//        }
//        return globalQualifiers;
//    }

}
