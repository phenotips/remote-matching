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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.phenotips.remote.common.internal;

import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.common.ApiFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Component
@Singleton
public class DefaultApiFactory implements ApiFactory, Initializable
{
    @Inject
    @Named("api-data-converter-v1")
    private ApiDataConverter apiDataConverterV1;

    @Inject
    @Named("api-data-converter-v2")
    private ApiDataConverter apiDataConverterV2;

    private Map<String, ApiDataConverter> allApiDataConverters = new HashMap<String, ApiDataConverter>();

    @Override
    public void initialize()
    {
        allApiDataConverters.put("1.0", this.apiDataConverterV1);
        //allApiDataConverters.put("1.1", this.apiDataConverterV2);
    }

    @Override
    public ApiDataConverter getApiVersion(String apiVersion)
    {
        if (allApiDataConverters.containsKey(apiVersion)) {
            return allApiDataConverters.get(apiVersion);
        }
        throw new IllegalArgumentException("Unsupported API version [" + apiVersion + "]");
    }

    @Override
    public Set<String> getSupportedApiVersions()
    {
        return allApiDataConverters.keySet();
    }
}
