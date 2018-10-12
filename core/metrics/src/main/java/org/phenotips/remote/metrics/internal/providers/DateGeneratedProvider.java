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

import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Computes the date and time at which the stats were generated, in the
 * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 format</a>.
 *
 * @version $Id$
 */
@Component
@Named("dateGenerated")
@Singleton
public class DateGeneratedProvider implements MetricProvider
{
    /** ISO8601 date and time formatter, printing dates in the format {@code 2000-12-31T23:00:00.123Z}. */
    private static final DateTimeFormatter ISO_DATETIME_FORMATTER = ISODateTimeFormat.dateTime().withZone(
        DateTimeZone.UTC);

    @Override
    public Object compute()
    {
        return ISO_DATETIME_FORMATTER.print(new DateTime());
    }
}
