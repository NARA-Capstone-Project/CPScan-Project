package com.example.avendano.cp_scan.Network_Handler;

import com.android.volley.VolleyError;

import org.json.JSONArray;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public interface VolleyJSONArrayCallback {
    void onSuccessResponse(JSONArray response);
    void onErrorResponse(VolleyError error);
}
