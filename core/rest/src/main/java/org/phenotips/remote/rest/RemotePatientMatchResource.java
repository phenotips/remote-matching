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
package org.phenotips.remote.rest;

import org.phenotips.data.rest.PatientResource;
import org.phenotips.rest.ParentResource;
import org.phenotips.rest.Relation;

import org.xwiki.stability.Unstable;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for working with remote similar cases data.
 *
 * @version $Id$
 * @since 1.2
 */
@Unstable("New API introduced in 1.2")
@Path("/patients/{entity-id}/similar-remote-cases")
@Relation("https://phenotips.org/rel/patientSimilarityRemote")
@ParentResource(PatientResource.class)
public interface RemotePatientMatchResource
{
    /**
     * Finds matching patients for a provided reference patient. The following additional parameters may be
     * specified:
     *
     * <dl>
     * <dt>patientId</dt>
     * <dd>the internal identifier for a local reference patient; must be specified</dd>
     * <dt>offset</dt>
     * <dd>the offset for the returned match data (one-based), must be an int; default value is set to 1</dd>
     * <dt>maxResults</dt>
     * <dd>the maximum number of matches to return, must be an int; default is -1, which signifies "all matches"</dd>
     * <dt>reqNo</dt>
     * <dd>the request number, must be an integer; default value is set to 1</dd>
     * <dt>server</dt>
     * <dd>the remote server to be queried for matching patients; must be specified</dd>
     * <dt>sendNewRequest</dt>
     * <dd>true iff a new request should be sent to the remote matching server; default value is false</dd>
     * </dl>
     *
     * @param patientId the identifier of the patient to fetch matches for
     * @return a response containing the reference patient and matched patients data, or an error code if unsuccessful
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    Response findRemoteMatchingPatients(@PathParam("entity-id") String patientId);
}
