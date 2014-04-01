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
package org.phenotips.remote.hibernate;

import org.phenotips.remote.api.internal.HibernatePatient;
import org.phenotips.remote.api.internal.IncomingSearchRequest;
import org.phenotips.remote.api.internal.OutgoingSearchRequest;

import org.xwiki.component.annotation.Component;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.cfg.Configuration;

import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

/**
 * TODO fix the doc
 * Registers the IncomingSearchRequest with Hibernate
 * 
 * @version $Id: 47ee02c319f36bb8f11d9b37bd16052635f82614 $
 * @since 1.0M10
 */
@Component
@Named("remote-matching-incoming-search-request-orm-registration")
@Singleton
public class ORMRegistrationHandler implements EventListener
{
    /** The Hibernate session factory where the entity must be registered. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Override
    public String getName()
    {
        return "remote-matching-incoming-search-request-orm-registration";
    }

    @Override
    public List<Event> getEvents()
    {
        return Collections.<Event> singletonList(new ApplicationStartedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        Configuration configuration = this.sessionFactory.getConfiguration();
        configuration.addAnnotatedClass(HibernateFeature.class);
        configuration.addAnnotatedClass(IncomingSearchRequest.class);
        configuration.addAnnotatedClass(HibernatePatientFeature.class);
        configuration.addAnnotatedClass(HibernatePatient.class);
        configuration.addAnnotatedClass(OutgoingSearchRequest.class);
    }
}
