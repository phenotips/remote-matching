package org.phenotips.remote.server.internal;

import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.remote.api.RequestHandlerInterface;
import org.phenotips.remote.api.WrapperInterface;

import org.hibernate.Session;
import org.hibernate.Transaction;

import net.sf.json.JSONObject;

/**
 * TODO.
 */
public class IncomingRequestHandler implements RequestHandlerInterface<IncomingSearchRequestInterface>
{
    IncomingSearchRequestInterface request;

    JSONObject json;

    WrapperInterface<JSONObject, HibernatePatientInterface> patientWrapper;

    WrapperInterface<JSONObject, IncomingSearchRequestInterface> metaWrapper;

    public IncomingRequestHandler(JSONObject json, WrapperInterface<JSONObject, HibernatePatientInterface> patientWrapper,
        WrapperInterface<JSONObject, IncomingSearchRequestInterface> metaWrapper)
    {
        this.json = json;
        this.patientWrapper = patientWrapper;
        this.metaWrapper = metaWrapper;
    }

    @Override
    public IncomingSearchRequestInterface createRequest()
    {
        request = metaWrapper.wrap(json);
        if (!request.getHTTPStatus().equals(Configuration.HTTP_OK)) {
            return request;
        }

        HibernatePatientInterface hibernatePatient = patientWrapper.wrap(JSONObject.fromObject(json));
        if (hibernatePatient == null) {
            request.setHTTPStatus(Configuration.HTTP_BAD_REQUEST);
            return request;
        }

        request.setReferencePatient(hibernatePatient);

        return request;
    }

    @Override
    public Long saveRequest(Session session)
    {
        Transaction t = session.beginTransaction();
        Long id;
        if (request.getRequestId() == null) {
            id = (Long) session.save(request);
        } else {
            session.saveOrUpdate(request);
            id = request.getRequestId();
        }
        t.commit();

        return id;
    }
}
