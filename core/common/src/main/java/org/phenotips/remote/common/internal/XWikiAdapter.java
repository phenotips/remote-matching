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

import org.phenotips.data.Patient;
import org.phenotips.data.internal.PhenoTipsPatient;
import org.phenotips.remote.common.ApplicationConfiguration;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
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
    public static BaseObject getSubmitter(String userId, XWiki wiki, XWikiContext context,
        DocumentReferenceResolver<String> resolver) throws XWikiException
    {
        // Fixme. Does not check if the document is a user, but will return null if not.
        // Fixme. [possible] Does not check if the patient belongs to the user?

        EntityReference userReference = resolver.resolve(userId);
        return wiki.getDocument(userReference, context).getXObject(ApplicationConfiguration.USER_OBJECT_REFERENCE);
    }

    public static Patient getPatient(XWikiDocument doc)
    {
        return new PhenoTipsPatient(doc);
    }

    public static XWikiDocument getPatientDoc(String patientId, XWikiContext context)
    {
        try {
            EntityReference patientReference =
                new EntityReference(patientId, EntityType.DOCUMENT, Patient.DEFAULT_DATA_SPACE);

            XWiki wiki = context.getWiki();

            return wiki.getDocument(patientReference, context);
        } catch (Exception ex) {
            return null;
        }
    }
}
