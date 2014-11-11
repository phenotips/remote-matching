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
package org.phenotips.remote.server.internal.todo;

import org.phenotips.data.PatientRepository;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.IncomingSearchRequest;
import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.common.ApplicationConfiguration;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.mailsender.Mail;
import com.xpn.xwiki.plugin.mailsender.MailSenderPlugin;

import net.sf.json.JSONObject;

/**
 * TODO.
 */
public class IncomingRequestHandler
{
    /*
    private IncomingSearchRequest request = null;

    private JSONObject json;

    private String configuredResponseType;

    private String baseURL;

    public IncomingRequestHandler(JSONObject json,
        WrapperInterface<JSONObject, MatchingPatient> patientWrapper,
        WrapperInterface<JSONObject, IncomingSearchRequest> metaWrapper, BaseObject xwikiConfigurationObject)
    {
        this.json = json;
        this.patientWrapper = patientWrapper;
        this.metaWrapper = metaWrapper;
        this.configuredResponseType = xwikiConfigurationObject.getStringValue(AppConfiguration.REMOTE_RESPONSE_FORMAT);
        this.baseURL = xwikiConfigurationObject.getStringValue(AppConfiguration.CONFIGDOC_REMOTE_BASE_URL_FIELD).trim();
    }

    @Override
    public IncomingSearchRequest getRequest()
    {
        if (this.request != null) {
            return this.request;
        }

        this.request = this.metaWrapper.wrap(this.json);
        if (!this.request.getHTTPStatus().equals(AppConfiguration.HTTP_OK)) {
            return this.request;
        }

        // If the original request contains a response type, do not change it.
        if (StringUtils.isBlank(this.request.getResponseType())) {
            this.request.setResponseType(this.configuredResponseType);
        }

        //
        // URL must always be set after! the response type is. Although it is a design hole, at the same time it can
        // never fully be mitigated, even if the the process which determines the final url is given to the getTargetURL
        // function. On second thought it's a FIXME
        //
        this.request.setTargetURL(this.baseURL);

        if (StringUtils.equals(this.request.getResponseType(), AppConfiguration.REQUEST_RESPONSE_TYPE_EMAIL) &&
            StringUtils.isBlank(this.request.getSubmitterEmail()))
        {
            this.request.setHTTPStatus(AppConfiguration.HTTP_BAD_REQUEST1);
            return this.request;
        }

        MatchingPatient hibernatePatient = this.patientWrapper.wrap(JSONObject.fromObject(this.json));
        if (hibernatePatient == null) {
            this.request.setHTTPStatus(AppConfiguration.HTTP_BAD_REQUEST2);
            return this.request;
        }

        this.request.setReferencePatient(hibernatePatient);

        return this.request;
    }

    @Override
    public Boolean mail(XWikiContext context,
        MultiTypeWrapperInterface<IncomingSearchRequest, JSONObject> wrapper)
    {
        try {
            MailSenderPlugin mailSender =
                (MailSenderPlugin) context.getWiki().getPlugin(AppConfiguration.MAIL_SENDER, context);
            // The mail object should be constructed in the wrapper.
            Mail mail = new Mail(AppConfiguration.EMAIL_FROM_ADDRESS, this.request.getSubmitterEmail(), null, null,
                AppConfiguration.EMAIL_SUBJECT, "", wrapper.mailWrap(this.request));
            mailSender.sendMail(mail, context);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            return false;
        }
        return true;
    }*/
}
