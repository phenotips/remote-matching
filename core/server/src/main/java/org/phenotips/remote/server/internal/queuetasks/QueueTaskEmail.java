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
package org.phenotips.remote.server.internal.queuetasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.phenotips.remote.api.IncomingSearchRequest;
//import org.phenotips.remote.api.RequestHandlerInterface;

import org.slf4j.Logger;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import net.sf.json.JSONObject;

public class QueueTaskEmail implements Runnable
{
    //private RequestHandlerInterface<IncomingSearchRequest> requestHandler;

    //private MultiTypeWrapperInterface<IncomingSearchRequest, JSONObject> requestWrapper;

    //private ExecutionContext executionContext;

    public QueueTaskEmail(IncomingSearchRequest request, BaseObject configurationObject,
                          Logger logger, ExecutionContext executionContext)
    {
        //this.requestHandler = _requestHandler;
        //this.requestWrapper = _requestWrapper;
        //this.executionContext = _executionContext;
    }

    @Override
    public void run()
    {/*
        try {
            EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
            componentManager.initialize(this.getClass().getClassLoader());
            Execution execution = componentManager.getInstance(Execution.class);
            execution.setContext(this.executionContext);
            XWikiContext context = (XWikiContext) this.executionContext.getProperty("xwikicontext");

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Date date = new Date();
            String emailText = "The following matches were found for the query submitted on " + dateFormat.format(date) +
                               " for patient labelled as [" + request.getRemotePatient().getLabel() + "]:\n";

            //this.requestHandler.mail(context, this.requestWrapper);
             *
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
    }
                 *
            componentManager.dispose();
        } catch (ComponentLookupException ex) {
            // There is nothing that can be done.
        }*/
    }
}
