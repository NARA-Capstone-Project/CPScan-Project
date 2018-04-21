package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.ReportDetailsAdapter;
import com.example.avendano.cp_scan.Network_Handler.Connection_Detector;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
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
    RecyclerView.LayoutManager layoutManager;
    List<ReportDetails> reportDetailsList;
    Button sign;
    TextView tech, cust, dean, technician, custodian;
    int rep_id;
    String remark;
    android.app.AlertDialog progress;
    Connection_Detector connection_detector;
    VolleyRequestSingleton volley;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_inventory_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        volley = new VolleyRequestSingleton(this);
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

        tech = (TextView) findViewById(R.id.tech);
        cust = (TextView) findViewById(R.id.cust);
        dean = (TextView) findViewById(R.id.dean);
        technician = (TextView) findViewById(R.id.reporter);
        custodian = (TextView) findViewById(R.id.custodian);

        Log.e("INTENT", "ID - " + rep_id);

        if (SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("custodian") ||
                SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("head technician") ||
                SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("admin") || SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("admin")) {
            sign.setVisibility(View.VISIBLE);
        }

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

        setDataFrmServer();
    }

    private void checkSignature() {
        Map<String, String> param = new HashMap<>();
        param.put("user_id", SharedPrefManager.getInstance(this).getUserId());

        volley.sendStringRequestPost(AppConfig.GET_USER_INFO
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            Log.e("SIGN", response);
                            JSONObject obj = new JSONObject(response);
                            if (obj.isNull("signature")) {
                                signReport();
                            } else {
                                updateSignedStatus();
                                progress.dismiss();
                            }

                        } catch (JSONException e) {
                            Log.e("SIGN", response);
                            e.printStackTrace();
                            Toast.makeText(ViewInventoryReport.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                            progress.dismiss();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError)
                            Toast.makeText(ViewInventoryReport.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ViewInventoryReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }, param);
    }

    private void updateSignedStatus() {
        String user_role = SharedPrefManager.getInstance(this).getUserRole();
        String query;

        if (user_role.equalsIgnoreCase("head technician"))
            query = "UPDATE assessment_reports SET htech_signed = 1 where rep_id = '" + rep_id + "'";
        else if (user_role.equalsIgnoreCase("custodian"))
            query = "UPDATE assessment_reports SET cust_signed = 1 where rep_id = '" + rep_id + "'";
        else
            query = "UPDATE assessment_reports SET admin_signed = 1 where rep_id = '" + rep_id + "'";

        Map<String, String> param = new HashMap<>();
        param.put("query", query);

        volley.sendStringRequestPost(AppConfig.UPDATE_QUERY
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                recreate();
                            }else{
                                Toast.makeText(ViewInventoryReport.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ViewInventoryReport.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewInventoryReport.this, "Can't connect to the server, pleaase try again later", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        error.printStackTrace();
                    }
                }, param);
    }

    private void signReport() {
        progress.dismiss();
        Intent intent = new Intent(ViewInventoryReport.this, SignatureActivity.class);
        intent.putExtra("rep_id", rep_id);
        intent.putExtra("from", "report");
        startActivity(intent);
        finish();
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
                volley.sendStringRequestGet(AppConfig.GET_INVENTORY_REPORTS
                        , new VolleyCallback() {
                            @Override
                            public void onSuccessResponse(String response) {
                                Log.w("REsP", response);
                                try {
                                    JSONArray array = new JSONArray(response);
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject obj = array.getJSONObject(i);
                                        int id = obj.getInt("rep_id");
                                        String cust_name = obj.getString("cust_name");
                                        String tech_name = obj.getString("tech_name");
                                        int cust_signed = obj.getInt("cust_signed");
                                        String remarks = obj.getString("remarks");
                                        int tech_sign = obj.getInt("tech_signed");
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

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error instanceof TimeoutError)
                                    Toast.makeText(ViewInventoryReport.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ViewInventoryReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                            }
                        });
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
                Map<String, String> param = new HashMap<>();
                param.put("rep_id", String.valueOf(rep_id));

                volley.sendStringRequestPost(AppConfig.GET_INVENTORY_REPORTS_DETAILS
                        , new VolleyCallback() {
                            @Override
                            public void onSuccessResponse(String response) {
                                Log.e("RESPONSE", response);
                                try {
                                    JSONArray array = new JSONArray(response);
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject obj = array.getJSONObject(i);
                                        String model = obj.getString("model");
                                        String mb = obj.getString("mb_serial");
                                        String pr = obj.getString("pr");
                                        String mon = obj.getString("mon_serial");
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

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                progress.dismiss();
                                if (error instanceof TimeoutError)
                                    Toast.makeText(ViewInventoryReport.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ViewInventoryReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                            }
                        }, param);
            }
        }

        new getData().execute();
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ViewInventoryReport.this.finish();
    }
}
