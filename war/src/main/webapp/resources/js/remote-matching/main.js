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

var RemoteMatching = (function (RemoteMatching)
{
    // Start augmentation.
    var outgoingRequest = RemoteMatching.outgoingRequest = RemoteMatching.outgoingRequest || {};

    require(['jquery'], function ($)
    {
        outgoingRequest.simpleAjaxCall = function (url, data, domObj)
        {
            $.get(url, data, function (response)
            {
//                domObj.html(response);
            });
        };

        $("div.remoteSearch span.buttonwrapper a").click(function (event)
        {
            event.stopPropagation();
            var parent = $(event.target).parents("div.remoteSearch");
            var requestGuid = parent.attr("id");
            var patientId = parent.children("input#patientId").val();
            var user = parent.children("input#userId").val();
            var url = parent.children("input#url").val();
            outgoingRequest.simpleAjaxCall(url, {"patientId": patientId, "userId": user, "guid": requestGuid},
                parent.children("span#requestStatus"));
            return false;
        });
    });

    // End augmentation.
    return RemoteMatching;
}(RemoteMatching || {}));
