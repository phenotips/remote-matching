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
package org.phenotips.remote.common.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.similarity.MatchedPatientClusterView;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.OutgoingMatchRequest;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Remote matches implementation of {@link MatchedPatientClusterView} that provides access to the reference patient and
 * its remote matches.
 */
public class RemoteMatchedPatientClusterView implements MatchedPatientClusterView
{
    private static final String RESPONSE = "response";

    private static final String REQUEST = "request";

    private static final String QUERY = "query";

    private static final String RESULTS = "results";

    private static final String TOTAL_SIZE = "resultsCount";

    private static final String RETURNED_SIZE = "returnedCount";

    private static final String OFFSET = "offset";

    /** @see #getReference(). */
    private final Patient reference;

    /** The response from remote server. */
    private final OutgoingMatchRequest response;

    /** @see #getMatches(). */
    private final List<RemotePatientSimilarityView> remoteMatches;

    /**
     * Default constructor that takes a local reference {@code patient}, and its {@code remoteMatches remote matches}.
     *
     * @param patient the local reference {@code patient}
     * @param response the response from the remote server, as {@link OutgoingMatchRequest}
     * @param remoteMatches a list of {@link PatientSimilarityView} objects representing remote matching patients
     */
    public RemoteMatchedPatientClusterView(
        @Nonnull final Patient patient,
        @Nonnull final OutgoingMatchRequest response,
        @Nonnull final List<RemotePatientSimilarityView> remoteMatches)
    {
        Validate.notNull(patient, "The reference patient must not be null.");
        Validate.notNull(response, "The remote response must not be null.");
        Validate.notNull(remoteMatches, "The list of matches must not be null.");
        this.reference = patient;
        this.response = response;
        this.remoteMatches = remoteMatches;
    }

    @Override
    public Patient getReference()
    {
        return this.reference;
    }

    @Override
    public List<PatientSimilarityView> getMatches()
    {
        return Collections.<PatientSimilarityView>unmodifiableList(this.remoteMatches);
    }

    @Override
    public int size()
    {
        return this.remoteMatches.size();
    }

    @Override
    public JSONObject toJSON()
    {
        final int size = size() == 0 ? 0 : size() - 1;
        return toJSON(0, size);
    }

    @Override
    public JSONObject toJSON(final int fromIndex, final int toIndex)
        throws IndexOutOfBoundsException, IllegalArgumentException
    {
        if (toIndex < fromIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        final JSONArray matchesJson = requestedEmpty(fromIndex, toIndex)
            ? new JSONArray()
            : buildMatchesJSONArray(fromIndex, toIndex);
        return new JSONObject()
            .put(QUERY, this.reference.toJSON())
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RESULTS, matchesJson)
            .put(TOTAL_SIZE, size())
            .put(RETURNED_SIZE, matchesJson.length())
            .put(OFFSET, fromIndex + 1);
    }

    /**
     * Returns true iff there are no matches, and this empty data set should be converted to JSON.
     *
     * @param fromIndex the starting index for match to convert (inclusive, zero-based)
     * @param toIndex the last index for match to convert (inclusive, zero-based)
     * @return true iff there are no matches, but the empty {@link #getMatches()} should be converted to JSON
     */
    private boolean requestedEmpty(final int fromIndex, final int toIndex)
    {
        return size() == 0 && fromIndex == 0 && toIndex == 0;
    }

    /**
     * Builds a JSON representation of {@link #getMatches()}.
     *
     * @param fromIndex the starting index for match to convert (inclusive, zero-based)
     * @param toIndex the last index for match to convert (inclusive, zero-based)
     * @return a {@link JSONArray} of {@link #getMatches()}
     */
    private JSONArray buildMatchesJSONArray(final int fromIndex, final int toIndex)
    {
        // Sort by score
        Collections.sort(this.remoteMatches, new Comparator<RemotePatientSimilarityView>()
        {
            @Override
            public int compare(RemotePatientSimilarityView o1, RemotePatientSimilarityView o2)
            {
                double score1 = o1.getScore();
                double score2 = o2.getScore();
                return (int) Math.signum(score2 - score1);
            }
        });

        final JSONArray matchesJson = new JSONArray();

        for (int i = fromIndex; i <= toIndex; i++) {
            final JSONObject matchJson = this.remoteMatches.get(i).toJSON();
            matchesJson.put(matchJson);
        }
        return matchesJson;
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
        return Objects.equals(this.reference, that.reference)
            && Objects.equals(this.response, that.response)
            && Objects.equals(this.remoteMatches, that.remoteMatches);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.reference, this.response, this.remoteMatches);
    }
}
