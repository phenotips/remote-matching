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
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.server.RequestProcessorInterface;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.RequestInterface;
import org.phenotips.remote.hibernate.internal.HibernatePatient;
import org.phenotips.remote.hibernate.internal.IncomingSearchRequest;
import org.phenotips.remote.adapters.jsonwrappers.JSONToHibernatePatientWrapper;
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
public class IncomingRequestProcessor implements RequestProcessorInterface
{
    /** Handles persistence. */
    @Inject
    private HibernateSessionFactory sessionFactory;

    @Inject
    private SimilarPatientsFinder patientsFinder;

    @Inject
    private Execution execution;

    /** This object is populated with information (at least status) on error */
    private JSONObject errorJson = new JSONObject();

    public JSONObject processRequest(String json) throws XWikiException
    {
        XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        //FIXME will break in virtual env.
        XWikiDocument fixDoc =
            context.getWiki().getDocument(new DocumentReference("xwiki", "Main", "WebHome"), context);
        context.setDoc(fixDoc);
        //FIXME. Should not be admin.
        //Should use setUserReference(DocumentReference userReference);
        context.setUser("xwiki:XWiki.Admin");

        HibernatePatientInterface hibernatePatient;
        try {
            hibernatePatient = new JSONToHibernatePatientWrapper(JSONObject.fromObject(json));
        } catch (Exception ex) {
            errorJson.put("status", 400);
            return errorJson;
        }
        IncomingSearchRequestInterface requestObject = new IncomingSearchRequest();
        requestObject.setReferencePatient((HibernatePatient) hibernatePatient);
        //FIXME. Check if the request needs to be stored. Which should be done by the #storeRequest function.
        Long requestObjectId = storeRequest(requestObject);

        JSONObject response = new JSONObject();
        JSONArray results = new JSONArray();

        //Error here most likely means that the request contained malformed patient data, or none at all.
        List<PatientSimilarityView> similarPatients;
        try {
            similarPatients = requestObject.getResults(patientsFinder);
        } catch (IllegalArgumentException ex) {
            errorJson.put("status", getStatus(requestObject));
            return errorJson;
        }

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
        response.put("responseType", requestObject.getResponseType());
        response.put("results", results);
        response.put("status", getStatus(requestObject));

        return response;
    }

    // (FIXME?) Rather redundant
    private Integer getStatus(RequestInterface requestObject)
    { return requestObject.getResponseStatus(); }

    private Long storeRequest(RequestInterface requestObject)
    {
        Session session = this.sessionFactory.getSessionFactory().openSession();

        Transaction t = session.beginTransaction();
        t.begin();
        Long requestObjectId = (Long) session.save(requestObject);
        t.commit();

        session.close();
        return requestObjectId;
    }
}
