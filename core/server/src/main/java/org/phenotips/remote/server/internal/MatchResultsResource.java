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

import org.phenotips.data.PatientRepository;
import org.phenotips.remote.adapters.JSONToMetadataConverter;
import org.phenotips.remote.adapters.internal.OutgoingRequestHandler;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.server.MatchResultsInterface;
import org.phenotips.remote.server.internal.queuetasks.ContextSetter;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.hibernate.Session;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Resource for receiving asynchronous answers from remote search servers.
 *
 * @version $Id$
 */
@Component("org.phenotips.remote.server.internal.MatchResultsResource")
public class MatchResultsResource extends XWikiResource implements MatchResultsInterface
{
    // @Inject
    // RequestProcessorInterface requestProcessor;
    @Inject
    private HibernateSessionFactory sessionFactory;

    /** Wrapped trusted API, doing the actual work. */
    @Inject
    private PatientRepository internalPatientService;

    @Override
    public Response matchResultsPost(String json) throws XWikiRestException, XWikiException
    {
        try {
            XWikiContext context = this.getXWikiContext();
            JSONArray jsonResponse;

            // FIXME. Should the response here be verified? Well, yes, but how pressing is it?
            // HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();
            Session session = this.sessionFactory.getSessionFactory().openSession();
            ContextSetter.set(context);

            jsonResponse = JSONArray.fromObject(json);
            RequestHandlerInterface<OutgoingSearchRequestInterface> requestHandler =
                new OutgoingRequestHandler(session);
            for (Object responseUC : jsonResponse) {
                try {
                    JSONObject response = (JSONObject) responseUC;
                    String id = JSONToMetadataConverter.externalResponseId(response);
                    // FIXME
                    OutgoingSearchRequestInterface request =
                        requestHandler.loadRequest(Long.parseLong(id), this.internalPatientService);
                    RequestHandlerInterface<OutgoingSearchRequestInterface> handler =
                        new OutgoingRequestHandler(request, response);
                    handler.saveRequest(session);
                } catch (Exception ex) {
                    // FIXME. Nothing to do for now. Probably should be logged.
                }
            }
            return Response.status(Configuration.HTTP_OK).build();
        } catch (Exception e) {
            return Response.status(Configuration.HTTP_SERVER_ERROR).build();
        }
    }
}
