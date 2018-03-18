package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AddReportsFrmServer {

    Context mCtx;
    SQLiteHandler db;

    public AddReportsFrmServer(Context mCtx) {
        this.mCtx = mCtx;
        this.db = new SQLiteHandler(mCtx);
    }

    public void addAllReports() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_REPORT
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if (response != null && response.length() > 0) {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int rep_id = obj.getInt("rep_id");
                            String cat = obj.getString("category");
                            int room_id = obj.getInt("room_id");
                            String cust_id = obj.getString("cust_id");
                            String tech_id = obj.getString("tech_id");
                            String date = obj.getString("date");
                            String time = obj.getString("time");
                            int signed = obj.getInt("cust_signed");
                            String remarks = obj.getString("remarks");
                            int comp_id = obj.getInt("comp_id");
                            String model = obj.getString("model");
                            String mb = obj.getString("mb");
                            String mb_serial = obj.getString("mb_serial");
                            String pr = obj.getString("pr");
                            String monitor = obj.getString("monitor");
                            String mon_serial = obj.getString("mon_serial");
                            String ram = obj.getString("ram");
                            String kb = obj.getString("kb");
                            String mouse = obj.getString("mouse");
                            String vga = obj.getString("vga");
                            String hdd = obj.getString("hdd");
                            String comp_status = obj.getString("status");
                            String room_name = obj.getString("room_name");
                            int pc_no = obj.getInt("pc_no");
                            int htech_signed = obj.getInt("htech_signed");
                            int admin_signed = obj.getInt("admin_signed");


                            saveReportToLocal(rep_id, room_id, cust_id, cat, tech_id, date, time,
                                    signed, htech_signed, admin_signed,
                                    remarks, room_name);

                            saveDetailsToLocal(rep_id, comp_id, pc_no, model, mb, mb_serial, pr, monitor
                                    , mon_serial, ram, kb, mouse, vga, hdd, comp_status);
                        }
                    }
                } catch (Exception e) {
                    Log.e("REPORTS DB: ", "EXCEPTION: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RESPONSE ERROR(syncreport): ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(mCtx).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);
    }


    private void saveDetailsToLocal(int rep_id, int comp_id, int pc_no, String model, String mb, String mb_serial
            , String pr, String monitor, String mon_serial, String ram, String kboard, String mouse, String vga
            , String hdd, String status) {
        long inserted = db.addReportDetails(rep_id, comp_id, pc_no, model, mb, mb_serial, pr, monitor, mon_serial, ram,
                kboard, mouse, vga, hdd, status);

        Log.w("Details Insert TO SQL: ", "Status : " + inserted);
        return;
    }

    private void saveReportToLocal(int rep_id, int room, String cust_id,
                                   String category, String user_id, String date, String time
            , int cust_signed, int htech_signed, int admin_signed, String remarks, String room_name) {
        long insert = db.addReport(rep_id, room, cust_id, category, user_id,
                date, time, cust_signed, htech_signed, admin_signed, remarks, room_name);

        Log.w("REPORT Insert TO SQL: ", "Status : " + insert);
        return;
    }
}
