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

/**
 * The functions essential to the servers ability to store, track, an answer search requests.
 */
public interface RequestInterface
{
    Long getRequestId();

    void setExternalId(String id);

    String getExternalId();

    /**
     * Is not mandatory for {@link org.phenotips.remote.api.OutgoingSearchRequestInterface} as it is instantiated with
     * the default value.
     */
    void setResponseType(String type);

    String getResponseType();

    /**
     * The target URL for the request. This is the final URL and must be valid without further modifications.
     *
     * @param url the processed URL to which a request could be sent
     */
    void setTargetURL(String url);

    String getTargetURL();

    void setSubmitterName(String name);

    String getSubmitterName();

    void setSubmitterEmail(String email);

    String getSubmitterEmail();

    void setSubmitterInstitution(String institution);

    String getSubmitterInstitution();

    void setKey(String key);

    String getKey();

    void setQueryType(String type);

    String getQueryType();
}
