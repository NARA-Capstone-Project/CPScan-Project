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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.InventoryAdapter;
import com.example.avendano.cp_scan.Adapter.PeripheralAdapter;
import com.example.avendano.cp_scan.Adapter.RepairAdapter;
import com.example.avendano.cp_scan.Model.*;
import com.example.avendano.cp_scan.Model.RequestPeripherals;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class RequestListsActivity extends AppCompatActivity {
    Spinner request_type;
    RecyclerView recyclerView;
    //request inventory, repair, peripherals
    List<RequestInventory> inventoryList;
    List<RequestRepair> repairList;
    List<com.example.avendano.cp_scan.Model.RequestPeripherals> peripheralsList;
    SwipeRefreshLayout refresh;
    AlertDialog progress;
    ProgressBar progressBar;
    InventoryAdapter inventoryAdapter;
    RepairAdapter repairAdapter;
    PeripheralAdapter peripheralAdapter;
    VolleyRequestSingleton volley;
    int previousSelection = -1;
    View v;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_and_spinner);


        inventoryList = new ArrayList<>();
        repairList = new ArrayList<>();
        peripheralsList = new ArrayList<>();

        volley = new VolleyRequestSingleton(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        request_type = (Spinner) findViewById(R.id.list_type);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        progress.setCancelable(false);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        v = (View) findViewById(R.id.view);
        relativeLayout = (RelativeLayout) findViewById(R.id.spinner);

        String[] items = new String[]{"Inventory Request", "Repair Request", "Peripherals Request"};
        if(SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("admin")){
            //peripherals lang ung iloload for approval
            relativeLayout.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
            request_type.setVisibility(View.GONE);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, items);
        request_type.setAdapter(adapter);
        request_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelection < 0)
                    previousSelection = 0;
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    new LoadRequests().execute();
                    previousSelection = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh.setRefreshing(true);
                new LoadRequests().execute();
            }
        });
        new LoadRequests().execute();
    }

    class LoadRequests extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inventoryList.clear();
            repairList.clear();
            peripheralsList.clear();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(SharedPrefManager.getInstance(RequestListsActivity.this).getUserRole().equalsIgnoreCase("admin")){
                loadRequestPeripherals();
            }else{
                if (request_type.getSelectedItem().toString().equalsIgnoreCase("Inventory Request")) {
                    loadRequestInventory();
                } else if (request_type.getSelectedItem().toString().equalsIgnoreCase("Repair Request")) {
                    loadRequestRepair();
                } else {
                    loadRequestPeripherals();
                }
            }
            return null;
        }
    }

    private void loadRequestInventory() {
        String url = AppConfig.GET_INVENTORY_REQ;

        volley.sendStringRequestGet(url, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                Log.e("RESPONSE", response);
                try {
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
                        String cancel_rem="";
                        if(!obj.isNull("cancel_remarks"))
                            cancel_rem = obj.getString("cancel_remarks");
                        //pending accepted ignored done
                        if (SharedPrefManager.getInstance(RequestListsActivity.this).getUserRole().equalsIgnoreCase("custodian")) {
                            if (cust_id.equals(SharedPrefManager.getInstance(RequestListsActivity.this).getUserId())) {
                                if (!(req_status.equalsIgnoreCase("cancel")) ||
                                        req_status.equalsIgnoreCase("done")) {
                                    //check if missed
                                    if (!date.equalsIgnoreCase("anytime")) {
                                            //check date if before
                                            Date today = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                            Date set_date = new SimpleDateFormat("yyyy-MM-dd").parse(date);

                                            Log.e("DATE", "today: " + today + " set date: " + set_date);
                                            if(set_date.before(today) && req_status.equalsIgnoreCase("pending")){
                                                req_status = "Need to Resched";
                                            }else if(set_date.before(today) && req_status.equalsIgnoreCase("accepted")){
                                                req_status = "Missed";
                                            }
                                    }
                                    RequestInventory inventory = new RequestInventory(req_id, room_id, cust_id
                                            , tech_id, date, time, msg, req_date, req_time, req_status, cancel_rem);
                                    inventoryList.add(inventory);
                                }
                            }
                        } else {
                            if (req_status.equalsIgnoreCase("pending")) {
                                if (tech_id.equals(SharedPrefManager.getInstance(RequestListsActivity.this).getUserId())) {
                                    if (!date.equalsIgnoreCase("anytime")) {
                                        //check date if before
                                        Date today = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                        Date set_date = new SimpleDateFormat("yyyy-MM-dd").parse(date);

                                        Log.e("DATE", "today: " + today + " set date: " + set_date);
                                        if(set_date.before(today)){
                                            //missed or not accepted or hindi napansin need to resched
                                            //do nothing
                                        }else{
                                            RequestInventory inventory = new RequestInventory(req_id, room_id, cust_id
                                                    , tech_id, date, time, msg, req_date, req_time, req_status, cancel_rem);
                                            inventoryList.add(inventory);
                                        }
                                    }else{
                                        RequestInventory inventory = new RequestInventory(req_id, room_id, cust_id
                                                , tech_id, date, time, msg, req_date, req_time, req_status, cancel_rem);
                                        inventoryList.add(inventory);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(RequestListsActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
                if (inventoryList.size() != 0) {
                    inventoryAdapter = new InventoryAdapter(inventoryList, RequestListsActivity.this, RequestListsActivity.this
                            , refresh);
                    recyclerView.setAdapter(inventoryAdapter);
                } else {
                    Toast.makeText(RequestListsActivity.this, "No Request", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
                progress.dismiss();
                refresh.setRefreshing(false);

            }

            @Override
            public void onErrorResponse(VolleyError error) {

                progressBar.setVisibility(View.GONE);
                progress.dismiss();
                refresh.setRefreshing(false);
                error.printStackTrace();
            }
        });
    }

    private void loadRequestRepair() {
        //check connection
        String url = AppConfig.GET_REPAIR_REQ;

        volley.sendStringRequestGet(url, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                Log.e("RESPONSE", response);
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int req_id = obj.getInt("req_id");
                        int rep_id = 0;
                        if (!obj.isNull("rep_id"))
                            rep_id = obj.getInt("rep_id");
                        int comp_id = obj.getInt("comp_id");
                        String cust_id = obj.getString("cust_id");
                        String tech_id = obj.getString("tech_id");
                        String date = obj.getString("set_date");
                        String time = obj.getString("set_time");
                        String msg = obj.getString("msg");
                        String req_status = obj.getString("req_status");
                        String req_date = obj.getString("date_req");
                        String req_time = obj.getString("time_req");
                        String req_details = obj.getString("req_details");
                        String path = obj.getString("image");
                        String cancel_rem = "";
                        if(!obj.isNull("cancel_remarks"))
                            cancel_rem = obj.getString("cancel_remarks");
                        if (obj.isNull("image"))
                            path = "";

                        if (SharedPrefManager.getInstance(RequestListsActivity.this).getUserRole().equalsIgnoreCase("custodian")) {
                            if (cust_id.equals(SharedPrefManager.getInstance(RequestListsActivity.this).getUserId())) {
                                if (!(req_status.equalsIgnoreCase("cancel") ||
                                        req_status.equalsIgnoreCase("done"))) {
                                    //check if missed
                                    if (!date.equalsIgnoreCase("anytime")) {
                                        //check date if before
                                        Date today = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                        Date set_date = new SimpleDateFormat("yyyy-MM-dd").parse(date);

                                        Log.e("DATE", "today: " + today + " set date: " + set_date);
                                        if(set_date.before(today) && req_status.equalsIgnoreCase("pending")){
                                            req_status = "Need to Resched";
                                        }else if(set_date.before(today) && req_status.equalsIgnoreCase("accepted")){
                                            req_status = "Missed";
                                        }
                                    }
                                    RequestRepair repair = new RequestRepair(req_id, comp_id, cust_id, tech_id
                                            , date, time, msg, req_date, req_time, req_status, path, req_details, cancel_rem);
                                    repairList.add(repair);
                                }
                            }
                        } else {
                            if (req_status.equalsIgnoreCase("pending")) {
                                if (tech_id.equals(SharedPrefManager.getInstance(RequestListsActivity.this).getUserId())) {

                                    if (!date.equalsIgnoreCase("anytime")) {
                                        //check date if before
                                        Date today = new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                                        Date set_date = new SimpleDateFormat("yyyy-MM-dd").parse(date);

                                        Log.e("DATE", "today: " + today + " set date: " + set_date);
                                        if(set_date.before(today)){
                                            //missed or not accepted
                                            //do nothing
                                        }else{
                                            RequestRepair repair = new RequestRepair(req_id, comp_id, cust_id, tech_id
                                                    , date, time, msg, req_date, req_time, req_status, path, req_details, cancel_rem);
                                            repairList.add(repair);
                                        }
                                    }else{
                                        RequestRepair repair = new RequestRepair(req_id, comp_id, cust_id, tech_id
                                                , date, time, msg, req_date, req_time, req_status, path, req_details,cancel_rem);
                                        repairList.add(repair);
                                    }
                                }
                            }
                        }
                    }
                    if (repairList.size() != 0) {
                        repairAdapter = new RepairAdapter(repairList, RequestListsActivity.this, RequestListsActivity.this, refresh);
                        recyclerView.setAdapter(repairAdapter);
                    } else {
                        Toast.makeText(RequestListsActivity.this, "No Request", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(RequestListsActivity.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
                progress.dismiss();
                refresh.setRefreshing(false);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                progress.dismiss();
                refresh.setRefreshing(false);
                if (error instanceof TimeoutError)
                    Toast.makeText(RequestListsActivity.this, "Server took too long to respond, check your connection", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(RequestListsActivity.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRequestPeripherals() {
        //statuses = pending, confirmed, approved, issued(dito plang pwedeng ireceive ni custodian), received - cancel ignored
        //if confirmed, pending pa rin, if naapproved na after confirmation may button na lalabas na issue
        volley.sendStringRequestGet(AppConfig.GET_PERIPHERALS
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int req_id = obj.getInt("req_id");
                                String status = obj.getString("req_status");
                                String room_name = obj.getString("room_name");
                                String tech_id = obj.getString("tech_id");
                                String cust_id = obj.getString("cust_id");
                                String user_id = SharedPrefManager.getInstance(RequestListsActivity.this).getUserId();
                                String cancel_rem="";
                                if(!obj.isNull("cancel_remarks"))
                                    cancel_rem = obj.getString("cancel_remarks");

                                //if admin = for approval lang ung kukunin
                                if(SharedPrefManager.getInstance(RequestListsActivity.this).getUserRole().equalsIgnoreCase("admin")){
                                    if (status.equalsIgnoreCase("confirmed")) {
                                        RequestPeripherals peripherals = new RequestPeripherals(req_id, room_name, status, cancel_rem);
                                        peripheralsList.add(peripherals);
                                    }
                                }else{
                                    if (!status.equalsIgnoreCase("received")) {
                                        if (SharedPrefManager.getInstance(RequestListsActivity.this).getUserRole().equalsIgnoreCase("main technician")) {
                                            if (!status.equalsIgnoreCase("cancel") || status.equalsIgnoreCase("ignored")) {
                                                RequestPeripherals peripherals = new RequestPeripherals(req_id, room_name, status, cancel_rem);
                                                peripheralsList.add(peripherals);
                                            }
                                        }else{
                                            if (cust_id.equals(user_id)) {
                                                RequestPeripherals peripherals = new RequestPeripherals(req_id, room_name, status, cancel_rem);
                                                peripheralsList.add(peripherals);
                                            } else if (tech_id.equals(user_id)) {
                                                if (!status.equalsIgnoreCase("cancel") || status.equalsIgnoreCase("ignored")) {
                                                    RequestPeripherals peripherals = new RequestPeripherals(req_id, room_name, status, cancel_rem);
                                                    peripheralsList.add(peripherals);
                                                }
                                            }
                                        }

                                    }
                                }

                            }
                            if (!peripheralsList.isEmpty()) {
                                peripheralAdapter = new PeripheralAdapter(peripheralsList, RequestListsActivity.this, RequestListsActivity.this, refresh);
                                recyclerView.setAdapter(peripheralAdapter);
                            } else {
                                Toast.makeText(RequestListsActivity.this, "No Requests", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RequestListsActivity.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                        progress.dismiss();
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        progress.dismiss();
                        refresh.setRefreshing(false);
                        if (error instanceof TimeoutError) {
                            Toast.makeText(RequestListsActivity.this, "Server took too long to response", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(RequestListsActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!request_type.getSelectedItem().toString().equalsIgnoreCase("Inventory Request")) {
            new LoadRequests().execute();
        }
    }
}
