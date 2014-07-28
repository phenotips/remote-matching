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

import org.phenotips.remote.adapters.JSONToHibernatePatientConverter;
import org.phenotips.remote.api.HibernatePatientDisorderInterface;
import org.phenotips.remote.api.HibernatePatientFeatureInterface;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.WrapperInterface;
import org.phenotips.remote.hibernate.internal.HibernatePatient;

import org.xwiki.component.annotation.Component;

import java.util.Set;

import javax.inject.Named;

import net.sf.json.JSONObject;

/**
 * TODO.
 */
@Component
@Named("json-patient")
public class JSONToHibernatePatientWrapper implements WrapperInterface<JSONObject, HibernatePatientInterface>
{
    @Override
    public HibernatePatientInterface wrap(JSONObject json)
    {
        HibernatePatientInterface patient = new HibernatePatient();

        try {
            Set<HibernatePatientFeatureInterface> features = JSONToHibernatePatientConverter.convertFeatures(json);
            Set<HibernatePatientDisorderInterface> disorders = JSONToHibernatePatientConverter.convertDisorders(json);
            patient.addFeatures(features);
            patient.addDisorders(disorders);
        } catch (Exception ex) {
            return null;
        }
        return patient;
    }
}
