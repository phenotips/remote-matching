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
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;

import org.xwiki.model.reference.DocumentReference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RemoteMatchedPatientClusterView}.
 */
public class RemoteMatchedPatientClusterViewTest
{
    private static final String ID_LABEL = "id";

    private static final String REFERENCE = "reference";

    private static final String PATIENT_1 = "patient1";

    private static final String PATIENT_2 = "patient2";

    private static final String PATIENT_3 = "patient3";

    private static final String PATIENT_4 = "patient4";

    private static final String PATIENT_5 = "patient5";

    private static final String QUERY_LABEL = "query";

    private static final String TOTAL_LABEL = "resultsCount";

    private static final String RETURNED_LABEL = "returnedCount";

    private static final String RESULTS_LABEL = "results";

    private static final String OFFSET_LABEL = "offset";

    private static final String REQUEST_JSON = "requestJSON";

    private static final String RESPONSE_JSON = "responseJSON";

    private static final String REQUEST = "request";

    private static final String RESPONSE = "response";

    @Mock
    private Patient reference;

    @Mock
    private DocumentReference docRef;

    @Mock
    private RemotePatientSimilarityView patient1;

    @Mock
    private DocumentReference doc1;

    @Mock
    private RemotePatientSimilarityView patient2;

    @Mock
    private DocumentReference doc2;

    @Mock
    private RemotePatientSimilarityView patient3;

    @Mock
    private DocumentReference doc3;

    @Mock
    private RemotePatientSimilarityView patient4;

    @Mock
    private DocumentReference doc4;

    @Mock
    private RemotePatientSimilarityView patient5;

    @Mock
    private DocumentReference doc5;

    @Mock
    private OutgoingMatchRequest response;

    private RemoteMatchedPatientClusterView matches;

    private List<RemotePatientSimilarityView> matchList;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        when(this.reference.toJSON()).thenReturn(new JSONObject().put(ID_LABEL, REFERENCE));
        when(this.reference.getDocumentReference()).thenReturn(this.docRef);

        when(this.patient1.toJSON()).thenReturn(new JSONObject().put(ID_LABEL, PATIENT_1));
        when(this.patient1.getDocumentReference()).thenReturn(this.doc1);

        when(this.patient2.toJSON()).thenReturn(new JSONObject().put(ID_LABEL, PATIENT_2));
        when(this.patient2.getDocumentReference()).thenReturn(this.doc2);

        when(this.patient3.toJSON()).thenReturn(new JSONObject().put(ID_LABEL, PATIENT_3));
        when(this.patient3.getDocumentReference()).thenReturn(this.doc3);

        when(this.patient4.toJSON()).thenReturn(new JSONObject().put(ID_LABEL, PATIENT_4));
        when(this.patient4.getDocumentReference()).thenReturn(this.doc4);

        when(this.patient5.toJSON()).thenReturn(new JSONObject().put(ID_LABEL, PATIENT_5));
        when(this.patient5.getDocumentReference()).thenReturn(this.doc5);

        when(this.response.getRequestJSON()).thenReturn(new JSONObject().put(REQUEST_JSON, REQUEST_JSON));
        when(this.response.getResponseJSON()).thenReturn(new JSONObject().put(RESPONSE_JSON, RESPONSE_JSON));

