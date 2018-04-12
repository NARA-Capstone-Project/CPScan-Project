package com.example.avendano.cp_scan.Pages;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.TaskAdapter;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Task;
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
 * Created by Avendano on 9 Apr 2018.
 */

public class SchedulesActivity extends AppCompatActivity {

    String TAG = "TASK";
    Spinner list_type;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private AlertDialog progress;
    List<Task> taskList;
    TaskAdapter taskAdapter;
    Connection_Detector connection_detector;
    SQLiteHandler db;
    ProgressBar progressBar;
    VolleyRequestSingleton volley;
    int previousSelection = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_and_spinner);

        list_type = findViewById(R.id.list_type);
        taskList = new ArrayList<>();
        volley = new VolleyRequestSingleton(this);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swiper = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                new loadSchedule().execute();
            }
        });
        connection_detector = new Connection_Detector(this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String[] items = new String[]{"Inventory Schedule", "Repair Schedule"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, items);
        list_type.setAdapter(adapter);
        list_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelection == -1) {
                    previousSelection = 0;
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    new loadSchedule().execute();
                    previousSelection = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        new loadSchedule().execute();
    }

    private class loadSchedule extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (list_type.getSelectedItem().toString().equalsIgnoreCase("Inventory Schedule"))
                loadInventorySchedule();
            else
                loadRepairSchedule();
            return null;
        }
    }

    private void loadInventorySchedule() {
        volley.sendStringRequestGet(AppConfig.GET_INVENTORY_REQ
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e(TAG, response);
                        try {
                            progress.dismiss();
                            JSONArray array = new JSONArray(response);
                            if (array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    String set_date = obj.getString("date");
                                    String set_time = obj.getString("time");
                                    String msg = obj.getString("msg");
                                    int room_id = obj.getInt("room_id");
                                    int req_id = obj.getInt("req_id");
                                    String status = obj.getString("req_status");
                                    String tech_id = obj.getString("technician");

                                    if (status.equalsIgnoreCase("accepted") ||
                                            status.equalsIgnoreCase("done")) {
                                        if (SharedPrefManager.getInstance(SchedulesActivity.this).getUserId().equalsIgnoreCase(tech_id)) {
                                            Task task = new Task(set_date, set_time, msg,
                                                    "Inventory", room_id, req_id, status);
                                            taskList.add(task);
                                        }
                                    }

                                }
                                if (taskList.size() != 0) {
                                    taskAdapter = new TaskAdapter(SchedulesActivity.this, SchedulesActivity.this, taskList, swiper);
                                    recyclerView.setAdapter(taskAdapter);
                                } else {
                                    Toast.makeText(SchedulesActivity.this, "No Inventory Schedule", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SchedulesActivity.this, "No Inventory Schedule", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progress.dismiss();
                        }
                        swiper.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                        progress.dismiss();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadRepairSchedule() {
        volley.sendStringRequestGet(AppConfig.GET_REPAIR_REQ
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e(TAG, response);
                        try {
                            progress.dismiss();
                            JSONArray array = new JSONArray(response);
                            if (array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    String set_date = obj.getString("set_date");
                                    String set_time = obj.getString("set_time");
                                    String msg = obj.getString("msg");
                                    int comp_id = obj.getInt("comp_id");
                                    int req_id = obj.getInt("req_id");
                                    String status = obj.getString("req_status");
                                    String tech_id = obj.getString("tech_id");

                                    if (status.equalsIgnoreCase("accepted") ||
                                            status.equalsIgnoreCase("done")) {
                                        if (SharedPrefManager.getInstance(SchedulesActivity.this).getUserId().equalsIgnoreCase(tech_id)) {
                                            Task task = new Task(set_date, set_time, msg,
                                                    "Repair", comp_id, req_id, status);
                                            taskList.add(task);
                                        }
                                    }

                                }
                                if (taskList.size() != 0) {
                                    taskAdapter = new TaskAdapter(SchedulesActivity.this, SchedulesActivity.this, taskList, swiper);
                                    recyclerView.setAdapter(taskAdapter);
                                } else {
                                    Toast.makeText(SchedulesActivity.this, "No Repair Schedule", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SchedulesActivity.this, "No Repair Schedule", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progress.dismiss();
                        }
                        swiper.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                        progressBar.setVisibility(View.GONE);
                        progress.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SchedulesActivity.this.finish();
    }
}
