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

import org.phenotips.data.PatientRepository;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.api.MultiTaskWrapperInterface;
import org.phenotips.remote.api.WrapperInterface;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.mailsender.Mail;
import com.xpn.xwiki.plugin.mailsender.MailSenderPlugin;

import net.sf.json.JSONObject;

/**
 * TODO.
 */
public class IncomingRequestHandler implements RequestHandlerInterface<IncomingSearchRequestInterface>
{
    private IncomingSearchRequestInterface request = null;

    private JSONObject json;

    private WrapperInterface<JSONObject, HibernatePatientInterface> patientWrapper;

    private WrapperInterface<JSONObject, IncomingSearchRequestInterface> metaWrapper;

    private String configuredResponseType;

    public IncomingRequestHandler(JSONObject json,
        WrapperInterface<JSONObject, HibernatePatientInterface> patientWrapper,
        WrapperInterface<JSONObject, IncomingSearchRequestInterface> metaWrapper, String responseFormat)
    {
        this.json = json;
        this.patientWrapper = patientWrapper;
        this.metaWrapper = metaWrapper;
        this.configuredResponseType = responseFormat;
    }

    @Override
    public IncomingSearchRequestInterface getRequest()
    {
        if (request != null) {
            return request;
        }

        request = metaWrapper.wrap(json);
        if (!request.getHTTPStatus().equals(Configuration.HTTP_OK)) {
            return request;
        }

        //If the original request contains a response type, do not change it.
        if (StringUtils.isBlank(request.getResponseType())) {
            request.setResponseType(configuredResponseType);
        }

        HibernatePatientInterface hibernatePatient = patientWrapper.wrap(JSONObject.fromObject(json));
        if (hibernatePatient == null) {
            request.setHTTPStatus(Configuration.HTTP_BAD_REQUEST);
            return request;
        }

        request.setReferencePatient(hibernatePatient);

        return request;
    }

    @Override
    public Long saveRequest(Session session)
    {
        Transaction t = session.beginTransaction();
        Long id;
        if (request.getRequestId() == null) {
            id = (Long) session.save(request);
        } else {
            session.saveOrUpdate(request);
            id = request.getRequestId();
        }
        t.commit();

        return id;
    }

    @Override
    public IncomingSearchRequestInterface loadRequest(Long id, PatientRepository internal)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean mail(XWikiContext context,
        MultiTaskWrapperInterface<IncomingSearchRequestInterface, JSONObject> wrapper)
    {
        try {
            MailSenderPlugin mailSender =
                (MailSenderPlugin) context.getWiki().getPlugin(Configuration.MAIL_SENDER, context);
            //The mail object should be constructed in the wrapper.
            Mail mail = new Mail(Configuration.EMAIL_FROM_ADDRESS, request.getSubmitterEmail(), null, null,
                Configuration.EMAIL_SUBJECT, "", wrapper.mailWrap(request));
            mailSender.sendMail(mail, context);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            return false;
        }
        return true;
    }
}
