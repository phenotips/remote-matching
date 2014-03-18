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
package org.phenotips.remote.adapters.script;

import org.phenotips.remote.adapters.DataAdapter;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;

import net.sf.json.JSONObject;

/**
 * Gives velocity access to the functions it needs to perform remote matching.
 * There is a set of functions for sending the request, and a set for retrieving the data.
 */
@Unstable
@Component
@Named("remoteMatching")
@Singleton
public class RemoteMatchingScriptService implements ScriptService
{
    @Inject
    private Logger logger;

    @Inject
    private DataAdapter dataAdapter;

    public boolean sendRequest(String patientId, String submitterId, Integer remoteId, Boolean isPeriodic) {
        try {
            dataAdapter.setPatient(patientId);
            dataAdapter.setSubmitter(submitterId);
            dataAdapter.setPeriodic(isPeriodic);
            JSONObject json = dataAdapter.toJSON();
            return true;
        } catch (Exception ex) {
            logger.error("Failed to process data", ex);
        }
        return false;
    }

    public boolean sendRequest(String patientId, String submitterId, Integer remoteId) {
        return sendRequest(patientId, submitterId, remoteId, false);
    }
}
