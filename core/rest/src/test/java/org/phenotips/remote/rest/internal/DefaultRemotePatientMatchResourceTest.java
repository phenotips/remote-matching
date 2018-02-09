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
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;
import org.phenotips.remote.rest.RemotePatientMatchResource;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.Container;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.store.UnexpectedException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultRemotePatientMatchResource}.
 */
public class DefaultRemotePatientMatchResourceTest
{
    private static final String REFERENCE = "reference";

    private static final String MATCH_1 = "match1";

    private static final String MATCH_2 = "match2";

    private static final String MATCH_3 = "match3";

    private static final String SECURE = "secure";

    private static final String ID = "id";

    private static final String QUERY = "query";

    private static final String TOTAL_SIZE = "resultsCount";

    private static final String RETURNED_SIZE = "returnedCount";

    private static final String RESULTS = "results";

    private static final String REQ_NO = "reqNo";

    private static final String UNAUTHORIZED_MSG = "User unauthorized";

    private static final String UNEXPECTED_MSG = "Unexpected exception";

    private static final String LIMIT = "maxResults";

    private static final String OFFSET = "offset";

    private static final String REMOTE_SERVER = "remoteServer";

    private static final String REQUEST_JSON = "requestJson";

    private static final String RESPONSE_JSON = "responseJson";

    private static final String REQUEST = "request";

    private static final String RESPONSE = "response";

    private static final String SEND_NEW_REQUEST = "sendNewRequest";

    private static final String SERVER = "server";

    private static final String ERROR = "error";

    @Rule
    public MockitoComponentMockingRule<RemotePatientMatchResource> mocker =
        new MockitoComponentMockingRule<RemotePatientMatchResource>(DefaultRemotePatientMatchResource.class);

    @Mock
    private Patient reference;

    @Mock
    private OutgoingMatchRequest response;

    @Mock
    private RemotePatientSimilarityView match1;

    @Mock
    private RemotePatientSimilarityView match2;

    @Mock
    private RemotePatientSimilarityView match3;

    @Mock
    private org.xwiki.container.Request request;

    private RemotePatientMatchResource component;

    private Logger logger;

    private PatientRepository repository;

    private RemoteMatchingService matchingService;

    private JSONObject expectedAll;

