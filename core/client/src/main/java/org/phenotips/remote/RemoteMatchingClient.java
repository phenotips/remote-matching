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
package org.phenotips.remote;

import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.WrapperInterface;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import net.sf.json.JSONObject;

/**
 * Sends requests to remote servers supporting the specifications. Does not handle data in the responses.
 *
 * FIXME Should there be an API for this?
 */
public class RemoteMatchingClient
{
    public static String sendRequest(OutgoingSearchRequestInterface request,
        WrapperInterface<OutgoingSearchRequestInterface, JSONObject> wrapper) throws Exception
    {
        JSONObject json = wrapper.wrap(request);
        CloseableHttpClient client = HttpClients.createDefault();

        StringEntity jsonEntity = new StringEntity(json.toString(), ContentType.create("application/json", "UTF-8"));

        HttpPost httpRequest = new HttpPost(request.getTargetURL());
        httpRequest.setEntity(jsonEntity);

        CloseableHttpResponse httpResponse = client.execute(httpRequest);
        return EntityUtils.toString(httpResponse.getEntity());
    }
}
