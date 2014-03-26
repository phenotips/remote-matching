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
package org.phenotips.remote.server;

import org.xwiki.rest.XWikiRestException;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.xpn.xwiki.XWikiException;

/**
 * Interface for the /match endpoint. This is where a remote request would send the request to.
 *
 * @version $Id: 2a40da554444794e3dadbee73dbce9828151e281 $
 */
@Path("/remoteMatcher/match")
public interface MatchInterface
{
    /**
     * Place a search request to this server.
     *
     * FIXME The get version is here only for testing through the browser. Delete for production.
     * TODO fix the doc.
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST Map<String, ?> matchPost(String json)
        throws XWikiRestException, XWikiException;
}

