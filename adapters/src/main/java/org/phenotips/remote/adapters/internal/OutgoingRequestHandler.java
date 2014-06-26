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
package org.phenotips.remote.adapters.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.remote.adapters.JSONToMetadataConverter;
import org.phenotips.remote.adapters.XWikiAdapter;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.MultiTypeWrapperInterface;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.hibernate.internal.OutgoingSearchRequest;

import org.xwiki.model.reference.DocumentReferenceResolver;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * TODO fix the doc
 */
public class OutgoingRequestHandler implements RequestHandlerInterface<OutgoingSearchRequestInterface>
{
    private Logger logger;

    private String baseURL;

    private String key;

    private Patient patient;

    private XWikiDocument patientDocument;

    private String submitterName;

    private String submitterEmail;

    private OutgoingSearchRequestInterface request;

    private BaseObject xwikiRequestObject;

    private XWikiContext wikiContext;

    private XWiki wiki;

    private Session session;

    public OutgoingRequestHandler(BaseObject requestObject, XWiki wiki, XWikiContext context,
        DocumentReferenceResolver<String> resolver, Logger logger) throws XWikiException
    {
        this.logger = logger;
        xwikiRequestObject = requestObject;
        wikiContext = context;
        this.wiki = wiki;
        String patientId = requestObject.getStringValue("patientId");
        String submitterId = requestObject.getStringValue("submitterId");

        BaseObject submitter = XWikiAdapter.getSubmitter(submitterId, wiki, context, resolver);
        patientDocument = XWikiAdapter.getPatientDoc(patientId, wiki, context);
        patient = XWikiAdapter.getPatient(patientDocument);
        baseURL = requestObject.getStringValue(Configuration.REMOTE_BASE_URL_FIELD).trim();
        key =
            XWikiAdapter.getRemoteConfiguration(baseURL, wiki, context, logger).getStringValue(Configuration.REMOTE_KEY_FIELD);
        processSubmitter(submitter);
    }

    /** Little hack for updating a request object and not having to write another function into the interface */
    public OutgoingRequestHandler(OutgoingSearchRequestInterface existingRequest, JSONObject json)
    {
        request = existingRequest;
        try {
            existingRequest.setExternalId(JSONToMetadataConverter.externalResponseId(json));

            String responseType = null;
            try {
                responseType = JSONToMetadataConverter.responseType(json);
            } catch (JSONException ex) {
                //FIXME. This can also become a site for bugs.
                if (existingRequest.getResponseType() != null) {
                    //Let the function continue.
                } else {
                    //Assume default (on null)
                    responseType = Configuration.DEFAULT_NULL_REQUEST_RESPONSE_TYPE;
                }
            }
            if (responseType != null) {
                existingRequest.setResponseType(responseType);
            }

            /* TODO. Figure out if this check is actually needed.
            If more types of responses are included later on this will become a site for bugs. */
            if (!StringUtils
                .equalsIgnoreCase(existingRequest.getResponseType(), Configuration.REQUEST_RESPONSE_TYPE_EMAIL))
            {
                existingRequest.addResults(JSONToMetadataConverter.responseResults(json));
            }
        } catch (JSONException ex) {
            //Do nothing
        }
    }

    public OutgoingRequestHandler(Session session)
    {
        this.session = session;
    }

    private void processSubmitter(BaseObject submitter)
    {
        submitterName = submitter.getStringValue("first_name") + " " + submitter.getStringValue("last_name");
        submitterEmail = submitter.getStringValue("email");
    }

    private Patient getPhenoTipsPatient()
    {
        return patient;
    }

    public OutgoingSearchRequestInterface getRequest()
    {
        if (request != null) {
            return request;
        }
        request = new OutgoingSearchRequest();

        request.setReferencePatient(getPhenoTipsPatient());
        request.setKey(key);
        request.setTargetURL(baseURL);
        request.setSubmitterName(submitterName);
        request.setSubmitterEmail(submitterEmail);
        request.setQueryType(Configuration.DEFAULT_REQUEST_QUERY_TYPE);

        return request;
    }

    @Override
    public Long saveRequest(Session session) throws Exception
    {
        Transaction t = session.beginTransaction();

        Long id;
        if (request.getRequestId() == null) {
            id = (Long) session.save(request);
            xwikiRequestObject.set(Configuration.REMOTE_HIBERNATE_ID, id, wikiContext);
            wiki.saveDocument(xwikiRequestObject.getOwnerDocument(), wikiContext);
        } else {
            session.saveOrUpdate(request);
            id = request.getRequestId();
        }

        t.commit();
        return id;
    }

    @Override
    public OutgoingSearchRequestInterface loadRequest(Long id, PatientRepository internalService)
    {
        Transaction t = session.beginTransaction();
        OutgoingSearchRequestInterface request = new OutgoingSearchRequest();
        session.load(request, id);
        t.commit();

        request.setReferencePatient(internalService.getPatientById(request.getReferencePatientId()));
        return request;
    }

    @Override
    public Boolean mail(XWikiContext context,
        MultiTypeWrapperInterface<IncomingSearchRequestInterface, JSONObject> wrapper)
    {
        throw new NotImplementedException();
    }
}
