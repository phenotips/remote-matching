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

import org.phenotips.matchingnotification.storage.MatchStorageManager;
import org.phenotips.remote.metrics.spi.MetricProvider;

import org.xwiki.component.annotation.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Computes the number of potential matches returned by this node as the result of all MME requests.
 *
 * @version $Id$
 */
@Component
@Named("numberOfPotentialMatchesSent")
@Singleton
public class NumberOfPotentialMatchesSentProvider implements MetricProvider
{
    @Inject
    private MatchStorageManager matchStorageManager;

    @Override
    public Object compute()
    {
        return this.matchStorageManager.getNumberOfRemoteMatches();
    }
}
