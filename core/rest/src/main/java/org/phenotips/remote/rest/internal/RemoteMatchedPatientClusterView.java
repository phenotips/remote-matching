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
import org.phenotips.data.similarity.MatchedPatientClusterView;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.internal.DefaultMatchedPatientClusterView;
import org.phenotips.matchingnotification.match.PatientMatch;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.json.JSONObject;

/**
 * Remote matches implementation of {@link MatchedPatientClusterView} that provides access to the reference patient and
 * its remote matches.
 */
public class RemoteMatchedPatientClusterView extends DefaultMatchedPatientClusterView implements MatchedPatientClusterView
{
    private static final String RESPONSE = "response";

    private static final String REQUEST = "request";

    /** The response from remote server. */
    private final OutgoingMatchRequest mmeMatchRequest;

    /**
     * Default constructor that takes a local reference {@code patient}, and its {@code remoteMatches remote matches}.
     *
     * @param patient the local reference {@code patient}
     * @param response the response from the remote server, as {@link OutgoingMatchRequest}
     * @param remoteMatches a list of {@link PatientSimilarityView} objects representing remote matching patients
     * @param matchesIds a map of {@link PatientSimilarityView} objects to the IDs of corresponding matches saved in DB
     */
    public RemoteMatchedPatientClusterView(
        @Nonnull final Patient patient,
        @Nonnull final OutgoingMatchRequest mmeMatchRequest,
        @Nullable final List<RemotePatientSimilarityView> remoteMatches,
        @Nullable final Map<PatientSimilarityView, PatientMatch> matchesIds)
    {
        super(patient, remoteMatches, matchesIds);

        Validate.notNull(mmeMatchRequest, "The remote response must not be null.");
        this.mmeMatchRequest = mmeMatchRequest;
    }

    @Override
    public JSONObject toJSON(final int fromIndex, final int maxResults)
        throws IndexOutOfBoundsException
    {
        JSONObject result = super.toJSON(fromIndex, maxResults);
        result.put(REQUEST, this.mmeMatchRequest.getRequestJSON());
        result.put(RESPONSE, this.mmeMatchRequest.getResponseJSON());
        return result;
    }

    @Override
    public boolean equals(@Nullable final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RemoteMatchedPatientClusterView that = (RemoteMatchedPatientClusterView) o;
        return Objects.equals(this.mmeMatchRequest, that.mmeMatchRequest) && super.equals(that);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.mmeMatchRequest, super.hashCode());
    }
}
