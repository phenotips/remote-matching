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

import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.server.MatchInterface;
import org.phenotips.remote.server.RequestProcessorInterface;

import org.xwiki.component.annotation.Component;
import org.xwiki.rest.XWikiResource;
import org.xwiki.rest.XWikiRestException;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

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
    RequestProcessorInterface requestProcessor;

    @Override
    public Response matchPost(String json) throws XWikiRestException, XWikiException
    {
        JSONObject jsonResponse;
        XWikiContext context = this.getXWikiContext();
        HttpServletRequest httpRequest = context.getRequest().getHttpServletRequest();

        Boolean isAuthorized;
        try {
            isAuthorized = validateRequest(httpRequest, context);
        } catch (XWikiException ex) {
            return Response.status(Configuration.HTTP_SERVER_ERROR).build();
        } catch (MalformedURLException ex) {
            return Response.status(Configuration.HTTP_SERVER_ERROR).build();
        } catch (UnknownHostException ex) {
            return Response.status(Configuration.HTTP_BAD_REQUEST).build();
        }
        if (!isAuthorized) {
            return Response.status(Configuration.HTTP_UNAUTHORIZED).build();
        }

        try {
            setCurrentContextDocument(context);
            jsonResponse = requestProcessor.processHTTPRequest(json);
        } catch (Exception e) {
            return Response.status(Configuration.HTTP_SERVER_ERROR).build();
        }
        Integer status = (Integer) jsonResponse.remove("status");
        if (status == 200) {
            return Response.ok(jsonResponse, MediaType.APPLICATION_JSON).build();
        } else {
            return Response.status(status).build();
        }
    }

    private boolean validateRequest(HttpServletRequest httpRequest, XWikiContext context)
        throws XWikiException, UnknownHostException, MalformedURLException
    {
        //Slightly inefficient, as it is also called in setCurrentContextDocument
        XWiki wiki = context.getWiki();
        String key = httpRequest.getParameter(Configuration.URL_KEY_PARAMETER);

        XWikiDocument configurationsDocument =
            wiki.getDocument(Configuration.REMOTE_CONFIGURATIONS_DOCUMENT_REFERENCE, context);
        List<BaseObject> remotes =
            configurationsDocument.getXObjects(Configuration.REMOTE_CONFIGURATION_OBJECT_REFERENCE);

        for (BaseObject remote : remotes) {
            String testKey = remote.getStringValue(Configuration.REMOTE_KEY_FIELD);
            if (StringUtils.equalsIgnoreCase(testKey, key)) {
                String baseURL = remote.getStringValue(Configuration.REMOTE_BASE_URL_FIELD);
                return validateIP(baseURL, httpRequest.getRemoteAddr());
            }
        }
        return false;
    }

    private boolean validateIP(String baseURL, String ip) throws UnknownHostException, MalformedURLException
    {
        URL url = new URL(baseURL);
        InetAddress address = InetAddress.getByName(url.getHost());
        String resolvedIP = address.getHostAddress();
        return StringUtils.equalsIgnoreCase(resolvedIP, ip);
    }

    private void setCurrentContextDocument(XWikiContext context) throws XWikiException
    {
        XWiki wiki = context.getWiki();
        XWikiDocument currentDoc = wiki.getDocument(Configuration.ABSOLUTE_DOCUMENT_REFERENCE, context);
        context.setDoc(currentDoc);
    }
}
