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
package org.phenotips.remote.server.internal;

import org.phenotips.remote.api.IncomingRequestProcessor;
import org.phenotips.remote.server.MatchInterface;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import javax.inject.Inject;

import com.xpn.xwiki.XWikiException;

import net.sf.json.JSONObject;

/**
 * Resource for listing full patient phenotype.
 *
 * @version $Id: a5e0487469d4280ae58cd29e702f50b6bc891ab6 $
 */
@Component("org.phenotips.remote.server.internal.MatchResource")
public class MatchResource extends XWikiResource implements MatchInterface
{
    @Inject
    IncomingRequestProcessor requestProcessor;

    @Override
    public JSONObject matchPost(String json) throws XWikiRestException, XWikiException
    {
        JSONObject jsonResponse = requestProcessor.processRequest(json);

        return jsonResponse;
    }
}
