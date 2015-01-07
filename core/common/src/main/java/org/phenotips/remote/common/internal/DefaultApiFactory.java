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
package org.phenotips.remote.common.internal;

import org.phenotips.remote.api.ApiDataConverter;
import org.phenotips.remote.common.ApiFactory;

import org.xwiki.component.annotation.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Component
@Singleton
public class DefaultApiFactory implements ApiFactory
{
    @Inject
    @Named("api-data-converter-v1")
    private ApiDataConverter apiDataConverterV1;

    @Inject
    @Named("api-data-converter-v2")
    private ApiDataConverter apiDataConverterV2;

    // TODO:
    //@Inject
    //private Map<String, ApiDataConverter> allApiDataConverters;  // the string is the hint

    @Override
    public ApiDataConverter getApiVersion(String apiVersion)
    {
        if (apiVersion.equals("v1")) {
            return this.apiDataConverterV1;
        } else if (apiVersion.equals("v2")) {
            return this.apiDataConverterV2;
        }
        throw new IllegalArgumentException("Unsupported API version [" + apiVersion + "]");
    }
}
