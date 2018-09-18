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
package org.phenotips.remote.metrics;

import org.phenotips.remote.api.ApiDataConverter;

import org.xwiki.component.annotation.Role;

import org.json.JSONObject;

/**
 * Processor for remote MME metrics request. Provides basic matching metrics with the following information:
 *
 * <dl>
 * <dt>numberOfCases</dt>
 * <dd>number of patient records with the granted MME consent</dd>
 * <dt>numberOfSubmitters</dt>
 * <dd>number of unique owners of patient records</dd>
 * <dt>numberOfGenes</dt>
 * <dd>number of gene objects with status solved or candidate from patient records with the granted MME consent</dd>
 * <dt>numberOfUniqueGenes</dt>
 * <dd>number of unique gene objects with status solved or candidate from patient records with the granted
 *    MME consent (each gene id should be counted only once)</dd>
 * <dt>numberOfCasesWithDiagnosis</dt>
 * <dd>number of patient records with the granted MME consent that have a final diagnosis</dd>
 * <dt>numberOfPotentialMatchesSent</dt>
 * <dd>number of rows in the match table with one remote case</dd>
 * </dl>
 *
 * @param apiVersionSpecificConverter passed to be used to generate Internal Server Error Response in case of Exception
 * @return a response containing the MME metrics data, or an error code if unsuccessful
 */
@Role
public interface MetricsRequestProcessor
{
    JSONObject generateMetricsResponse(ApiDataConverter apiVersionSpecificConverter) throws Exception;
}
