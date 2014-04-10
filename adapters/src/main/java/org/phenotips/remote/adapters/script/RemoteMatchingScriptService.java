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
package org.phenotips.remote.adapters.script;

import org.phenotips.data.Patient;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.data.similarity.PatientSimilarityViewFactory;
import org.phenotips.remote.RemoteMatchingClient;
import org.phenotips.remote.adapters.internal.OutgoingRequestConfigurator;
import org.phenotips.remote.api.WrapperInterface;
import org.phenotips.remote.adapters.jsonwrappers.OutgoingSearchRequestToJSONWrapper;
import org.phenotips.remote.api.RequestConfigurationInterface;
import org.phenotips.remote.api.OutgoingSearchRequestInterface;
import org.phenotips.remote.hibernate.internal.OutgoingSearchRequest;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Gives velocity access to the functions it needs to perform remote matching. There is a set of functions for sending
 * the request, and a set for retrieving the data.
 */
@Unstable
@Component
@Named("remoteMatching")
@Singleton
public class RemoteMatchingScriptService implements ScriptService
{
    @Inject
    private Logger logger;

    @Inject
    private Execution execution;

    @Inject
    @Named("restricted")
    private PatientSimilarityViewFactory viewFactory;

    @Inject
    private HibernateSessionFactory sessionFactory;

    public boolean sendRequest(com.xpn.xwiki.api.Object xwikiObject)
    {
        //FIXME. PatientId, submitterId should be in the requestObject.
        try {
            XWikiContext context = getContext();
            XWiki wiki = getWiki(context);

            BaseObject xwikiRequestObject = xwikiObject.getXWikiObject();
            RequestConfigurationInterface configuration = new OutgoingRequestConfigurator(xwikiRequestObject, wiki, context);
            OutgoingSearchRequestInterface requestObject = configuration.createRequest();

            WrapperInterface<OutgoingSearchRequestInterface, JSONObject> requestWrapper =
                new OutgoingSearchRequestToJSONWrapper(wiki, context);
            String result = RemoteMatchingClient.sendRequest(requestObject, requestWrapper);

            Session session = this.sessionFactory.getSessionFactory().openSession();
            Transaction t = session.beginTransaction();
            JSONObject jsonResult = JSONObject.fromObject(result);
            for (Object resultPatientUC : (JSONArray) jsonResult.get("results")) {
                requestObject.addResult((JSONObject) resultPatientUC);
            }

            Long requestObjectId = (Long) session.save(requestObject);
            t.commit();

//            EntityReference patientReference =
//                new EntityReference(patientId, EntityType.DOCUMENT, Patient.DEFAULT_DATA_SPACE);
//
//            XWikiDocument patientDoc = wiki.getDocument(patientReference, context);
//            EntityReference remoteRequestReference = new EntityReference("RemoteRequest", EntityType.DOCUMENT,
//                new EntityReference("PhenomeCentral", EntityType.SPACE));
            xwikiRequestObject.set("hibernateId", requestObjectId, context);
//
//            //FIXME. Double saving?
//            wiki.saveDocument(patientDoc, context);

            return true;
        } catch (Exception ex) {
            logger.error("Failed to send request", ex);
        }
        return false;
    }

    public List<PatientSimilarityView> getSimilarityResults(Patient patient) throws XWikiException
    {
        List<PatientSimilarityView> resultsList = new LinkedList<PatientSimilarityView>();
        try {
            Session session = this.sessionFactory.getSessionFactory().openSession();
            Transaction t = session.beginTransaction();
            t.begin();

            XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
            XWiki wiki = context.getWiki();
            XWikiDocument patientDoc = wiki.getDocument(patient.getDocument(), context);
            EntityReference remoteRequestReference = new EntityReference("RemoteRequest", EntityType.DOCUMENT,
                new EntityReference("PhenomeCentral", EntityType.SPACE));
            List<BaseObject> requestObjects = patientDoc.getXObjects(remoteRequestReference);
            for (BaseObject request : requestObjects) {
                long requestId = Long.valueOf(request.getStringValue("id"));
                OutgoingSearchRequest outgoingSearchRequest = new OutgoingSearchRequest();
                session.load(outgoingSearchRequest, requestId);

                Hibernate.initialize(outgoingSearchRequest);

//                for (HibernatePatient patientResult : outgoingSearchRequest.results) {
//                    Hibernate.initialize(patientResult);
//                }
                //FIXME. First need to have the reference patient when this is going to work for real.
                List<PatientSimilarityView> allResults = outgoingSearchRequest.getResults(viewFactory);
                resultsList.addAll(allResults);
            }

            t.commit();
            return resultsList;
        } catch (Exception ex) {
            return resultsList;
        }
    }

    private XWikiContext getContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }

    private XWiki getWiki(XWikiContext context)
    {
        return context.getWiki();
    }
}
