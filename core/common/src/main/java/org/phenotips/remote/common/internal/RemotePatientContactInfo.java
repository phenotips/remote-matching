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

import org.phenotips.remote.api.ContactInfo;

public class RemotePatientContactInfo implements ContactInfo
{
    private String contactName;
    private String contactInstitution;
    private String contactHREF;

    public RemotePatientContactInfo(String contactName, String contactInstitution, String contactHREF)
    {
        this.contactName = contactName;
        this.contactInstitution = contactInstitution;
        this.contactHREF = contactHREF;
    }

    @Override
    public String getContactName()
    {
        return this.contactName;
    }

    @Override
    public String getContactInstitution()
    {
        return this.contactInstitution;
    }

    @Override
    public String getContactHREF()
    {
        return this.contactHREF;
    }

}
