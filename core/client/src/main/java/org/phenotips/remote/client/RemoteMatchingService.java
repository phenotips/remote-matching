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
package org.phenotips.remote.client;

import java.util.List;
import java.util.Map;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiViolationException;
import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

import net.sf.json.JSONObject;

/**
 * Sends requests to remote servers supporting the specifications.
 *
 * FIXME Should there be an API for this?
 */
@Unstable
@Role
public interface RemoteMatchingService
{
    // TODO: a better split of duties between generate & send. Keeping as is for debug purposes for now
    public JSONObject sendRequest(String patientId, String remoteServerId, int addTopNGenes);

    // Note: request may differ from server to server, e.g. we may want to include more or less private
    //       data depending on the server
    public JSONObject generateRequestJSON(String patientId, String remoteServerId, int addTopNGenes)
        throws ApiViolationException;

    public List<PatientSimilarityView> getSimilarityResults(String patientId, String remoteServerId);

    public Map<String,List<PatientSimilarityView>> getAllSimilarityResults(String patientId);
}
