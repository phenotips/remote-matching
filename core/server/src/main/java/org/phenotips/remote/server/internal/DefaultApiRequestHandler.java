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
package org.phenotips.remote.server.internal;

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.common.ApiFactory;
import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;
import org.phenotips.remote.server.ApiRequestHandler;
import org.phenotips.remote.server.SearchRequestProcessor;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Resource for listing full patient phenotype.
 *
 * @version $Id$
 */
@Singleton
@Component("org.phenotips.remote.server.internal.DefaultApiRequestHandler")
public class DefaultApiRequestHandler extends XWikiResource implements ApiRequestHandler
{
    @Inject
    private Logger logger;

    @Inject
    private SearchRequestProcessor searchRequestProcessor;

    @Inject
    private ApiFactory apiFactory;

    @Inject
    private RemoteConfigurationManager remoteConfigurationManager;

    @Override
    public Response matchPost(String json) throws XWikiRestException
    {
        this.logger.debug("MME MATCH REQUEST; INPUT JSON: [{}]", json);

        try {
            JSONObject jsonResponse;

            // XWiki boilerplate
            XWikiContext context = this.getXWikiContext();
            XWiki wiki = context.getWiki();
            XWikiDocument currentDoc = wiki.getDocument(ApplicationConfiguration.ABSOLUTE_DOCUMENT_REFERENCE, context);
            context.setDoc(currentDoc);

            HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

            String apiVersion = this.parseApiVersion(httpRequest.getHeader(ApiConfiguration.HTTPHEADER_API_VERSION));
            try {
                ApiDataConverter apiVersionSpecificConverter = this.apiFactory.getDataConverterForApiVersion(apiVersion);

                this.logger.debug("Request version: <<{}>>", apiVersion);

                String requestKey = httpRequest.getHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER);
                BaseObject remoteServerConfiguration = this.remoteConfigurationManager
                    .getRemoteConfigurationGivenRemoteIPAndToken(httpRequest.getRemoteAddr(), requestKey, context);

                if (remoteServerConfiguration == null) {
                    jsonResponse = new JSONObject();
                    jsonResponse.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, ApiConfiguration.HTTP_UNAUTHORIZED);
                    jsonResponse.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, "unauthorized server");
                } else {
                    String remoteServerId =
                        remoteServerConfiguration.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_ID);

                    jsonResponse = this.searchRequestProcessor.processHTTPSearchRequest(
                        apiVersionSpecificConverter, json, remoteServerId, httpRequest);
                }
            } catch (IllegalArgumentException ex) {
                this.logger.error("Incorrect incoming request: unsupported API version: [{}]", apiVersion);
                jsonResponse = new JSONObject();
                jsonResponse.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS,
                    ApiConfiguration.HTTP_UNSUPPORTED_API_VERSION);
                jsonResponse.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION,
                    "unsupported API version");
                jsonResponse.put(ApiConfiguration.REPLY_JSON_SUPPORTEDVERSIONS,
                    new JSONArray(this.apiFactory.getSupportedApiVersions()));
                apiVersion = ApiConfiguration.LATEST_API_VERSION_STRING;
            } catch (Exception ex) {
                jsonResponse = new JSONObject();
                jsonResponse.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS,
                    ApiConfiguration.HTTP_SERVER_ERROR);
                apiVersion = ApiConfiguration.LATEST_API_VERSION_STRING;
            }

            this.logger.debug("RESPONSE JSON: [{}]", jsonResponse.toString());

            Integer status = (Integer) jsonResponse.remove(ApiConfiguration.REPLY_JSON_HTTP_STATUS);
            if (status == null) {
                status = ApiConfiguration.HTTP_SERVER_ERROR;
            }

            ResponseBuilder response = Response.status(status);
            response.entity(jsonResponse.toString());
            response.type(this.generateContentType(apiVersion));
            return response.build();
        } catch (Exception ex) {
            this.logger.error("Could not process remote matching request: {}", ex.getMessage(), ex);
            return Response.status(ApiConfiguration.HTTP_SERVER_ERROR).build();
        }
    }

    private String parseApiVersion(String apiHeader)
    {
        String result = apiHeader.replaceAll("^" + Pattern.quote(ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX)
            + "(\\d+\\.\\d+)" + Pattern.quote(ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX) + "(.*)$", "$1");
        this.logger.debug("Request api version: [{}]", result);
        return result;
    }

    private String generateContentType(String apiVersion)
    {
        return ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX + apiVersion
            + ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX;
    }
}
