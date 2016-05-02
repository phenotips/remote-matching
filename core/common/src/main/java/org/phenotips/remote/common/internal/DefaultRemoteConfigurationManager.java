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
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.remote.common.internal;

import org.phenotips.remote.common.ApplicationConfiguration;
import org.phenotips.remote.common.RemoteConfigurationManager;

import org.xwiki.component.annotation.Component;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Singleton
@Component
public class DefaultRemoteConfigurationManager implements RemoteConfigurationManager
{
    @Inject
    private Logger logger;

    @Override
    public List<BaseObject> getListOfRemotes(XWikiContext context)
    {
        List<BaseObject> remotes = null;
        try {
            XWikiDocument prefsDoc =
                    context.getWiki().getDocument(ApplicationConfiguration.XWIKI_PREFERENCES_DOCUMENT_REFERENCE, context);

            remotes = prefsDoc.getXObjects(ApplicationConfiguration.REMOTE_CONFIGURATION_OBJECT_REFERENCE);
        } catch (Exception ex) {
            logger.error("Remote matching admin section is absent or empty - can not process request: [{}] {}",
                    ex.getMessage(), ex);
        }

        if (remotes == null) {
            return Collections.emptyList();
        } else {
            return remotes;
        }
    }

    @Override
    public BaseObject getRemoteConfiguration(String baseURL, XWiki wiki, XWikiContext context)
            throws XWikiException
    {
        XWikiDocument configurationsDoc =
                wiki.getDocument(ApplicationConfiguration.XWIKI_PREFERENCES_DOCUMENT_REFERENCE, context);
        if (configurationsDoc == null) {
            this.logger.error("Could not find configurations document");
        }
        List<BaseObject> configurations =
                configurationsDoc.getXObjects(ApplicationConfiguration.REMOTE_CONFIGURATION_OBJECT_REFERENCE);
        for (BaseObject remote : configurations) {
            if (remote == null) {
                continue;
            }
            String url = remote.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD);
            if (StringUtils.equalsIgnoreCase(url, baseURL)) {
                logger.info("Matched configuration with URL: " + url);
                return remote;
            }
        }
        logger.error(
                "Could not find any remote configuration objects or no match was found. Configurations list size: " +
                        configurations.size());
        // FIXME. Not exactly true.
        throw new XWikiException();
    }

    @Override
    public BaseObject getRemoteConfigurationGivenRemoteIPAndToken(String remoteIP, String providedToken, XWikiContext context)
    {
        try {
            List<BaseObject> remotes = getListOfRemotes(context);
            if (remotes == null) {
                return null;
            }
            for (BaseObject remote : remotes) {
                if (remote == null) {
                    continue;
                }

                String configuredToken = remote.getStringValue(ApplicationConfiguration.CONFIGDOC_LOCAL_KEY_FIELD);

                String remoteServerName = remote.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_NAME);

                boolean limitIPs = (remote.getIntValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_LIMIT_IP) == 1);

                if (limitIPs) {
                    String configuredURL = remote.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD);
                    try {
                        String configuredIP = InetAddress.getByName(new URL(configuredURL).getHost()).getHostAddress();
                        if (!StringUtils.equalsIgnoreCase(remoteIP, configuredIP)) {
                            continue;
                        }
                    } catch (MalformedURLException ex) {
                        logger.error("One of the configured remote matching servers has an incorrectly formatted URL [{}]: {}",
                                configuredURL, ex.getMessage());
                    } catch (UnknownHostException ex) {
                        logger.error("One of the configured remote matching server URLs has no valid DNS record [{}]: {}",
                                configuredURL, ex.getMessage());
                    }

                    if (!StringUtils.equalsIgnoreCase(providedToken, configuredToken)) {
                        logger.error("Remote server token validation failed for server [{}]: Provided: {}",
                                remoteServerName, providedToken);
                        return null;
                    }
                    logger.error("Remote server IP and token validated OK for server [{}] (remote IP {})",
                            remoteServerName, remoteIP);
                    return remote;
                } else {
                    if (StringUtils.equalsIgnoreCase(providedToken, configuredToken)) {
                        logger.error("Remote server token validated OK for server [{}] (remote IP {})",
                                remoteServerName, remoteIP);
                        return remote;
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Error while getting server info for IP [{}]: [{}] {}", remoteIP, ex.getMessage(), ex);
        }
        logger.error("Remote server token validation failed for remote IP {}", remoteIP);
        return null;
    }

    @Override
    public BaseObject getRemoteConfigurationGivenRemoteServerID(String remoteServerID, XWikiContext context)
    {
        try {
            List<BaseObject> remotes = this.getListOfRemotes(context);
            if (remotes == null) {
                return null;
            }
            for (BaseObject remote : remotes) {
                if (remote == null) {
                    continue;
                }
                String configuredServerId = remote.getStringValue(ApplicationConfiguration.CONFIGDOC_REMOTE_SERVER_ID);
                if (StringUtils.equalsIgnoreCase(remoteServerID, configuredServerId)) {
                    return remote;
                }
            }
        } catch (Exception ex) {
            logger.error("Error while getting server info for serverID [{}]: [{}] {}",
                    remoteServerID, ex.getMessage(), ex);
        }
        return null;
    }

}
