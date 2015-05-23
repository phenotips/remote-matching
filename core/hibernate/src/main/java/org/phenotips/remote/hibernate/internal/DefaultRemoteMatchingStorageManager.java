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
package org.phenotips.remote.hibernate.internal;

import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.xwiki.component.annotation.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;

import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

/**
 * Default implementation using Hibernate
 *
 * @version $Id$
 * @since 1.0M10
 */
@Component
@Singleton
public class DefaultRemoteMatchingStorageManager implements RemoteMatchingStorageManager
{
    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    /** Logging helper object. */
    @Inject
    private Logger logger;

    @Override
    public void saveIncomingRequest(IncomingMatchRequest request)
    // public String saveIncomingPeriodicRequest(IncomingSearchRequest request)
    {
        Session session = this.sessionFactory.getSessionFactory().openSession();
        Transaction t = session.beginTransaction();
        try {
            Long id = (Long)session.save(request);
            t.commit();
            this.logger.info("Stored new incoming request from server [{}] with id [{}]",
                             request.getRemoteServerId(), id);
        } catch (HibernateException ex) {
            this.logger.error("ERROR storing new incoming request from server [{}]: [{}]",
                              request.getRemoteServerId(), ex);
            if (t!=null) {
                t.rollback();
            }
            throw ex;
        } finally {
            session.close();
        }
    }

    @Override
    public void saveOutgoingRequest(OutgoingMatchRequest request)
    {
        Session session = this.sessionFactory.getSessionFactory().openSession();
        Transaction t = session.beginTransaction();
        try {
            Long id = (Long)session.save(request);
            t.commit();
            this.logger.info("Saved outgoing request for patient [{}] to server [{}] with id [{}]",
                             request.getLocalReferencePatientId(), request.getRemoteServerId(), id);
        } catch (HibernateException ex) {
            this.logger.error("ERROR saving outgoing request for patient [{}] to server [{}]: [{}]",
                              request.getLocalReferencePatientId(), request.getRemoteServerId(), ex);
            if (t!=null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
    }

    @Override
    public OutgoingMatchRequest loadCachedOutgoingRequest(String patientId, String remoteServerId)
    {
        if (patientId == null || remoteServerId == null) {
            return null;
        }
        Session session = this.sessionFactory.getSessionFactory().openSession();
        try {
            OutgoingMatchRequest data = (OutgoingMatchRequest) session.createCriteria(DefaultOutgoingMatchRequest.class)
                .add(Restrictions.eq("localReferencePatientId", patientId))
                .add(Restrictions.eq("remoteServerId", remoteServerId))
                .addOrder( Property.forName("requestTime").desc() )
                .setMaxResults(1)
                .uniqueResult();

            if (data == null) {
                this.logger.info("No outstanding match queries to server [{}] for patient [{}]", remoteServerId, patientId);
            } else {
                this.logger.info("Found an outstanding query to server [{}] for patient [{}]", remoteServerId, patientId);
            }
            return data;
        } catch (HibernateException ex) {
            this.logger.error("loadOutgoingRequest: ERROR: [{}]", ex);
        } finally {
            session.close();
        }
        return null;
    }
}
