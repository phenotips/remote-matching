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
package org.phenotips.remote.metrics.internal;

import org.phenotips.matchingnotification.storage.MatchStorageManager;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.MMEMetricsApiConfiguration;
import org.phenotips.remote.metrics.MetricsRequestProcessor;

import org.xwiki.component.annotation.Component;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

/**
 * Does all the MME metrics request processing functionality.
 */
@Component
@Singleton
public class IncomingMetricsRequestProcessor implements MetricsRequestProcessor
{
    private static final String OWNER_PROPERTY_NAME = "owner";

    private static final String CONSENT_PROPERTY_NAME = "granted";

    private static final String MATCHING_CONSENT_ID = "matching";

    private static final String GENE_STATUS_PROPERTY_NAME = "status";

    private static final String GENE_ID_PROPERTY_NAME = "gene";

    private static final String GENE_CANDIDATE = "candidate";

    private static final String GENE_SOLVED = "solved";

    private static final String OMIM_ID_PROPERTY_NAME = "omim_id";

    private static final String DIAGNOSIS_PROPERTY_NAME = "clinical_diagnosis";

    @Inject
    private MatchStorageManager matchStorageManager;

    @Inject
    private Logger logger;

    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Override
    public JSONObject generateMetricsResponse(ApiDataConverter apiVersionSpecificConverter)
    {
        this.logger.debug("Received MME metrics request");

        try {
            Session session = this.sessionFactory.getSessionFactory().openSession();

            JSONObject responseJSON = new JSONObject();

            JSONObject metricsJson = new JSONObject();
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_NUMBER_OF_CASES, getNumberOfCases(session));
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_NUMBER_OF_SUBMITTERS, getNumberOfSubmitters(session));
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_NUMBER_OF_GENES, getNumberOfGenes(session));
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_NUMBER_OF_UNIQUEGENES, getNumberOfUniqueGenes(session));
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_NUMBER_OF_CASES_WITH_DIAGNOSIS,
                getNumberOfCasesWithDiagnosis(session));
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_NUMBER_OF_POTENTIAL_MATCHES_SENT,
                this.matchStorageManager.getNumberOfRemoteMatches());
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_DATE_GENERATED, new Date().toString());

            responseJSON.put(MMEMetricsApiConfiguration.JSON_MME_METRICS, metricsJson);
            return responseJSON;
        } catch (Exception ex) {
            this.logger.error("CODE Error: {}", ex);
            return apiVersionSpecificConverter.generateInternalServerErrorResponse(null);
        }
    }

    /**
     * Returns number of patient records with the MME consent ON.
     */
    private Long getNumberOfCases(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct doc.name) from XWikiDocument as doc, "
                    + "BaseObject as obj, BaseObject consentObj, DBStringListProperty consentProp where "
                    + "obj.name = doc.fullName and obj.className = 'PhenoTips.PatientClass' and "
                    + "obj.name <> 'PhenoTips.PatientTemplate' and "
                    + "consentObj.name = doc.fullName and consentProp.id.id = consentObj.id and "
                    + "consentProp.id.name = :g and "
                    + "consentObj.className = 'PhenoTips.PatientConsent' and :m in elements(consentProp.list)");
            q.setString("g", CONSENT_PROPERTY_NAME);
            q.setString("m", MATCHING_CONSENT_ID);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of unique owners of patient records with the MME consent ON.
     */
    private Long getNumberOfSubmitters(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct ownerProp.value) from XWikiDocument as doc, "
                    + "BaseObject as patientObj, BaseObject ownerObj, StringProperty ownerProp, "
                    + "BaseObject consentObj, DBStringListProperty consentProp where "
                    + "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass' and "
                    + "patientObj.name <> 'PhenoTips.PatientTemplate' and "
                    + "ownerObj.name = doc.fullName and ownerProp.id.id = ownerObj.id and ownerProp.id.name = :o and "
                    + "ownerObj.className = 'PhenoTips.OwnerClass' and "
                    + "consentObj.name = doc.fullName and consentProp.id.id = consentObj.id and "
                    + "consentProp.id.name = :g and "
                    + "consentObj.className = 'PhenoTips.PatientConsent' and :m in elements(consentProp.list)");
            q.setString("o", OWNER_PROPERTY_NAME);
            q.setString("g", CONSENT_PROPERTY_NAME);
            q.setString("m", MATCHING_CONSENT_ID);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of gene objects with status solved or candidate from patient records with the MME consent ON.
     */
    private Long getNumberOfGenes(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (geneObj.name) from XWikiDocument as doc, "
                    + "BaseObject as patientObj, BaseObject geneObj, StringProperty geneProp, "
                    + "BaseObject consentObj, DBStringListProperty consentProp where "
                    + "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass' and "
                    + "patientObj.name <> 'PhenoTips.PatientTemplate' and "
                    + "geneObj.name = doc.fullName and geneProp.id.id = geneObj.id and geneProp.id.name = :s and "
                    + "(geneProp.value = :v1 or geneProp.value = :v2) and "
                    + "geneObj.className = 'PhenoTips.GeneClass' and "
                    + "consentObj.name = doc.fullName and consentProp.id.id = consentObj.id and "
                    + "consentProp.id.name = :c and "
                    + "consentObj.className = 'PhenoTips.PatientConsent' and :m in elements(consentProp.list)");
            q.setString("s", GENE_STATUS_PROPERTY_NAME);
            q.setString("v1", GENE_CANDIDATE);
            q.setString("v2", GENE_SOLVED);
            q.setString("c", CONSENT_PROPERTY_NAME);
            q.setString("m", MATCHING_CONSENT_ID);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of unique genes with status solved or candidate from patient records with the MME consent ON.
     */
    private Long getNumberOfUniqueGenes(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct geneIDProp.value) from XWikiDocument as doc, "
                    + "BaseObject as patientObj, BaseObject geneObj, StringProperty geneStatusProp, "
                    + "StringProperty geneIDProp, BaseObject consentObj, DBStringListProperty consentProp where "
                    + "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass' and "
                    + "patientObj.name <> 'PhenoTips.PatientTemplate' and "
                    + "geneIDProp.id.id = geneObj.id and geneIDProp.id.name = :g and "
                    + "geneObj.name = doc.fullName and geneStatusProp.id.id = geneObj.id and "
                    + "geneStatusProp.id.name = :s and "
                    + "(geneStatusProp.value = :v1 or geneStatusProp.value = :v2) and "
                    + "geneObj.className = 'PhenoTips.GeneClass' and "
                    + "consentObj.name = doc.fullName and consentProp.id.id = consentObj.id and "
                    + "consentProp.id.name = :c and "
                    + "consentObj.className = 'PhenoTips.PatientConsent' and :m in elements(consentProp.list)");
            q.setString("g", GENE_ID_PROPERTY_NAME);
            q.setString("s", GENE_STATUS_PROPERTY_NAME);
            q.setString("v1", GENE_CANDIDATE);
            q.setString("v2", GENE_SOLVED);
            q.setString("c", CONSENT_PROPERTY_NAME);
            q.setString("m", MATCHING_CONSENT_ID);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of unique genes with status solved or candidate from patient records with the MME consent ON.
     */
    private Long getNumberOfCasesWithDiagnosis(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct doc.name) from XWikiDocument as doc, "
                    + "BaseObject as patientObj, DBStringListProperty as omimProp, DBStringListProperty as ordoProp, "
                    + "BaseObject consentObj, DBStringListProperty consentProp where "
                    + "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass' and "
                    + "patientObj.name <> 'PhenoTips.PatientTemplate' and "
                    + "omimProp.id.id = patientObj.id and omimProp.id.name = :o and "
                    + "ordoProp.id.id = patientObj.id and ordoProp.id.name = :d and "
                    + "(omimProp.list.size > 0 or ordoProp.list.size > 0) and "
                    + "consentObj.name = doc.fullName and consentProp.id.id = consentObj.id and "
                    + "consentProp.id.name = :c and "
                    + "consentObj.className = 'PhenoTips.PatientConsent' and :m in elements(consentProp.list)");
            q.setString("o", OMIM_ID_PROPERTY_NAME);
            q.setString("d", DIAGNOSIS_PROPERTY_NAME);
            q.setString("c", CONSENT_PROPERTY_NAME);
            q.setString("m", MATCHING_CONSENT_ID);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }
}
