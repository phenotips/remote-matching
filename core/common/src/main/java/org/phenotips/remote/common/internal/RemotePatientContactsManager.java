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

import org.phenotips.data.ContactInfo;
import org.phenotips.data.PatientContactsManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RemotePatientContactsManager implements PatientContactsManager
{
    private ContactInfo info;

    public RemotePatientContactsManager()
    {
    }

    public RemotePatientContactsManager(ContactInfo info)
    {
        this.info = info;
    }

    @Override
    public int size()
    {
        return (this.info == null ? 0 : 1);
    }

    @Override
    public ContactInfo getFirst()
    {
        return this.info;
    }

    @Override
    public Collection<ContactInfo> getAll()
    {
        if (this.info == null) {
            return Collections.emptyList();
        } else {
            Collection<ContactInfo> all = new ArrayList<>();
            all.add(this.info);
            return all;
        }
    }

    @Override
    public Collection<String> getEmails()
    {
        Collection<String> allEmails = new ArrayList<>();
        for (ContactInfo info : getAll()) {
        	List<String> emails = info.getEmails();
            if (!emails.isEmpty()) {
                allEmails.addAll(emails);
            }
        }
        return allEmails;
    }
}