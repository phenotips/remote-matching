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
package org.phenotips.remote.common.internal.api;

import org.phenotips.remote.api.tojson.OutgoingRequestToJSONConverter;
import org.phenotips.remote.api.OutgoingSearchRequest;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;
import org.phenotips.remote.common.internal.api.DefaultPatientToJSONConverter;

import org.slf4j.Logger;

import net.sf.json.JSONObject;

public class DefaultOutgoingRequestToJSONConverter implements OutgoingRequestToJSONConverter
{
    private Logger logger;

    private PatientToJSONConverter patientToJSONConverter;

    private final String apiVersion;

    public DefaultOutgoingRequestToJSONConverter(String apiVersion, Logger logger)
    {
        this.apiVersion = apiVersion;
        this.logger     = logger;

        this.patientToJSONConverter = new DefaultPatientToJSONConverter(apiVersion, logger);
    }

    @Override
    public JSONObject toJSON(OutgoingSearchRequest request, String remoteServerId)
    {
        Long patientId = request.getReferencePatientId();

        JSONObject json = new JSONObject();
        /*
        Patient reference = null;
        try {
            reference = request.getReferencePatient();
        } catch (NullPointerException ex) {
            // FIXME. The second catch can lead to bugs, but it should not.
            try {
                reference = XWikiAdapter.getPatient(request.getReferencePatientId(), this.wiki, this.context);
            } catch (XWikiException wEx) {
                // Should not happen. If the id of the patient does not exist, an error should have been thrown before
                // this code is executed.
            }
        }
        if (reference == null) {
            return json;
        }
        PatientToJSONWrapper patientWrapper = new PatientToJSONWrapper();

        JSONObject submitter = new JSONObject();
        submitter.put("name", request.getSubmitterName());
        submitter.put("email", request.getSubmitterEmail());

        json.put("id", request.getRequestId());
        json.put("queryType", request.getQueryType());
        json.put("submitter", submitter);
        json.putAll(patientWrapper.wrap(reference));
        */
        return json;
    }
}
