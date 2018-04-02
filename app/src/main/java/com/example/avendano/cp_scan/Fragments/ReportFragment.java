package com.example.avendano.cp_scan.Fragments;


import android.app.AlertDialog;
import android.database.Cursor;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.ReportAdapter;
import com.example.avendano.cp_scan.Adapter.RequestAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.Reports;
import com.example.avendano.cp_scan.Model.RequestReport;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {
    private SQLiteHandler db;
    List<Reports> reportsList;
    List<RequestReport> requestList;
    RecyclerView recyclerView;
    SwipeRefreshLayout swiper;
    AlertDialog dialog;
    ReportAdapter reportAdapter;
    View view;
    Spinner rep_type;
    RequestAdapter requestAdapter;
    ProgressBar progressBar;
    RelativeLayout spinner;
    int previousSelection = -1;


    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_room, container, false);
        rep_type = (Spinner) view.findViewById(R.id.report_type);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        swiper = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        progressBar =(ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        spinner = (RelativeLayout) view.findViewById(R.id.spinner);
        spinner.setVisibility(View.VISIBLE);
        String[] items = new String[]{"Room Inventory Report", "Request Inventory Report", "Request Repair Report"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, items);
        rep_type.setAdapter(adapter);
        rep_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(previousSelection == -1){
                    previousSelection = 0;
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    new ReportsLoader().execute("Server");
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
                new ReportsLoader().execute("Server");
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dialog = new SpotsDialog(getContext(), "Loading...");
        dialog.show();

        new ReportsLoader().execute("Server");

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(getContext());
        reportsList = new ArrayList<>();
        requestList = new ArrayList<>();
        previousSelection = -1;
    }

    class ReportsLoader extends AsyncTask<String, Void, Void> {
//        select * from assessment_reports where not exists (select null from request_repair where request_repair.rep_id = assessment_reports.rep_id) and not exists (select null from request_inventory where request_inventory.rep_id = assessment_reports.rep_id);
//        select * from assessment_reports where (rep_id in (select rep_id from request_repair as i where req_status = 'Done')) and (technician_id = '123456' or custodian_id = '123456');  // select request report
        //ung query fr request walang param so lahat sa query na nakalagay

        @Override
        protected Void doInBackground(String... strings) {
            String method = strings[0];
            if(rep_type.getSelectedItem().toString().equalsIgnoreCase("Room Inventory Report")){
                if (method.equalsIgnoreCase("local"))
                    loadLocalReports();
                else
                    loadFromServer();
            }else if(rep_type.getSelectedItem().toString().equalsIgnoreCase("Request Inventory Report")){
                loadInventoryRequestReport();
            }else{ // request repair
                loadRepairRequestReport();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(rep_type.getSelectedItem().toString().equalsIgnoreCase("Room Inventory Report")){
                reportAdapter = new ReportAdapter(getActivity(), getContext(), reportsList, swiper);
                recyclerView.setAdapter(reportAdapter);
//                reportAdapter.notifyDataSetChanged();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    swiper.setRefreshing(false);
                    dialog.dismiss();
                    progressBar.setVisibility(View.GONE);
                }
            }, 3000);
         }
    }

    private void loadRepairRequestReport(){
        requestList.clear();
        String query = "";
        String role = SharedPrefManager.getInstance(getContext()).getUserRole();
        String user_id = SharedPrefManager.getInstance(getContext()).getUserId();
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
                    requestAdapter = new RequestAdapter(getActivity(),getContext(),requestList,swiper);
                    recyclerView.setAdapter(requestAdapter);
                    requestAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
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
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }

    private void loadInventoryRequestReport(){
        requestList.clear();
        String query = "";
        String role = SharedPrefManager.getInstance(getContext()).getUserRole();
        String user_id = SharedPrefManager.getInstance(getContext()).getUserId();
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
                    requestAdapter = new RequestAdapter(getActivity(),getContext(),requestList,swiper);
                    recyclerView.setAdapter(requestAdapter);
                    requestAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
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
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }

    private void loadFromServer() {
        reportsList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_REPORT
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    db.deleteReportDetails();
                    db.deleteReport();
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

                        addReportToLocal(cat, rep_id, room_id, signed, htech_signed, admin_signed,
                                cust_id, tech_id, date, time, remarks,
                                room_name);
                    }

                    Log.w("LOADED", "Server reports");
                    if (reportsList.isEmpty()) {
                        db.deleteReportDetails();
                        db.deleteReport();
                        Log.w("NOREPORTS", "NO REPORTS");
                    } else {
                        addDetails();
                        reportAdapter = new ReportAdapter(getActivity(), getContext(), reportsList, swiper);
                        recyclerView.setAdapter(reportAdapter);
                        reportAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    new ReportsLoader().execute("Local");
                    Log.e("JSON ERROR 1", "ReportFragment: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Volleyerror 1", "Load ReportsLoader: " + error.getMessage());
                new ReportsLoader().execute("Local");
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(getContext()).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void addDetails() {
        class addDetailsToLocal extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {Cursor c = db.getAllReports();
                if(c.moveToFirst()){
                    do{
                        int rep_id = c.getInt(c.getColumnIndex(db.REPORT_ID));
                        addReportDetails(rep_id);
                    }while(c.moveToNext());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                //delay (para masave sa sqlite)
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        dialog.dismiss();
                    }
                }, 5000);

            }
        }
        new addDetailsToLocal().execute();
    }

    private void saveToDetails(int rep_id, int comp_id, int pc_no, String mb, String mb_serial
            , String pr, String monitor, String mon_serial
            , String ram, String hdd, String vga, String kb, String comp_status, String model, String mouse) {
        db.addReportDetails(rep_id, comp_id, pc_no, model, mb, mb_serial, pr, monitor, mon_serial,
                ram, kb, mouse, vga, hdd, comp_status);
        Log.w("REPORT DETAILS", "COUNT: " + db.getReportDetailsCount());
    }

    private void addReportToLocal(String cat, int rep_id, int room_id,
                                  int cust_signed, int htech_signed, int admin_signed, String cust_id, String tech_id,
                                  String date, String time, String remarks, String room_name) {

        long in = db.addReport(rep_id, room_id, cust_id, cat, tech_id
                , date, time, cust_signed, htech_signed, admin_signed, remarks, room_name);
        Log.w("NEW REPORT INSERT:", "Status : " + in);
    }

    private void addReportDetails(final int rep_id) {
        StringRequest str = new StringRequest(Request.Method.POST,
                AppConfig.URL_GET_REPORT_DETAILS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("RESPONSE", response);
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);

                                int comp_id = obj.getInt("comp_id");
                                String model = obj.getString("model");
                                String mb = obj.getString("mb");
                                String mb_serial = obj.getString("mb_serial");
                                String pr = obj.getString("pr");
                                String mon = obj.getString("mon");
                                String mon_serial = obj.getString("mon_serial");
                                String ram = obj.getString("ram");
                                String kb = obj.getString("kb");
                                String mouse = obj.getString("mouse");
                                String vga = obj.getString("vga");
                                String hdd = obj.getString("hdd");
                                String comp_status = obj.getString("comp_status");
                                int pc_no = obj.getInt("pc_no");

                                saveToDetails(rep_id, comp_id, pc_no, mb, mb_serial, pr, mon, mon_serial
                                        , ram, hdd, vga, kb, comp_status, model, mouse);
                            }
                        } catch (JSONException e) {
                            Log.e("JSON DETAILS", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("RESPONSE DETAILS", error.getMessage());
                Toast.makeText(getContext(), "Can't connect to the server, try again later", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("rep_id", String.valueOf(rep_id));
                return param;
            }
        };
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    private void loadLocalReports() {
        Log.w("LOADED", "Local reports");
        Cursor c = db.getAllReports();
        if (c.moveToFirst()) {
            do {
                int rep_id = c.getInt(c.getColumnIndex(db.REPORT_ID));
                String date = c.getString(c.getColumnIndex(db.REPORT_DATE));
                String cat = c.getString(c.getColumnIndex(db.REPORT_CATEGORY));
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                String room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
                String time = c.getString(c.getColumnIndex(db.REPORT_TIME));

                Reports reports = new Reports(date + " " + time, cat, room_name, room_id, rep_id);
                reportsList.add(reports);
            } while (c.moveToNext());
        } else {
            //no list
            Log.e("LOCALREPORTS", " EMPTY");
        }
    }
}
