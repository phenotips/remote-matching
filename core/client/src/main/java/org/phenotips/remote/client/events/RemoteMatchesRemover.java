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
package org.phenotips.remote.client.events;

import org.phenotips.data.events.PatientDeletingEvent;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;

import org.xwiki.component.annotation.Component;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Removes local matches from database for the patient that was deleted.
 *
 * @version $Id$
 */
@Component
@Named("remote-matches-remover")
@Singleton
public class RemoteMatchesRemover extends AbstractEventListener
{
    @Inject
    private RemoteMatchingStorageManager requestStorageManager;

    /** Default constructor, sets up the listener name and the list of events to subscribe to. */
    public RemoteMatchesRemover()
    {
        super("remote-matches-remover", new PatientDeletingEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument doc = (XWikiDocument) source;
        String patientId = doc.getDocumentReference().getName();

        this.requestStorageManager.deleteMatchesForLocalPatient(patientId);
    }
}