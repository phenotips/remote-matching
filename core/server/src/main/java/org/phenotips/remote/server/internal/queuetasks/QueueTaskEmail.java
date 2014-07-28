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

import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.MultiTypeWrapperInterface;
import org.phenotips.remote.api.RequestHandlerInterface;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.xpn.xwiki.XWikiContext;

import net.sf.json.JSONObject;

public class QueueTaskEmail implements Runnable
{
    private RequestHandlerInterface<IncomingSearchRequestInterface> requestHandler;

    private MultiTypeWrapperInterface<IncomingSearchRequestInterface, JSONObject> requestWrapper;

    private ExecutionContext executionContext;

    public QueueTaskEmail(RequestHandlerInterface<IncomingSearchRequestInterface> _requestHandler,
        MultiTypeWrapperInterface<IncomingSearchRequestInterface, JSONObject> _requestWrapper,
        ExecutionContext _executionContext)
    {
        this.requestHandler = _requestHandler;
        this.requestWrapper = _requestWrapper;
        this.executionContext = _executionContext;
    }

    @Override
    public void run()
    {
        try {
            EmbeddableComponentManager componentManager = new EmbeddableComponentManager();
            componentManager.initialize(this.getClass().getClassLoader());
            Execution execution = componentManager.getInstance(Execution.class);
            execution.setContext(this.executionContext);
            XWikiContext context = (XWikiContext) this.executionContext.getProperty("xwikicontext");

            this.requestHandler.mail(context, this.requestWrapper);
            componentManager.dispose();
        } catch (ComponentLookupException ex) {
            // There is nothing that can be done.
        }
    }
}
