package com.example.avendano.cp_scan.Fragments;


import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.InventoryAdapter;
import com.example.avendano.cp_scan.Adapter.RepairAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Model.RequestInventory;
import com.example.avendano.cp_scan.Model.RequestRepair;
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
public class Request_Page extends Fragment {

    RecyclerView recyclerView;
    Spinner req_type;
    SwipeRefreshLayout refresh;
    List<RequestInventory> inventoryList;
    List<RequestRepair> repairList;
    AlertDialog progress;
    ProgressBar progressBar;
    InventoryAdapter inventoryAdapter;
    RepairAdapter repairAdapter;
    int previousSelection = -1;

    public Request_Page() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_request__page, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.request_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refresh = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
        req_type = (Spinner) v.findViewById(R.id.request_type);
        progress = new SpotsDialog(getContext(), "Loading...");
        progress.show();
        progress.setCancelable(false);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh.setRefreshing(true);
                new LoadRequests().execute();
            }
        });

        String[] items = new String[]{"Inventory Request", "Repair Request"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, items);
        req_type.setAdapter(adapter);
        req_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //load repair or inventory request
                progressBar.setVisibility(View.VISIBLE);
                new LoadRequests().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        new LoadRequests().execute();
        return v;
    }

    class LoadRequests extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (req_type.getSelectedItem().toString().trim().contains("Repair")) {
                loadRepair();
            } else {
                loadInventory();
            }
            return null;
        }
    }

    private void loadInventory() {
        inventoryList.clear();
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_ALL_INVENTORY_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.v("REQUEST", response);
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int req_id = obj.getInt("req_id");
                        int room_id = obj.getInt("room_id");
                        String cust_id = obj.getString("custodian");
                        String tech_id = obj.getString("technician");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        String msg = obj.getString("msg");
                        String req_status = obj.getString("req_status");
                        String req_date = obj.getString("date_requested");
                        String req_time = obj.getString("time_requested");

                        if (req_status.equalsIgnoreCase("pending")) {
                            RequestInventory inventory = new RequestInventory(req_id, room_id, cust_id
                                    , tech_id, date, time, msg, req_date, req_time, req_status);
                            inventoryList.add(inventory);
                        }
                        Log.e("PENDING", req_status);
                    }
                    inventoryAdapter = new InventoryAdapter(inventoryList, getContext(), getActivity()
                            , refresh);
                    recyclerView.setAdapter(inventoryAdapter);
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBar.getVisibility() == View.VISIBLE)
                                progressBar.setVisibility(View.GONE);
                            progress.dismiss();
                            refresh.setRefreshing(false);
                        }
                    }, 4000);
                } catch (JSONException e) {
                    if (progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);
                    progress.dismiss();
                    refresh.setRefreshing(false);
                    e.printStackTrace();
                    Toast.makeText(getContext(), "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                refresh.setRefreshing(false);
                progress.dismiss();
                Log.v("RESULT", "Error: " + error.getMessage());
                Toast.makeText(getContext(), "Can't connect to the server, please try again later.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("id", SharedPrefManager.getInstance(getContext()).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }

    private void loadRepair() {
        repairList.clear();
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_REPAIR_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.v("REQUEST", response);
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
                        String path = obj.getString("image");

                        if (req_status.equalsIgnoreCase("pending")) {
                            RequestRepair repair = new RequestRepair(req_id, comp_id, cust_id, tech_id
                                    , date, time, msg, req_date, req_time, req_status, path, req_details);
                            repairList.add(repair);
                        }
                        Log.e("PENDING", req_status);

                    }
                    repairAdapter = new RepairAdapter(repairList, getContext(), getActivity(), refresh);
                    recyclerView.setAdapter(repairAdapter);
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBar.getVisibility() == View.VISIBLE)
                                progressBar.setVisibility(View.GONE);
                            progress.dismiss();
                            refresh.setRefreshing(false);
                        }
                    }, 2000);
                } catch (JSONException e) {
                    if (progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);
                    refresh.setRefreshing(false);
                    progress.dismiss();
                    Toast.makeText(getContext(), "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
                refresh.setRefreshing(false);
                progress.dismiss();
                Toast.makeText(getContext(), "Can't connect to the server, please try again later.", Toast.LENGTH_SHORT).show();
                Log.v("RESULT", "Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("id", SharedPrefManager.getInstance(getContext()).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inventoryList = new ArrayList<>();
        repairList = new ArrayList<>();
        Log.e("ONCREATE", "crreate");
    }
}
