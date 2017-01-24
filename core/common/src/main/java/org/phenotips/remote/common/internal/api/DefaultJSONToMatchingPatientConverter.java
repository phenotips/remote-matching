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
import org.phenotips.data.Patient;
import org.phenotips.data.internal.DefaultContactInfo;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.MatchingPatientGene;
import org.phenotips.remote.api.fromjson.JSONToMatchingPatientConverter;
import org.phenotips.remote.common.internal.RemoteMatchingPatient;
import org.phenotips.remote.common.internal.RemotePatientDisorder;
import org.phenotips.remote.common.internal.RemotePatientFeature;
import org.phenotips.remote.common.internal.RemotePatientGene;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyTerm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

public class DefaultJSONToMatchingPatientConverter implements JSONToMatchingPatientConverter
{
    private Logger logger;

    private final String apiVersion;

    private final Pattern hpoTerm; // not static: may be different from api version to api version

    private final Pattern disorderTerm;

    private final Vocabulary ontologyService;

    public DefaultJSONToMatchingPatientConverter(String apiVersion, Logger logger, Vocabulary ontologyService)
    {
        this.apiVersion = apiVersion;
        this.logger = logger;
        this.ontologyService = ontologyService;

        this.hpoTerm = Pattern.compile("^HP:\\d+$");
        this.disorderTerm = Pattern.compile("^MIM:\\d+$"); // TODO: Orphanet:#####
    }

    @Override
    public Patient convert(JSONObject patientJSON)
    {
        try {
            if (patientJSON == null) {
                this.logger.error("No patient object is provided in the request");
                throw new ApiViolationException("No patient object is provided in the request");
            }

            String remotePatientId = patientJSON.optString(ApiConfiguration.JSON_PATIENT_ID, null);
            if (remotePatientId == null) {
                this.logger.error("Remote patient has no id: violates API requirements");
                throw new ApiViolationException("Remote patient has no id");
            }

            Set<Feature> features = this.convertFeatures(patientJSON);
            Set<Disorder> disorders = this.convertDisorders(patientJSON);
            Set<MatchingPatientGene> genes = this.convertGenes(patientJSON);

            if ((features == null || features.isEmpty()) &&
                (genes == null || genes.isEmpty())) {
                this.logger.error("There are no features and no genes: violates API requirements (patient JSON: [{}])",
                    patientJSON.toString());
                throw new ApiViolationException("There are no features and no genes");
            }

            ContactInfo contactInfo = this.parseContactInfo(patientJSON);
            String label = patientJSON.optString(ApiConfiguration.JSON_PATIENT_LABEL, null);

            RemoteMatchingPatient patient =
                new RemoteMatchingPatient(remotePatientId, label, features, disorders, genes, contactInfo);

            return patient;
        } catch (ApiViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            this.logger.error("Incoming matching JSON->Patient conversion error: [{}]", ex);
            return null;
        }
    }

    private Set<Feature> convertFeatures(JSONObject json)
    {
        try {
            if (json.has(ApiConfiguration.JSON_FEATURES)) {
                Set<Feature> featureSet = new HashSet<Feature>();
                JSONArray featuresJson = (JSONArray) json.get(ApiConfiguration.JSON_FEATURES);
                for (Object jsonFeatureUncast : featuresJson) {
                    JSONObject jsonFeature = (JSONObject) jsonFeatureUncast;
                    String id = jsonFeature.getString(ApiConfiguration.JSON_FEATURE_ID).toUpperCase();
                    // TODO: throw an error if a term is not a supported one (HPO).
                    // TODO: maybe report an error, to be reviewed once spec is updated
                    if (!this.hpoTerm.matcher(id).matches()) {
                        logger.error("Patient feature parser: ignoring unsupported term with ID [{}]", id);
                        continue;
                    }
                    String observed = jsonFeature.optString(ApiConfiguration.JSON_FEATURE_OBSERVED,
                        ApiConfiguration.JSON_FEATURE_OBSERVED_YES).toLowerCase();
                    if (!observed.equals(ApiConfiguration.JSON_FEATURE_OBSERVED_YES) &&
                        !observed.equals(ApiConfiguration.JSON_FEATURE_OBSERVED_NO)) {
                        logger.error("Patient feature parser: ignoring term with unsupported observed status [{}]",
                            observed);
                        continue;
                    }
                    Feature feature = new RemotePatientFeature(id, observed);
                    featureSet.add(feature);
                }
                return featureSet;
            }
        } catch (Exception ex) {
            // TODO: catch only json exceptions, and throw other type upwads if feature id is unsupported
            this.logger.error("Error converting features: [{}]", ex);
        }
        return null;
    }

