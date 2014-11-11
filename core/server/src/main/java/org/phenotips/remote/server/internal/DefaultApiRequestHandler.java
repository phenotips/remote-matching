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
package org.phenotips.remote.server.internal;

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.server.ApiRequestHandler;
import org.phenotips.remote.server.SearchRequestProcessor;
import org.phenotips.remote.server.AsyncResponseProcessor;
import org.phenotips.remote.server.internal.queuetasks.ContextSetter;
import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
//import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import net.sf.json.JSONObject;

/**
 * Resource for listing full patient phenotype.
 *
 * @version $Id$
 */
//@Singleton
@Component("org.phenotips.remote.server.internal.DefaultApiRequestHandler")
public class DefaultApiRequestHandler extends XWikiResource implements ApiRequestHandler
{
    @Inject
    private Logger logger;

    @Inject
    SearchRequestProcessor searchRequestProcessor;

    @Inject
    AsyncResponseProcessor asyncReponseProcessor;

    @Override
    public Response matchPost(String json, String apiVersion) throws XWikiRestException, XWikiException
    {
        logger.error("PROCESS MATCH for version [{}]", apiVersion);
        logger.error("INPUT JSON: [{}]", json);

        try {
            JSONObject jsonResponse;
            XWikiContext context = this.getXWikiContext();
            HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

            ContextSetter.set(context);
            // Using futures to queue tasks and to retrieve results.
            ExecutorService queue = Executors.newSingleThreadExecutor();
            jsonResponse = this.searchRequestProcessor.processHTTPSearchRequest(apiVersion, json, queue, httpRequest);

            Integer status = (Integer) jsonResponse.remove("status");
            if (status == null) {
                status = ApiConfiguration.HTTP_SERVER_ERROR;
            }
            if (status.equals(ApiConfiguration.HTTP_OK)) {
                return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
            } else {
                return Response.status(status).build();
            }
        } catch (Exception ex) {
            Logger logger = LoggerFactory.getLogger(DefaultApiRequestHandler.class);
            logger.error("Could not process remote matching request: {}", ex.getMessage(), ex);
            return Response.status(ApiConfiguration.HTTP_SERVER_ERROR).build();
        }
    }

    @Override
    public Response matchResultsPost(String json, String apiVersion) throws XWikiRestException, XWikiException
    {
        logger.error("PROCESS MATCHRESULTS for version [{}]", apiVersion);
        logger.error("INPUT JSON: [{}]", json);

        try {
            JSONObject jsonResponse = new JSONObject();
            XWikiContext context = this.getXWikiContext();
            HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

            ContextSetter.set(context);
            Integer status = this.asyncReponseProcessor.processHTTPAsyncResponse(apiVersion, json, httpRequest);

            if (status.equals(ApiConfiguration.HTTP_OK)) {
                return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
            } else {
                return Response.status(status).build();
            }
        } catch (Exception ex) {
            Logger logger = LoggerFactory.getLogger(DefaultApiRequestHandler.class);
            logger.error("Could not process remote async response: {}", ex.getMessage(), ex);
            return Response.status(ApiConfiguration.HTTP_SERVER_ERROR).build();
        }
    }
}
