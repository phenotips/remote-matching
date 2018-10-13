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
package org.phenotips.remote.metrics.internal;

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;
import org.phenotips.remote.metrics.MetricsRequestHandler;
import org.phenotips.remote.metrics.spi.MetricProvider;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Resource for implementing the MME API of metrics endpoint.
 *
 * @version $Id$
 */
@Singleton
@Component("org.phenotips.remote.metrics.internal.DefaultMetricsRequestHandler")
public class DefaultMetricsRequestHandler extends XWikiResource implements MetricsRequestHandler
{
    @Inject
    private Logger logger;

    @Inject
    private RemoteConfigurationManager remoteConfigurationManager;

    @Inject
    private Map<String, MetricProvider> providers;

    @Override
    public Response getMetrics()
    {
        try {
            JSONObject jsonResponse = new JSONObject();

            // XWiki boilerplate
            XWikiContext context = this.getXWikiContext();
            XWiki wiki = context.getWiki();
            XWikiDocument currentDoc = wiki.getDocument(ApplicationConfiguration.ABSOLUTE_DOCUMENT_REFERENCE, context);
            context.setDoc(currentDoc);

            HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

            String requestKey = httpRequest.getHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER);
            BaseObject remoteServerConfiguration = this.remoteConfigurationManager
                .getRemoteConfigurationGivenRemoteIPAndToken(httpRequest.getRemoteAddr(), requestKey, context);

            if (remoteServerConfiguration == null) {
                jsonResponse.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, ApiConfiguration.HTTP_UNAUTHORIZED);
                jsonResponse.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, "unauthorized server");
            } else {
                JSONObject metrics = new JSONObject();
                for (Map.Entry<String, MetricProvider> provider : this.providers.entrySet()) {
                    try {
                        metrics.putOpt(provider.getKey(), provider.getValue().compute());
                    } catch (Exception ex) {
                        this.logger.error("Error computing {} for MME metrics: {}", provider.getKey(), ex.getMessage(),
                            ex);
                    }
                }
                jsonResponse.put("metrics", metrics);
            }
            this.logger.debug("RESPONSE JSON: [{}]", jsonResponse.toString());

            Integer status = (Integer) jsonResponse.remove(ApiConfiguration.REPLY_JSON_HTTP_STATUS);
            if (status == null) {
                status = ApiConfiguration.HTTP_OK;
            }

            ResponseBuilder response = Response.status(status);
            response.entity(jsonResponse.toString());
            response.type(MediaType.APPLICATION_JSON_TYPE);
            return response.build();
        } catch (Exception ex) {
            this.logger.error("Could not process remote metrics request: {}", ex.getMessage(), ex);
            return Response.status(ApiConfiguration.HTTP_SERVER_ERROR).build();
        }
    }
}
