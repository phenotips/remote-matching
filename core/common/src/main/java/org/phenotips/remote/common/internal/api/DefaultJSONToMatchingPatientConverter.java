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

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.data.ContactInfo;
import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.Patient;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.MatchingPatientGene;
import org.phenotips.remote.api.fromjson.JSONToMatchingPatientConverter;
import org.phenotips.remote.common.internal.RemoteMatchingPatient;
import org.phenotips.remote.common.internal.RemotePatientDisorder;
import org.phenotips.remote.common.internal.RemotePatientFeature;
import org.phenotips.remote.common.internal.RemotePatientGene;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyManager;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJSONToMatchingPatientConverter implements JSONToMatchingPatientConverter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJSONToMatchingPatientConverter.class);

    // note: different API versions may have different patterns, may need to add support
    // for multiple patterns when we support multiple API versions

    private static final Pattern HPO_TERM_PATTERN = Pattern.compile("^HP:\\d+$");

    private static final Pattern MIM_DISORDER_TERM_PATTERN = Pattern.compile("^MIM:\\d+$");

    private static final Pattern ORPHANET_DISORDER_TERM_PATTERN = Pattern.compile("^Orphanet:\\d+$");

    private static final Vocabulary MIM_VOCABULARY;

    private static final Vocabulary HPO_VOCABULARY;

    private static final Vocabulary HGNC_VOCABULARY;

    private static final Vocabulary ORDO_VOCABULARY;

    /**
     * Parsing some patients generates a huge amount of errors, which may spam the server log.
     * So this implementation supports grouping all errors together and displaying them on request,
     * with the goal of displaying only one error message per match response (not one per patient).
     * This is not a very clean solution, but otherwise server log is way too busy with these.
     */
    private boolean groupMinorErrors;

    private Set<String> groupedUnsupportedFeatures = new HashSet<String>();

    private Set<String> groupedUnsupportedDisorders = new HashSet<String>();

    static {
        Vocabulary mim = null;
        Vocabulary hpo = null;
        Vocabulary hgnc = null;
        Vocabulary ordo = null;
        try {
            ComponentManager ccm = ComponentManagerRegistry.getContextComponentManager();
            VocabularyManager vm = ccm.getInstance(VocabularyManager.class);
            mim = vm.getVocabulary("MIM");
            hpo = vm.getVocabulary("HPO");
            hgnc = vm.getVocabulary("HGNC");
            ordo = vm.getVocabulary("ORDO");
        } catch (ComponentLookupException e) {
            LOGGER.error("Error loading static components: {}", e.getMessage(), e);
        }
        MIM_VOCABULARY = mim;
        HPO_VOCABULARY = hpo;
        HGNC_VOCABULARY = hgnc;
        ORDO_VOCABULARY = ordo;
    }

    public DefaultJSONToMatchingPatientConverter()
    {
        this(false);
    }

    public DefaultJSONToMatchingPatientConverter(boolean groupMinorErrors)
    {
        this.groupMinorErrors = groupMinorErrors;
    }

    public void logGroupedMinorErrors()
    {
        this.logUnsuportedFeatures(this.groupedUnsupportedFeatures);
        this.logUnsuportedDisorders(this.groupedUnsupportedDisorders);
        this.groupedUnsupportedFeatures.clear();
        this.groupedUnsupportedDisorders.clear();
    }

    public boolean hasLoggedMinorErrors()
    {
        return !this.groupedUnsupportedFeatures.isEmpty() || !this.groupedUnsupportedDisorders.isEmpty();
    }

    private void logUnsuportedFeatures(Set<String> unsupportedFeatureIdList)
    {
        if (unsupportedFeatureIdList.size() > 0) {
            LOGGER.error("Patient feature parser: received {} unsupported terms: [{}]",
                unsupportedFeatureIdList.size(), String.join(",", unsupportedFeatureIdList));
        }
    }

    private void logUnsuportedDisorders(Set<String> unsupportedDisorderIdList)
    {
        if (unsupportedDisorderIdList.size() > 0) {
            LOGGER.error("Patient disorders parser: ignored {} unsupported disorders: [{}]",
                unsupportedDisorderIdList.size(), String.join(",", unsupportedDisorderIdList));
        }
    }

    @Override
    public Patient convert(JSONObject patientJSON)
    {
        try {
            if (patientJSON == null) {
                LOGGER.error("No patient object is provided in the request");
                throw new ApiViolationException("No patient object is provided in the request");
            }

            String remotePatientId = patientJSON.optString(ApiConfiguration.JSON_PATIENT_ID, null);
            if (remotePatientId == null) {
                LOGGER.error("Remote patient has no id: violates API requirements");
                throw new ApiViolationException("Remote patient has no id");
            }

            Set<Feature> features = this.convertFeatures(patientJSON);
            Set<Disorder> disorders = this.convertDisorders(patientJSON);
            Set<MatchingPatientGene> genes = this.convertGenes(patientJSON);

            if ((features == null || features.isEmpty()) && (genes == null || genes.isEmpty())) {
                LOGGER.error("There are no features and no genes: violates API requirements (patient JSON: [{}])",
                    patientJSON.toString());
                throw new ApiViolationException("There are no features and no genes");
            }

            ContactInfo contactInfo = this.parseContactInfo(patientJSON);
            String label = patientJSON.optString(ApiConfiguration.JSON_PATIENT_LABEL, null);

            RemoteMatchingPatient patient = new RemoteMatchingPatient(remotePatientId, label, features, disorders,
                genes, contactInfo);

            return patient;
        } catch (ApiViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Incoming matching JSON->Patient conversion error: [{}]", ex);
            return null;
        }
    }

    private Set<Feature> convertFeatures(JSONObject json)
    {
        try {
            if (!json.has(ApiConfiguration.JSON_FEATURES)) {
                return null;
            }

            Set<String> ignoredTerms = new HashSet<>();
            Set<Feature> featureSet = new HashSet<>();
            JSONArray featuresJson = json.optJSONArray(ApiConfiguration.JSON_FEATURES);

            for (Object jsonFeatureUncast : featuresJson) {
                JSONObject jsonFeature = (JSONObject) jsonFeatureUncast;
                String id = jsonFeature.getString(ApiConfiguration.JSON_FEATURE_ID).toUpperCase();

                if (!HPO_TERM_PATTERN.matcher(id).matches()) {
                    ignoredTerms.add(id);
                    // save this term as a free text term using its label, if available
                    String label = jsonFeature.getString(ApiConfiguration.JSON_FEATURE_LABEL);
                    if (StringUtils.isNotBlank(label)) {
                        id = label;
                    }
                }

                // resolve the given feature identifier to an human phenotype ontology feature ID
                VocabularyTerm term = HPO_VOCABULARY.getTerm(id);
                id = (term != null) ? term.getId() : id;

                String observed = jsonFeature.optString(ApiConfiguration.JSON_FEATURE_OBSERVED,
                    ApiConfiguration.JSON_FEATURE_OBSERVED_YES).toLowerCase();
                if (!observed.equals(ApiConfiguration.JSON_FEATURE_OBSERVED_YES) &&
                    !observed.equals(ApiConfiguration.JSON_FEATURE_OBSERVED_NO)) {
                    LOGGER.error("Patient feature parser: ignoring term with unsupported observed status [{}]",
                        observed);
                    continue;
                }

                Feature feature = new RemotePatientFeature(id, observed);
                featureSet.add(feature);
            }

            // print all rejected terms together in order not to clutter the log
            // (since some remote servers send a LOT of unsupported terms)
            if (ignoredTerms.size() > 0) {
                if (this.groupMinorErrors) {
                    // group them even further, with the goal to group unsupported features over multiple patients
                    this.groupedUnsupportedFeatures.addAll(ignoredTerms);
                } else {
                    this.logUnsuportedFeatures(ignoredTerms);
                }
            }

            return featureSet;
        } catch (Exception ex) {
            LOGGER.error("Error converting features: [{}]", ex);
        }
        return null;
    }

    private Set<Disorder> convertDisorders(JSONObject json)
    {
        try {
            if (!json.has(ApiConfiguration.JSON_DISORDERS)) {
                return null;
            }

            Set<String> ignoredTerms = new HashSet<>();
            Set<Disorder> disorderSet = new HashSet<>();
            JSONArray disorderJson = json.optJSONArray(ApiConfiguration.JSON_DISORDERS);

            for (Object jsonDisorderUncast : disorderJson) {
                JSONObject jsonDisorder = (JSONObject) jsonDisorderUncast;
                String id = jsonDisorder.getString(ApiConfiguration.JSON_DISORDER_ID);
                String label = jsonDisorder.optString(ApiConfiguration.JSON_DISORDER_LABEL, null);

                if (MIM_DISORDER_TERM_PATTERN.matcher(id).matches()) {
                    // resolve the given disease identifier to a MIM ontology disease ID
                    VocabularyTerm term = MIM_VOCABULARY.getTerm(id);
                    // since MIM vocabulary terms ids are stored in solr without prefix
                    // prefix has to be manually added to every id
                    id = (term != null) ? "MIM:" + term.getId() : id;
                } else if (ORPHANET_DISORDER_TERM_PATTERN.matcher(id).matches()) {
                    id = id.replace(ApiConfiguration.JSON_DISORDER_ORPHANET_PREFIX, "ORDO:");
                    // resolve the given disease identifier to a ORDO ontology disease ID
                    VocabularyTerm term = ORDO_VOCABULARY.getTerm(id);
                    id = (term != null) ? term.getId() : id;
                } else {
                    ignoredTerms.add(id);
                    continue;
                }

                Disorder disorder = new RemotePatientDisorder(id, label);
                disorderSet.add(disorder);
            }

            // print all rejected disorders together in order not to clutter the log
            // (since some remote servers send a LOT of unsupported terms)
            if (ignoredTerms.size() > 0) {
                if (this.groupMinorErrors) {
                    // group them even further, with the goal to group unsupported features over multiple patients
                    this.groupedUnsupportedDisorders.addAll(ignoredTerms);
                } else {
                    this.logUnsuportedDisorders(ignoredTerms);
                }
            }

            return disorderSet;
        } catch (Exception ex) {
            LOGGER.error("Error converting disorders: {}", ex);
        }
        return null;
    }

    private Set<MatchingPatientGene> convertGenes(JSONObject json)
    {
        try {
            if (!json.has(ApiConfiguration.JSON_GENES)) {
                return null;
            }

            Set<MatchingPatientGene> geneSet = new HashSet<>();
            JSONArray genesJson = json.optJSONArray(ApiConfiguration.JSON_GENES);

            for (Object jsonGeneUncast : genesJson) {
                JSONObject jsonGenomicFeature = (JSONObject) jsonGeneUncast;

                JSONObject jsonGeneId = jsonGenomicFeature.optJSONObject(ApiConfiguration.JSON_GENES_GENE);
                String geneName = (jsonGeneId != null)
                    ? jsonGeneId.optString(ApiConfiguration.JSON_GENES_GENE_ID) : "";

                if (StringUtils.isBlank(geneName)) {
                    LOGGER.error("Patient genomic features parser: gene has no id in JSON [{}]",
                        jsonGenomicFeature.toString());
                    continue;
                }
                String geneId;
                try {
                    geneId = normalizeGeneId(geneName);
                    if (!geneName.equals(geneId)) {
                        LOGGER.debug("Converted incoming gene id [{}] to symbol [{}]", geneName, geneId);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Patient genomic features parser: can not obtain gene symbol for gene ID [{}]",
                        geneName);
                    continue;
                }
                MatchingPatientGene gene = new RemotePatientGene(geneId);
                geneSet.add(gene);
                // TODO: variants
            }

            return geneSet;
        } catch (Exception ex) {
            LOGGER.error("Error converting genes: {}", ex);
        }
        return null;
    }

    private ContactInfo parseContactInfo(JSONObject json)
    {
        JSONObject submitter = json.optJSONObject(ApiConfiguration.JSON_CONTACT);
        if (submitter == null || submitter.length() == 0) {
            return null;
        }

        String name = submitter.optString(ApiConfiguration.JSON_CONTACT_NAME, null);
        String href = submitter.optString(ApiConfiguration.JSON_CONTACT_HREF, null);
        String institution = submitter.optString(ApiConfiguration.JSON_CONTACT_INSTITUTION, null);

        ContactInfo.Builder contactInfo = new ContactInfo.Builder();
        contactInfo.withName(name);
        contactInfo.withUrl(href);
        contactInfo.withInstitution(institution);

        if (href != null && href.startsWith("mailto:")) {
            URL emailUrl;
            try {
                emailUrl = new URL(href);
                String emails = emailUrl.getPath();
                if (StringUtils.isNotBlank(emails) && !emails.contains(",")) {
                    // Only one email in mailto
                    contactInfo.withEmail(emails);
                }
            } catch (MalformedURLException e) {
                LOGGER.warn("Invalid mailto URL: " + href);
            }
        }

        return contactInfo.build();
    }

    /**
     * Normalize the given gene symbol or identifier to an Ensembl ID. If a corresponding Ensembl ID is not found, the
     * passed string is returned.
     *
     * @param geneId a gene symbol or identifier
     * @return the string representation of the corresponding resolved Ensembl ID, or {@code geneId} if the lookup failed.
     */
    private String normalizeGeneId(String geneId)
    {
        final VocabularyTerm term = HGNC_VOCABULARY.getTerm(geneId);
        if (term == null) {
            LOGGER.error("Patient genomic features parser: gene id [{}] was not found in the vocabulary", geneId);
        }
        @SuppressWarnings("unchecked")
        final List<String> ensemblIdList = term != null ? (List<String>) term.get("ensembl_gene_id") : null;
        final String ensemblId = ensemblIdList != null && !ensemblIdList.isEmpty() ? ensemblIdList.get(0) : null;
        // Retain information as is if we can't find Ensembl ID.
        return ensemblId != null ? ensemblId : geneId;
    }
}
