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
package org.phenotips.remote.hibernate.internal;

import org.phenotips.remote.api.ApiConfiguration;
import org.phenotips.remote.api.IncomingMatchRequest;
import org.phenotips.remote.api.OutgoingMatchRequest;
import org.phenotips.remote.hibernate.RemoteMatchingStorageManager;
import org.xwiki.component.annotation.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
            Long id = (Long) session.save(request);
            t.commit();
            this.logger.info("Stored new incoming request from server [{}] with id [{}]",
                request.getRemoteServerId(), id);
        } catch (HibernateException ex) {
            this.logger.error("ERROR storing new incoming request from server [{}]: [{}]",
                request.getRemoteServerId(), ex);
            if (t != null) {
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
            Long id = (Long) session.save(request);
            t.commit();
            this.logger.info("Saved outgoing request for patient [{}] to server [{}] with id [{}]",
                request.getLocalReferencePatientId(), request.getRemoteServerId(), id);
        } catch (HibernateException ex) {
            this.logger.error("ERROR saving outgoing request for patient [{}] to server [{}]: [{}]",
                request.getLocalReferencePatientId(), request.getRemoteServerId(), ex);
            if (t != null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
    }

    @Override
    public OutgoingMatchRequest getLastOutgoingRequest(String patientId, String remoteServerId)
    {
        return this.getLastOutgoingRequest(patientId, remoteServerId, false);
    }

    @Override
    public OutgoingMatchRequest getLastSuccessfulOutgoingRequest(String patientId, String remoteServerId)
    {
        return this.getLastOutgoingRequest(patientId, remoteServerId, true);
    }

    private OutgoingMatchRequest getLastOutgoingRequest(String patientId, String remoteServerId, boolean successful)
    {
        if (patientId == null || remoteServerId == null) {
            return null;
        }
        Session session = this.sessionFactory.getSessionFactory().openSession();
        try {
            Criteria queryCriteria = session.createCriteria(DefaultOutgoingMatchRequest.class);
            queryCriteria.add(Restrictions.eq("localReferencePatientId", patientId))
                         .add(Restrictions.eq("remoteServerId", remoteServerId));
            if (successful) {
                queryCriteria.add(Restrictions.eq("replyHTTPStatus", ApiConfiguration.HTTP_OK));
            }

            OutgoingMatchRequest data = (OutgoingMatchRequest) queryCriteria
                .addOrder(Property.forName("requestTime").desc())
                .setMaxResults(1)
                .uniqueResult();

            if (data == null) {
                this.logger.info("Found no {}match requests to server [{}] for patient [{}]",
                    (successful ? "successful " : ""), remoteServerId, patientId);
            } else {
                this.logger.info("Found a {}match request to server [{}] for patient [{}]",
                    (successful ? "successful " : ""), remoteServerId, patientId);
            }

            return data;
        } catch (HibernateException ex) {
            this.logger.error("ERROR loading last outgoing request for local patient [{}] to server [{}]: {}",
                    patientId, remoteServerId, ex);
        } finally {
            session.close();
        }
        return null;
    }

    @Override
    public void deleteMatchesForLocalPatient(String patientId)
    {
        if (patientId == null) {
            return;
        }
        Session session = this.sessionFactory.getSessionFactory().openSession();
        Transaction t = session.beginTransaction();
        try {
            Query query = session.createQuery("delete DefaultOutgoingMatchRequest where localReferencePatientId = :localId");
            query.setParameter("localId", patientId);

            int numDeleted = query.executeUpdate();

            t.commit();
            session.flush();

            this.logger.info("Removed [{}] stored outgoing match requests", numDeleted);
        } catch (HibernateException ex) {
            this.logger.error("ERROR deleting outgoing request for local patient [{}]: {}", patientId, ex);
            if (t != null) {
                t.rollback();
            }
        } finally {
            session.close();
        }
    }
}
