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

import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.remote.api.MatchingPatientGene;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Hibernate entity for storing patient candidate genes.
 */
@Entity
public class HibernatePatientGene implements MatchingPatientGene
{
    @Id
    @GeneratedValue
    private long hibernateId;

    @Basic
    private String geneName;

    @Basic
    private String assembly;

    @Basic
    private String referenceName;

    @Basic
    private Long start;

    @Basic
    private Long end;

    @Basic
    private String mutationType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hibernatepatient_id", nullable = false)
    public HibernatePatient hibernatepatient;

    public HibernatePatientGene()
    {
    }

    @Override
    public String getName()
    {
        return this.geneName;
    }

    @Override
    public void setParent(MatchingPatient patient)
    {
        this.hibernatepatient = (HibernatePatient) patient;
    }

    @Override
    public void setName(String geneName)
    {
        this.geneName = geneName;
    }
}
