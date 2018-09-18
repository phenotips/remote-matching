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
 * MME metrics JSON, see https://github.com/ga4gh/mme-apis/blob/1.1/metrics-api.md.
 */
public interface MMEMetricsApiConfiguration
{
    String JSON_MME_METRICS = "metrics";

    String JSON_MME_NUMBER_OF_CASES = "numberOfCases";

    String JSON_MME_NUMBER_OF_SUBMITTERS = "numberOfSubmitters";

    String JSON_MME_NUMBER_OF_GENES = "numberOfGenes";

    String JSON_MME_NUMBER_OF_UNIQUEGENES = "numberOfUniqueGenes";

    String JSON_MME_NUMBER_OF_VARIANTS = "numberOfVariants";

    String JSON_MME_NUMBER_OF_UNIQUE_VARIANTS = "numberOfUniqueVariants";

    String JSON_MME_NUMBER_OF_FEATURES = "numberOfFeatures";

    String JSON_MME_NUMBER_OF_UNIQUE_FEATURES = "numberOfUniqueFeatures";

    String JSON_MME_NUMBER_OF_FEATURE_SETS = "numberOfFeatureSets";

    String JSON_MME_NUMBER_OF_UNIQUE_GENES_MATCHED = "numberOfUniqueGenesMatched";

    String JSON_MME_NUMBER_OF_CASES_WITH_DIAGNOSIS = "numberOfCasesWithDiagnosis";

    String JSON_MME_NUMBER_OF_REQUESTS_RECEIVED = "numberOfRequestsReceived";

    String JSON_MME_NUMBER_OF_POTENTIAL_MATCHES_SENT = "numberOfPotentialMatchesSent";

    String JSON_MME_DATE_GENERATED = "dateGenerated";
}
