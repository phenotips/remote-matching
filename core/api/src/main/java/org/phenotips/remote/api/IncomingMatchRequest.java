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

import org.json.JSONObject;
import org.phenotips.data.Patient;

public interface IncomingMatchRequest extends MatchRequest
{
    /**
     * @return the model patient stuffed into a Phenotips Patient class
     */
    Patient getModelPatient();

    /**
     * @param response the raw response sent back to the requesting server as a reply to this incoming request
     */
    void addResponse(JSONObject response);

    /**
     * @return true iff the model patient is a test patient (and thus resulting matches are not "real"
     *              and should not be stored and reported to users => request is a "test request")
     */
    boolean isTestRequest();
}
