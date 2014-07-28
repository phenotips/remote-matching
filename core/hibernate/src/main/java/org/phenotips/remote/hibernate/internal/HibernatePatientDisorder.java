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

import org.phenotips.data.internal.AbstractPhenoTipsOntologyProperty;
import org.phenotips.remote.api.HibernatePatientDisorderInterface;
import org.phenotips.remote.api.HibernatePatientInterface;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Hibernate entity for storing patient features.
 */
@Entity
public class HibernatePatientDisorder extends AbstractPhenoTipsOntologyProperty
    implements HibernatePatientDisorderInterface
{
    @Id
    @GeneratedValue
    private long hibernateId;

    @Basic
    private String id;

    @Basic
    private String value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hibernatepatient_id", nullable = false)
    public HibernatePatient hibernatepatient;

    public HibernatePatientDisorder()
    {
        super("");
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public void setParent(HibernatePatientInterface patient)
    {
        this.hibernatepatient = (HibernatePatient) patient;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getValue()
    {
        return this.value;
    }
}
