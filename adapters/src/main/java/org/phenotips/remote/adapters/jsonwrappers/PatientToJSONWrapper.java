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
import org.phenotips.remote.api.WrapperInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class PatientToJSONWrapper implements WrapperInterface<Patient, Map<String, Object>>
{
    private Boolean isPrivate;

    public PatientToJSONWrapper(Boolean... isPrivate)
    {
        this.isPrivate = isPrivate.length > 0 ? isPrivate[0] : false;
    }

    public Map<String, Object> wrap(Patient patient)
    {
        Map<String, Object> json = new HashMap<String, Object>();

        try {
            json.put("gender", PatientToJSONConverter.gender(patient));
            json.putAll(PatientToJSONConverter.globalQualifiers(patient));
        } catch (Exception ex) {
            //Do nothing. These are optional.
        }
        json.put("disorders", PatientToJSONConverter.disorders(patient));
        if (!isPrivate) {
            json.put("features", PatientToJSONConverter.features(patient));
        } else {
            json.put("features", PatientToJSONConverter.nonPersonalFeatures(patient));
        }
        return json;
    }
}