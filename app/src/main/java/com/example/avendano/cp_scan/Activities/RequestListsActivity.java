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
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Fragments.Request_Page;
import com.example.avendano.cp_scan.Model.RequestInventory;
import com.example.avendano.cp_scan.Model.RequestRepair;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    SwipeRefreshLayout refresh;
    AlertDialog progress;
    ProgressBar progressBar;
    InventoryAdapter inventoryAdapter;
    RepairAdapter repairAdapter;
    VolleyRequestSingleton volley;
    int previousSelection = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_request__page);

        volley = new VolleyRequestSingleton(this);
        recyclerView = (RecyclerView) findViewById(R.id.request_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        request_type = (Spinner) findViewById(R.id.request_type);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        progress.setCancelable(false);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        String[] items = new String[]{"Inventory Request", "Repair Request", "Peripherals Request"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, items);
        request_type.setAdapter(adapter);
        request_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(previousSelection  < 0)
                    previousSelection = 0;
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    new LoadRequests().execute();
                    previousSelection = position;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        new LoadRequests().execute();
    }

    class LoadRequests extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            inventoryList.clear();
            repairList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (request_type.getSelectedItem().toString().equalsIgnoreCase("Inventory Request")) {
                loadInventory();
            }
            return null;
        }
    }

    private void loadInventory() {
        Map<String, String> param = new HashMap<>();
        param.put("user_id", SharedPrefManager.getInstance(this).getUserId());

        volley.sendStringRequestPost("", new VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {
                try {
                    JSONArray array = new JSONArray(result);
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
                    }
                } catch (Exception e) {
                    Toast.makeText(RequestListsActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
                if(inventoryList.size() != 0){
                    inventoryAdapter = new InventoryAdapter(inventoryList,RequestListsActivity.this, RequestListsActivity.this
                            , refresh);
                    recyclerView.setAdapter(inventoryAdapter);
                }
                progressBar.setVisibility(View.GONE);
                progress.dismiss();
                refresh.setRefreshing(false);
            }
        }, param);
    }
}
