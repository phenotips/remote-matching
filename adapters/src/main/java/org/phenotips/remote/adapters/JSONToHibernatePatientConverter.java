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

import org.phenotips.remote.api.HibernatePatientFeatureInterface;
import org.phenotips.remote.hibernate.internal.HibernatePatientFeature;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * The preferred way for this class to exist was to have only static functions and have an interface. Due to Java's
 * limitations, it was not possible to have both. If the code is being modified, please keep all the functions static,
 * even though they are not explicitly declared so.
 */
public class JSONToHibernatePatientConverter
{
    public static Set<HibernatePatientFeatureInterface> convertFeatures(JSONArray featuresJson)
    {
        Set<HibernatePatientFeatureInterface> featureSet = new HashSet<HibernatePatientFeatureInterface>();
        for (Object jsonFeatureUncast : featuresJson) {
            JSONObject jsonFeature = (JSONObject) jsonFeatureUncast;
            HibernatePatientFeature feature = new HibernatePatientFeature();
            feature.setId(jsonFeature.getString("id"));
            feature.setPresent(convertTextToIntBool(jsonFeature.getString("observed")));
            featureSet.add(feature);
        }
        return featureSet;
    }

    private static int convertTextToIntBool(String text)
    {
        if (StringUtils.equalsIgnoreCase(text, "yes")) {
            return 1;
        } else if (StringUtils.equalsIgnoreCase(text, "no")) {
            return -1;
        } else {
            return 0;
        }
    }
}
