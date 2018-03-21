package com.example.avendano.cp_scan.Fragments;


import android.app.AlertDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.RoomAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.Rooms;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoomFragment extends Fragment {
    private SQLiteHandler db;
    List<Rooms> roomsList;
    RecyclerView recyclerView;
    SwipeRefreshLayout swiper;
    AlertDialog progressDialog;
    RoomAdapter roomAdapter;
    View view;

    public RoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_room, container, false);
        // Inflate the layout for this fragment

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        swiper = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
//        swiper.setColorSchemeColors(new Color(getResources(R.color.darkorange)));
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                loadFromServer(SharedPrefManager.getInstance(getContext()).getUserRole());
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        progressDialog = new SpotsDialog(getContext(), "Loading...");
        showDialog();

        loadFromServer(SharedPrefManager.getInstance(getContext()).getUserRole());

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(getContext());
        roomsList = new ArrayList<>();
    }

    class RoomsLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String toLoad = strings[0];
            if (toLoad.equalsIgnoreCase("custodian")) {
                loadCustodianRooms();
            } else {
                loadAllRoom();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideDialog();
            roomAdapter = new RoomAdapter(getActivity(), getContext(), roomsList, swiper);
            recyclerView.setAdapter(roomAdapter);
            swiper.setRefreshing(false);
            roomAdapter.notifyDataSetChanged();
        }
    }

    private void loadFromServer(final String role) {
        roomsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_ROOM
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    hideDialog();
                    if (response != null && response.length() > 0) {
                        JSONArray array = new JSONArray(response);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int room_id = obj.getInt("room_id");
                            int floor = obj.getInt("floor");
                            String room_name = "";
                            if (obj.isNull("dept_name")) {
                                room_name = obj.getString("room_name");
                            } else {
                                room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                            }
                            String custodian = obj.getString("room_custodian");
                            String cust_id = obj.getString("cust_id");
                            String technician = obj.getString("room_technician");
                            String tech_id = obj.getString("tech_id");
                            String building = obj.getString("building");
                            int pc_count = obj.getInt("pc_count");
                            int pc_working = obj.getInt("pc_working");
                            String lastAssess = "";
                            if (obj.isNull("lastAssess")) {
                                lastAssess = "--";
                            } else {
                                lastAssess = obj.getString("lastAssess");
                            }
                            Rooms rooms = new Rooms(room_id,custodian,technician,room_name,building);

                            if(role.equalsIgnoreCase("custodian")){
                                if(cust_id.equalsIgnoreCase(SharedPrefManager.getInstance(getContext()).getUserId()))
                                    roomsList.add(rooms);
                            }
                            else
                                roomsList.add(rooms);

                            checkRoom(room_id, room_name, custodian, cust_id, technician, tech_id,
                                    building, floor, pc_count, pc_working, lastAssess);

                        }
                        Log.w("LOADED" , "Server rooms");
                        roomAdapter = new RoomAdapter(getActivity(), getContext(), roomsList, swiper);
                        recyclerView.setAdapter(roomAdapter);
                    }else{
                        //empty list view
                        //delete ung sqlite kung may laman
                    }
                    Log.e("ROOM RESPONSE", response);
                } catch (JSONException e) {
                    Log.e("JSON ERROR 1", "RoomFragment: " + e.getMessage());
                    new RoomsLoader().execute(role);
                }
                swiper.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volleyerror 1", "Load RoomsLoader: " + error.getMessage());
                new RoomsLoader().execute(role);
            }
        });
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void checkRoom(int room_id, String room_name, String custodian, String cust_id,
                           String technician, String tech_id, String building, int floor,
                           int pc_count, int pc_working, String lastAssess) {
        Cursor c = db.getRoomDetails(room_id);
        if (!c.moveToFirst()) {
            long in = db.addRooms(room_id, room_name, custodian, cust_id, technician, tech_id, building
                    , floor, pc_count, pc_working, lastAssess);
            Log.w("NEW ROOM INSERT:", "Status : " + in);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    private void loadCustodianRooms() {
        Cursor c = db.getCustodianRoom(SharedPrefManager.getInstance(getContext()).getUserId());
        if (c.moveToNext()) {
            do {
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                String room_cust = c.getString(c.getColumnIndex(db.ROOMS_CUSTODIAN));
                String room_tech = c.getString(c.getColumnIndex(db.ROOMS_TECHNICIAN));
                String room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
                String room_building = c.getString(c.getColumnIndex(db.ROOMS_BUILDING));

                Rooms rooms = new Rooms(room_id, room_cust, room_tech, room_name,
                        room_building);
                roomsList.add(rooms);
            } while (c.moveToNext());
        } else {
            //empty fragment
        }
        swiper.setRefreshing(false);
    }

    private void loadAllRoom() {
        Cursor c = db.getAllRoom();
        if (c.moveToFirst()) {
            do {
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                String room_cust = c.getString(c.getColumnIndex(db.ROOMS_CUSTODIAN));
                String room_tech = c.getString(c.getColumnIndex(db.ROOMS_TECHNICIAN));
                String room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
                String room_building = c.getString(c.getColumnIndex(db.ROOMS_BUILDING));

                Rooms rooms = new Rooms(room_id, room_cust, room_tech, room_name,
                        room_building);
                roomsList.add(rooms);
            } while (c.moveToNext());
        }
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
