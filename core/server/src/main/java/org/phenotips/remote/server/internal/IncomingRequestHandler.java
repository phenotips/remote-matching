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

import org.phenotips.data.PatientRepository;
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

    @Override
    public IncomingSearchRequestInterface loadRequest(Long id, PatientRepository internal) {
        throw new UnsupportedOperationException();
    }
}
