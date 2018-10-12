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

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.tojson.OutgoingJSONGenerator;

import org.xwiki.component.annotation.Role;

import java.util.List;

import org.json.JSONObject;

/**
 * Converts matching requests and replies to and from JSON.
 *
 * @version $Id$
 */
@Role
public interface ApiDataConverter
{
    String getApiVersion();

    JSONObject generateWrongInputDataResponse(String reasonMsg);

    JSONObject generateInternalServerErrorResponse(String reasonMsg);

    IncomingJSONParser getIncomingJSONParser();

    JSONObject generateServerResponse(IncomingMatchRequest request, List<PatientSimilarityView> resultList);

    OutgoingJSONGenerator getOutgoingJSONGenerator();
}
