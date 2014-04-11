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
package org.phenotips.remote.adapters.jsonwrappers;

import org.phenotips.remote.adapters.JSONToMetadataConverter;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.WrapperInterface;
import org.phenotips.remote.hibernate.internal.IncomingSearchRequest;

import org.xwiki.component.annotation.Component;

import java.util.Map;

import javax.inject.Named;

import net.sf.json.JSONObject;

/**
 * TODO
 */
@Component
@Named("json-meta")
public class JSONtoMetadataWrapper implements WrapperInterface<JSONObject, IncomingSearchRequestInterface>
{
    @Override
    public IncomingSearchRequestInterface wrap(JSONObject json)
    {
        IncomingSearchRequestInterface request = new IncomingSearchRequest();

        //FIXME. Not enough integrity checking.
        try {
            request.setExternalId(JSONToMetadataConverter.externalRequestId(json));

            request.setQueryType(JSONToMetadataConverter.queryType(json));

            Map<String, String> submitterMap = JSONToMetadataConverter.submitter(json);
            request.setSubmitterInstitution(submitterMap.get(Configuration.JSON_SUBMITTER_INSTITUTION));
            request.setSubmitterName(submitterMap.get(Configuration.JSON_SUBMITTER_NAME));
            request.setSubmitterEmail(submitterMap.get(Configuration.JSON_SUBMITTER_EMAIL));
        } catch (Exception ex) {
            request.setHTTPStatus(Configuration.HTTP_BAD_REQUEST);
        }

        return request;
    }
}
