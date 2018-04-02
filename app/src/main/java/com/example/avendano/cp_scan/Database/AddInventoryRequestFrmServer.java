package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Model.ReportDetails;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AddInventoryRequestFrmServer {

    Context mCtx;
    SQLiteHandler db;

    public AddInventoryRequestFrmServer(Context mCtx, SQLiteHandler db) {
        this.mCtx = mCtx;
        this.db = db;
    }

    public void SyncFunction() {
        db.updateSync(0, "req_inv");
        getInventoryRequest();
    }

    private void getInventoryRequest() {
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_INVENTORY_REQUEST
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
                                int room_id = obj.getInt("room_id");
                                String cust_id = obj.getString("custodian");
                                String tech_id = obj.getString("technician");
                                String date = obj.getString("date");
                                String time = obj.getString("time");
                                String msg = obj.getString("msg");
                                String req_status = obj.getString("req_status");
                                String req_date = obj.getString("date_requested");
                                String req_time = obj.getString("time_requested");

                                addInventoryRequest(req_id, rep_id, room_id, cust_id, tech_id, date, time,
                                        msg, req_date, req_time, req_status);
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

    private void addInventoryRequest(int req_id, int rep_id, int room_id, String cust_id
            , String tech_id, String date, String time, String msg, String req_date, String req_time, String status) {
        Cursor c = db.getReqInventoryDetails(req_id);
        if(c.moveToFirst()){
            db.updateRequestInventory(req_id, rep_id,room_id,cust_id,tech_id,date, time, msg,req_date
            ,req_time,status);
            Log.w("UPDATE: ", "INVENTORY REQUEST");
        }else{
            long insert = db.addReqInventory(req_id, rep_id, room_id, cust_id, tech_id, date, time, msg,
                    req_date, req_time, status);
            Log.w("INSERT TO SQLITE: ", "INVENTORY REQUEST : " + insert);
        }
    }

    public void getReqIventory() {

        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_INVENTORY_REQUEST
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
                        int room_id = obj.getInt("room_id");
                        String cust_id = obj.getString("custodian");
                        String tech_id = obj.getString("technician");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        String msg = obj.getString("msg");
                        String req_status = obj.getString("req_status");
                        String req_date = obj.getString("date_requested");
                        String req_time = obj.getString("time_requested");

                        checkInventoryRequest(req_id, rep_id, room_id, cust_id, tech_id, date, time,
                                msg, req_date, req_time, req_status);

                    }
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

    private void checkInventoryRequest(int req_id, int rep_id, int room_id, String cust_id,
                                       String tech_id, String date, String time, String msg,
                                       String req_date, String req_time, String req_status) {
        Cursor c = db.getReqInventoryDetails(req_id);
        if (!c.moveToFirst()) {
            long insert = db.addReqInventory(req_id, rep_id, room_id, cust_id, tech_id, date, time, msg,
                    req_date, req_time, req_status);
            Log.w("INSERT TO SQLITE: ", "INVENTORY REQUEST : " + insert);
        }
    }
}
