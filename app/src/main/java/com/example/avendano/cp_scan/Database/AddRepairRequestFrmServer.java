package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AddRepairRequestFrmServer {

    Context mCtx;
    SQLiteHandler db;

    public AddRepairRequestFrmServer(Context mCtx, SQLiteHandler db) {
        this.mCtx = mCtx;
        this.db = db;
    }

    public void SyncFunction() {
        db.updateSync(0, "req_rep");
        getRepairRequest();
    }

    private void getRepairRequest() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_ALL_REPAIR_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                                int req_id = obj.getInt("req_id");
                                int rep_id = 0;
                                if (!obj.isNull("rep_id"))
                                    rep_id = obj.getInt("rep_id");
                                int comp_id = obj.getInt("comp_id");
                                String cust_id = obj.getString("custodian");
                                String tech_id = obj.getString("technician");
                                String date = obj.getString("date");
                                String time = obj.getString("time");
                                String msg = obj.getString("msg");
                                String req_status = obj.getString("req_status");
                                String req_date = obj.getString("date_requested");
                                String req_time = obj.getString("time_requested");
                                String req_details = obj.getString("req_details");

                                addRepairRequest(req_id, rep_id, comp_id, cust_id, tech_id, date, time,
                                        msg, req_date, req_time, req_status, req_details);
                    }
                    db.deleteAllUnsync("req_inv");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("RESULT", "Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("id", SharedPrefManager.getInstance(mCtx).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);

    }

    private void addRepairRequest(int req_id, int rep_id, int comp_id, String cust_id
            , String tech_id, String date, String time, String msg, String req_date, String req_time,
                                  String status, String req_details) {
        Cursor c = db.getReqRepairDetails(req_id);
        if(c.moveToFirst()){
            db.updateReqRepair(req_id, rep_id,comp_id,cust_id,tech_id,date,time,msg, req_details,req_date
            , req_time, status);
            Log.w("UPDATE: ", "REPAIR REQUEST");
        }else{
            long insert = db.addReqRepair(req_id, rep_id, comp_id,cust_id, tech_id,date,time,msg
            ,req_details,req_date, req_time,status);
            Log.w("INSERT TO SQLITE: ", "REPAIR REQUEST : " + insert);
        }
    }
}
