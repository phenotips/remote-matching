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

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.xpn.xwiki.XWikiException;

/**
 * Resource for interacting with a specific wiki.
 *
 * @version $Id: 2a40da554444794e3dadbee73dbce9828151e281 $
 */
@Path("/patient/{patientId}/phenotype")
public interface ExampleInterface
{
    /**
     * Get full list of phenotypes.
     *
     * @param patientId String id for patient document
     * @param wikiName String wiki the patient document is attached to
     * @return list of phenotypes
     * @throws XWikiRestException if something goes wrong.
     * @throws com.xpn.xwiki.XWikiException if something goes wrong.
     */
    @GET Map<String, Map<String, String>> getAllPhenotypes(@PathParam("patientId") String patientId,
        @QueryParam("wiki") @DefaultValue("xwiki") String wikiName)
        throws XWikiRestException, XWikiException;
}

