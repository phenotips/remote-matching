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
package org.phenotips.remote.api;

//import org.phenotips.remote.api.ApiConfiguration;
import java.util.List;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.tojson.OutgoingJSONGenerator;

import net.sf.json.JSONObject;

import org.xwiki.component.annotation.Role;

/*
 * Converts matching requests and replies to and from JSON
 */
@Role
public interface ApiDataConverter
{
    //TODO:
    //ApiConfiguration getApiConfiguration();

    String getApiVersion();

    //================================================================

    JSONObject generateWrongInputDataResponse(String reasonMsg);

    JSONObject generateInternalServerErrorResponse(String reasonMsg);

    //================================================================

    IncomingJSONParser getIncomingJSONParser();

    JSONObject generateServerResponse(IncomingMatchRequest request, List<PatientSimilarityView> resultList);

    //================================================================

    OutgoingJSONGenerator getOutgoingJSONGenerator();
}
