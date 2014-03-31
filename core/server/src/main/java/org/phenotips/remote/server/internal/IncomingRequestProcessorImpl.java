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
package org.phenotips.remote.server.internal;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.adapters.internal.OutgoingResultsAdapter;
import org.phenotips.remote.api.IncomingRequestProcessor;
import org.phenotips.remote.api.internal.IncomingSearchRequest;
import org.phenotips.similarity.SimilarPatientsFinder;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Takes a json string in the constructor and does all the request processing functionality.
 */
@Component
@Singleton
public class IncomingRequestProcessorImpl implements IncomingRequestProcessor
{
    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Inject
    private SimilarPatientsFinder patientsFinder;

    @Inject
    private Execution execution;

//    @Inject
//    private DataAdapter dataAdapter;

    public JSONObject processRequest(String json) throws XWikiException
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        //FIXME will break in virtual env.
        XWikiDocument fixDoc =
            context.getWiki().getDocument(new DocumentReference("xwiki", "Main", "WebHome"), context);
        context.setDoc(fixDoc);
        //Fixme. Should not be admin.
        //Should use setUserReference(DocumentReference userReference);
        context.setUser("xwiki:XWiki.Admin");

        Session session = this.sessionFactory.getSessionFactory().openSession();

        IncomingSearchRequest requestObject = new IncomingSearchRequest(JSONObject.fromObject(json), session);

        Transaction t = session.beginTransaction();
        t.begin();
        Long requestObjectId = (Long) session.save(requestObject);
        t.commit();

        JSONObject response = new JSONObject();
        JSONArray results = new JSONArray();

        List<PatientSimilarityView> similarPatients = requestObject.getResults(patientsFinder);
        for (PatientSimilarityView patient : similarPatients) {
            OutgoingResultsAdapter resultsAdapter = new OutgoingResultsAdapter();
            resultsAdapter.setPatient(patient);
            try {
                results.add(resultsAdapter.patientJSON());
            } catch (Exception ex) {
                //Should not happen
            }
        }

        response.put("queryID", requestObjectId);
        response.put("responseType", "inline");
        response.put("results", results);

        return response;
    }
}
