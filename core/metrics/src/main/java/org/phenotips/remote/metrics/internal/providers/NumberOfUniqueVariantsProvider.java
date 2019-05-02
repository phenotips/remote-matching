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
 * Computes the number of variant objects, where variant cDNA is unique, associated with genes that
 * are candidate or solved that are available for matching using MME in this node.
 *
 * @version $Id$
 */
@Component
@Named("numberOfUniqueVariants")
@Singleton
public class NumberOfUniqueVariantsProvider implements MetricProvider
{
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Override
    public Object compute()
    {
        Session session = null;
        try {
            session = this.sessionFactory.getSessionFactory().openSession();

            String candidateStatuses = "";
            GENE_STATUS_VALUES.stream()
                .filter(status -> status.startsWith("candidate"))
                .forEach(status -> candidateStatuses.concat("'" + status + "',"));

            Query q = session.createQuery("select count (distinct variantIDProp.value) from "
                + HQL_BASE_MME_PATIENT_FILTER_FROM
                + ", BaseObject geneObj, BaseObject variantObj, StringProperty geneStatusProp"
                + ", StringProperty geneIDProp, StringProperty variantIDProp, StringProperty variantGeneProp where "
                + HQL_BASE_MME_PATIENT_FILTER_WHERE
                + " and geneObj.name = doc.fullName and geneObj.className = 'PhenoTips.GeneClass'"
                + " and variantObj.name = doc.fullName and variantObj.className = 'PhenoTips.GeneVariantClass'"
                + " and geneStatusProp.id.id = geneObj.id and geneStatusProp.id.name = 'status'"
                + " and geneStatusProp.value in (" + candidateStatuses + " 'solved')"
                + " and variantIDProp.id.id = variantObj.id and variantIDProp.id.name = 'cdna'"
                + " and variantGeneProp.id.id = variantObj.id and variantGeneProp.id.name = 'gene'"
                + " and geneIDProp.id.id = geneObj.id and geneIDProp.id.name = 'gene'"
                + " and geneIDProp.value = variantGeneProp.value");
            return q.uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
