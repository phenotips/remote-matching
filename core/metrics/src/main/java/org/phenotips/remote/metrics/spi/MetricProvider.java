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
package org.phenotips.remote.metrics.spi;

import org.phenotips.data.internal.PhenoTipsGene;
import org.phenotips.remote.metrics.MetricsRequestHandler;

import org.xwiki.component.annotation.Role;

import java.util.List;

/**
 * Provides a specific metric to be included in a {@link MetricsRequestHandler#getMetrics() metrics response}. The
 * produced metric value will be included in the response JSON, inside the {@code metrics} map, under the same name as
 * the actual implementation's {@code @Named} name.
 *
 * @version $Id$
 */
@Role
public interface MetricProvider
{
    /** A "from" part of a query to select non-private patients with matching MME consent granted. **/
    String HQL_BASE_MME_PATIENT_FILTER_FROM =
        "XWikiDocument as doc, BaseObject as patientObj,"
            + " BaseObject consentObj, DBStringListProperty consentProp,"
            + " BaseObject visibilityObj, StringProperty visibilityProp";

    /** A "where" part of a query to select non-private patients with matching MME consent granted. **/
    String HQL_BASE_MME_PATIENT_FILTER_WHERE =
        "patientObj.name = doc.fullName and patientObj.className = 'PhenoTips.PatientClass'"
            + " and doc.fullName <> 'PhenoTips.PatientTemplate'"

            + " and consentObj.name = doc.fullName and consentObj.className = 'PhenoTips.PatientConsent'"
            + " and consentProp.id.id = consentObj.id and consentProp.id.name = 'granted'"
            + " and 'matching' in elements(consentProp.list)"

            + " and visibilityObj.name = doc.fullName and visibilityObj.className = 'PhenoTips.VisibilityClass'"
            + " and visibilityProp.id.id = visibilityObj.id and visibilityProp.id.name = 'visibility'"
            + " and visibilityProp.value <> 'private'";

    List<String> GENE_STATUS_VALUES = PhenoTipsGene.getStatusValues();

    /**
     * Compute and return a specific metric to be included in the response to a MME metrics query.
     *
     * @return a metric value, usually a number; if {@code null}, this specific metric will be skipped from the response;
     *             any exception thrown by the implementation will be caught and ignored by the aggregator code in
     *             {@link MetricsRequestHandler#getMetrics()}
     */
    Object compute();
}
