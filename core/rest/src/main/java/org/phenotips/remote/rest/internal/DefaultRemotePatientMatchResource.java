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
package org.phenotips.remote.rest.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.similarity.MatchedPatientClusterView;
import org.phenotips.matchingnotification.storage.MatchStorageManager;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;
import org.phenotips.remote.rest.RemotePatientMatchResource;

import org.xwiki.component.annotation.Component;
import org.xwiki.container.Container;
import org.xwiki.container.Request;
import org.xwiki.rest.XWikiResource;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;

/**
 * Default implementation of the {@link RemotePatientMatchResource}.
 *
 * @version $Id$
 * @since 1.1
 */
@Component
@Named("org.phenotips.remote.rest.internal.DefaultRemotePatientMatchResource")
@Singleton
public class DefaultRemotePatientMatchResource extends XWikiResource implements RemotePatientMatchResource
{
    private static final String REQ_NO = "reqNo";

    private static final String OFFSET = "offset";

    private static final String LIMIT = "maxResults";

    private static final String SERVER = "server";

    private static final String SEND_NEW_REQUEST = "sendNewRequest";

    /** The secure patient repository. */
    @Inject
    @Named("secure")
    private PatientRepository repository;

    /** The remote similar patients matching service. */
    @Inject
    private RemoteMatchingService matchingService;

    /** The XWiki container. */
    @Inject
    private Container container;

    @Inject
    private MatchStorageManager matchStorageManager;

    @Override
    public Response findRemoteMatchingPatients(final String patientId)
    {
        // Get the request container.
        final Request request = this.container.getRequest();
        // The patient ID must not be blank.
        if (StringUtils.isBlank(patientId)) {
            this.slf4Jlogger.error("Patient ID is not specified.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // The server must not be blank.
        final String server = (String) request.getProperty(SERVER);
        if (StringUtils.isBlank(server)) {
            this.slf4Jlogger.error("Server is not specified.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // Get the other parameters, if specified, or set the defaults.
        final int offset = NumberUtils.toInt((String) request.getProperty(OFFSET), 1);
        if (offset < 1) {
            this.slf4Jlogger.error("The requested offset is out of bounds: {}", offset);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        final boolean newRequest = BooleanUtils.toBoolean((String) request.getProperty(SEND_NEW_REQUEST));
        final int limit = NumberUtils.toInt((String) request.getProperty(LIMIT), -1);
        final int reqNo = NumberUtils.toInt((String) request.getProperty(REQ_NO), 1);
        // Build the response.
        return buildResponse(patientId, server, offset, limit, newRequest, reqNo);
    }

    /**
     * Builds a response containing the reference patient and matched patients data, or an error code if unsuccessful.
     *
     * @param patientId the local reference patient identifier
     * @param server the remote server that will be queried for matches
     * @param offset the offset for the returned matches
     * @param limit the maximum number of matches to return after the offset
     * @param newRequest true iff a new match request should be made to the remote server
     * @param reqNo the current request number
     * @return a {@link Response} containing the matched patients data, or an error code if unsuccessful
     */
    @SuppressWarnings("ReturnCount")
    private Response buildResponse(
        @Nonnull final String patientId,
        @Nonnull final String server,
        final int offset,
        final int limit,
        final boolean newRequest,
        final int reqNo)
    {
        try {
            // Checks if the current user has access to the requested patient.
            final Patient patient = this.repository.get(patientId);
            // If patient with requested ID is not found, this is an error.
            if (patient == null) {
                this.slf4Jlogger.error("Patient with ID: {} could not be found.", patientId);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            // don't send any top Exomiser genes in outgoing requests
            final OutgoingMatchRequest remoteResponse = newRequest
                ? this.matchingService.sendRequest(patientId, server, 0)
                : this.matchingService.getLastRequestSent(patientId, server);

            // If the response is null, the request was never initiated.
            if (remoteResponse == null) {
                this.slf4Jlogger.warn("Remote match request to [{}] was never initiated for patient [{}]",
                    server, patientId);
                return Response.status(Response.Status.NO_CONTENT).build();
            }

            if (!remoteResponse.wasSent()) {
                if (remoteResponse.errorContactingRemoteServer()) {
                    this.slf4Jlogger.error("Unable to connect to remote server [{}]", server);
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
                } else {
                    this.slf4Jlogger.error("Could not initialte an MME match request for patient [{}]", patientId);
                    return Response.status(Response.Status.CONFLICT).build();
                }
            }
            // If no valid reply, retrieve the request status code and the JSON.
            if (!remoteResponse.gotValidReply()) {
                if (remoteResponse.getRequestStatusCode().equals(ApiConfiguration.HTTP_UNAUTHORIZED)) {
                    this.slf4Jlogger.error("Not authorized to contact selected MME server [{}]", server);
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
                if (remoteResponse.getRequestStatusCode().equals(ApiConfiguration.HTTP_UNSUPPORTED_API_VERSION)) {
                    this.slf4Jlogger.error("Unsupported MME version when contacting MME server [{}]", server);
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
                }
                this.slf4Jlogger.error("Remote MME server [{}] rejected match request with status code [{}]",
                    server, remoteResponse.getRequestStatusCode());
                return Response.status(Response.Status.NOT_ACCEPTABLE).build();
            }

            return buildMatches(patient, remoteResponse, offset, limit, reqNo);
        } catch (final SecurityException e) {
            this.slf4Jlogger.error("Failed to retrieve patient with ID [{}]: {}", patientId, e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (final IndexOutOfBoundsException e) {
            this.slf4Jlogger.error("The requested offset [{}] is out of bounds", offset);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (final Exception e) {
            this.slf4Jlogger.error("Unexpected exception while generating remote matches: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Builds a response containing the reference patient and matched patients data.
     *
     * @param patient the reference {@link Patient} object
     * @param mmeMatchRequest the response received from the remote server
     * @param offset the offset for the returned matches
     * @param limit the maximum number of matches to return after the offset
     * @param reqNo the current request number
     * @return a {@link Response} containing the matched patients data
     */
    private Response buildMatches(
        @Nonnull final Patient patient,
        @Nonnull final OutgoingMatchRequest mmeMatchRequest,
        final int offset,
        final int limit,
        final int reqNo)
    {
        final List<RemotePatientSimilarityView> matches = this.matchingService.getSimilarityResults(mmeMatchRequest);

        this.matchStorageManager.saveRemoteMatches(matches, patient.getId(),
            mmeMatchRequest.getRemoteServerId(), false);

        final MatchedPatientClusterView matchedCluster =
            new RemoteMatchedPatientClusterView(patient, mmeMatchRequest, matches);

        final JSONObject matchesJson = matchedCluster.toJSON(offset - 1, limit);
        matchesJson.put(REQ_NO, reqNo);
        return Response.ok(matchesJson, MediaType.APPLICATION_JSON_TYPE).build();
    }
}