    private Set<Disorder> convertDisorders(JSONObject json)
    {
        try {
            if (json.has(ApiConfiguration.JSON_DISORDERS)) {
                Set<Disorder> disorderSet = new HashSet<Disorder>();
                JSONArray disorderJson = (JSONArray) json.get(ApiConfiguration.JSON_DISORDERS);
                for (Object jsonDisorderUncast : disorderJson) {
                    JSONObject jsonDisorder = (JSONObject) jsonDisorderUncast;
                    String id = jsonDisorder.getString(ApiConfiguration.JSON_DISORDER_ID).toUpperCase();
                    if (!this.disorderTerm.matcher(id).matches()) {
                        logger.error("Patient feature parser: ignoring unsupported term with ID [{}]", id);
                        continue;
                    }
                    Disorder disorder = new RemotePatientDisorder(id, null);
                    disorderSet.add(disorder);
                }
                return disorderSet;
            }
        } catch (Exception ex) {
            this.logger.error("Error converting disorders: {}", ex);
        }
        return null;
    }

    private Set<MatchingPatientGene> convertGenes(JSONObject json)
    {
        try {
            if (json.has(ApiConfiguration.JSON_GENES)) {
                Set<MatchingPatientGene> geneSet = new HashSet<MatchingPatientGene>();
                JSONArray genesJson = (JSONArray) json.get(ApiConfiguration.JSON_GENES);

                for (Object jsonGeneUncast : genesJson) {
                    JSONObject jsonGenomicFeature = (JSONObject) jsonGeneUncast;
                    JSONObject jsonGeneId = jsonGenomicFeature.optJSONObject(ApiConfiguration.JSON_GENES_GENE);
                    String geneName = (jsonGeneId != null)
                        ? jsonGeneId.optString(ApiConfiguration.JSON_GENES_GENE_ID).toUpperCase() : "";
                    if (geneName.length() == 0) {
                        logger.error("Patient genomic features parser: gene has no id");
                        throw new ApiViolationException("A gene has no id");
                    }
                    // TODO: check if name is a valid gene symbol or ensembl id
                    VocabularyTerm geneTerm = this.ontologyService.getTerm(geneName);
                    if (geneTerm == null) {
                        logger.error("Patient genomic features parser: gene id [{}] was not found in the vocabulary",
                            geneName);
                        throw new ApiViolationException("A gene has unsupported id [" + geneName + "]");
                    }
                    String symbol;
                    try {
                        symbol = (String) (geneTerm.get("symbol"));
                        if (!geneName.equals(symbol)) {
                            this.logger.debug("Converted incoming gene id [{}] to symbol [{}]", geneName, symbol);
                        }
                    } catch (Exception ex) {
                        logger.error("Patient genomic features parser: can not obtain gene symbol for gene ID [{}]",
                            geneName);
                        throw new ApiViolationException("Internal error processing gene id [" + geneName + "]");
                    }
                    MatchingPatientGene gene = new RemotePatientGene(symbol);
                    geneSet.add(gene);
                    // TODO: variants
                }
                return geneSet;
            }
        } catch (ApiViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            this.logger.error("Error converting genes: {}", ex);
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

        DefaultContactInfo contactInfo = new DefaultContactInfo();
        contactInfo.setName(name);
        contactInfo.setUrl(href);
        contactInfo.setInstitution(institution);

        if (href.startsWith("mailto:")) {
            URL emailUrl;
            try {
                emailUrl = new URL(href);
                String emails = emailUrl.getPath();
                if (StringUtils.isNotBlank(emails) && !emails.contains(",")) {
                    // Only one email in mailto
                    contactInfo.setEmails(Arrays.asList(emails));
                }
            } catch (MalformedURLException e) {
                this.logger.warn("Invalid mailto URL: " + href);
            }
        }

        return contactInfo;
    }
}
