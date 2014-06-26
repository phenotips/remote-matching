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

import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.server.MatchInterface;
import org.phenotips.remote.server.RequestProcessorInterface;
import org.phenotips.remote.server.internal.queuetasks.ContextSetter;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import net.sf.json.JSONObject;

/**
 * Resource for listing full patient phenotype.
 *
 * @version $Id$
 */
@Component("org.phenotips.remote.server.internal.MatchResource")
public class MatchResource extends XWikiResource implements MatchInterface
{
    @Inject
    RequestProcessorInterface requestProcessor;

    @Override
    public Response matchPost(String json) throws XWikiRestException, XWikiException
    {
        JSONObject jsonResponse;
        XWikiContext context = this.getXWikiContext();
        HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

        try {
            ContextSetter.set(context);

            //Using futures to queue tasks and to retrieve results.
            ExecutorService queue = Executors.newSingleThreadExecutor();
            jsonResponse = requestProcessor.processHTTPRequest(json, queue, httpRequest);
        } catch (Exception e) {
            return Response.status(Configuration.HTTP_SERVER_ERROR).build();
        }
        Integer status = (Integer) jsonResponse.remove("status");
        if (status.equals(Configuration.HTTP_OK)) {
            return Response.ok(jsonResponse.toString(), MediaType.APPLICATION_JSON).build();
        } else {
            return Response.status(status).build();
        }
    }
}
