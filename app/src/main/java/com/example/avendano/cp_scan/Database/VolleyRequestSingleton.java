package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Activities.RequestListsActivity;

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
                Toast.makeText(mCtx, "Can't connect to the server, please try again later.", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);

    }
}
