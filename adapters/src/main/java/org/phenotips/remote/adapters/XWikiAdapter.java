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

import org.phenotips.data.Patient;
import org.phenotips.data.internal.PhenoTipsPatient;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * TODO.
 */
public class XWikiAdapter
{
    static public BaseObject getSubmitter(String userId, XWiki wiki, XWikiContext context) throws XWikiException
    {
        //TODO field names should be in a configuration
        //Fixme does not check if the document is a user
        String[] splitUserId = userId.split("\\.");
        if (splitUserId.length != 2) {
            //TODO This isn't exactly true
            throw new XWikiException();
        }
        DocumentReference userReference = new DocumentReference("xwiki", splitUserId[0], splitUserId[1]);
        EntityReference xwikiSpace = new EntityReference(splitUserId[0], EntityType.SPACE);
        EntityReference userObjectReference = new EntityReference("XWikiUsers", EntityType.DOCUMENT,
            xwikiSpace);

        return wiki.getDocument(userReference, context).getXObject(userObjectReference);
    }

    static public Patient getPatient(String patientId, XWiki wiki, XWikiContext context) throws XWikiException
    {
        EntityReference patientReference =
            new EntityReference(patientId, EntityType.DOCUMENT, Patient.DEFAULT_DATA_SPACE);

        XWikiDocument doc = wiki.getDocument(patientReference, context);

        return new PhenoTipsPatient(doc);
    }
}
