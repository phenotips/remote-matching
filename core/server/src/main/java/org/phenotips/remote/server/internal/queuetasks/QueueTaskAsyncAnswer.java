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

import org.phenotips.remote.RemoteMatchingClient;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.MultiTypeWrapperInterface;
import org.phenotips.remote.api.RequestHandlerInterface;

import net.sf.json.JSONObject;

public class QueueTaskAsyncAnswer implements Runnable
{
    private RequestHandlerInterface<IncomingSearchRequestInterface> requestHandler;

    private MultiTypeWrapperInterface<IncomingSearchRequestInterface, JSONObject> requestWrapper;


    public QueueTaskAsyncAnswer(RequestHandlerInterface<IncomingSearchRequestInterface> _requestHandler,
        MultiTypeWrapperInterface<IncomingSearchRequestInterface, JSONObject> _requestWrapper)
    {
        requestHandler = _requestHandler;
        requestWrapper = _requestWrapper;
    }

    @Override
    public void run()
    {
        try {
            RemoteMatchingClient.sendAsyncAnswer(requestHandler.getRequest(), requestWrapper);
        } catch (Exception ex) {
            //Do nothing
        }
    }
}
