package org.phenotips.remote.api;

import net.sf.json.JSONObject;

/**
 * The functions essential to the servers ability to store, track, an answer search requests.
 */
public interface RequestEntity
{
    long getRequestId();

    String getResponseType();

    boolean getResponseStatus();

    String getResponseTargetURL();

    String getSubmitterEmail();

    JSONObject getResults();
}
