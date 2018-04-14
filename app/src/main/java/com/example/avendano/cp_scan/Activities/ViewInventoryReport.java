package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.ReportDetailsAdapter;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.ReportDetails;
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

public class ViewInventoryReport extends AppCompatActivity {

    RecyclerView recyclerView;
    SQLiteHandler db;
    RecyclerView.LayoutManager layoutManager;
    List<ReportDetails> reportDetailsList;
    Button sign;
    TextView tech, cust, dean, technician, custodian;
    int rep_id;
    String type;
    String remark;
    android.app.AlertDialog progress;
    Connection_Detector connection_detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_inventory_report);
        db = new SQLiteHandler(ViewInventoryReport.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        connection_detector = new Connection_Detector(this);
        reportDetailsList = new ArrayList<>();
        remark = "";
        recyclerView = (RecyclerView) findViewById(R.id.inventory_details);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        progress.setCancelable(false);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        sign = (Button) findViewById(R.id.sign);
        rep_id = getIntent().getIntExtra("rep_id", 0);
        type = getIntent().getStringExtra("type");
        tech = (TextView) findViewById(R.id.tech);
        cust = (TextView) findViewById(R.id.cust);
        dean = (TextView) findViewById(R.id.dean);
        technician = (TextView) findViewById(R.id.reporter);
        custodian = (TextView) findViewById(R.id.custodian);

        Log.e("INTENT", "ID - " + rep_id + " " + type);

        if (SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("custodian") ||
                SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("main technician") ||
                SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("admin") || SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("admin")) {
            sign.setVisibility(View.VISIBLE);
        }
        if (type.equalsIgnoreCase("request"))
            setDataFrmServer();
        else
            setData();

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection_detector.isConnected()) {
                    progress.show();
                    checkSignature();
                } else
                    Toast.makeText(ViewInventoryReport.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSignature() {
        final String username = SharedPrefManager.getInstance(ViewInventoryReport.this).getKeyUsername();
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_SIGNATURE
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("SIGN", response);
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        if (obj.isNull("signature")) {
                            alertUser();
                        } else {
//                            updateSignedStatus();
                            progress.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    Log.e("SIGN", response);
                    e.printStackTrace();
                    progress.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("SIGN", error.getMessage());
                progress.dismiss();
                Toast.makeText(ViewInventoryReport.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("username", username);
                return param;
            }
        };
        RequestQueueHandler.getInstance(this).addToRequestQueue(str);
    }

    private void alertUser() {
        progress.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Signature...")
                .setMessage("Seems you don't have " +
                        "digital signature yet, press continue to create one now.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(ViewInventoryReport.this, SignatureActivity.class);
                        intent.putExtra("from", "inventory");
                        intent.putExtra("type", type);
                        intent.putExtra("rep_id", rep_id);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setDataFrmServer() {
        reportDetailsList.clear();
        class getData extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                getReport();
                return null;
            }

            private void getReport() {
                String query = "";
                String role = SharedPrefManager.getInstance(ViewInventoryReport.this).getUserRole();
                String user_id = SharedPrefManager.getInstance(ViewInventoryReport.this).getUserId();
                Log.e("USER", role + " " + user_id);
                if (role.equalsIgnoreCase("main technician") || role.equalsIgnoreCase("admin"))
                    query = "select * from assessment_reports where rep_id in (select rep_id from " +
                            "request_inventory as i where req_status = 'Done');";
                else
                    query = "select * from assessment_reports where (rep_id in (select rep_id from " +
                            "request_inventory as i where req_status = 'Done')) and (technician_id = '" + user_id + "' " +
                            "or custodian_id = '" + user_id + "');";
                final String finalQuery = query;
                StringRequest stringRequest = new StringRequest(Request.Method.POST
                        , AppConfig.URL_GET_REQ_REPORTS
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.w("REsP", response);
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int id = obj.getInt("rep_id");
                                String cust_name = obj.getString("custodian");
                                String tech_name = obj.getString("technician");
                                int cust_signed = obj.getInt("cust_signed");
                                String remarks = obj.getString("remarks");
                                int tech_sign = obj.getInt("htech_signed");
                                int admin_sign = obj.getInt("admin_signed");

                                if (rep_id == id) {
                                    remark = remarks;
                                    custodian.setText(cust_name);
                                    technician.setText(tech_name);
                                    if (cust_signed != 0)
                                        cust.setText("Yes");
                                    else
                                        cust.setText("No");
                                    if (tech_sign != 0)
                                        tech.setText("Yes");
                                    else
                                        tech.setText("No");
                                    if (admin_sign != 0)
                                        dean.setText("Yes");
                                    else
                                        dean.setText("No");

                                    loadDetailsHandler();
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            progress.dismiss();
                            Log.e("JSON ERROR 1", "ReportFragment: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        Log.w("Volleyerror 1", "Load ReportsLoader: " + error.getMessage());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("query", finalQuery);
                        param.put("req_type", "Inventory");
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(ViewInventoryReport.this).addToRequestQueue(stringRequest);
            }

            private void loadDetailsHandler() {
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getReportDetails();
                    }
                }, 3000);
            }

            private void getReportDetails() {
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
                                        String model = obj.getString("model");
                                        String mb = obj.getString("mb");
                                        String pr = obj.getString("pr");
                                        String mon = obj.getString("mon");
                                        String ram = obj.getString("ram");
                                        String kb = obj.getString("kb");
                                        String mouse = obj.getString("mouse");
                                        String vga = obj.getString("vga");
                                        String hdd = obj.getString("hdd");
                                        String comp_status = obj.getString("comp_status");
                                        int pc_no = obj.getInt("pc_no");

                                        ReportDetails reports = new ReportDetails(pc_no, mon, mb, pr, ram, hdd, vga
                                                , mouse, kb, comp_status, model, true);
                                        reportDetailsList.add(reports);
                                    }
                                    ReportDetailsAdapter adapter = new ReportDetailsAdapter(reportDetailsList);
                                    recyclerView.setAdapter(adapter);
                                    progress.dismiss();
                                } catch (JSONException e) {
                                    progress.dismiss();
                                    Log.e("JSON DETAILS", e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        Log.e("RESPONSE DETAILS", error.getMessage());
                        Toast.makeText(ViewInventoryReport.this, "Can't connect to the server, try again later", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("rep_id", String.valueOf(rep_id));
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(ViewInventoryReport.this).addToRequestQueue(str);
            }
        }

        new getData().execute();
    }

    private void setData() {
        reportDetailsList.clear();
        Cursor report_details = db.getReportDetailsById(rep_id);
        Cursor report = db.getReportByRepId(rep_id);
        if (report_details.moveToFirst()) {
            do {
                //COMP_ID,COMP_NAME, COMP_MODEL, COMP_MB, COMP_PR, COMP_MONITOR,
//                COMP_RAM, COMP_KBOARD, COMP_MOUSE, COMP_VGA, COMP_HDD, COMP_STATUS, REPORT_MB_SERIAL,REPORT_MON_SERIAL
                int pc_no = report_details.getInt(report_details.getColumnIndex(db.COMP_NAME));
                String status = report_details.getString(report_details.getColumnIndex(db.COMP_STATUS));
                String monitor = report_details.getString(report_details.getColumnIndex(db.COMP_MONITOR));
                String mb = report_details.getString(report_details.getColumnIndex(db.COMP_MB));
                String model = report_details.getString(report_details.getColumnIndex(db.COMP_MODEL));
                String pr = report_details.getString(report_details.getColumnIndex(db.COMP_PR));
                String ram = report_details.getString(report_details.getColumnIndex(db.COMP_RAM));
                String hdd = report_details.getString(report_details.getColumnIndex(db.COMP_HDD));
                String vga = report_details.getString(report_details.getColumnIndex(db.COMP_VGA));
                String mouse = report_details.getString(report_details.getColumnIndex(db.COMP_MOUSE));
                String kb = report_details.getString(report_details.getColumnIndex(db.COMP_KBOARD));

                ReportDetails reports = new ReportDetails(pc_no, monitor, mb, pr, ram, hdd, vga
                        , mouse, kb, status, model, true);
                reportDetailsList.add(reports);
            } while (report_details.moveToNext());
            ReportDetailsAdapter adapter = new ReportDetailsAdapter(reportDetailsList);
            recyclerView.setAdapter(adapter);
        }
        if (report.moveToFirst()) {
            int cust_sign = report.getInt(report.getColumnIndex(db.REPORT_CUST_SIGNED));
            int tech_sign = report.getInt(report.getColumnIndex(db.REPORT_HTECH_SIGNED));
            int dean_sign = report.getInt(report.getColumnIndex(db.REPORT_ADMIN_SIGNED));
            String cust_id = report.getString(report.getColumnIndex(db.COLUMN_CUST_ID));
            String tech_id = report.getString(report.getColumnIndex(db.COLUMN_TECH_ID));
            String rem = report.getString(report.getColumnIndex(db.REPORT_REMARKS));
            remark = rem;
            Cursor custName = db.getCustName(cust_id);
            Cursor techName = db.getTechName(tech_id);
            if (custName.moveToFirst()) {
                custodian.setText(custName.getString(custName.getColumnIndex(db.ROOMS_CUSTODIAN)));
            }
            if (techName.moveToFirst()) {
                technician.setText(techName.getString(techName.getColumnIndex(db.ROOMS_TECHNICIAN)));
            }
            if (cust_sign != 0)
                cust.setText("Yes");
            else
                cust.setText("No");
            if (tech_sign != 0)
                tech.setText("Yes");
            else
                tech.setText("No");
            if (dean_sign != 0)
                dean.setText("Yes");
            else
                dean.setText("No");
        }
        progress.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                ViewInventoryReport.this.finish();
                return true;
            }
            case R.id.remarks: {
                showRemark();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRemark() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewInventoryReport.this);
        builder.setTitle("Technician Remarks");
        builder.setMessage("" + remark);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        if (remark.trim().length() == 0)
            Toast.makeText(this, "No remarks", Toast.LENGTH_SHORT).show();
        else
            alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remark_view_inventory, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ViewInventoryReport.this.finish();
    }
}
