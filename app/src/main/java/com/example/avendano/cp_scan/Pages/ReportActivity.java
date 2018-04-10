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
        String[] items = new String[]{"Inventory Reports", "Repair Reports", "Peripherals Request Reports"};
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
            } else {  //peripherals
                loadInventoryRequestReport();
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

    private void loadRepairRequestReport(){
        requestList.clear();
        String query = "";
        String role = SharedPrefManager.getInstance(ReportActivity.this).getUserRole();
        String user_id = SharedPrefManager.getInstance(ReportActivity.this).getUserId();
        Log.e("USER", role + " " + user_id);
        if(role.equalsIgnoreCase("main technician") || role.equalsIgnoreCase("admin"))
            query = "select * from assessment_reports where rep_id in (select rep_id from " +
                    "request_repair as i where req_status = 'Done') order by date desc, time desc;";
        else
            query= "select * from assessment_reports where (rep_id in (select rep_id from " +
                    "request_repair as i where req_status = 'Done')) and (technician_id = '"+user_id+"' " +
                    "or custodian_id = '"+user_id+"')  order by date desc, time desc;";
        final String finalQuery = query;
        Log.e("QUERY", finalQuery);
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_REQ_REPORTS
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("RESPONSE 186", response);
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        String category = obj.getString("category");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        String name = obj.getString("name");
                        int rep_id = obj.getInt("rep_id");

                        RequestReport report = new RequestReport(rep_id,date + " " + time, category, name);
                        requestList.add(report);
                    }
                    requestAdapter = new RequestAdapter(ReportActivity.this,ReportActivity.this,requestList,swiper);
                    recyclerView.setAdapter(requestAdapter);
                    requestAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ReportActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("query", finalQuery);
                params.put("req_type", "Repair");
                return params;
            }
        };
        RequestQueueHandler.getInstance(ReportActivity.this).addToRequestQueue(str);
    }

    private void loadInventoryRequestReport(){
        requestList.clear();
        String query = "";
        String role = SharedPrefManager.getInstance(ReportActivity.this).getUserRole();
        String user_id = SharedPrefManager.getInstance(ReportActivity.this).getUserId();
        Log.e("USER", role + " " + user_id);
        if(role.equalsIgnoreCase("main technician") || role.equalsIgnoreCase("admin"))
            query = "select * from assessment_reports where rep_id in (select rep_id from " +
                    "request_inventory as i where req_status = 'Done') order by date desc, time desc;";
        else
            query= "select * from assessment_reports where (rep_id in (select rep_id from " +
                    "request_inventory as i where req_status = 'Done')) and (technician_id = '"+user_id+"' " +
                    "or custodian_id = '"+user_id+"') order by date desc, time desc;";

        final String finalQuery = query;
        Log.e("QUERY", finalQuery);
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_REQ_REPORTS
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("RESPONSE 186", response);
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        String category = obj.getString("category");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        String name = obj.getString("name");
                        int rep_id = obj.getInt("rep_id");

                        RequestReport report = new RequestReport(rep_id,date + " " + time, category, name);
                        requestList.add(report);
                    }
                    requestAdapter = new RequestAdapter(ReportActivity.this,ReportActivity.this,requestList,swiper);
                    recyclerView.setAdapter(requestAdapter);
                    requestAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ReportActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("query", finalQuery);
                params.put("req_type", "Inventory");
                return params;
            }
        };
        RequestQueueHandler.getInstance(ReportActivity.this).addToRequestQueue(str);
    }

    private void loadInventoryReports() {
        reportsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_REPORT
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.w("REsP", "length : " + response.length());
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int rep_id = obj.getInt("rep_id");
                        String cat = obj.getString("category");
                        int room_id = obj.getInt("room_id");
                        String date = obj.getString("date");
                        String room_name = obj.getString("room_name");
                        String cust_id = obj.getString("cust_id");
                        String tech_id = obj.getString("tech_id");
                        String time = obj.getString("time");
                        int signed = obj.getInt("cust_signed");
                        String remarks = obj.getString("remarks");
                        int htech_signed = obj.getInt("htech_signed");
                        int admin_signed = obj.getInt("admin_signed");

                        Reports reports = new Reports(date + " " + time, cat, room_name, room_id, rep_id);
                        reportsList.add(reports);
                    }

                    Log.w("LOADED", "Server reports");
                    if (reportsList.isEmpty()) {
                        Log.w("NOREPORTS", "NO REPORTS");
                    } else {
//                        addDetails();
                        reportAdapter = new ReportAdapter(ReportActivity.this, ReportActivity.this, reportsList, swiper);
                        recyclerView.setAdapter(reportAdapter);
                        reportAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    Log.e("RESPONSE", response);
                    Log.e("JSON ERROR 1", "ReportFragment: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Volleyerror 1", "Load ReportsLoader: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(ReportActivity.this).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(ReportActivity.this).addToRequestQueue(stringRequest);
    }
}
