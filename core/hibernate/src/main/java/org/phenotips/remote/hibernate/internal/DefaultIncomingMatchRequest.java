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
package org.phenotips.remote.hibernate.internal;

import net.sf.json.JSONObject;

import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.MatchingPatient;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons.
 *
 * @version $Id$
 */
@Entity
@Table(name = "remote_matching_incoming_requests_2")
public class DefaultIncomingMatchRequest extends AbstractSearchRequest implements IncomingMatchRequest
{
    @Transient
    private MatchingPatient remotePatient;

    /**
     * Hibernate requires a no-args constructor
     */
    protected DefaultIncomingMatchRequest()
    {
    }

    /**
     * @param queryID when not null, the ID of the request that needs to be updated;
     *                iff null, an auto-generatred ID wil be assigned when the request is stored in the database
     */
    public DefaultIncomingMatchRequest(String remoteServerId, String apiVersionUsed,
                                       String request, MatchingPatient remotePatient)
    {
        super(remoteServerId, apiVersionUsed, request, null);

        this.remotePatient = remotePatient;
    }

    @Override
    public void addResponse(JSONObject response)
    {
        this.setResponse(response.toString());
    }

    public MatchingPatient getModelPatient()
    {
        return this.remotePatient;
    }
}
