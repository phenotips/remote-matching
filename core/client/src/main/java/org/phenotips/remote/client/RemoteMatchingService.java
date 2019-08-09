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
package org.phenotips.remote.client;

import org.phenotips.matchingnotification.match.PatientMatch;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.common.internal.RemotePatientSimilarityView;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

import java.util.List;

/**
 * Sends requests to remote servers supporting the specifications.
 *
 * FIXME Should there be an API for this?
 */
@Unstable
@Role
public interface RemoteMatchingService
{
    OutgoingMatchRequest sendRequest(String patientId, String remoteServerId, int addTopNGenes,
        List<PatientMatch> matchesList);

    OutgoingMatchRequest getLastRequestSent(String patientId, String remoteServerId);

    List<RemotePatientSimilarityView> getSimilarityResults(OutgoingMatchRequest request);
}
