/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.remote.common.internal.api;

import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.ApiViolationException;
import org.phenotips.remote.api.tojson.OutgoingJSONGenerator;
import org.phenotips.remote.api.tojson.PatientToJSONConverter;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;

import org.json.JSONObject;
import org.slf4j.Logger;

public class DefaultOutgoingJSONGenerator implements OutgoingJSONGenerator
{
    private Logger logger;

    private PatientRepository patientRepository;

    private AuthorizationManager access;

    private DocumentAccessBridge bridge;

    private PatientToJSONConverter patientToJSONConverter;

    private final String apiVersion;

    public DefaultOutgoingJSONGenerator(String apiVersion, Logger logger, PatientRepository patientRepository,
        AuthorizationManager access, DocumentAccessBridge bridge)
    {
        this.apiVersion = apiVersion;

        this.logger = logger;
        this.patientRepository = patientRepository;
        this.access = access;
        this.bridge = bridge;

        this.patientToJSONConverter = new DefaultPatientToJSONConverter(this.apiVersion, logger);
    }

    @Override
    public JSONObject generateRequestJSON(String remoteServerID, String localPatientId, int includedTopGenes)
    {
        Patient referencePatient = this.getPatientByID(localPatientId);
        if (referencePatient == null) {
            logger.error("Unable to get patient with id [{}]", localPatientId);
            // can't access the requested patient
            return null;
        }

        try {
            JSONObject patientJson = this.patientToJSONConverter.convert(referencePatient, false, includedTopGenes);

            if ((!patientJson.has(ApiConfiguration.JSON_FEATURES) ||
                patientJson.getJSONArray(ApiConfiguration.JSON_FEATURES).length() == 0) &&
                (!patientJson.has(ApiConfiguration.JSON_GENES) ||
                    patientJson.getJSONArray(ApiConfiguration.JSON_GENES).length() == 0)) {
                this.logger.error("Can't send a query for a patient with no features and no genes");
                throw new ApiViolationException("Can't send a query for a patient with no features and no genes");
            }
            patientJson.put("id", localPatientId);

            JSONObject json = new JSONObject();
            json.put(ApiConfiguration.JSON_PATIENT, patientJson);

            // JSONObject submitter = new JSONObject();
            // submitter.put("name", request.getSubmitterName());

            return json;
        } catch (ApiViolationException ex) {
            throw ex;
        } catch (Exception ex) {
            this.logger.error("Error converting patient to JSON: [{}]", ex);
            return null;
        }
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
