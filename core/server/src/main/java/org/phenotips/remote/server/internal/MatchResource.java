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

import org.phenotips.remote.server.MatchInterface;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import java.util.HashMap;
import java.util.Map;

import com.xpn.xwiki.XWikiException;

/**
 * Resource for listing full patient phenotype.
 *
 * @version $Id: a5e0487469d4280ae58cd29e702f50b6bc891ab6 $
 */
@Component("org.phenotips.remote.server.internal.MatchResource")
public class MatchResource extends XWikiResource implements MatchInterface
{
    public Map<String, ?> match(String key) throws XWikiRestException, XWikiException
    {
       Map<String, Object> jsonResponse = new HashMap<String, Object>();

       jsonResponse.put("key", key);
       return jsonResponse;
    }

    @Override
    public Map<String, ?> matchGet(String key) throws XWikiRestException, XWikiException
    {
        return match(key);
    }

    @Override
    public Map<String, ?> matchPost(String key) throws XWikiRestException, XWikiException
    {
        return match(key);
    }

}
