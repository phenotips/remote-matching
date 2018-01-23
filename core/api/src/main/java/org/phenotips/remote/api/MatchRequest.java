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
package org.phenotips.remote.api;

import java.util.Date;

import org.json.JSONObject;

/**
 * This is a raw request/response pair. The original JSONs are stored as-is.
 *
 * @version $Id$
 */
public interface MatchRequest
{
    /**
     * The id of the other server (for both incoming and outgoing requests).
     *
     * @return a short identifier
     */
    String getRemoteServerId();

    /**
     * @return the whole request JSON
     */
    JSONObject getRequestJSON();

    /**
     * @return the whole response JSON
     */
    JSONObject getResponseJSON();

    /**
     * @return the time when the request was sent or received
     */
    Date getRequestTime();

    /**
     * @return the version used for the request
     */
    String getApiVersionUsed();
}
