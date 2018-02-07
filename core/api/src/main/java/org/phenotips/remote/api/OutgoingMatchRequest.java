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
package org.phenotips.remote.api;

/**
 * A raw request/response pair, sent from the local server to a connected MME node.
 *
 * @version $Id$
 */
public interface OutgoingMatchRequest extends MatchRequest
{
    /**
     * @return the identifier of the local patient sent in the match request
     */
    String getLocalReferencePatientId();

    /**
     * @return {@code true} if the request was sent already
     */
    boolean wasSent();

    /**
     * @return {@code true} if there was a problem contacting remote server
     */
    boolean errorContactingRemoteServer();

    /**
     * @return {@code true} if the remote server replied with a valid response
     */
    boolean gotValidReply();

    /**
     * @return the HTTP status code received from the server
     */
    Integer getRequestStatusCode();
}
