package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.RoomAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Rooms;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 8 Apr 2018.
 */

public class RoomActivity extends AppCompatActivity {
    private SQLiteHandler db;
    List<Rooms> roomsList;
    RecyclerView recyclerView;
    SwipeRefreshLayout swiper;
    AlertDialog progress;
    RoomAdapter roomAdapter;
    View view;
    TextView no_list;
    ProgressBar progressBar;
    EditText search;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_room);

        db = new SQLiteHandler(RoomActivity.this);
        roomsList = new ArrayList<>();

        search = (EditText) findViewById(R.id.search);
        search.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swiper = (SwipeRefreshLayout) findViewById(R.id.refresh);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(RoomActivity.this));
        progress = new SpotsDialog(this, "Loading");
        progress.setCancelable(false);
        progress.show();
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                new RoomsLoader().execute();
            }
        });
        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        String uniq = search.getText().toString().trim();
                        if (uniq.length() > 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            new RoomsLoader().execute();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        new RoomsLoader().execute();
    }

    class RoomsLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            roomsList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            loadRooms();
            return null;
        }
    }

    private void loadRooms() {
        String query = "", user_id = "", string = "", url = "";
        Map<String, String> param;

        user_id = SharedPrefManager.getInstance(RoomActivity.this).getUserId();
        string = RoomActivity.this.search.getText().toString().trim();
        query = "select * from (select r.room_id, department.dept_name, r.room_custodian_id," +
                " r.room_technician_id, r.room_name, r.building, users.name, " +
                "u.name 'technician',CONCAT(department.dept_name,' ',r.room_name) as 'dept_room'" +
                " from room r join users on users.user_id = r.room_custodian_id " +
                "join users u on u.user_id = r.room_technician_id  left join department on" +
                " department.dept_id = r.dept_id) as rooms where (rooms.dept_name like '%" + string + "%'" +
                " or rooms.room_name like '%" + string + "%' or rooms.building like '%" + string + "%' " +
                " or rooms.dept_room = '" + string + "')";
        param = new HashMap<>();
        VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        final String finalUser_id = user_id;

        if (string.isEmpty()) {
            url = AppConfig.GET_ROOMS;
            volley.sendStringRequestGet(url, new com.example.avendano.cp_scan.Database.VolleyCallback() {
                @Override
                public void onSuccessResponse(String result) {
                    try {
                        Log.e("RESPONSE", result);
                        JSONArray array = new JSONArray(result);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int room_id = obj.getInt("room_id");
                            String room_name = "";
                            if (obj.isNull("dept_name")) {
                                room_name = obj.getString("room_name");
                            } else {
                                room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                            }
                            String building = obj.getString("building");
                            String room_cust = obj.getString("cust_name");
                            String room_tech = obj.getString("tech_name");
                            String cust_id = obj.getString("cust_id");
                            String tech_id = obj.getString("tech_id");

                            if (tech_id.equals(finalUser_id) || cust_id.equals(finalUser_id)) {
                                Rooms rooms = new Rooms(room_id, room_cust,
                                        room_tech, room_name, building);
                                roomsList.add(rooms);
                            }
                        }
                        Log.e("ROOMSLIST", "" + roomsList.size());
                        if(roomsList.size() != 0 ){
                            roomAdapter = new RoomAdapter(RoomActivity.this, RoomActivity.this, roomsList
                                    , swiper);
                            recyclerView.setAdapter(roomAdapter);
                        }else{
                            Toast.makeText(RoomActivity.this, "Room not found", Toast.LENGTH_SHORT).show();
                        }
                        swiper.setRefreshing(false);
                        progress.dismiss();
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception ex) {
                        swiper.setRefreshing(false);
                        progress.dismiss();
                        progressBar.setVisibility(View.GONE);
                        Log.e("RESPONSE", result);
                        Log.e("EXCEPTION", ex.getMessage());
                        Toast.makeText(RoomActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError)
                        Toast.makeText(RoomActivity.this, "Server took too long to respond, chack your connection", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(RoomActivity.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();

                }
            });

        } else {
            url = AppConfig.SEARCH_ROOMS;
            param.put("query", query);
            volley.sendStringRequestPost(url, new com.example.avendano.cp_scan.Database.VolleyCallback() {
                @Override
                public void onSuccessResponse(String result) {
                    try {
                        Log.e("RESPONSE", result);
                        JSONArray array = new JSONArray(result);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int room_id = obj.getInt("room_id");
                            String room_name = "";
                            if (obj.isNull("dept_name")) {
                                room_name = obj.getString("room_name");
                            } else {
                                room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                            }
                            String building = obj.getString("building");
                            String room_cust = obj.getString("cust_name");
                            String room_tech = obj.getString("tech_name");
                            String cust_id = obj.getString("cust_id");
                            String tech_id = obj.getString("tech_id");

                            if (tech_id.equals(finalUser_id) || cust_id.equals(finalUser_id)) {
                                Rooms rooms = new Rooms(room_id, room_cust,
                                        room_tech, room_name, building);
                                roomsList.add(rooms);
                            }
                        }
                        Log.e("ROOMSLIST", "" + roomsList.size());
                        if(roomsList.size() != 0 ){
                            roomAdapter = new RoomAdapter(RoomActivity.this, RoomActivity.this, roomsList
                                    , swiper);
                            recyclerView.setAdapter(roomAdapter);
                        }else{
                            Toast.makeText(RoomActivity.this, "Room not found", Toast.LENGTH_SHORT).show();
                        }
                        swiper.setRefreshing(false);
                        progress.dismiss();
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception ex) {
                        swiper.setRefreshing(false);
                        progress.dismiss();
                        progressBar.setVisibility(View.GONE);
                        Log.e("RESPONSE", result);
                        Log.e("EXCEPTION", ex.getMessage());
                        Toast.makeText(RoomActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError)
                        Toast.makeText(RoomActivity.this, "Server took too long to respond, chack your connection", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(RoomActivity.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();

                }
            }, param);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

}
