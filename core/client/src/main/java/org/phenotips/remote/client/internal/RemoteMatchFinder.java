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

import org.phenotips.consents.ConsentManager;
import org.phenotips.data.Patient;
import org.phenotips.matchingnotification.finder.MatchFinder;
import org.phenotips.matchingnotification.finder.internal.AbstractMatchFinder;
import org.phenotips.matchingnotification.match.PatientMatch;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

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
    private RemoteConfigurationManager remoteConfigurationManager;

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
    public Set<String> getSupportedServerIdList()
    {
        return this.getRemotesList();
    }

    @Override
    protected Response specificFindMatches(Patient patient, String remoteId, List<PatientMatch> matchesList)
    {
        try {
            // Checking if a patient has a consent for remote matching
            if (!this.consentManager.hasConsent(patient, REMOTE_MATCHING_CONSENT_ID)) {
                this.logger.debug("Skipping patient {}. No consent for remote matching", patient.getId());
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            this.logger.debug("Finding remote matches for patient [{}] on server [{}]", patient.getId(), remoteId);

            OutgoingMatchRequest remoteResponse =
                this.matchingService.sendRequest(patient.getId(), remoteId, ADD_TOP_N_GENES_PARAMETER, matchesList);

            // If the response is null, the request was never initiated.
            if (remoteResponse == null) {
                this.logger.warn("Remote match request to [{}] was never initiated for patient [{}]",
                    remoteId, patient.getId());
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            if (!remoteResponse.wasSent()) {
                if (remoteResponse.errorContactingRemoteServer()) {
                    this.logger.error("Unable to connect to remote server [{}]", remoteId);
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
                } else {
                    this.logger.error("Could not initialte an MME match request for patient [{}]", patient.getId());
                    return Response.status(Response.Status.CONFLICT).build();
                }
            }
            // If no valid reply, retrieve the request status code and the JSON.
            if (!remoteResponse.gotValidReply()) {
                if (remoteResponse.getRequestStatusCode().equals(ApiConfiguration.HTTP_UNAUTHORIZED)) {
                    this.logger.error("Not authorized to contact selected MME server [{}]", remoteId);
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
                if (remoteResponse.getRequestStatusCode().equals(ApiConfiguration.HTTP_UNSUPPORTED_API_VERSION)) {
                    this.logger.error("Unsupported MME version when contacting MME server [{}]", remoteId);
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                }
                this.logger.error("Remote MME server [{}] rejected match request with status code [{}]",
                    remoteId, remoteResponse.getRequestStatusCode());
                this.logger.error(" ...and error details: [{}]", remoteResponse.getResponseJSON());
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }

            return Response.status(Response.Status.OK).build();

        } catch (final Exception e) {
            this.logger.error("Unexpected exception while generating remote matches: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    private Set<String> getRemotesList()
    {
        XWikiContext context = this.contextProvider.get();
        List<BaseObject> potentialRemotes = this.remoteConfigurationManager.getListOfRemotes(context);

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

    @Override
    public JSONObject getLastUpdatedDateForServerForPatient(String patientId, String serverId)
    {
        Set<String> supportedServers = this.getSupportedServerIdList();
        if (!supportedServers.contains(serverId)) {
            return null;
        }

        JSONObject result = new JSONObject();

        OutgoingMatchRequest lastRequest =
                this.matchingService.getLastOutgoingRequest(serverId, patientId);
        OutgoingMatchRequest lastSuccessfulRequest =
                this.matchingService.getLastSuccessfulOutgoingRequest(serverId, patientId);

        result.put("lastSuccessfulMatchUpdateDate", this.getRequestDateForJSON(lastSuccessfulRequest));
        result.put("lastMatchUpdateDate", this.getRequestDateForJSON(lastRequest));

        if (lastRequest != null && (!lastRequest.wasSent() || !lastRequest.gotValidReply())) {
            result.put("lastMatchUpdateErrorCode", lastRequest.getRequestStatusCode());
            result.put("lastMatchUpdateError", lastRequest.getResponseJSON());
        }
        return result;
    }

    private Object getRequestDateForJSON(OutgoingMatchRequest request)
    {
        return (request == null) ? JSONObject.NULL : request.getRequestTime();
    }
}
