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
import org.phenotips.matchingnotification.finder.internal.AbstractMatchFinder;
import org.phenotips.matchingnotification.match.PatientMatch;
import org.phenotips.matchingnotification.match.internal.DefaultPatientMatch;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
@Component
@Named("remote")
@Singleton
public class RemoteMatchFinder extends AbstractMatchFinder implements MatchFinder
{
    private static final int ADD_TOP_N_GENES_PARAMETER = 0;

    private static final String REMOTE_MATCHING_CONSENT_ID = "matching";

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
    private ConsentManager consentManager;

    @Override
    public int getPriority()
    {
        return 100;
    }

    @Override
    protected Set<String> getSupportedServerIdList()
    {
        return this.getRemotesList();
    }

    @Override
    protected MatchRunStatus specificFindMatches(Patient patient, String remoteId, List<PatientMatch> matchesList)
    {
        // Checking if a patient has a consent for remote matching
        if (!this.consentManager.hasConsent(patient, REMOTE_MATCHING_CONSENT_ID)) {
            this.logger.debug("Skipping patient {}. No consent for remote matching", patient.getId());
            return MatchRunStatus.NOT_RUN;
        }

        this.logger.debug("Finding remote matches for patient [{}] on server [{}]", patient.getId(), remoteId);

        OutgoingMatchRequest request =
            this.matchingService.sendRequest(patient.getId(), remoteId, ADD_TOP_N_GENES_PARAMETER);

        MatchRunStatus status = checkRequestValidity(request, patient.getId(), remoteId);
        if (status != MatchRunStatus.OK) {
            return status;
        }

        List<RemotePatientSimilarityView> parsedResults = this.matchingService.getSimilarityResults(request);
        for (RemotePatientSimilarityView result : parsedResults) {
            PatientMatch match = new DefaultPatientMatch(result, null, remoteId);
            matchesList.add(match);
        }
        return MatchRunStatus.OK;
    }

    private MatchRunStatus checkRequestValidity(OutgoingMatchRequest request, String patientId, String remoteId)
    {
        if (request != null && request.errorContactingRemoteServer()) {
            this.logger.error("Unable to connect to remote server [{}] to send a request for patient [{}]",
                    remoteId, patientId);
            return MatchRunStatus.ERROR;
        }

        if (request == null || !request.wasSent()) {
            this.logger.error("Request for patientId [{}] was not sent to server [{}]", patientId, remoteId);
            return MatchRunStatus.NOT_RUN;
        }

        if (!request.gotValidReply()) {
            this.logger.error("Request for patientId {}, remoteId {} returned with status code: {}",
                patientId, remoteId, request.getRequestStatusCode());
            this.logger.error(" ...and error details: [{}]", request.getResponseJSON());
            return MatchRunStatus.ERROR;
        }

        return MatchRunStatus.OK;
    }

    private Set<String> getRemotesList()
    {
        XWikiContext context = this.contextProvider.get();
        List<BaseObject> potentialRemotes = this.RemoteConfigurationManager.getListOfRemotes(context);

        Set<String> remoteIdsList = new HashSet<>();
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
}
