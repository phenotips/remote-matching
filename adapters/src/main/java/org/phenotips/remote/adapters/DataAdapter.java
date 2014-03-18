/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.remote.adapters;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiException;

import net.sf.json.JSONObject;

/**
 * Serves no purpose but to provide a way to inject XWiki's execution context into {@link PatientAdapterImpl}.
 */
@Unstable
@Role
public interface DataAdapter
{
    void setPatient(String patientDocumentId) throws XWikiException;

    void setSubmitter(String fullUserId) throws XWikiException;

    void setPeriodic(Boolean isPeriodic);

    JSONObject toJSON();
}
