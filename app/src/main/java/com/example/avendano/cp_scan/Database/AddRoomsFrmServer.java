package com.example.avendano.cp_scan.Database;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AddRoomsFrmServer {

    Context mCtx;
    SQLiteHandler db;

    public AddRoomsFrmServer(Context mCtx, SQLiteHandler db) {
        this.mCtx = mCtx;
        this.db = db;
    }

    public void SyncFunction() {
        db.updateSync(0, "room");
        getRoomsFromServer();
    }

    private void getRoomsFromServer(){

        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_ROOM
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
                        int floor = obj.getInt("floor");
                        String room_name = "";
                        if(obj.isNull("dept_name")){
                            room_name = obj.getString("room_name");
                        }else{
                            room_name = obj.getString("dept_name") + " "+ obj.getString("room_name");
                        }
                        String custodian = obj.getString("room_custodian");
                        String cust_id = obj.getString("cust_id");
                        String technician = obj.getString("room_technician");
                        String tech_id = obj.getString("tech_id");
                        String building = obj.getString("building");
                        int pc_count = obj.getInt("pc_count");
                        int pc_working = obj.getInt("pc_working");
                        String lastAssess = "";
                        if(obj.isNull("lastAssess")){
                            lastAssess = "--";
                        }else{
                            lastAssess = obj.getString("lastAssess");
                        }
                        addRooms(room_id,room_name,custodian,cust_id, technician, tech_id, building,
                                floor,pc_count,pc_working, lastAssess);
                    }
                    db.deleteAllUnsync("room");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("RESULT", "Error: "+ error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(mCtx).addToRequestQueue(str);

    }
    private long addRooms(int room_id, String room_name,String cust, String cust_id, String tech, String tech_id, String build,
                          int floor, int pc_count, int pc_working, String lastAssess){
        long insert = db.addRooms(room_id,room_name,cust,cust_id, tech, tech_id, build,
                floor,pc_count,pc_working, lastAssess);
        Log.w("ROOM INSERT TO SQLITE: ", "Status : " + insert);
        return insert;
    }
}
