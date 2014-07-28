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
package org.phenotips.remote.adapters;

import org.phenotips.remote.adapters.jsonwrappers.JSONToHibernatePatientWrapper;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.WrapperInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * TODO.
 * Keep this static.
 */
public class JSONToMetadataConverter
{
    public static Map<String, String> submitter(JSONObject json) throws Exception
    {
        JSONObject submitter = json.getJSONObject(Configuration.JSON_SUBMITTER);
        if (submitter.isEmpty()) {
            throw new Exception("There is no submitter information");
        }

        String[] keys = { Configuration.JSON_SUBMITTER_NAME, Configuration.JSON_SUBMITTER_EMAIL,
            Configuration.JSON_SUBMITTER_INSTITUTION };
        Map<String, String> submitterMap = new HashMap<String, String>();
        for (String key : keys) {
            Object valueObject = submitter.get(key);
            if (valueObject == null) {
                continue;
            }
            String value = valueObject.toString();
            if (StringUtils.isNotBlank(value)) {
                submitterMap.put(key, value);
            }
        }
        return submitterMap;
    }

    public static String queryType(JSONObject json)
    {
        try {
            return json.getString(Configuration.JSON_QUERY_TYPE);
        } catch (Exception ex) {
            return Configuration.DEFAULT_REQUEST_QUERY_TYPE;
        }
    }

    public static String externalRequestId(JSONObject json)
    {
        return json.getString(Configuration.JSON_REQUEST_ID);
    }

    public static String externalResponseId(JSONObject json)
    {
        Object id = json.get(Configuration.JSON_RESPONSE_ID);
        return id == null ? "" : id.toString();
    }

    public static String responseType(JSONObject json)
    {
        return json.getString(Configuration.JSON_RESPONSE_TYPE);
    }

    public static Set<HibernatePatientInterface> responseResults(JSONObject json)
    {
        Set<HibernatePatientInterface> patients = new HashSet<HibernatePatientInterface>();
        WrapperInterface<JSONObject, HibernatePatientInterface> wrapper = new JSONToHibernatePatientWrapper();

        JSONArray results = json.getJSONArray(Configuration.JSON_RESULTS);
        for (Object jsonPatient : results) {
            HibernatePatientInterface patient = wrapper.wrap((JSONObject) jsonPatient);
            patients.add(patient);
        }

        return patients;
    }
}
