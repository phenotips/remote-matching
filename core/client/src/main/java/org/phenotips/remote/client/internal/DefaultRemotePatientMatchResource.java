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
import org.phenotips.data.PatientRepository;
import org.phenotips.data.similarity.MatchedPatientClusterView;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.client.RemotePatientMatchResource;
import org.phenotips.remote.common.internal.RemoteMatchedPatientClusterView;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;

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
import org.slf4j.Logger;

/**
 * Default implementation of the {@link RemotePatientMatchResource}.
 *
 * @version $Id$
 * @since 1.2
 */
@Component
@Named("org.phenotips.remote.client.internal.DefaultRemotePatientMatchResource")
@Singleton
public class DefaultRemotePatientMatchResource extends XWikiResource implements RemotePatientMatchResource
{
    private static final String REQ_NO = "reqNo";

    private static final String OFFSET = "offset";

    private static final String LIMIT = "maxResults";

    private static final String SERVER = "server";

    private static final String SEND_NEW_REQUEST = "sendNewRequest";

    /** The logging object. */
    @Inject
    private Logger logger;

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

    @Override
    public Response findRemoteMatchingPatients(final String patientId)
    {
        // Get the request container.
        final Request request = this.container.getRequest();
        // The patient ID must not be blank.
        if (StringUtils.isBlank(patientId)) {
            this.logger.error("Patient ID is not specified.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // The server must not be blank.
        final String server = (String) request.getProperty(SERVER);
        if (StringUtils.isBlank(server)) {
            this.logger.error("Server is not specified.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        // Get the other parameters, if specified, or set the defaults.
        final int offset =  NumberUtils.toInt((String) request.getProperty(OFFSET), 1);
        if (offset < 1) {
            this.logger.error("The requested offset is out of bounds: {}", offset);
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
                this.logger.error("Patient with ID: {} could not be found.", patientId);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            // FIXME: Magic number taken from PhenomeCentral.RemoteResults
            final OutgoingMatchRequest remoteResponse = newRequest
                ? this.matchingService.sendRequest(patientId, server, 5)
                : this.matchingService.getLastRequestSent(patientId, server);
            // If the response is null, the request was never initiated.
            if (remoteResponse == null) {
                this.logger.warn("Remote match request was never initiated.");
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            // If no valid reply, retrieve the request status code and the JSON.
            if (!remoteResponse.gotValidReply()) {
                this.logger.error("The response received from remote server {} was not valid: {}",
                    remoteResponse.getRequestStatusCode(), remoteResponse.getRequestJSON());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            return buildMatches(patient, remoteResponse, offset, limit, reqNo);
        } catch (final SecurityException e) {
            this.logger.error("Failed to retrieve patient with ID [{}]: {}", patientId, e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (final IndexOutOfBoundsException | IllegalArgumentException e) {
            this.logger.error("The requested offset [{}] is out of bounds", offset);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (final Exception e) {
            this.logger.error("Unexpected exception while generating remote matches: {}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Builds a response containing the reference patient and matched patients data.
     *
     * @param patient the reference {@link Patient} object
     * @param remoteResponse the response received from the remote server
     * @param offset the offset for the returned matches
     * @param limit the maximum number of matches to return after the offset
     * @param reqNo the current request number
     * @return a {@link Response} containing the matched patients data
     */
    private Response buildMatches(
        @Nonnull final Patient patient,
        @Nonnull final OutgoingMatchRequest remoteResponse,
        final int offset,
        final int limit,
        final int reqNo)
    {
        final List<RemotePatientSimilarityView> matches = this.matchingService.getSimilarityResults(remoteResponse);
        final MatchedPatientClusterView matchedCluster =
            new RemoteMatchedPatientClusterView(patient, remoteResponse, matches);
        final JSONObject matchesJson = !matches.isEmpty()
            ? matchedCluster.toJSON(offset - 1, getLastIndex(matchedCluster, offset, limit))
            : matchedCluster.toJSON();
        matchesJson.put(REQ_NO, reqNo);
        return Response.ok(matchesJson, MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Calculates the last index based on the provided {@code offset}, {@code limit}, and size of
     * {@code matchedCluster}.
     *
     * @param matchedCluster the {@link MatchedPatientClusterView} object containing the matches for requested patient
     * @param offset the offset for the match data to be returned
     * @param limit the limit for the number of matches to be returned
     * @return the index of the last match to be returned
     */
    private int getLastIndex(@Nonnull final MatchedPatientClusterView matchedCluster, final int offset, final int limit)
    {
        final int totalSize = matchedCluster.size();
        final int lastItemIdx = totalSize - 1;
        final int requestedLast = limit >= 0 ? offset + limit - 2 : lastItemIdx;
        return requestedLast <= lastItemIdx ? requestedLast : lastItemIdx;
    }
}
