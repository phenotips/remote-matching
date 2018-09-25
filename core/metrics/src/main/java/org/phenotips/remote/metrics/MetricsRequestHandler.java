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

import org.phenotips.remote.api.ApiConfiguration;

import org.xwiki.rest.XWikiRestException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
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
     * Takes no inputs.
     *
     * Returns a JSON in the format specified in
     *  https://github.com/ga4gh/mme-apis/blob/1.1/metrics-api.md
     *
     * Possible error codes are defined in the same document.
     *
     * Note: as for all other MME requests, every request must specify the API version
     * within the HTTP Content-Type header as "application/vnd.ga4gh.matchmaker.{version}+json",
     * where {version} takes the form "vX.Y", where X is a major version and Y is a minor version.
     * It is generally assumed that a server supporting major version X can handle requests
     * with major version X and any minor version Y.
     *
     * For example for version "1.0": Content-Type: application/vnd.ga4gh.matchmaker.v1.0+json
     */
    @Consumes({ MediaType.APPLICATION_JSON,
        ApiConfiguration.HTTPHEADER_CONTENT_TYPE_PREFIX
            + ApiConfiguration.LATEST_API_VERSION_STRING
            + ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SUFFIX,
        ApiConfiguration.HTTPHEADER_CONTENT_TYPE_SIMPLE,
        "application/*+json" })
    @GET
    Response getMetrics(String json) throws XWikiRestException;
}
