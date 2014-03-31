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
package org.phenotips.remote.adapters.internal;

import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;
import org.phenotips.data.internal.PhenoTipsPatient;
import org.phenotips.ontology.internal.solr.SolrOntologyTerm;
import org.phenotips.remote.adapters.DataAdapter;

import org.xwiki.component.phase.InitializationException;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Contains functions used to extract data and format it into JSON.
 *
 * TODO Maybe come up with a better name (jsonRequestAdapter?). TODO The adapters most likely will need to be split up
 * into an "export" and "import" packages to avoid circular dependencies TODO Strings into configuration file! This is
 * bad.
 */
//@Component
//@Unstable
//@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DataAdapterImpl implements DataAdapter
{
    //    @Inject
    private Execution execution;

    private XWikiContext context;

    private XWiki wiki;

    private Patient patient;

    private BaseObject submitter;

    private String queryType;

    private Boolean setPatientCalled = false;

    public DataAdapterImpl(Execution execution) throws InitializationException
    {
        this.execution = execution;
        context = (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        wiki = context.getWiki();
    }

    public void setPatient(String patientDocId) throws XWikiException
    {
        EntityReference patientReference =
            new EntityReference(patientDocId, EntityType.DOCUMENT, Patient.DEFAULT_DATA_SPACE);

        XWikiDocument doc = wiki.getDocument(patientReference, context);

        patient = new PhenoTipsPatient(doc);
        setPatientCalled = true;
    }

    public void setPatient(Patient patientObject)
    {
        patient = patientObject;
        setPatientCalled = true;
    }

    public Patient getPatient()
    {
        return patient;
    }

    public void setSubmitter(String fullUserId) throws XWikiException
    {
        //TODO field names should be in a configuration
        //Fixme does not check if the document is a user
        String[] splitUserId = fullUserId.split("\\.");
        if (splitUserId.length != 2) {
            //TODO This isn't exactly true
            throw new XWikiException();
        }
        DocumentReference userReference = new DocumentReference("xwiki", splitUserId[0], splitUserId[1]);
        EntityReference xwikiSpace = new EntityReference(splitUserId[0], EntityType.SPACE);
        EntityReference userObjectReference = new EntityReference("XWikiUsers", EntityType.DOCUMENT,
            xwikiSpace);

        submitter = wiki.getDocument(userReference, context).getXObject(userObjectReference);
    }

    public void setPeriodic(Boolean isPeriodic)
    {
        if (isPeriodic) {
            queryType = "periodic";
        } else {
            queryType = "once";
        }
    }

    public JSONObject patientJSON() throws Exception
    {
        /*
            The way this is implemented is that if when looking up a piece of data in the extraData of the patient,
            that piece is not found, this function will throw an exception.
            FIXME In the API there should be an interface class that would specify exactly how to hold this data.
            FIXME This way the API will be more reusable
        */
        if (!setPatientCalled) {
            //Fixme exception not specific enough
            throw new Exception("The patient object was never set");
        }

        Map<String, String> remappedGlobalQualifierStrings = new HashMap<String, String>();
        Map<String, String> remappedGlobalQualifiers = new HashMap<String, String>();
        remappedGlobalQualifierStrings.put("global_age_of_onset", "age_of_onset");
        remappedGlobalQualifierStrings.put("global_mode_of_inheritance", "mode_of_inheritance");

        JSONObject json = new JSONObject();
        JSONArray disorders = new JSONArray();
        JSONArray features = new JSONArray();

        PatientData<ImmutablePair<String, SolrOntologyTerm>> globalQualifiers =
            patient.<ImmutablePair<String, SolrOntologyTerm>>getData("global-qualifiers");
        if (globalQualifiers != null) {
            for (ImmutablePair<String, SolrOntologyTerm> qualifierPair : globalQualifiers) {
                for (String key : remappedGlobalQualifierStrings.keySet()) {
                    //Could do contains, but is it safe?
                    if (StringUtils.equalsIgnoreCase(qualifierPair.getLeft(), key)) {
                        remappedGlobalQualifiers
                            .put(remappedGlobalQualifierStrings.get(key), qualifierPair.getRight().getId());
                        break;
                    }
                }
            }
        }
//        PatientData<ImmutablePair<String, String>> getSexData = patient.<ImmutablePair<String, String>>getData("sex");
//        ImmutablePair<String, String> getSexPair = getSexData.get(0);
//        String sex = getSexPair.getRight();
        String sex = patient.<ImmutablePair<String, String>>getData("sex").get(0).getRight();

        for (Disorder disease : patient.getDisorders()) {
            disorders.add(disease.toJSON().get("id"));
        }
        for (Feature phenotype : patient.getFeatures()) {
            JSONObject phenotypeJson = phenotype.toJSON();
            JSONObject featureJson = new JSONObject();
            featureJson.put("id", phenotypeJson.get("id"));
            try {
                featureJson.put("observed", trueFalseToYesNo(phenotypeJson.getString("isPresent")));
            } catch (Exception ex) {
                featureJson.put("observed", phenotypeJson.getString("observed"));
            }
            Object ageOfOnset = phenotypeJson.get("age_of_onset");
            if (ageOfOnset != null) {
                featureJson.put("ageOfOnset", ageOfOnset.toString());
            }
            features.add(featureJson);
        }

        json.put("id", patient.getId());
        //TODO check if label should correspond to the external id
        json.put("label", patient.getExternalId());
        json.put("gender", sex);
        json.putAll(remappedGlobalQualifiers);
        json.put("disorders", disorders);
        json.put("features", features);

        return json;
    }

    private JSONObject submitterJSON()
    {
        JSONObject json = new JSONObject();
        json.put("name", submitter.getStringValue("first_name") + " " + submitter.getStringValue("last_name"));
        json.put("email", submitter.getStringValue("email"));
        //FIXME Did this field's name get changed?
        json.put("institution", submitter.getStringValue("company"));

        return json;
    }

    public JSONObject toJSON() throws Exception
    {
        //TODO make sure all setters were called
        //It is much easier to take the patient JSON object and plug the rest of the data into it, than to be
        //ideologically correct and do proper merging.

        JSONObject fullJson;
        JSONObject submitterJson = submitterJSON();
        JSONObject patientJson;
        try {
            patientJson = patientJSON();
        } catch (Exception ex) {
            throw ex;
        }
        fullJson = patientJson;
        fullJson.put("submitter", submitterJson);
        fullJson.put("queryType", queryType);

        return fullJson;
    }

    protected static String trueFalseToYesNo(String text)
    {
        if (StringUtils.equalsIgnoreCase(text, "true")) {
            return "yes";
        } else if (StringUtils.equalsIgnoreCase(text, "false")) {
            return "no";
        } else {
            return text;
        }
    }
}
