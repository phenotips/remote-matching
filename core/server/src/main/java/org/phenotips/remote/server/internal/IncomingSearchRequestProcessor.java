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
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.api.WrapperInterface;
import org.phenotips.remote.server.RequestProcessorInterface;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

import net.sf.json.JSONObject;

/**
 * Takes a json string in the constructor and does all the request processing functionality.
 */
@Component
@Singleton
public class IncomingSearchRequestProcessor implements RequestProcessorInterface
{
    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Inject
    private Execution execution;

    @Inject
    @Named("json-patient")
    private WrapperInterface<JSONObject, HibernatePatientInterface> patientWrapper;

    @Inject
    @Named("json-meta")
    private WrapperInterface<JSONObject, IncomingSearchRequestInterface> metaWrapper;

    @Inject
    @Named("incoming-json")
    private WrapperInterface<IncomingSearchRequestInterface, JSONObject> requestWrapper;

    public JSONObject processHTTPRequest(String stringJson) throws Exception
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        //FIXME will break in virtual env.
        XWikiDocument fixDoc =
            context.getWiki().getDocument(new DocumentReference("xwiki", "Main", "WebHome"), context);
        context.setDoc(fixDoc);
        //FIXME. Should not be admin. Should use setUserReference(DocumentReference userReference);
        context.setUser("xwiki:XWiki.Admin");

        JSONObject json = JSONObject.fromObject(stringJson);
        Session session = this.sessionFactory.getSessionFactory().openSession();

        RequestHandlerInterface<IncomingSearchRequestInterface> requestHandler =
            new IncomingRequestHandler(json, patientWrapper, metaWrapper);
        IncomingSearchRequestInterface request = requestHandler.createRequest();
        if (!request.getHTTPStatus().equals(Configuration.HTTP_OK)) {
            return requestWrapper.wrap(request);
        }
        //For now all request are stored. However if for inline request it is not necessary to get a unique id,
        //that should be changed.
        requestHandler.saveRequest(session);

        return requestWrapper.wrap(request);
    }
}
