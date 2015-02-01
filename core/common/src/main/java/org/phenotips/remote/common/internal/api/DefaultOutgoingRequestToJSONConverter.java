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

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.tojson.OutgoingRequestToJSONConverter;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;

import org.slf4j.Logger;

import net.sf.json.JSONObject;

public class DefaultOutgoingRequestToJSONConverter implements OutgoingRequestToJSONConverter
{
    private Logger logger;

    private PatientRepository patientRepository;

    private AuthorizationManager access;

    private DocumentAccessBridge bridge;

    private PatientToJSONConverter patientToJSONConverter;

    private final String apiVersion;

    public DefaultOutgoingRequestToJSONConverter(String apiVersion, Logger logger, PatientRepository patientRepository,
                                                 AuthorizationManager access, DocumentAccessBridge bridge)
    {
        this.apiVersion = apiVersion;

        this.logger            = logger;
        this.patientRepository = patientRepository;
        this.access            = access;
        this.bridge            = bridge;

        this.patientToJSONConverter = new DefaultPatientToJSONConverter(this.apiVersion, logger);
    }

    @Override
    public JSONObject toJSON(String patientId, int includedTopGenes)
    {
        Patient referencePatient = this.getPatientByID(patientId);
        if (referencePatient == null) {
            logger.error("Unable to get patient with id [{}]", patientId);
            // can't access the requested patient
            return null;
        }

        try {
            JSONObject json = this.patientToJSONConverter.convert(referencePatient, false, includedTopGenes);

            if ((!json.has(ApiConfiguration.JSON_FEATURES) ||
                  json.getJSONArray(ApiConfiguration.JSON_FEATURES).isEmpty()) &&
                (!json.has(ApiConfiguration.JSON_GENES) ||
                  json.getJSONArray(ApiConfiguration.JSON_GENES).isEmpty())) {
                this.logger.error("Can't send a query for a patient with no features and no genes");
                throw new ApiViolationException("Can't send a query for a patient with no features and no genes");
            }

            json.put("id", MD5(patientId));
            json.put("queryType", "once");

            //JSONObject submitter = new JSONObject();
            //submitter.put("name",  request.getSubmitterName());
            //submitter.put("email", request.getSubmitterEmail());

            return json;
        } catch (ApiViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            this.logger.error("Error converting patient to JSON: [{}]", ex);
            return null;
        }
    }

    private String MD5(String md5) {
        try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             byte[] array = md.digest(md5.getBytes());
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
               sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
             return sb.toString();
         } catch (java.security.NoSuchAlgorithmException e) {
         }
         return null;
     }

    private Patient getPatientByID(String patientID)
    {
        Patient patient = this.patientRepository.getPatientById(patientID);
        if (patient == null) {
            return null;
        }

        String accessLevelName = "view";
        // TODO: should access rights should be checked in the script service?
        if (!this.access.hasAccess(Right.toRight(accessLevelName), this.bridge.getCurrentUserReference(),
            patient.getDocument())) {
            this.logger.error("Can't send outgoing matching request for patient [{}]: no access level [{}]",
                              patientID, accessLevelName);
            return null;
        }

        return patient;
    }
}
