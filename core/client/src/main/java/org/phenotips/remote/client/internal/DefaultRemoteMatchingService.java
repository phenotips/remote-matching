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
package org.phenotips.remote.client.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.ApiViolationException;
//import org.phenotips.remote.api.OutgoingSearchRequest;
import org.phenotips.remote.client.RemoteMatchingService;
import org.phenotips.remote.common.ApiFactory;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.phenotips.remote.hibernate.internal.DefaultOutgoingSearchRequest;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

@Unstable
@Component
@Singleton
public class DefaultRemoteMatchingService implements RemoteMatchingService
{
    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    ApiFactory apiFactory;

    @Inject
    private RemoteMatchingStorageManager requestStorageManager;

    @Override
    public JSONObject generateRequestJSON(String patientId, String remoteServerId, int addTopNGenes)
        throws ApiViolationException
    {
        // TODO: for now always use the latest supported version for the request
        ApiDataConverter apiVersionSpecificConverter =
            this.apiFactory.getApiVersion(ApiConfiguration.LATEST_API_VERSION_STRING);

        // TODO: remove this step form here and only create one when storing to the DB
        DefaultOutgoingSearchRequest request = new DefaultOutgoingSearchRequest(patientId, remoteServerId);

        JSONObject requestJSON = apiVersionSpecificConverter.getOutgoingRequestToJSONConverter().toJSON(request, addTopNGenes);

        return requestJSON;
    }

    @Override
    public JSONObject sendRequest(String patientId, String remoteServerId, int addTopNGenes)
    {
        XWikiContext context = this.getContext();

        BaseObject configurationObject = XWikiAdapter.getRemoteConfigurationGivenRemoteName(remoteServerId, context);

        if (configurationObject == null) {
            logger.error("Requested matching server is not configured: [{}]", remoteServerId);
            return generateErrorReply(ApiConfiguration.ERROR_NOT_SENT,
                                      "requested matching server ["+remoteServerId+"] is not configured");
        }

        JSONObject requestJSON;
        try {
            requestJSON = this.generateRequestJSON(patientId, remoteServerId, addTopNGenes);
        } catch (ApiViolationException ex) {
            return generateErrorReply(ApiConfiguration.ERROR_NOT_SENT, ex.getMessage());
        }
        if (requestJSON == null) {
            this.logger.error("Unable to convert patient to JSON: [{}]", patientId);
            return generateErrorReply(ApiConfiguration.ERROR_NOT_SENT,
                                      "unable to convert patient with ID ["+patientId.toString()+"] to JSON");
        }

        CloseableHttpClient client = HttpClients.createDefault();

        StringEntity jsonEntity = new StringEntity(requestJSON.toString(), ContentType.create("application/json", "utf-8"));

        // TODO: hack to make charset lower-cased so that GeneMatcher accepts it
        //jsonEntity.setContentType("application/json; charset=utf-8");
        jsonEntity.setContentType(ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX +
                                  ApiConfiguration.LATEST_API_VERSION_STRING +
                                  ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX + "; charset=utf-8");
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
            httpRequest.setHeader(ApiConfiguration.HTTPHEADER_API_VERSION, ApiConfiguration.LATEST_API_VERSION_STRING);
            httpResponse = client.execute(httpRequest);
        } catch (javax.net.ssl.SSLHandshakeException ex) {
            this.logger.error("Error sending matching request to ["+ targetURL +
                              "]: SSL handshake exception: [{}]", ex);
            return generateErrorReply(ApiConfiguration.ERROR_NOT_SENT, "SSL handshake problem");
        } catch (Exception ex) {
            this.logger.error("Error sending matching request to [" + targetURL + "]: [{}]", ex);
            return generateErrorReply(ApiConfiguration.ERROR_NOT_SENT, ex.getMessage());
        }

        try {
            Integer httpStatus   = (Integer) httpResponse.getStatusLine().getStatusCode();
            String stringReply   = EntityUtils.toString(httpResponse.getEntity());

            logger.error("Reply to matching request: STATUS: [{}], DATA: [{}]", httpStatus, stringReply);

            JSONObject replyJSON = getParsedJSON(stringReply);

            //TODO: store for audit and caching purposes
            //requestStorageManager.saveOutgoingRequest(request);

            return generateReply(httpStatus, replyJSON);
        } catch (Exception ex) {
            this.logger.error("Error processing matching request reply to [" + targetURL + "]: [{}]", ex);
            return generateReply(ApiConfiguration.ERROR_INTERNAL, null);
        }
    }

    private JSONObject generateErrorReply(Integer httpStatusCode, String errorMessage)
    {
        if (errorMessage == null) {
            return generateReply(httpStatusCode, null);
        } else {
            JSONObject errorJSON = new JSONObject();
            errorJSON.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, errorMessage);
            return generateReply(httpStatusCode, errorJSON);
        }
    }

    private JSONObject generateReply(Integer httpStatusCode, JSONObject reply)
    {
        if (reply == null) {
            reply = new JSONObject();
        }
        reply.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, httpStatusCode);
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

    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    @Override
    public List<PatientSimilarityView> getSimilarityResults(String patientId, String remoteServerId)
    {
        List<PatientSimilarityView> resultsList = new LinkedList<PatientSimilarityView>();

        // TODO: update for new storage scheme
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

    @Override
    public Map<String,List<PatientSimilarityView>> getAllSimilarityResults(String patientId)
    {
        return null;
    }
}
