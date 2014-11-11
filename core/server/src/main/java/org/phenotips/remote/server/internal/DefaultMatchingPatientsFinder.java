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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.phenotips.remote.server.MatchingPatientsFinder;
import org.phenotips.remote.api.MatchingPatient;
import org.phenotips.data.similarity.PatientSimilarityView;
import org.phenotips.similarity.SimilarPatientsFinder;
import org.xwiki.component.annotation.Component;

@Component
@Singleton
public class DefaultMatchingPatientsFinder implements MatchingPatientsFinder
{
    @Inject
    private SimilarPatientsFinder patientsFinder;

    public List<PatientSimilarityView> findMatchingPatients(MatchingPatient modelPatient)
    {
        return patientsFinder.findSimilarPatients(new RemoteMatchingPatient(modelPatient));
    }

}
