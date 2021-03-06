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
package org.phenotips.remote.metrics;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Interface for the MME API of /metrics endpoint. This is where a remote request would send the request to.
 *
 * @version $Id$
 */
@Path("/remoteMatcher/metrics")
public interface MetricsRequestHandler
{
    /**
     * An endpoint to get MME metrics.
     *
     * @return a JSON in the format specified in https://github.com/ga4gh/mme-apis/blob/1.1/metrics-api.md
     */
    @GET
    Response getMetrics();
}
