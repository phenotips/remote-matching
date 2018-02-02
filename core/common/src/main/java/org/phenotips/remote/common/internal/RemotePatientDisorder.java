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
package org.phenotips.remote.common.internal;

import org.phenotips.data.Disorder;
import org.phenotips.data.internal.AbstractPhenoTipsVocabularyProperty;

public class RemotePatientDisorder extends AbstractPhenoTipsVocabularyProperty implements Disorder
{
    public RemotePatientDisorder(String id, String label)
    {
        super(id);
        if (this.name == null) {
            this.name = label;
        }
    }

    @Override
    public String getValue()
    {
        // TODO: not very clear what a "Phenotips value" of a disorder should be
        return null;
    }
}
