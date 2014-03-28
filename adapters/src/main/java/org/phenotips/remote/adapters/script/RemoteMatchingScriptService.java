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
import org.phenotips.remote.adapters.DataAdapter;
import org.phenotips.remote.adapters.internal.DataAdapterImpl;
import org.phenotips.remote.api.RequestConfiguration;
import org.phenotips.remote.api.internal.HibernatePatient;
import org.phenotips.remote.api.internal.OutgoingSearchRequest;
import org.phenotips.remote.api.internal.RequestConfigurationImpl;

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

import org.apache.commons.lang3.StringUtils;
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

    public boolean sendRequest(String patientId, String submitterId, String requestGuid)
    {
        try {
            DataAdapter dataAdapter = new DataAdapterImpl(execution);
            dataAdapter.setPatient(patientId);
            dataAdapter.setSubmitter(submitterId);
            dataAdapter.setPeriodic(false);
            JSONObject json = dataAdapter.toJSON();
            RequestConfiguration configuration = new RequestConfigurationImpl();

            Session session = this.sessionFactory.getSessionFactory().openSession();

            Transaction t = session.beginTransaction();

            OutgoingSearchRequest requestObject = new OutgoingSearchRequest();

            String result = RemoteMatchingClient.sendRequest(json, configuration);

            JSONObject jsonResult = JSONObject.fromObject(result);
            for (Object resultPatientUC : (JSONArray) jsonResult.get("results")) {
                requestObject.addResult((JSONObject) resultPatientUC);
            }

            Long requestObjectId = (Long) session.save(requestObject);
            t.commit();

            XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
            XWiki wiki = context.getWiki();
            EntityReference patientReference =
                new EntityReference(patientId, EntityType.DOCUMENT, Patient.DEFAULT_DATA_SPACE);

            XWikiDocument patientDoc = wiki.getDocument(patientReference, context);
            EntityReference remoteRequestReference = new EntityReference("RemoteRequest", EntityType.DOCUMENT,
                new EntityReference("PhenomeCentral", EntityType.SPACE));
            List<BaseObject> objects = patientDoc.getXObjects(remoteRequestReference);
            for (BaseObject object : objects) {
                if (StringUtils.equalsIgnoreCase(object.getGuid(), requestGuid)) {
                    object.set("id", requestObjectId, context);
                    break;
                }
            }

            wiki.saveDocument(patientDoc, context);

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

                for (HibernatePatient patientResult : outgoingSearchRequest.results) {
                    Hibernate.initialize(patientResult);
                }
                List<PatientSimilarityView> allResults = outgoingSearchRequest.getResults(patient, viewFactory);
                resultsList.addAll(allResults);
            }

            t.commit();
            return resultsList;
        } catch (Exception ex) {
            return resultsList;
        }
    }
}