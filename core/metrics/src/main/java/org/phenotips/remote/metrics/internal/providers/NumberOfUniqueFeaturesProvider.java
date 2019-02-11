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
 * Computes the number of unique HPO codes in the 'phenotype' and 'prenatal phenotype' fields.
 *
 * @version $Id$
 */
@Component
@Named("numberOfUniqueFeatures")
@Singleton
public class NumberOfUniqueFeaturesProvider implements MetricProvider
{
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Override
    public Object compute()
    {
        Session session = null;
        try {
            session = this.sessionFactory.getSessionFactory().openSession();

            Query q = session.createQuery("select count (distinct elements(prop.list)) from "
                + HQL_BASE_MME_PATIENT_FILTER_FROM
                + ", DBStringListProperty as prop"
                + " where "
                + HQL_BASE_MME_PATIENT_FILTER_WHERE
                + " and prop.id.id = patientObj.id"
                + " and prop.id.name in ('phenotype', 'prenatal_phenotype')"
                + " and prop.list.size > 0");
            return q.uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
