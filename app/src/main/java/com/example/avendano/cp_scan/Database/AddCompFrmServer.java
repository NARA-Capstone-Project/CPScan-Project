package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AddCompFrmServer {

    Context mCtx;
    SQLiteHandler db;

    public AddCompFrmServer(Context mCtx, SQLiteHandler db) {
        this.mCtx = mCtx;
        this.db = db;
    }

    public void SyncFunction() {
        db.updateSync(0, "computers");
        getComputersFromServer();
    }

    private void getComputersFromServer() {
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_PC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int comp_id = obj.getInt("comp_id");
                        int room_id = 0;
                        if (!obj.isNull("room_id")) {
                            room_id = obj.getInt("room_id");
                        }
                        int pc_no = obj.getInt("pc_no");
                        String model = obj.getString("model");
                        String mb = obj.getString("mb");
                        String pr = obj.getString("pr");
                        String monitor = obj.getString("monitor");
                        String ram = obj.getString("ram");
                        String kboard = obj.getString("kboard");
                        String mouse = obj.getString("mouse");
                        String vga = obj.getString("vga");
                        String hdd = obj.getString("hdd");
                        String comp_status = obj.getString("comp_status");
                        String os = obj.getString("os");

                        Log.w("COMP: " + comp_id, " MODEL: " + model);
                        addComputers(comp_id, room_id, pc_no, os, model
                                , mb, pr, monitor, ram, kboard, mouse, vga, hdd, comp_status);
                    }
                    db.deleteAllUnsync("computers");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("RESULT", "Error: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);

    }

    private void addComputers(int comp_id, int room_id, int pc_no, String os, String model
            , String mb, String pr, String monitor, String ram, String kboard, String mouse, String vga, String hdd, String comp_status) {
        Cursor c = db.getCompDetails(comp_id);
        if (c.moveToFirst()) {
            db.updateComputers(comp_id, room_id, pc_no, os, model, mb, pr, monitor, ram, kboard
                    , mouse, comp_status, vga, hdd);
            Log.w("COMP UPDATE TO SQLITE: ", "UPDATE!");
        } else {

            long insert = db.addComputers(comp_id, room_id, pc_no, os, model, mb, pr
                    , monitor, ram, kboard, mouse, comp_status, vga, hdd);
            Log.w("COMP INSERT TO SQLITE: ", "Status : " + insert);
        }
    }
}
