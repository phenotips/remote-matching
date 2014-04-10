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
import org.phenotips.remote.adapters.XWikiAdapter;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.RequestConfigurationInterface;
import org.phenotips.remote.hibernate.internal.OutgoingSearchRequest;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * TODO fix the doc
 */
public class OutgoingRequestConfigurator implements RequestConfigurationInterface
{
    private String baseURL;

    private String key;

    private Patient patient;

    private XWikiDocument patientDocument;

    private String submitterName;

    private String submitterEmail;

    public OutgoingRequestConfigurator(BaseObject requestObject, XWiki wiki, XWikiContext context) throws XWikiException
    {
        String patientId = requestObject.getStringValue("patientId");
        String submitterId = requestObject.getStringValue("submitterId");
        BaseObject submitter = XWikiAdapter.getSubmitter(submitterId, wiki, context);

        patientDocument = XWikiAdapter.getPatientDoc(patientId, wiki, context);
        patient = XWikiAdapter.getPatient(patientDocument);
        baseURL = requestObject.getStringValue(Configuration.REMOTE_BASE_URL_FIELD);
        key =
            XWikiAdapter.getRemoteConfiguration(baseURL, wiki, context).getStringValue(Configuration.REMOTE_KEY_FIELD);
        processSubmitter(submitter);
    }

    private void processSubmitter(BaseObject submitter)
    {
        submitterName = submitter.getStringValue("first_name") + " " + submitter.getStringValue("last_name");
        submitterEmail = submitter.getStringValue("email");
    }

    private Patient getPatient()
    {
        return patient;
    }

    private String getURL()
    {
        return baseURL + Configuration.REMOTE_URL_SEARCH_EXTENSION + key;
    }

    public OutgoingSearchRequestInterface createRequest()
    {
        OutgoingSearchRequestInterface request = new OutgoingSearchRequest();

        request.setReferencePatient(getPatient());
        request.setURL(getURL());
        request.setSubmitterName(submitterName);
        request.setSubmitterEmail(submitterEmail);
        request.setKey(key);

        return request;
    }

    @Override
    public Boolean saveRequest()
    {
        return null;
    }
}
