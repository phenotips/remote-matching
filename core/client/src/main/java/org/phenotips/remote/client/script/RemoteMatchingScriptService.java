/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.remote.client.script;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.OutgoingSearchRequest;
import org.phenotips.remote.common.ApiFactory;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.remote.hibernate.internal.DefaultOutgoingSearchRequest;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import net.sf.json.JSONObject;

/**
 * Gives velocity access to the functions it needs to perform remote matching. There is a set of functions for sending
 * the request, and a set for retrieving the data.
 */
@Unstable
@Component
@Named("remoteMatching")
@Singleton
public class RemoteMatchingScriptService implements ScriptService
{
    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    ApiFactory apiFactory;

    @Inject
    private RemoteMatchingStorageManager requestStorageManager;


    public JSONObject sendRequest(String patientId, String remoteServerId)
    {
        return sendRequest(patientId, remoteServerId, false, false, 0);
    }

    public JSONObject sendRequest(String patientId, String remoteServerId, boolean async, boolean periodic)
    {
        return sendRequest(patientId, remoteServerId, async, periodic, 0);
    }

    public JSONObject sendRequest(String patientId, String remoteServerId, boolean async, boolean periodic, int addTopNGenes)
    {
        this.logger.error("Sending outgoing request for patient [{}] to server [{}]", patientId, remoteServerId);
        if (async) {
            this.logger.error("Requesting async replies");
        }
        if (periodic) {
            this.logger.error("Requesting periodic replies");
        }

        XWikiContext context = this.getContext();

        BaseObject configurationObject = XWikiAdapter.getRemoteConfigurationGivenRemoteName(remoteServerId, context);

        if (configurationObject == null) {
            logger.error("Requested matching server is not configured: [{}]", remoteServerId);
            return generateScriptErrorReply(ApiConfiguration.ERROR_NOT_SENT,
                                            "requested matching server ["+remoteServerId+"] is not configured");
        }

        // TODO: get API version from server configuration

        String apiVersion = "v1";
        ApiDataConverter apiVersionSpecificConverter = this.apiFactory.getApiVersion(apiVersion);

        DefaultOutgoingSearchRequest request = new DefaultOutgoingSearchRequest(patientId, remoteServerId);
        if (periodic) {
            request.setQueryType(ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC);
        }
        if (async) {
            request.setResponseType(ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS);
        }

        JSONObject requestJSON;
        try {
            requestJSON = apiVersionSpecificConverter.getOutgoingRequestToJSONConverter().toJSON(request, addTopNGenes);
        } catch (ApiViolationException ex) {
            return generateScriptErrorReply(ApiConfiguration.ERROR_NOT_SENT, ex.getMessage());
        }
        if (requestJSON == null) {
            this.logger.error("Unable to convert patient to JSON: [{}]", patientId);
            return generateScriptErrorReply(ApiConfiguration.ERROR_NOT_SENT,
                                            "unable to convert patient with ID ["+patientId.toString()+"] to JSON");
        }

        CloseableHttpClient client = HttpClients.createDefault();

        StringEntity jsonEntity = new StringEntity(requestJSON.toString(), ContentType.create("application/json", "utf-8"));

        // TODO: hack to make charset lower-cased so that GeneMatcher accepts it
        jsonEntity.setContentType("application/json; charset=utf-8");
        this.logger.error("Using content type: [{}]", jsonEntity.getContentType().toString());

        String key     = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_KEY_FIELD);
        String baseURL = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD);
        if (baseURL.charAt(baseURL.length() - 1) != '/') {
            baseURL += "/";
        }
        String targetURL = baseURL + ApiConfiguration.REMOTE_URL_SEARCH_ENDPOINT;

        this.logger.error("Sending matching request to [" + targetURL + "]: " + requestJSON.toString());

        CloseableHttpResponse httpResponse;
        try {
            HttpPost httpRequest = new HttpPost(targetURL);
            httpRequest.setEntity(jsonEntity);
            httpRequest.setHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER, key);
            httpResponse = client.execute(httpRequest);
        } catch (javax.net.ssl.SSLHandshakeException ex) {
            this.logger.error("Error sending matching request to ["+ targetURL +
                              "]: SSL handshake exception: [{}]", ex);
            return generateScriptErrorReply(ApiConfiguration.ERROR_NOT_SENT, "SSL handshake problem");
        } catch (Exception ex) {
            this.logger.error("Error sending matching request to [" + targetURL + "]: [{}]", ex);
            return generateScriptErrorReply(ApiConfiguration.ERROR_NOT_SENT, ex.getMessage());
        }

        try {
            Integer httpStatus   = (Integer) httpResponse.getStatusLine().getStatusCode();
            String stringReply   = EntityUtils.toString(httpResponse.getEntity());

            logger.error("Reply to matching request: STATUS: [{}], DATA: [{}]", httpStatus, stringReply);

            JSONObject replyJSON = getParsedJSON(stringReply);

            if (StringUtils.equalsIgnoreCase(request.getQueryType(), ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC) ||
                StringUtils.equalsIgnoreCase(request.getResponseType(), ApiConfiguration.REQUEST_RESPONSE_TYPE_ASYNCHRONOUS)) {
                // get query ID assigned by the remote server and saveto DB
                if (!replyJSON.has(ApiConfiguration.JSON_RESPONSE_ID)) {
                    this.logger.error("Can not store outgoing request: no queryID is provided by remote server");
                } else {
                    String queruID = replyJSON.getString(ApiConfiguration.JSON_RESPONSE_ID);
                    this.logger.error("Remote server assigned id to the submitted query: [{}]", queruID);
                    request.setQueryID(replyJSON.getString(ApiConfiguration.JSON_RESPONSE_ID));
                    requestStorageManager.saveOutgoingRequest(request);
                }
            }

            return generateScriptReply(httpStatus, replyJSON);
        } catch (Exception ex) {
            this.logger.error("Error processing matching request reply to [" + targetURL + "]: [{}]", ex);
            return generateScriptReply(ApiConfiguration.ERROR_INTERNAL, null);
        }
    }

    private JSONObject generateScriptErrorReply(Integer httpStatusCode, String errorMessage)
    {
        if (errorMessage == null) {
            return generateScriptReply(httpStatusCode, null);
        } else {
            JSONObject errorJSON = new JSONObject();
            errorJSON.put(ApiConfiguration.INTERNAL_JSON_ERROR_DESCRIPTION, errorMessage);
            return generateScriptReply(httpStatusCode, errorJSON);
        }
    }

    private JSONObject generateScriptReply(Integer httpStatusCode, JSONObject reply)
    {
        if (reply == null) {
            reply = new JSONObject();
        }
        reply.put(ApiConfiguration.INTERNAL_JSON_STATUS, httpStatusCode);
        return reply;
    }

    private JSONObject getParsedJSON(String stringJson)
    {
        JSONObject reply;
        try {
            reply = (stringJson == null) ? new JSONObject() : JSONObject.fromObject(stringJson);
        } catch (Exception ex) {
            reply = new JSONObject();
        }
        return reply;
    }

    public List<PatientSimilarityView> getSimilarityResults(Patient patient)
    {
        List<PatientSimilarityView> resultsList = new LinkedList<PatientSimilarityView>();

        /*
        Session session = this.sessionFactory.getSessionFactory().openSession();
        RequestHandlerInterface<OutgoingSearchRequest> requestHandler = new OutgoingRequestHandler(session);

        try {
            XWikiContext context = getContext();
            XWiki wiki = getWiki(context);
            XWikiDocument patientDoc = wiki.getDocument(patient.getDocument(), context);
            List<BaseObject> requestObjects = patientDoc.getXObjects(Configuration.REMOTE_REQUEST_REFERENCE);
            for (BaseObject requestObject : requestObjects) {
                String requestIdString = requestObject.getStringValue(Configuration.REMOTE_HIBERNATE_ID);
                if (StringUtils.isBlank(requestIdString)) {
                    return resultsList;
                }
                OutgoingSearchRequest request =
                    requestHandler.loadRequest(Long.valueOf(requestIdString), this.internalPatientService);

                List<PatientSimilarityView> allResults = request.getResults(this.viewFactory);
                resultsList.addAll(allResults);
            }
            return resultsList;
        } catch (Exception ex) {
            return resultsList;
        }*/

        return resultsList;
    }

    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    private XWiki getWiki(XWikiContext context)
    {
        return context.getWiki();
    }
}
