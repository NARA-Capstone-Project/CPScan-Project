package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class VolleyRequestSingleton {
Context mCtx;

    public VolleyRequestSingleton(Context mCtx) {
        this.mCtx = mCtx;
    }

    public void sendStringRequestPost(String url, final VolleyCallback callback, final Map<String, String> params){
        StringRequest str = new StringRequest(Request.Method.POST
                , url
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccessResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               callback.onErrorResponse(error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        int requestTimeout = 20000;
        str.setRetryPolicy(new DefaultRetryPolicy(requestTimeout, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
    }

    public void sendStringRequestGet(String url, final VolleyCallback callback){
        StringRequest str = new StringRequest(Request.Method.GET
                , url
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccessResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onErrorResponse(error);
            }
        });
        int requestTimeout = 20000;
        str.setRetryPolicy(new DefaultRetryPolicy(requestTimeout, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
    }
}
