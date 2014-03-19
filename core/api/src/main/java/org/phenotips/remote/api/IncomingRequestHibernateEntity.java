package org.phenotips.remote.api;

import org.phenotips.data.Disorder;
import org.phenotips.data.Feature;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientData;

import org.xwiki.model.reference.DocumentReference;

import java.util.Set;

import net.sf.json.JSONObject;

/**
 * Class for storing an incoming request outside the main PhenoTips database for privacy reasons. It is a combination of
 * a Patient interface, and a Request interface. Some functions, such as getId are ambiguous, because they can apply
 * both to the patient and the request. However, this seems to be the lesser evil at this time.
 *
 * @version $Id$
 */
public class IncomingSearchRequest implements Patient, RequestEntity
{
    public long getRequestId();

    public String getResponseType();

    public boolean getResponseStatus();

    public String getResponseTargetURL();

    public String getSubmitterEmail();

    public JSONObject getResults();

    public String getId();

    public String getExternalId();

    public DocumentReference getDocument();

    public DocumentReference getReporter();

    public Set<? extends Feature> getFeatures();

    public Set<? extends Disorder> getDisorders();

    public <T> PatientData<T> getData(String name)
    {
        throw new UnsupportedOperationException();
    }

    public JSONObject toJSON()
    {
        throw new UnsupportedOperationException();
    }
}
