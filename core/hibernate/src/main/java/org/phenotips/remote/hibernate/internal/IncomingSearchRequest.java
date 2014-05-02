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
package org.phenotips.remote.hibernate.internal;

import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.remote.api.Configuration;
import org.phenotips.remote.api.HibernatePatientInterface;
import org.phenotips.remote.api.IncomingSearchRequestInterface;
import org.phenotips.similarity.SimilarPatientsFinder;

import org.xwiki.model.reference.DocumentReference;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons. It is a combination
 * of
 * a Patient interface, and a Request interface. Some functions, such as getId are ambiguous, because they can apply
 * both to the patient and the request. However, this seems to be the lesser evil at this time.
 *
 * @version $Id$
 */
@Entity
@DiscriminatorValue("incoming")
public class IncomingSearchRequest extends AbstractRequest implements IncomingSearchRequestInterface
{
    @OneToOne(mappedBy = "requestentity", cascade = CascadeType.ALL)
    private HibernatePatient referencePatient = null;

    @Transient
    private Integer httpStatus = Configuration.HTTP_OK;

    public IncomingSearchRequest()
    {}

    @Override
    protected String getURLSuffix() throws Exception
    {
        if (StringUtils.equals(this.getResponseType(), Configuration.REQUEST_RESPONSE_TYPE_ASYCHRONOUS)) {
            return Configuration.REMOTE_URL_ASYNCHRONOUS_RESULTS_ENDPOINT;
        }
        return "";
    }

    public void setReferencePatient(HibernatePatientInterface patient)
    {
        referencePatient = (HibernatePatient) patient;
    }

    public HibernatePatientInterface getReferencePatient() throws IllegalArgumentException
    {
        if (referencePatient == null) {
            //FIXME. This should be custom.
            httpStatus = Configuration.HTTP_BAD_REQUEST;
            throw new IllegalArgumentException("The reference patient for the incoming request has not been set");
        }
        return referencePatient;
    }

    public List<PatientSimilarityView> getResults(SimilarPatientsFinder finder) throws IllegalArgumentException
    {
        try {
            return finder.findSimilarPatients(getReferencePatient());
        } catch (IllegalArgumentException ex) {
            httpStatus = Configuration.HTTP_BAD_REQUEST;
            return null;
        }
    }

    public Integer getHttpStatus() { return httpStatus; }

//    public String getExternalId() { return "RemoteRequest" + id; }
    public String getExternalId() { return id == null ? super.getExternalId() : id.toString(); }

    public DocumentReference getDocument()
    {
        //FIXME this is ugly. Can't leave empty.
        DocumentReference fakeReference = new DocumentReference("xwiki", "data", "0");
        return fakeReference;
    }

    public DocumentReference getReporter()
    {
        return new DocumentReference("xwiki", "XWiki", "Admin");
    }

    @Override
    public void setHTTPStatus(Integer httpStatus)
    {
        this.httpStatus = httpStatus;
    }

    @Override public Integer getHTTPStatus()
    {
        return this.httpStatus;
    }
}