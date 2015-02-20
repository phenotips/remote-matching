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
package org.phenotips.remote.common.internal.api.v2;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.fromjson.IncomingJSONParser;
import org.phenotips.remote.api.tojson.OutgoingRequestToJSONConverter;
import org.xwiki.component.annotation.Component;

import java.util.List;

import javax.inject.Named;

import net.sf.json.JSONObject;

@Component
@Named("api-data-converter-v2")
public class ApiDataConverterV2 implements ApiDataConverter
{
    private final static String VERSION_STRING = "v2";

    public void initialize()
    {
    }

    @Override
    public String getApiVersion()
    {
        return VERSION_STRING;
    }

    //================================================================

    @Override
    public JSONObject generateWrongInputDataResponse(String reasonMsg)
    {
        return null;
    }

    @Override
    public JSONObject generateInternalServerErrorResponse(String reasonMsg)
    {
        return null;
    }

    //================================================================

    @Override
    public IncomingJSONParser getIncomingJSONParser()
    {
        return null;
    }

    @Override
    public JSONObject generateInlineResponse(IncomingSearchRequest request, List<PatientSimilarityView> resultList)
    {
        return null;
    }

    //================================================================

    @Override
    public OutgoingRequestToJSONConverter getOutgoingRequestToJSONConverter()
    {
        return null;
    }
}
