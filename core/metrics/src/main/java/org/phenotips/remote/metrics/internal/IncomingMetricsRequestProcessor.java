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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    /** A "from" part of a query to select not private patients with matching MME consent granted **/
    private static final String HQL_BASE_MME_PATIENT_FILTER_FROM =
        "XWikiDocument as doc, BaseObject as patientObj, BaseObject consentObj, "
            + "DBStringListProperty consentProp, BaseObject visibilityObj, StringProperty visibilityProp";

    /** A "where" part of a query to select not private patients with matching MME consent granted **/
    private static final String HQL_BASE_MME_PATIENT_FILTER_WHERE =
        "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass' and "
            + "patientObj.name <> 'PhenoTips.PatientTemplate' and "
            + "visibilityObj.name = doc.fullName and visibilityProp.id.id = visibilityObj.id and "
            + "consentObj.name = doc.fullName and consentProp.id.id = consentObj.id and "
            + "visibilityObj.className = 'PhenoTips.VisibilityClass' and "
            + "visibilityProp.id.name = 'visibility' and visibilityProp.value <> 'private' and "
            + "consentProp.id.name = '" + CONSENT_PROPERTY_NAME + "' and "
            + "consentObj.className = 'PhenoTips.PatientConsent' and '" + MATCHING_CONSENT_ID
            + "' in elements(consentProp.list)";

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
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            metricsJson.put(MMEMetricsApiConfiguration.JSON_MME_DATE_GENERATED, df.format(new Date()));

            responseJSON.put(MMEMetricsApiConfiguration.JSON_MME_METRICS, metricsJson);
            return responseJSON;
        } catch (Exception ex) {
            this.logger.error("CODE Error: {}", ex);
            return apiVersionSpecificConverter.generateInternalServerErrorResponse(null);
        }
    }

    /**
     * Returns number of not private patient records with the MME consent ON.
     */
    private Long getNumberOfCases(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct doc.name) from "
                    + HQL_BASE_MME_PATIENT_FILTER_FROM
                    + " where "
                    + HQL_BASE_MME_PATIENT_FILTER_WHERE);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of unique owners of not private patient records with the MME consent ON.
     */
    private Long getNumberOfSubmitters(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct ownerProp.value) from "
                    + HQL_BASE_MME_PATIENT_FILTER_FROM
                    + ", BaseObject ownerObj, StringProperty ownerProp"
                    + " where "
                    + HQL_BASE_MME_PATIENT_FILTER_WHERE
                    + " and ownerObj.name = doc.fullName and ownerProp.id.id = ownerObj.id and ownerProp.id.name = :o"
                    + " and ownerObj.className = 'PhenoTips.OwnerClass'");
            q.setString("o", OWNER_PROPERTY_NAME);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of gene objects with status solved or candidate from not private patient records with the MME consent ON.
     */
    private Long getNumberOfGenes(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (geneObj.name) from "
                    + HQL_BASE_MME_PATIENT_FILTER_FROM
                    + ", BaseObject geneObj, StringProperty geneProp"
                    + " where "
                    + HQL_BASE_MME_PATIENT_FILTER_WHERE
                    + " and geneObj.name = doc.fullName and geneProp.id.id = geneObj.id and geneProp.id.name = :s"
                    + " and (geneProp.value = :v1 or geneProp.value = :v2)"
                    + " and geneObj.className = 'PhenoTips.GeneClass'");
            q.setString("s", GENE_STATUS_PROPERTY_NAME);
            q.setString("v1", GENE_CANDIDATE);
            q.setString("v2", GENE_SOLVED);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of unique genes with status solved or candidate from not private patient records with the MME consent ON.
     */
    private Long getNumberOfUniqueGenes(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct geneIDProp.value) from "
                    + HQL_BASE_MME_PATIENT_FILTER_FROM
                    + ", BaseObject geneObj, StringProperty geneStatusProp, StringProperty geneIDProp"
                    + " where "
                    + HQL_BASE_MME_PATIENT_FILTER_WHERE
                    + " and geneIDProp.id.id = geneObj.id and geneIDProp.id.name = :g"
                    + " and geneObj.name = doc.fullName and geneStatusProp.id.id = geneObj.id"
                    + " and geneStatusProp.id.name = :s"
                    + " and (geneStatusProp.value = :v1 or geneStatusProp.value = :v2)"
                    + " and geneObj.className = 'PhenoTips.GeneClass'");
            q.setString("g", GENE_ID_PROPERTY_NAME);
            q.setString("s", GENE_STATUS_PROPERTY_NAME);
            q.setString("v1", GENE_CANDIDATE);
            q.setString("v2", GENE_SOLVED);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }

    /**
     * Returns number of unique genes with status solved or candidate from not private patient records with the MME consent ON.
     */
    private Long getNumberOfCasesWithDiagnosis(Session session)
    {
        try {
            Query q = session.createQuery(
                "select count (distinct doc.name) from "
                    + HQL_BASE_MME_PATIENT_FILTER_FROM
                    + ", DBStringListProperty as omimProp, DBStringListProperty as ordoProp"
                    + " where "
                    + HQL_BASE_MME_PATIENT_FILTER_WHERE
                    + " and omimProp.id.id = patientObj.id and omimProp.id.name = :o"
                    + " and ordoProp.id.id = patientObj.id and ordoProp.id.name = :d"
                    + " and (omimProp.list.size > 0 or ordoProp.list.size > 0)");
            q.setString("o", OMIM_ID_PROPERTY_NAME);
            q.setString("d", DIAGNOSIS_PROPERTY_NAME);
            return (Long) q.uniqueResult();
        } catch (Exception e) {
            this.logger.error("Error retrieving a list of patients for matching: {}", e);
        }

        return null;
    }
}
