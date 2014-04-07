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
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.RequestConfigurationInterface;
import org.phenotips.remote.hibernate.internal.OutgoingSearchRequest;

import org.xwiki.context.Execution;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;

/**
 * TODO fix the doc
 */
public class RequestConfiguration implements RequestConfigurationInterface
{
    private String url;

    private String key = "THE_KEY";

    private XWikiContext context;

    private Patient patient;

    public RequestConfiguration(BaseObject requestObject, Execution execution) throws XWikiException
    {
        context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        XWiki wiki = context.getWiki();

        String patientId = requestObject.getStringValue("patientId");
        String submitterId = requestObject.getStringValue("submitterId");

        patient = XWikiAdapter.getPatient(patientId, wiki, context);
        BaseObject submitter = XWikiAdapter.getSubmitter(submitterId, wiki, context);
        url = requestObject.getStringValue("baseUrl");
    }

    public Patient getPatient()
    {
        return patient;
    }

    public String getURL()
    {
        return url+"/match?media=json&key="+key;
    }

    public OutgoingSearchRequestInterface createRequest()
    {
        OutgoingSearchRequestInterface request = new OutgoingSearchRequest();

        request.setReferencePatient(getPatient());
        request.setURL(getURL());

        return request;
    }
}
