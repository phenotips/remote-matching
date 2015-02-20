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

import org.phenotips.remote.api.fromjson.JSONToMatchingPatientConverter;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.MatchingPatientDisorder;
import org.phenotips.remote.api.MatchingPatientFeature;
import org.phenotips.remote.api.MatchingPatientGene;
import org.phenotips.remote.hibernate.internal.HibernatePatient;
import org.phenotips.remote.hibernate.internal.HibernatePatientDisorder;
import org.phenotips.remote.hibernate.internal.HibernatePatientFeature;
import org.phenotips.remote.hibernate.internal.HibernatePatientGene;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DefaultJSONToMatchingPatientConverter implements JSONToMatchingPatientConverter
{
    private Logger logger;

    private final String apiVersion;

    private final Pattern hpoTerm; // not static: may be different from api version to api version
    private final Pattern disorderTerm;

    public DefaultJSONToMatchingPatientConverter(String apiVersion, Logger logger)
    {
        this.apiVersion = apiVersion;
        this.logger     = logger;

        this.hpoTerm      = Pattern.compile("^HP:\\d+$");
        this.disorderTerm = Pattern.compile("^MIM:\\d+$");  // TODO: Orphanet:#####
    }

    @Override
    public MatchingPatient convert(JSONObject json)
    {
        try {
            JSONObject patientJSON = json.optJSONObject(ApiConfiguration.JSON_PATIENT);
            if (patientJSON == null) {
                this.logger.error("No patient object is provided in the request");
                throw new ApiViolationException("No patient object is provided in the request");
            }

            String remotePatientId = patientJSON.optString(ApiConfiguration.JSON_PATIENT_ID, null);
            if (remotePatientId == null) {
                this.logger.error("Remote patient has no id: violates API requirements");
                throw new ApiViolationException("Remote patient has no id");
            }

            String label = patientJSON.optString(ApiConfiguration.JSON_PATIENT_LABEL, null);

            MatchingPatient patient = new HibernatePatient(remotePatientId, label);

            Set<MatchingPatientFeature> features   = this.convertFeatures(patientJSON);
            Set<MatchingPatientDisorder> disorders = this.convertDisorders(patientJSON);
            Set<MatchingPatientGene> genes         = this.convertGenes(patientJSON);

            if ((features == null || features.isEmpty()) &&
                (genes == null || genes.isEmpty())) {
                this.logger.error("There are no features and no genes: violates API requirements");
                throw new ApiViolationException("There are no features and no genes");
            }
            if (features != null) {
                patient.addFeatures(features);
            }
            if (disorders != null) {
                patient.addDisorders(disorders);
            }
            if (genes != null) {
                patient.addGenes(genes);
            }
            return patient;
        } catch (ApiViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            this.logger.error("Incoming matching JSON->Patient conversion error: [{}]", ex);
            return null;
        }
    }

    private Set<MatchingPatientFeature> convertFeatures(JSONObject json)
    {
        try {
            if (json.has(ApiConfiguration.JSON_FEATURES)) {
                Set<MatchingPatientFeature> featureSet = new HashSet<MatchingPatientFeature>();
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
                    MatchingPatientFeature feature = new HibernatePatientFeature();
                    feature.setId(id);
                    feature.setObserved(observed);
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

    private Set<MatchingPatientDisorder> convertDisorders(JSONObject json)
    {
        try {
            if (json.has(ApiConfiguration.JSON_DISORDERS)) {
                Set<MatchingPatientDisorder> disorderSet = new HashSet<MatchingPatientDisorder>();
                JSONArray disorderJson = (JSONArray) json.get(ApiConfiguration.JSON_DISORDERS);
                for (Object jsonDisorderUncast : disorderJson) {
                    JSONObject jsonDisorder = (JSONObject) jsonDisorderUncast;
                    String id = jsonDisorder.getString(ApiConfiguration.JSON_DISORDER_ID).toUpperCase();
                    if (!this.disorderTerm.matcher(id).matches()) {
                        logger.error("Patient feature parser: ignoring unsupported term with ID [{}]", id);
                        continue;
                    }
                    MatchingPatientDisorder disorder = new HibernatePatientDisorder();
                    disorder.setId(id);
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
                    String geneName = (jsonGeneId != null) ?
                                      jsonGeneId.optString(ApiConfiguration.JSON_GENES_GENE_ID).toUpperCase() : "";
                    if (geneName.length() == 0) {
                        logger.error("Patient genomic features parser: gene has no id");
                        continue;
                    }
                    // TODO: check if name is a valid gene symbol or ensembl id
                    MatchingPatientGene gene = new HibernatePatientGene();
                    gene.setName(geneName);
                    geneSet.add(gene);
                    // TODO: variants
                }
                return geneSet;
            }
        } catch (Exception ex) {
            this.logger.error("Error converting genes: {}", ex);
        }
        return null;
    }
}
