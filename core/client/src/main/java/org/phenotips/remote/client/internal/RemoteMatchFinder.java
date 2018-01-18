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
package org.phenotips.remote.client.internal;

import org.phenotips.data.ConsentManager;
import org.phenotips.data.Patient;
import org.phenotips.matchingnotification.finder.MatchFinder;
import org.phenotips.matchingnotification.match.PatientMatch;
import org.phenotips.matchingnotification.match.internal.DefaultPatientMatch;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
@Component
@Named("remote")
@Singleton
public class RemoteMatchFinder implements MatchFinder, Initializable
{
    private static final EntityReference PHENOMECENTRAL_SPACE = new EntityReference("PhenomeCentral", EntityType.SPACE);

    private static final EntityReference MATCHING_RUN_INFO_CLASS = new EntityReference(
        "MatchingRunInfoClass", EntityType.DOCUMENT, PHENOMECENTRAL_SPACE);

    private static final EntityReference MATCHING_RUN_INFO_DOCUMENT =
        new EntityReference("MatchingRunInfo", EntityType.DOCUMENT, PHENOMECENTRAL_SPACE);

    private static final int ADD_TOP_N_GENES_PARAMETER = 0;

    private static final String REMOTE_MATCHING_CONSENT_ID = "matching";

    private static final String RUN_INFO_DOCUMENT_SERVERNAME = "serverName";

    private static final String RUN_INFO_DOCUMENT_STARTTIME = "startedTime";

    private static final String RUN_INFO_DOCUMENT_ENDTIME = "completedTime";

    private static final String RUN_INFO_DOCUMENT_PATIENTCOUNT = "numPatientsUsedForLastMatchRun";


    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<EntityReference> entityResolver;

    @Inject
    private RemoteConfigurationManager RemoteConfigurationManager;

    @Inject
    private RemoteMatchingService matchingService;

    @Inject
    private Logger logger;

    @Inject
    private ConsentManager consentManager;

    @Inject
    private Provider<XWikiContext> provider;

    private XWikiDocument runInfoDoc;

    private Integer numPatientsTestedForMatches;

    @Override
    public void initialize() throws InitializationException
    {
        this.runInfoDoc = this.getMatchingRunInfoDoc();
    }

    private XWikiDocument getMatchingRunInfoDoc()
    {
        try {
            XWikiContext context = this.provider.get();
            XWikiDocument doc = context.getWiki().getDocument(MATCHING_RUN_INFO_DOCUMENT, context);

            if (doc != null && !doc.isNew()) {
                return doc;
            } else {
                this.logger.error("Failed to retrieve existing matching run info document");
            }

        } catch (Exception e) {
            this.logger.error("Failed to get matching run info document: {}", e.getMessage(), e);
        }

        return null;
    }

    @Override
    public int getPriority()
    {
        return 100;
    }

    @Override
    public List<PatientMatch> findMatches(Patient patient)
    {
        List<PatientMatch> patientMatches = new LinkedList<>();

        // Checking if a patient has a consent for remote matching
        if (!this.consentManager.hasConsent(patient, REMOTE_MATCHING_CONSENT_ID)) {
            this.logger.debug("Skipping patient {}. No consent for remote matching", patient.getId());
            return patientMatches;
        }

        this.logger.debug("Finding remote matches for patient {}.", patient.getId());

        List<String> remoteIds = this.getRemotesList();
        for (String remoteId : remoteIds) {
            List<PatientMatch> currentMatches = this.sendAndProcessRequest(patient.getId(), remoteId);
            patientMatches.addAll(currentMatches);
        }

        this.numPatientsTestedForMatches++;

        return patientMatches;
    }

    private List<PatientMatch> sendAndProcessRequest(String patientId, String remoteId)
    {
        List<PatientMatch> patientMatchList = new ArrayList<>();

        this.logger.debug("Processing request for patientId {}, remoteId {}.", patientId, remoteId);
        OutgoingMatchRequest request =
            this.matchingService.sendRequest(patientId, remoteId, ADD_TOP_N_GENES_PARAMETER);

        if (!checkRequestValidity(request, patientId, remoteId)) {
            return patientMatchList;
        }

        List<RemotePatientSimilarityView> parsedResults = this.matchingService.getSimilarityResults(request);
        for (RemotePatientSimilarityView result : parsedResults) {
            PatientMatch match = new DefaultPatientMatch(result, null, remoteId);
            patientMatchList.add(match);
        }
        return patientMatchList;
    }

    private boolean checkRequestValidity(OutgoingMatchRequest request, String patientId, String remoteId)
    {
        if (request == null) {
            return false;
        }

        if (!request.wasSent()) {
            this.logger.error("Request for patientId {}, remoteId {} was not sent.", patientId, remoteId);
            return false;
        }

        if (!request.gotValidReply()) {
            this.logger.error("Request for patientId {}, remoteId {} returned with status code: {}",
                patientId, remoteId, request.getRequestStatusCode());
            this.logger.error("and error details {}.", request.getRequestJSON());
            return false;
        }

        return true;
    }

    private List<String> getRemotesList()
    {
        XWikiContext context = this.contextProvider.get();
        List<BaseObject> potentialRemotes = this.RemoteConfigurationManager.getListOfRemotes(context);

        List<String> remoteIdsList = new LinkedList<>();
        for (BaseObject remote : potentialRemotes) {
            if (remote == null) {
                continue;
            }

            String configuredServerId = remote.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_ID);
            if (StringUtils.isEmpty(configuredServerId)) {
                continue;
            }

            // Include only servers that are marked for remote search
            int searchMatches = remote.getIntValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_SEARCH_MATCHES);
            if (searchMatches != 1) {
                continue;
            }

            remoteIdsList.add(configuredServerId);
        }

        return remoteIdsList;
    }

    @Override
    public void recordStartMatchesSearch()
    {
        // note: error() is used intentionally since this is important information we always want to have in the logs
        this.logger.error("Starting find all MME matches run...");

        this.numPatientsTestedForMatches = 0;

        this.recordMatchSearchStatus(RUN_INFO_DOCUMENT_STARTTIME);
    }

    @Override
    public void recordEndMatchesSearch()
    {
        this.logger.error("Finished find all MME matches run");

        this.recordMatchSearchStatus(RUN_INFO_DOCUMENT_ENDTIME);
    }

    private void recordMatchSearchStatus(String timePropertyName)
    {
        if (this.runInfoDoc == null) {
            return;
        }

        try {
            XWikiContext context = this.provider.get();

            List<String> remoteIds = this.getRemotesList();
            for (String remoteId : remoteIds) {
                BaseObject object = this.runInfoDoc.getXObject(MATCHING_RUN_INFO_CLASS, RUN_INFO_DOCUMENT_SERVERNAME,
                        remoteId, false);
                if (object == null) {
                    object = this.runInfoDoc.newXObject(MATCHING_RUN_INFO_CLASS, context);
                    object.setStringValue(RUN_INFO_DOCUMENT_SERVERNAME, remoteId);
                }
                object.setIntValue(RUN_INFO_DOCUMENT_PATIENTCOUNT, this.numPatientsTestedForMatches);
                object.setDateValue(timePropertyName, new Date());
            }
            context.getWiki().saveDocument(this.runInfoDoc, context);
        } catch (Exception e) {
            this.logger.error("Failed to save remote matching run status {}.", timePropertyName, e);
        }
    }
}
