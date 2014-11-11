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

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.OutgoingSearchRequest;
//import org.phenotips.remote.hibernate.internal.DefaultOutgoingSearchRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * TODO fix the doc
 */
public class OutgoingRequestHandler
{
    /*
    private String baseURL;

    private String key;

    private Patient patient;

    private XWikiDocument patientDocument;

    private String submitterName;

    private String submitterEmail;

    private OutgoingSearchRequest request;

    private BaseObject xwikiRequestObject;

    private XWikiContext wikiContext;

    private XWiki wiki;

    private Session session;

    public OutgoingRequestHandler(BaseObject requestObject, XWiki wiki, XWikiContext context,
        DocumentReferenceResolver<String> resolver) throws XWikiException
    {
        this.xwikiRequestObject = requestObject;
        this.wikiContext = context;
        this.wiki = wiki;
        String patientId = requestObject.getStringValue("patientId");
        String submitterId = requestObject.getStringValue("submitterId");

        BaseObject submitter = XWikiAdapter.getSubmitter(submitterId, wiki, context, resolver);
        this.patientDocument = XWikiAdapter.getPatientDoc(patientId, wiki, context);
        this.patient = XWikiAdapter.getPatient(this.patientDocument);
        this.baseURL = requestObject.getStringValue(Configuration.CONFIGDOC_REMOTE_BASE_URL_FIELD).trim();
        this.key = XWikiAdapter.getRemoteConfiguration(this.baseURL, wiki, context).getStringValue(
            Configuration.CONFIGDOC_REMOTE_KEY_FIELD);
        processSubmitter(submitter);
    }

    // Little hack for updating a request object and not having to write another function into the interface
    public OutgoingRequestHandler(OutgoingSearchRequest existingRequest, JSONObject json)
    {
        this.request = existingRequest;
        try {
            existingRequest.setExternalId(JSONToMetadataConverter.externalResponseId(json));

            String responseType = null;
            try {
                responseType = JSONToMetadataConverter.responseType(json);
            } catch (JSONException ex) {
                // FIXME. This can also become a site for bugs.
                if (existingRequest.getResponseType() != null) {
                    // Let the function continue.
                } else {
                    // Assume default (on null)
                    responseType = Configuration.DEFAULT_NULL_REQUEST_RESPONSE_TYPE;
                }
            }
            if (responseType != null) {
                existingRequest.setResponseType(responseType);
            }

            //
            // TODO. Figure out if this check is actually needed. If more types of responses are included later on this
            // will become a site for bugs.
            //
            if (!StringUtils.equalsIgnoreCase(existingRequest.getResponseType(),
                Configuration.REQUEST_RESPONSE_TYPE_EMAIL)) {
                existingRequest.addResults(JSONToMetadataConverter.responseResults(json));
            }
        } catch (JSONException ex) {
            // Do nothing
        }
    }

    public OutgoingRequestHandler(Session session)
    {
        this.session = session;
    }

    private void processSubmitter(BaseObject submitter)
    {
        this.submitterName = submitter.getStringValue("first_name") + " " + submitter.getStringValue("last_name");
        this.submitterEmail = submitter.getStringValue("email");
    }

    private Patient getPhenoTipsPatient()
    {
        return this.patient;
    }

    @Override
    public OutgoingSearchRequest getRequest()
    {
        if (this.request != null) {
            return this.request;
        }
        this.request = new DefaultOutgoingSearchRequest();

        this.request.setReferencePatient(getPhenoTipsPatient());
        //this.request.setKey(this.key);
        this.request.setTargetURL(this.baseURL);
        this.request.setSubmitterName(this.submitterName);
        this.request.setSubmitterEmail(this.submitterEmail);
        this.request.setQueryType(Configuration.DEFAULT_REQUEST_QUERY_TYPE);

        return this.request;
    }

    @Override
    public Long saveRequest(Session session) throws Exception
    {
        Transaction t = session.beginTransaction();

        Long id;
        if (this.request.getRequestId() == null) {
            id = (Long) session.save(this.request);
            this.xwikiRequestObject.set(Configuration.REMOTE_HIBERNATE_ID, id, this.wikiContext);
            this.wiki.saveDocument(this.xwikiRequestObject.getOwnerDocument(), this.wikiContext);
        } else {
            session.saveOrUpdate(this.request);
            id = this.request.getRequestId();
        }

        t.commit();
        return id;
    }

    @Override
    public OutgoingSearchRequest loadRequest(Long id, PatientRepository internalService)
    {
        Transaction t = this.session.beginTransaction();
        OutgoingSearchRequest request = new DefaultOutgoingSearchRequest();
        this.session.load(request, id);
        t.commit();

        request.setReferencePatient(internalService.getPatientById(request.getReferencePatientId()));
        return request;
    }

    @Override
    public Boolean mail(XWikiContext context,
        MultiTypeWrapperInterface<IncomingSearchRequest, JSONObject> wrapper)
    {
        throw new UnsupportedOperationException();
    }*/
}
