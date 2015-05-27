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
import org.phenotips.remote.common.internal.XWikiAdapter;
import org.phenotips.remote.server.ApiRequestHandler;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
    SearchRequestProcessor searchRequestProcessor;

    @Inject
    ApiFactory apiFactory;

    @Override
    public Response matchPost(String json) throws XWikiRestException
    {
        this.logger.error("PROCESS MME MATCH REQUEDST");
        this.logger.error("INPUT JSON: [{}]", json);

        try {
            JSONObject jsonResponse;

            // XWiki boilerplate
            XWikiContext context = this.getXWikiContext();
            XWiki wiki = context.getWiki();
            XWikiDocument currentDoc = wiki.getDocument(ApplicationConfiguration.ABSOLUTE_DOCUMENT_REFERENCE, context);
            context.setDoc(currentDoc);

            HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

            String apiVersion = this.parseApiVersion(httpRequest.getHeader(ApiConfiguration.HTTPHEADER_API_VERSION));
            ApiDataConverter apiVersionSpecificConverter;
            try {
                apiVersionSpecificConverter = this.apiFactory.getApiVersion(apiVersion);

                this.logger.debug("Request version: <<{}>>", apiVersion);

                if (!isRequestAuthorized(httpRequest, context)) {
                    jsonResponse = new JSONObject();
                    jsonResponse.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS, ApiConfiguration.HTTP_UNAUTHORIZED);
                    jsonResponse.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION, "unauthorized server");
                } else {
                    // Using futures to queue tasks and to retrieve results.
                    ExecutorService queue = Executors.newSingleThreadExecutor();
                    jsonResponse = this.searchRequestProcessor.processHTTPSearchRequest(
                                   apiVersionSpecificConverter, json, queue, httpRequest);
                }
            } catch (Exception ex) {
                this.logger.error("Incorrect incoming request: unsupported API version: [{}]", apiVersion);
                jsonResponse = new JSONObject();
                jsonResponse.put(ApiConfiguration.REPLY_JSON_HTTP_STATUS,
                                 ApiConfiguration.HTTP_UNSUPPORTED_API_VERSION);
                jsonResponse.put(ApiConfiguration.REPLY_JSON_ERROR_DESCRIPTION,
                                 "unsupported API version");
                jsonResponse.put(ApiConfiguration.REPLY_JSON_SUPPORTEDVERSIONS,
                                 JSONArray.fromObject(this.apiFactory.getSupportedApiVersions()));
                apiVersion = ApiConfiguration.LATEST_API_VERSION_STRING;
            }

            this.logger.error("RESPONSE JSON: [{}]", jsonResponse.toString());

            Integer status = (Integer) jsonResponse.remove(ApiConfiguration.REPLY_JSON_HTTP_STATUS);
            if (status == null) {
                status = ApiConfiguration.HTTP_SERVER_ERROR;
            }

            ResponseBuilder response = Response.status(status);
            response.entity(jsonResponse.toString());
            response.type(this.generateContentType(apiVersion));
            return response.build();
        } catch (Exception ex) {
            Logger logger = LoggerFactory.getLogger(DefaultApiRequestHandler.class);
            logger.error("Could not process remote matching request: {}", ex.getMessage(), ex);
            return Response.status(ApiConfiguration.HTTP_SERVER_ERROR).build();
        }
    }

    private boolean isRequestAuthorized(HttpServletRequest httpRequest, XWikiContext context)
    {
        BaseObject configurationObject =
            XWikiAdapter.getRemoteConfigurationGivenRemoteIP(httpRequest.getRemoteAddr(), context);
        if (configurationObject == null) {
            return false; // this server is not listed as an accepted server, and has no key
        }
        String requestKey = httpRequest.getHeader(ApiConfiguration.HTTPHEADER_KEY_PARAMETER);
        String configuredKey =
            configurationObject.getStringValue(ApplicationConfiguration.CONFIGDOC_LOCAL_KEY_FIELD);
        logger.error("Remote server key validation: Key: {}, Configured: {}", requestKey, configuredKey);
        if (requestKey == null || configuredKey == null || !requestKey.equals(configuredKey)) {
            return false;
        }
        return true;
    }

    private String parseApiVersion(String apiHeader)
    {
        String result = apiHeader.replaceAll("^" + Pattern.quote(ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX) +
            "(\\d+\\.\\d+)" + Pattern.quote(ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX) + "(.*)$", "$1");
        logger.error("Request api version: [{}]", result);
        return result;
    }

    private String generateContentType(String apiVersion)
    {
        return ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX + apiVersion +
            ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX;
    }
}
