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
package org.phenotips.remote.adapters.jsonwrappers;

import org.phenotips.data.Patient;
import org.phenotips.remote.adapters.PatientToJSONConverter;
import org.phenotips.remote.adapters.XWikiAdapter;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.api.WrapperInterface;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

import net.sf.json.JSONObject;

/**
 * Unfortunately because JSONObject is final, this class, unlike all the other wrappers cannot extend the object.
 * Therefore, breaking the existing pattern it uses {@link #wrap} method and returns JSONObject.
 */
public class OutgoingSearchRequestToJSONWrapper implements WrapperInterface<OutgoingSearchRequestInterface, JSONObject>
{
    XWikiContext context;

    XWiki wiki;

    public OutgoingSearchRequestToJSONWrapper(XWiki wiki, XWikiContext context)
    {
        this.wiki = wiki;
        this.context = context;
    }

    public JSONObject wrap(OutgoingSearchRequestInterface request)
    {
        JSONObject json = new JSONObject();

        Patient reference = null;
        try {
            reference = request.getReferencePatient();
        } catch (NullPointerException ex) {
            //FIXME. The second catch can lead to bugs, but it should not.
            try {
                reference = XWikiAdapter.getPatient(request.getReferencePatientId(), wiki, context);
            } catch (XWikiException wEx) {
                //Should not happen. If the id of the patient does not exist, an error should have been thrown before
                //this code is executed.
            }
        }
        if (reference == null) {
            return json;
        }
        JSONObject submitter = new JSONObject();
        submitter.put("name", request.getSubmitterName());
        submitter.put("email", request.getSubmitterEmail());

        json.put("id", request.getRequestId());
        json.put("queryType", request.getQueryType());
        json.put("submitter", submitter);
        json.put("gender", PatientToJSONConverter.gender(reference));
        json.putAll(PatientToJSONConverter.globalQualifiers(reference));
        json.put("disorders", PatientToJSONConverter.disorders(reference));
        json.put("features", PatientToJSONConverter.features(reference));

        return json;
    }
}
