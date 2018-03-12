package com.example.avendano.cp_scan.Fragments;


import android.app.ProgressDialog;
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
import com.example.avendano.cp_scan.Adapter.ReportAdapter;
import com.example.avendano.cp_scan.Adapter.RoomAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Getter_Setter.Reports;
import com.example.avendano.cp_scan.Getter_Setter.Rooms;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportFragment extends Fragment {
    private SQLiteHandler db;
    List<Reports> reportsList;
    RecyclerView recyclerView;
    SwipeRefreshLayout swiper;
    ProgressDialog progressDialog;
    ReportAdapter reportAdapter;

    public ReportFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        swiper = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        showDialog();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new SQLiteHandler(getContext());
        reportsList = new ArrayList<>();
    }

    private void loadFromServer() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_REPORT
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    hideDialog();
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
                        int comp_id = obj.getInt("comp_id");
                        String model = obj.getString("model");
                        String mb = obj.getString("mb");
                        String pr = obj.getString("pr");
                        String monitor = obj.getString("monitor");
                        String ram = obj.getString("ram");
                        String kb = obj.getString("kb");
                        String mouse = obj.getString("mouse");
                        String vga = obj.getString("vga");
                        String hdd = obj.getString("hdd");
                        String comp_status = obj.getString("status");
                        String mb_serial = obj.getString("mb_serial");
                        String mon_serial = obj.getString("mon_serial");
                        int pc_no = obj.getInt("pc_no");

                       boolean save = checkReport(cat,rep_id,room_id,signed,cust_id, tech_id,date, time, remarks,
                                room_name);
                       if(!save){
                           saveToDetails(rep_id,comp_id,pc_no, mb, mb_serial,pr,monitor, mon_serial,ram,hdd,vga,kb,comp_status, model,mouse);
                       }
                       if(cust_id.equals(SharedPrefManager.getInstance(getContext()).getUserId())
                               || tech_id.equals(SharedPrefManager.getInstance(getContext()).getUserId())){
                           Reports reports = new Reports(date, cat, room_name, room_id, rep_id);
                           reportsList.add(reports);
                       }
                    }
                    hideDialog();
                    reportAdapter = new ReportAdapter(getActivity(), getContext(), reportsList, swiper);
                    recyclerView.setAdapter(reportAdapter);
                } catch (JSONException e) {
                    Log.e("JSON ERROR 1", "ReportFragment: " + e.getMessage());
                    new ReportsLoader().execute(SharedPrefManager.getInstance(getContext()).getUserId());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("Volleyerror 1", "Load RoomsLoader");
                new ReportsLoader().execute(SharedPrefManager.getInstance(getContext()).getUserId());
            }
        });
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    private void saveToDetails(int rep_id, int comp_id,int pc_no, String mb, String mb_serial
            , String pr, String monitor, String mon_serial
            , String ram, String hdd, String vga, String kb, String comp_status, String model, String mouse) {
            db.addReportDetails(rep_id,comp_id,pc_no,model, mb, mb_serial,pr,monitor, mon_serial,ram,kb,mouse,vga, hdd,comp_status);
    }

    private boolean checkReport(String cat, int rep_id, int room_id,
                             int signed, String cust_id, String tech_id,
                             String date, String time, String remarks, String room_name) {
        Cursor c = db.getReportDetailsById(rep_id);
        if (!c.moveToFirst()) {
            long in = db.addReport(rep_id, room_id, cust_id, cat, tech_id
                    , date, time, signed, remarks, room_name);
            Log.w("NEW REPORT INSERT:", "Status : " + in);
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    class ReportsLoader extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String user = strings[0];
            loadLocalReports(user);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideDialog();
            reportAdapter = new ReportAdapter(getActivity(), getContext(), reportsList, swiper);
            recyclerView.setAdapter(reportAdapter);
            reportAdapter.notifyDataSetChanged();
        }
    }

    private void loadLocalReports(String user) {
        Cursor c = db.getReportByUserId(user);
        if (c.moveToFirst()) {
            do {

                int rep_id = c.getInt(c.getColumnIndex(db.REPORT_ID));
                String date = c.getString(c.getColumnIndex(db.REPORT_DATE));
                String cat = c.getString(c.getColumnIndex(db.REPORT_CATEGORY));
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                String room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));

                Reports reports = new Reports(date, cat, room_name, room_id, rep_id);
                reportsList.add(reports);
            } while (c.moveToNext());
            reportAdapter = new ReportAdapter(getActivity(), getContext(), reportsList, swiper);
            recyclerView.setAdapter(reportAdapter);
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
