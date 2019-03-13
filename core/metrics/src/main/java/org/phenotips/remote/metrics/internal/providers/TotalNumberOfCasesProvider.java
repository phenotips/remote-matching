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
package org.phenotips.remote.metrics.internal.providers;

import org.phenotips.remote.metrics.spi.MetricProvider;

import org.xwiki.component.annotation.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;

import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

/**
 * Computes the total number of cases.
 *
 * @version $Id$
 */
@Component
@Named("totalNumberOfCases")
@Singleton
public class TotalNumberOfCasesProvider implements MetricProvider
{
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Override
    public Object compute()
    {
        Session session = null;
        try {
            session = this.sessionFactory.getSessionFactory().openSession();
            Query q = session.createQuery(
                "select count (distinct doc.name) from "
                    + "XWikiDocument as doc, BaseObject as patientObj"
                    + " where "
                    + "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass'"
                    + " and doc.fullName <> 'PhenoTips.PatientTemplate'");
            return q.uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
