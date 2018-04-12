package com.example.avendano.cp_scan.Pages;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.ReportAdapter;
import com.example.avendano.cp_scan.Adapter.RequestAdapter;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Reports;
import com.example.avendano.cp_scan.Model.RequestReport;
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

import static java.security.AccessController.getContext;

/**
 * Created by Avendano on 10 Apr 2018.
 */

public class ReportActivity extends AppCompatActivity {

    String TAG = "REPORT";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private AlertDialog progress;
    List<Reports> reportsList;
    List<RequestReport> requestList;
    ReportAdapter reportAdapter;
    RequestAdapter requestAdapter;
    Connection_Detector connection_detector;
    ProgressBar progressBar;
    VolleyRequestSingleton volley;
    Spinner list_type;
    int previousSelection = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_and_spinner);

        reportsList = new ArrayList<>();
        volley = new VolleyRequestSingleton(this);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swiper = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
//                new loadSchedule().execute();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list_type = (Spinner) findViewById(R.id.list_type);
        String[] items = new String[]{"Inventory Reports", "Repair Reports"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, items);
        list_type.setAdapter(adapter);
        list_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (previousSelection == -1) {
                    previousSelection = 0;
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    new ReportsLoader().execute();
                    previousSelection = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                //loader
                new ReportsLoader().execute();
            }
        });

        reportsList = new ArrayList<>();
        requestList = new ArrayList<>();

        new ReportsLoader().execute();
    }

    class ReportsLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            //chesk spinner
            if (list_type.getSelectedItem().toString().equalsIgnoreCase("Inventory Reports")) { //inventory
                loadInventoryReports();
            } else if (list_type.getSelectedItem().toString().equalsIgnoreCase("Repair Reports")) { // repair
                loadRepairRequestReport();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    swiper.setRefreshing(false);
                    progress.dismiss();
                    progressBar.setVisibility(View.GONE);
                }
            }, 2000);
        }
    }

    private void loadRepairRequestReport() {
        requestList.clear();
        volley.sendStringRequestGet(AppConfig.GET_INVENTORY_REPORTS
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);

                                String date = obj.getString("date");
                                String time = obj.getString("time");
                                String cat = obj.getString("cat");
                                String cust_id = obj.getString("cust_id");
                                String tech_id = obj.getString("tech_id");
                                int rep_id = obj.getInt("rep_id");
                                String room_name = obj.getString("room_name");
                                String name = "PC " + obj.getString("pc_no") + " of " + room_name;

                                if(cat.contains("Repair")){
                                    String user_id = SharedPrefManager.getInstance(ReportActivity.this).getUserId();
                                    if(user_id.equals(cust_id) || user_id.equals(tech_id) ){
                                        RequestReport report = new RequestReport(rep_id, date + " " + time, cat, name);
                                        requestList.add(report);
                                    }
                                }
                            }
                            if (requestList.size() != 0) {
                                requestAdapter = new RequestAdapter(ReportActivity.this, ReportActivity.this, requestList, swiper);
                                recyclerView.setAdapter(requestAdapter);
                                requestAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(ReportActivity.this, "No Repair Reports", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ReportActivity.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error instanceof TimeoutError)
                            Toast.makeText(ReportActivity.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ReportActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                });
        }

    private void loadInventoryReports() {
        reportsList.clear();
        volley.sendStringRequestGet(AppConfig.GET_INVENTORY_REPORTS
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);

                                String date = obj.getString("date");
                                String time = obj.getString("time");
                                String cat = obj.getString("cat");
                                String room_name = obj.getString("room_name");
                                String cust_id = obj.getString("cust_id");
                                String tech_id = obj.getString("tech_id");
                                int room_id = obj.getInt("room_id");
                                int rep_id = obj.getInt("rep_id");

                                if(cat.contains("Inventory")){
                                    String user_id = SharedPrefManager.getInstance(ReportActivity.this).getUserId();
                                    if(user_id.equals(cust_id) || user_id.equals(tech_id) ){
                                        Reports reports = new Reports(date + " " + time, cat, room_name, room_id, rep_id);
                                        reportsList.add(reports);
                                    }
                                }
                            }

                            if (reportsList.size() != 0) {
                                reportAdapter = new ReportAdapter(ReportActivity.this, ReportActivity.this, reportsList, swiper);
                                recyclerView.setAdapter(reportAdapter);
                                reportAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(ReportActivity.this, "No Inventory Reports", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ReportActivity.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error instanceof TimeoutError)
                            Toast.makeText(ReportActivity.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ReportActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
