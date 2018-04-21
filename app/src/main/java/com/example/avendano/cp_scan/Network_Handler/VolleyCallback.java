package com.example.avendano.cp_scan.Network_Handler;

import com.android.volley.VolleyError;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public interface VolleyCallback {
    void onSuccessResponse(String response);
    void onErrorResponse(VolleyError error);
}
