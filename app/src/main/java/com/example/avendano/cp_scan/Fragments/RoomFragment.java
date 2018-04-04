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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    TextView no_list;
    ProgressBar progressBar;
    EditText search;

    public RoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_room, container, false);
        // Inflate the layout for this fragment

        search = (EditText) view.findViewById(R.id.search);
        search.setVisibility(View.VISIBLE);
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
                            new RoomsLoader().execute(SharedPrefManager.getInstance(getContext()).getUserRole(), "yes");
                        } else {
                            //load all
                            progressBar.setVisibility(View.VISIBLE);
                            loadFromServer(SharedPrefManager.getInstance(getContext()).getUserRole());
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        swiper = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
//        swiper.setColorSchemeColors(new Color(getResources(R.color.darkorange)));
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                if(search.getText().toString().trim().isEmpty())
                    loadFromServer(SharedPrefManager.getInstance(getContext()).getUserRole());
                else
                    new RoomsLoader().execute(SharedPrefManager.getInstance(getContext()).getUserRole(), "yes");
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
            String search = strings[1];
            if (toLoad.equalsIgnoreCase("custodian")) {
                if (search.equalsIgnoreCase("no"))
                    loadCustodianRooms();
                else
                    searchRooms();
            } else {
                if (search.equalsIgnoreCase("no"))
                    loadAllRoom();
                else
                    searchRooms();
            }
            return null;
        }

        private void searchRooms() {
            roomsList.clear();
            String string = search.getText().toString().trim();
            Log.e("STRING", string);
            String query = "";
            String user_id = SharedPrefManager.getInstance(getContext()).getUserId();
            if (SharedPrefManager.getInstance(getContext()).getUserRole().equalsIgnoreCase("custodian"))
                query = "select * from (select r.room_id, department.dept_name, r.room_custodian_id," +
                        " r.room_technician_id, r.room_name, r.building, users.name, " +
                        "u.name 'technician',CONCAT(department.dept_name,' ',r.room_name) as 'dept_room'" +
                        " from room r join users on users.user_id = r.room_custodian_id " +
                        "join users u on u.user_id = r.room_technician_id  left join department on" +
                        " department.dept_id = r.dept_id) as rooms where (rooms.dept_name like '%" + string + "%'" +
                        " or rooms.room_name like '%" + string + "%' or rooms.building like '%" + string + "%' " +
                        " or rooms.dept_room = '" + string + "')" +
                        "  and (rooms.room_custodian_id = '" + user_id + "' or rooms.room_technician_id " +
                        "= '" + user_id + "')";
            else
                query = "select * from (select r.room_id, department.dept_name, r.room_custodian_id," +
                        " r.room_technician_id, r.room_name , r.building, users.name, " +
                        "u.name 'technician', CONCAT(department.dept_name,' ',r.room_name) as 'dept_room' from room r join users on users.user_id = r.room_custodian_id " +
                        "join users u on u.user_id = r.room_technician_id  left join department on" +
                        " department.dept_id = r.dept_id) as rooms where rooms.dept_name like '%" + string + "%'" +
                        " or rooms.room_name like '%" + string + "%' or rooms.building like '%" + string + "%'" +
                        " or rooms.dept_room = '" + string + "'" +
                        " or rooms.building ='" + string + "'";

            Log.e("QUERY", query);
            final String finalQuery = query;
            StringRequest stringRequest = new StringRequest(Request.Method.POST
                    , AppConfig.URL_SEARCH_ROOM
                    , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.e("SEARCH", response);
                        JSONArray array = new JSONArray(response);
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int room_id = obj.getInt("id");
                                String room_name = "";
                                if (obj.isNull("dept_name")) {
                                    room_name = obj.getString("room_name");
                                } else {
                                    room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                                }
                                String custodian = obj.getString("custodian");
                                String technician = obj.getString("technician");
                                String building = obj.getString("building");

                                Rooms rooms = new Rooms(room_id, custodian, technician, room_name, building);
                                roomsList.add(rooms);

                            }
                            roomAdapter = new RoomAdapter(getActivity(), getContext(), roomsList, swiper);
                            recyclerView.setAdapter(roomAdapter);
                            Log.w("LOADED", "Server rooms");
                            progressBar.setVisibility(View.GONE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "No Result", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("JSON ERROR 1", "RoomFragment: " + e.getMessage());
                    }
                    swiper.setRefreshing(false);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("Volleyerror 1", "Load Room: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> param = new HashMap<>();
                    param.put("query", finalQuery);
                    return param;
                }
            };
            RequestQueueHandler.getInstance(getContext()).addToRequestQueue(stringRequest);

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
                    progressBar.setVisibility(View.GONE);
                    db.deleteRooms();
                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {
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
                            Rooms rooms = new Rooms(room_id, custodian, technician, room_name, building);

                            if (role.equalsIgnoreCase("custodian")) {
                                if (cust_id.equalsIgnoreCase(SharedPrefManager.getInstance(getContext()).getUserId()))
                                    roomsList.add(rooms);
                            } else
                                roomsList.add(rooms);

                            addRoomsToLocal(room_id, room_name, custodian, cust_id, technician, tech_id,
                                    building, floor, pc_count, pc_working, lastAssess);

                        }
                        Log.w("LOADED", "Server rooms");
                        roomAdapter = new RoomAdapter(getActivity(), getContext(), roomsList, swiper);
                        recyclerView.setAdapter(roomAdapter);
                    } else {
                        db.deleteRooms();
                    }
                    Log.e("ROOM RESPONSE", response);
                } catch (JSONException e) {
                    Log.e("JSON ERROR 1", "RoomFragment: " + e.getMessage());
                    new RoomsLoader().execute(role, "no");
                }
                swiper.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volleyerror 1", "Load RoomsLoader: " + error.getMessage());
                new RoomsLoader().execute(role, "no");
            }
        });
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void addRoomsToLocal(int room_id, String room_name, String custodian, String cust_id,
                                 String technician, String tech_id, String building, int floor,
                                 int pc_count, int pc_working, String lastAssess) {
        long in = db.addRooms(room_id, room_name, custodian, cust_id, technician, tech_id, building
                , floor, pc_count, pc_working, lastAssess);
        Log.w("NEW ROOM INSERT:", "Status : " + in);
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
        search.setText("");
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
        } else {
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
