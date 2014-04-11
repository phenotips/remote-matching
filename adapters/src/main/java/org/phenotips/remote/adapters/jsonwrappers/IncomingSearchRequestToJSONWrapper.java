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
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.WrapperInterface;
import org.phenotips.similarity.SimilarPatientsFinder;

import org.xwiki.component.annotation.Component;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Unfortunately because JSONObject is final, this class, unlike all the other wrappers cannot extend the object.
 * Therefore, breaking the existing pattern it uses {@link #wrap} method and returns JSONObject.
 */
@Component
@Named("incoming-json")
public class IncomingSearchRequestToJSONWrapper implements WrapperInterface<IncomingSearchRequestInterface, JSONObject>
{
    @Inject
    private SimilarPatientsFinder patientsFinder;

    public JSONObject wrap(IncomingSearchRequestInterface request)
    {
        JSONObject json = new JSONObject();
        JSONArray results = new JSONArray();

        Integer status = request.getHTTPStatus();
        json.put(Configuration.INTERNAL_JSON_STATUS, status);
        if(!status.equals(Configuration.HTTP_OK)) {
            return json;
        }

        List<PatientSimilarityView> similarPatients;
        try {
            similarPatients = request.getResults(patientsFinder);
        } catch (IllegalArgumentException ex) {
            return this.wrap(request);
        }

        WrapperInterface<Patient, Map<String, Object>> wrapper = new PatientToJSONWrapper(true);
        for (PatientSimilarityView patient : similarPatients) {
            try {
                results.add(wrapper.wrap(patient));
            } catch (Exception ex) {
                //Should not happen
            }
        }

        json.put(Configuration.JSON_RESPONSE_ID, request.getRequestId());
        json.put(Configuration.JSON_RESPONSE_TYPE, request.getResponseType());
        json.put(Configuration.JSON_RESULTS, results);

        return json;
    }
}
