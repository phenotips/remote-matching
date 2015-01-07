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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.IncomingSearchRequest;

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.server.MatchingPatientsFinder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
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

    public QueueTaskAsyncAnswer(IncomingSearchRequest request, BaseObject configurationObject,
                                Logger logger, ApiDataConverter apiVersionSpecificConverter,
                                MatchingPatientsFinder patientsFinder)
    {
        this.request = request;
        this.configurationObject = configurationObject;
        this.logger = logger;
        this.apiVersionSpecificConverter = apiVersionSpecificConverter;
        this.patientsFinder = patientsFinder;
    }

    @Override
    public void run()
    {
        try {
            List<PatientSimilarityView> matches = patientsFinder.findMatchingPatients(this.request.getRemotePatient());

            Map<IncomingSearchRequest, List<PatientSimilarityView>> manyResults = new HashMap<IncomingSearchRequest, List<PatientSimilarityView>>();

            manyResults.put(this.request, matches);

            JSONObject replyJSON = apiVersionSpecificConverter.generateAsyncResult(manyResults);

            CloseableHttpClient client = HttpClients.createDefault();

            StringEntity jsonEntity = new StringEntity(replyJSON.toString(), ContentType.create("application/json", "UTF-8"));

            //String remoteServerId = this.request.getRemoteServerId();
            String baseURL = configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD);
            if (baseURL.charAt(baseURL.length() - 1) != '/') {
                baseURL += "/";
            }
            String targetURL = baseURL + ApiConfiguration.REMOTE_URL_ASYNCHRONOUS_RESULTS_ENDPOINT;

            logger.debug("Sending async reply to [" + targetURL + "]: " + replyJSON.toString());

            HttpPost httpRequest = new HttpPost(targetURL);
            httpRequest.setEntity(jsonEntity);
            client.execute(httpRequest);
        } catch (Exception ex) {
            // Do nothing
            logger.error("Error posting async response: [{}]", ex);
        }
    }
}
