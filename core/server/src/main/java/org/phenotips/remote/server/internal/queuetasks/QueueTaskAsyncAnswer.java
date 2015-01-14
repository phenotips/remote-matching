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
package org.phenotips.remote.server.internal.queuetasks;

//import org.phenotips.remote.RemoteMatchingClient;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.hibernate.internal.DefaultIncomingSearchRequest;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.server.MatchingPatientsFinder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;

import com.xpn.xwiki.objects.BaseObject;
import org.slf4j.Logger;
import net.sf.json.JSONObject;

public class QueueTaskAsyncAnswer implements Runnable
{
    private IncomingSearchRequest request;

    private BaseObject configurationObject;

    private ApiDataConverter apiVersionSpecificConverter;

    private MatchingPatientsFinder patientsFinder;

    private Logger logger;

    private RemoteMatchingStorageManager requestStorageManager;

    public QueueTaskAsyncAnswer(IncomingSearchRequest request, BaseObject configurationObject,
                                Logger logger, ApiDataConverter apiVersionSpecificConverter,
                                MatchingPatientsFinder patientsFinder,
                                RemoteMatchingStorageManager storageManager)
    {
        this.request = request;
        this.configurationObject = configurationObject;
        this.logger = logger;
        this.apiVersionSpecificConverter = apiVersionSpecificConverter;
        this.patientsFinder = patientsFinder;
        this.requestStorageManager = storageManager;
    }

    @Override
    public void run()
    {
        try {
            List<PatientSimilarityView> matches =
                this.patientsFinder.findMatchingPatients(this.request.getRemotePatient());

            Map<IncomingSearchRequest, List<PatientSimilarityView>> manyResults =
                new HashMap<IncomingSearchRequest, List<PatientSimilarityView>>();

            manyResults.put(this.request, matches);

            JSONObject replyJSON = this.apiVersionSpecificConverter.generateAsyncResult(manyResults);

            CloseableHttpClient client = HttpClients.createDefault();

            StringEntity jsonEntity =
                new StringEntity(replyJSON.toString(), ContentType.create("application/json", "UTF-8"));

            //String remoteServerId = this.request.getRemoteServerId();
            String key =
                configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_KEY_FIELD);
            String baseURL =
                configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD);
            if (baseURL.charAt(baseURL.length() - 1) != '/') {
                baseURL += "/";
            }
            String targetURL = baseURL + ApiConfiguration.REMOTE_URL_ASYNCHRONOUS_RESULTS_ENDPOINT;

            this.logger.error("Sending async results to [" + targetURL + "]: " + replyJSON.toString());

            HttpPost httpRequest = new HttpPost(targetURL);
            httpRequest.setEntity(jsonEntity);
            httpRequest.setHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER, key);
            client.execute(httpRequest);

            if (!StringUtils.equalsIgnoreCase(request.getQueryType(),
                ApiConfiguration.REQUEST_QUERY_TYPE_PERIODIC)) {
                // remove request form the database if it was a one-tyme async response (as opposed to periodic)
                this.logger.debug("Removing one-tyme async request from the database (queryID: [{}])",
                                  request.getQueryId());
                this.requestStorageManager.deleteIncomingPeriodicRequest(request.getQueryId());
            } else {
                // update last results time
                ((DefaultIncomingSearchRequest)request).setLastResultsTimeToNow();
                this.requestStorageManager.updateIncomingPeriodicRequest(request);
            }
        } catch (Exception ex) {
            this.logger.error("Error posting async response: [{}]", ex);
        }
    }
}
