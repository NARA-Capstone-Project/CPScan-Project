package com.example.avendano.cp_scan.Database;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by Avendano on 7 Apr 2018.
 */

public class VolleyRequestSingleton {
    public String sendStringRequestPostMethod(String url, final Map<String, String> params){
        String resp = "";
        StringRequest str = new StringRequest(Request.Method.POST
                , url
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String getResponse = response;

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        return "";
    }


}