        this.matchList = Arrays.asList(this.patient1, this.patient2, this.patient3, this.patient4, this.patient5);
        this.matches = new RemoteMatchedPatientClusterView(this.reference, this.response, this.matchList, null);
    }

    @Test(expected = NullPointerException.class)
    public void instantiatingClassWithNullPatientThrowsException()
    {
        new RemoteMatchedPatientClusterView(null, this.response, this.matchList, null);
    }

    @Test(expected = NullPointerException.class)
    public void instantiatingClassWithNullResponseThrowsException()
    {
        new RemoteMatchedPatientClusterView(this.reference, null, this.matchList, null);
    }

    @Test
    public void getReferenceReturnsTheReferenceThatWasSet()
    {
        Assert.assertEquals(this.reference, this.matches.getReference());
    }

    @Test
    public void getMatchesReturnsEmptyListIfNoMatchesSet()
    {
        final MatchedPatientClusterView matches = new RemoteMatchedPatientClusterView(this.reference, this.response,
            Collections.<RemotePatientSimilarityView>emptyList(), null);
        Assert.assertTrue(matches.getMatches().isEmpty());
    }

    @Test
    public void getMatchesReturnsTheMatchesThatWereProvided()
    {
        Assert.assertEquals(this.matchList, this.matches.getMatches());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getMatchesReturnedMatchesCannotBeModified()
    {
        this.matches.getMatches().add(mock(PatientSimilarityView.class));
    }

    @Test
    public void sizeIsZeroIfMatchesIsEmpty()
    {
        final MatchedPatientClusterView matches = new RemoteMatchedPatientClusterView(this.reference, this.response,
            Collections.<RemotePatientSimilarityView>emptyList(), null);
        Assert.assertEquals(0, matches.size());
    }

    @Test
    public void sizeReturnsCorrectNumberOfMatches()
    {
        Assert.assertEquals(5, this.matches.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void toJSONThrowsExceptionIfFromIndexInvalid()
    {
        this.matches.toJSON(-1, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void toJSONThrowsExceptionIfFromIndexIsGreaterThanDataSize()
    {
        this.matches.toJSON(300, 10);
    }

    @Test
    public void toJSONGetsCorrectDataForProvidedIndices()
    {
        final JSONObject result = this.matches.toJSON(1, 3);
        final JSONObject expected = new JSONObject()
            .put(QUERY_LABEL, new JSONObject()
                .put(ID_LABEL, REFERENCE))
            .put(TOTAL_LABEL, 5)
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RETURNED_LABEL, 3)
            .put(OFFSET_LABEL, 2)
            .put(RESULTS_LABEL, new JSONArray()
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_2))
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_3))
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_4)));
        Assert.assertTrue(expected.similar(result));
    }

    @Test
    public void toJSONGetsAllDataIfNoIndicesProvided()
    {
        final JSONObject result = this.matches.toJSON();
        final JSONObject expected = new JSONObject()
            .put(QUERY_LABEL, new JSONObject()
                .put(ID_LABEL, REFERENCE))
            .put(TOTAL_LABEL, 5)
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RETURNED_LABEL, 5)
            .put(OFFSET_LABEL, 1)
            .put(RESULTS_LABEL, new JSONArray()
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_1))
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_2))
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_3))
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_4))
                .put(new JSONObject()
                    .put(ID_LABEL, PATIENT_5)));
        Assert.assertTrue(expected.similar(result));
    }

    @Test
    public void equalsReturnsTrueForTwoDifferentObjectsWithSameData()
    {
        final MatchedPatientClusterView v2 = new RemoteMatchedPatientClusterView(this.reference, this.response,
            this.matchList, null);
        Assert.assertTrue(v2.equals(this.matches));
    }

    @Test
    public void equalsReturnsTrueForTwoIdenticalObjects()
    {
        Assert.assertTrue(this.matches.equals(this.matches));
    }

    @Test
    public void equalsReturnsFalseForTwoDifferentObjectsWithDifferentResponses()
    {
        final OutgoingMatchRequest response2 = mock(OutgoingMatchRequest.class);
        final MatchedPatientClusterView v2 = new RemoteMatchedPatientClusterView(this.reference, response2,
            this.matchList, null);
        Assert.assertFalse(v2.equals(this.matches));
    }

    @Test
    public void equalsReturnsFalseForTwoDifferentObjectsWithDifferentReference()
    {
        final MatchedPatientClusterView v2 = new RemoteMatchedPatientClusterView(mock(Patient.class), this.response,
            this.matchList, null);
        Assert.assertFalse(v2.equals(this.matches));
    }

    @Test
    public void equalsReturnsFalseForTwoDifferentObjectsWithDifferentMatchList()
    {
        final List<RemotePatientSimilarityView> m2 = Arrays.asList(this.patient1, this.patient2, this.patient3);
        final MatchedPatientClusterView v2 = new RemoteMatchedPatientClusterView(this.reference, this.response, m2, null);
        Assert.assertFalse(v2.equals(this.matches));
    }

    @Test
    public void hashCodeIsTheSameForTwoObjectsWithSameData()
    {
        final MatchedPatientClusterView v2 = new RemoteMatchedPatientClusterView(this.reference, this.response,
            this.matchList, null);
        Assert.assertEquals(v2.hashCode(), this.matches.hashCode());
    }
}
