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

import org.phenotips.remote.server.RequestProcessorInterface;

import org.xwiki.context.ExecutionContext;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.xpn.xwiki.objects.BaseObject;

import net.sf.json.JSONObject;

public class QueueTaskInternalProcessing implements Callable<JSONObject>
{
    private String stringJson;

    private ExecutorService queue;

    private RequestProcessorInterface requestProcessor;

    private BaseObject configurationObject;

    private ExecutionContext executionContext;

    public QueueTaskInternalProcessing(String _stringJson, ExecutorService _queue,
        RequestProcessorInterface _requestProcessor, BaseObject _configurationObject,
        ExecutionContext _executionContext)
    {
        stringJson = _stringJson;
        queue = _queue;
        requestProcessor = _requestProcessor;
        configurationObject = _configurationObject;
        executionContext = _executionContext;
    }

    @Override public JSONObject call() throws Exception
    {
        return requestProcessor.internalProcessing(stringJson, queue, configurationObject, executionContext);
    }
}
