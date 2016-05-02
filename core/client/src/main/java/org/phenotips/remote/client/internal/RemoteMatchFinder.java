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

import org.phenotips.data.Patient;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.matchingnotification.finder.MatchFinder;
import org.phenotips.matchingnotification.match.PatientMatch;
import org.phenotips.matchingnotification.match.internal.DefaultPatientMatch;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
@Component
@Named("remote")
@Singleton
public class RemoteMatchFinder implements MatchFinder
{
    private static final int ADD_TOP_N_GENES_PARAMETER = 0;

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

    @Override
    public int getPriority()
    {
        return 100;
    }

    @Override
    public List<PatientMatch> findMatches(Patient patient)
    {
        List<String> remoteIds = this.getRemotesList();
        if (remoteIds == null) {
            this.logger.warn("No remote servers were found for remote matching.");
        }

        List<PatientMatch> patientMatches = new LinkedList<>();

        for (String remoteId : remoteIds) {
            List<PatientMatch> currentMatches = this.sendAndProcessRequest(patient.getId(), remoteId);
            patientMatches.addAll(currentMatches);
        }

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

        List<PatientSimilarityView> parsedResults = this.matchingService.getSimilarityResults(request);
        for (PatientSimilarityView result : parsedResults) {
            PatientMatch match = new DefaultPatientMatch(result, remoteId, true);
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

            remoteIdsList.add(configuredServerId);
        }

        return remoteIdsList;
    }
}