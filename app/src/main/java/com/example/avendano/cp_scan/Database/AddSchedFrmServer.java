package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.RequestQueueHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Avendano on 7 Mar 2018.
 */

public class AddSchedFrmServer {
    Context mCtx;
    SQLiteHandler db;

    public AddSchedFrmServer(Context mCtx, SQLiteHandler db) {
        this.mCtx = mCtx;
        this.db = db;
    }

    public void SyncFunction() {
        if(db.getSchedCount() > 0)
            db.deleteSched();
        getSchedFromServer();
    }

    private void getSchedFromServer(){
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_ROOM_SCHED
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for(int i=0; i< array.length();i++){
                        JSONObject obj = array.getJSONObject(i);
                        //room_id room name(dept + room no) custodian, custodian id technician technician id
                        //building, floor
                        int room_id = obj.getInt("room_id");
                        String prof = obj.getString("room_user");
                        String day = obj.getString("day");
                        String to_time = obj.getString("to");
                        String from_time = obj.getString("from");

                        addToSched(room_id, to_time,from_time,day,prof);
                    }
                } catch (JSONException e) {
                    Log.w("RESULT SCHED", "Error: "+ e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("RESULT SCHED", "Error: "+ error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
    }

    private void addToSched(int room_id, String to_time, String from_time, String day, String prof) {
        long insert = db.addSched(room_id,to_time,from_time,prof,day);
        Log.w("SCHED ADDED TO SQL: ", "Status: " + insert);
    }
}