    @Before
    public void setUp() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);
        final Execution execution = mock(Execution.class);
        final ExecutionContext executionContext = mock(ExecutionContext.class);
        final ComponentManager compManager = this.mocker.getInstance(ComponentManager.class, "context");
        final Provider<XWikiContext> provider = this.mocker.getInstance(XWikiContext.TYPE_PROVIDER);
        final XWikiContext context = provider.get();
        when(compManager.getInstance(Execution.class)).thenReturn(execution);
        when(execution.getContext()).thenReturn(executionContext);
        when(executionContext.getProperty("xwikicontext")).thenReturn(context);

        this.component = this.mocker.getComponentUnderTest();
        // Set up all injected classes.
        this.logger = this.mocker.getMockedLogger();
        this.repository = this.mocker.getInstance(PatientRepository.class, SECURE);
        this.matchingService = this.mocker.getInstance(RemoteMatchingService.class);

        // Mock the reference patient.
        when(this.repository.get(REFERENCE)).thenReturn(this.reference);
        when(this.reference.toJSON()).thenReturn(new JSONObject().put(ID, REFERENCE));

        // Mock matches search.
        final List<RemotePatientSimilarityView> matches = Arrays.asList(this.match1, this.match2, this.match3);
        when(this.matchingService.sendRequest(REFERENCE, REMOTE_SERVER, 0)).thenReturn(this.response);
        when(this.matchingService.getLastRequestSent(REFERENCE, REMOTE_SERVER)).thenReturn(this.response);
        when(this.matchingService.getSimilarityResults(this.response)).thenReturn(matches);

        // Mock response interactions.
        when(this.response.getRequestJSON()).thenReturn(new JSONObject().put(REQUEST_JSON, REQUEST_JSON));
        when(this.response.getResponseJSON()).thenReturn(new JSONObject().put(RESPONSE_JSON, RESPONSE_JSON));
        when(this.response.gotValidReply()).thenReturn(true);

        final Container container = this.mocker.getInstance(Container.class);
        // Mock container interactions.
        when(container.getRequest()).thenReturn(this.request);

        // Mock request data.
        when(this.request.getProperty(SERVER)).thenReturn(REMOTE_SERVER);
        when(this.request.getProperty(SEND_NEW_REQUEST)).thenReturn("false");
        when(this.request.getProperty(OFFSET)).thenReturn("1");
        when(this.request.getProperty(LIMIT)).thenReturn("10");
        when(this.request.getProperty(REQ_NO)).thenReturn("1");

        // Mock individual match data.
        when(this.match1.toJSON()).thenReturn(new JSONObject().put(ID, MATCH_1));
        when(this.match2.toJSON()).thenReturn(new JSONObject().put(ID, MATCH_2));
        when(this.match3.toJSON()).thenReturn(new JSONObject().put(ID, MATCH_3));

        this.expectedAll = constructAllMatchesJSON();
    }

    @Test
    public void findRemoteMatchingPatientsNullPatientIdResultsInBadRequest()
    {
        final Response response = this.component.findRemoteMatchingPatients(null);
        verify(this.logger).error("Patient ID is not specified.");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsEmptyPatientIdResultsInBadRequest()
    {
        final Response response = this.component.findRemoteMatchingPatients(StringUtils.EMPTY);
        verify(this.logger).error("Patient ID is not specified.");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsBlankPatientIdResultsInBadRequest()
    {
        final Response response = this.component.findRemoteMatchingPatients(StringUtils.SPACE);
        verify(this.logger).error("Patient ID is not specified.");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsNullServerResultsInBadRequest()
    {
        when(this.request.getProperty(SERVER)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("Server is not specified.");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsEmptyServerResultsInBadRequest()
    {
        when(this.request.getProperty(SERVER)).thenReturn(StringUtils.EMPTY);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("Server is not specified.");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsBlankServerResultsInBadRequest()
    {
        when(this.request.getProperty(SERVER)).thenReturn(StringUtils.SPACE);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("Server is not specified.");
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsPatientDoesNotExistResultsInBadRequest()
    {
        when(this.repository.get(REFERENCE)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("Patient with ID: {} could not be found.", REFERENCE);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsUserNotAuthorizedToSeeReferencePatientResultsInUnauthorized()
    {
        when(this.repository.get(REFERENCE)).thenThrow(new SecurityException(UNAUTHORIZED_MSG));
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("Failed to retrieve patient with ID [{}]: {}", REFERENCE, UNAUTHORIZED_MSG);
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientRetrieveOldRequestButNoneStoredResultsInNoContent()
    {
        when(this.matchingService.getLastRequestSent(REFERENCE, REMOTE_SERVER)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).warn("Remote match request was never initiated.");
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientRetrieveOldNonvalidRequestResultsInInternalServerError()
    {
        final String message = "I'm not valid.";
        final JSONObject errJSON = new JSONObject().put(ERROR, message);
        when(this.response.gotValidReply()).thenReturn(false);
        when(this.response.getRequestStatusCode()).thenReturn(-1);
        when(this.response.getRequestJSON()).thenReturn(errJSON);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("The response received from remote server {} was not valid: {}", -1, errJSON);
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsNoMatchesFoundResultsInValidResponse()
    {
        final List<RemotePatientSimilarityView> matches = Collections.emptyList();
        when(this.matchingService.getSimilarityResults(this.response)).thenReturn(matches);
                final JSONObject expected = new JSONObject()
            .put(QUERY, new JSONObject()
                .put(ID, REFERENCE))
            .put(TOTAL_SIZE, 0)
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RETURNED_SIZE, 0)
            .put(REQ_NO, 1)
            .put(OFFSET, 1)
            .put(RESULTS, new JSONArray());
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(expected.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsLessThanOneOffsetResultsInBadRequest()
    {
        when(this.request.getProperty(OFFSET)).thenReturn("-1");
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("The requested offset is out of bounds: {}", -1);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsTooLargeOffsetResultsInBadRequest()
    {
        when(this.request.getProperty(OFFSET)).thenReturn("60");
        when(this.request.getProperty(LIMIT)).thenReturn("80");
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("The requested offset [{}] is out of bounds", 60);
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void findRemoteMatchingPatientsOffsetNullDefaultsToOne()
    {
        when(this.request.getProperty(OFFSET)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(this.expectedAll.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsLimitNullDefaultsToNegativeOne()
    {
        when(this.request.getProperty(LIMIT)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(this.expectedAll.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsReqNoNullDefaultsToOne()
    {
        when(this.request.getProperty(REQ_NO)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(this.expectedAll.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsSendNewRequestNullDefaultsToFalse()
    {
        when(this.request.getProperty(SEND_NEW_REQUEST)).thenReturn(null);
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.matchingService, never()).sendRequest(anyString(), anyString(), anyInt());
        verify(this.matchingService, times(1)).getLastRequestSent(REFERENCE, REMOTE_SERVER);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(this.expectedAll.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsSendNewRequestTrueWorksAsExpected()
    {
        when(this.request.getProperty(SEND_NEW_REQUEST)).thenReturn("true");
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.matchingService, times(1)).sendRequest(REFERENCE, REMOTE_SERVER, 0);
        verify(this.matchingService, never()).getLastRequestSent(anyString(), anyString());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(this.expectedAll.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsLimitBiggerThanMatchesNumberReturnsAllMatchesFromOffset()
    {
        when(this.request.getProperty(LIMIT)).thenReturn("80");
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(this.expectedAll.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsLimitNegativeReturnsAllMatchesFromOffset()
    {
        when(this.request.getProperty(OFFSET)).thenReturn("2");
        when(this.request.getProperty(LIMIT)).thenReturn("-1");
        final JSONObject expected = new JSONObject()
            .put(QUERY, new JSONObject()
                .put(ID, REFERENCE))
            .put(TOTAL_SIZE, 3)
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RETURNED_SIZE, 2)
            .put(REQ_NO, 1)
            .put(OFFSET, 2)
            .put(RESULTS, new JSONArray()
                .put(this.match2.toJSON())
                .put(this.match3.toJSON()));

        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(expected.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsLimitLessThanLastResultReturnsCorrectSubset()
    {
        when(this.request.getProperty(OFFSET)).thenReturn("2");
        when(this.request.getProperty(LIMIT)).thenReturn("1");
        final JSONObject expected = new JSONObject()
            .put(QUERY, new JSONObject()
                .put(ID, REFERENCE))
            .put(TOTAL_SIZE, 3)
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RETURNED_SIZE, 1)
            .put(REQ_NO, 1)
            .put(OFFSET, 2)
            .put(RESULTS, new JSONArray()
                .put(this.match2.toJSON()));

        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assert.assertTrue(expected.similar(response.getEntity()));
    }

    @Test
    public void findRemoteMatchingPatientsUnexpectedExceptionIsThrown()
    {
        when(this.repository.get(REFERENCE)).thenThrow(new UnexpectedException(UNEXPECTED_MSG));
        final Response response = this.component.findRemoteMatchingPatients(REFERENCE);
        verify(this.logger).error("Unexpected exception while generating remote matches: {}", UNEXPECTED_MSG);
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }
    private JSONObject constructAllMatchesJSON()
    {
        return new JSONObject()
            .put(QUERY, new JSONObject()
                .put(ID, REFERENCE))
            .put(TOTAL_SIZE, 3)
            .put(REQUEST, this.response.getRequestJSON())
            .put(RESPONSE, this.response.getResponseJSON())
            .put(RETURNED_SIZE, 3)
            .put(REQ_NO, 1)
            .put(OFFSET, 1)
            .put(RESULTS, new JSONArray()
                .put(this.match1.toJSON())
                .put(this.match2.toJSON())
                .put(this.match3.toJSON()));
    }
}
