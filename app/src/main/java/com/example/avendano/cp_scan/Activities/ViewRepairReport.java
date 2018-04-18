package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ViewRepairReport extends AppCompatActivity {
    TextView pc_name, custodian, image, date_req, time_req, date, time, req_details, tech;
    TextView pc_model, pc_mon, pc_mb, pc_pr, pc_ram, pc_hdd, pc_kb, pc_mouse, pc_vga, status;
    TextView date_reported;
    AlertDialog progress;
    String image_path;
    int rep_id;
    Toolbar toolbar;
    String remark;
    VolleyRequestSingleton volley;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_repair_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        volley = new VolleyRequestSingleton(this);

        rep_id = getIntent().getIntExtra("rep_id", 0);

        remark = "";
        date_reported = (TextView) findViewById(R.id.datetime);
        req_details = (TextView) findViewById(R.id.req_details);
        tech = (TextView) findViewById(R.id.tech);
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);
        time_req = (TextView) findViewById(R.id.time_req);
        date_req = (TextView) findViewById(R.id.date_req);
        image = (TextView) findViewById(R.id.image);
        custodian = (TextView) findViewById(R.id.custodian);
        pc_name = (TextView) findViewById(R.id.pc_name);
        status = (TextView) findViewById(R.id.status);
        pc_mon = (TextView) findViewById(R.id.pc_mon);
        pc_model = (TextView) findViewById(R.id.pc_model);
        pc_mb = (TextView) findViewById(R.id.pc_mb);
        pc_pr = (TextView) findViewById(R.id.pc_pr);
        pc_ram = (TextView) findViewById(R.id.pc_ram);
        pc_hdd = (TextView) findViewById(R.id.pc_hdd);
        pc_mouse = (TextView) findViewById(R.id.pc_mouse);
        pc_vga = (TextView) findViewById(R.id.pc_vga);
        pc_kb = (TextView) findViewById(R.id.pc_kb);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.ROOT_URL + image_path))); /** replace with your own uri */
            }
        });

        loadDetails();
    }

    private void loadDetails() {
        loadRequestDetails();
    }

    private void loadRequestDetails() {
        volley.sendStringRequestGet(AppConfig.GET_REPAIR_REQ
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            Log.e("RESPONSE", response);
                            JSONArray array = new JSONArray(response);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int repid = 0;
                                if (!obj.isNull("rep_id"))
                                    repid = obj.getInt("rep_id");
                                String cust_name = obj.getString("cust_name");
                                String tech_name = obj.getString("tech_name");
                                String sched_date = obj.getString("set_date");
                                String sched_time = obj.getString("set_time");
                                String req_date = obj.getString("date_req");
                                String req_time = obj.getString("time_req");
                                String req_details = obj.getString("req_details");
                                String path = obj.getString("image");

                                if (rep_id == repid) {
                                    if (obj.isNull("image"))
                                        image_path = "";
                                    else
                                        image_path = AppConfig.ROOT_URL + path;
                                    if (image_path.trim().isEmpty())
                                        image.setVisibility(View.GONE);
                                    setRequestDetails(cust_name, tech_name, sched_date, sched_time, req_date,
                                            req_time, req_details);
                                    break;
                                }

                            }
                        } catch (JSONException e) {
                            progress.dismiss();
                            e.printStackTrace();
                            ViewRepairReport.this.finish();
                            Toast.makeText(ViewRepairReport.this, "An error occurred, try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        if (error instanceof TimeoutError)
                            Toast.makeText(ViewRepairReport.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ViewRepairReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setRequestDetails(String cust_name, String tech_name, String sched_date,
                                   String sched_time, String req_date, String req_time, String details) {
        custodian.setText(cust_name);
        tech.setText("Reported By: " + tech_name);
        date.setText(sched_date);
        time.setText(sched_time);
        date_req.setText(req_date);
        time_req.setText(req_time);
        req_details.setText(details);

        setReportDetails();
    }

    private void setReportDetails() {
        Map<String, String> param = new HashMap<>();
        param.put("rep_id", String.valueOf(rep_id));

        volley.sendStringRequestPost(AppConfig.GET_INVENTORY_REPORTS_DETAILS
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            Log.e("RESPONSE", response);
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
                                int repid = obj.getInt("rep_id");

                                if (rep_id == repid) {
                                    setReportDetails(model, mb, pr, mon, ram, kb, mouse, vga, hdd, pc_no, comp_status);
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            progress.dismiss();
                            Log.e("JSON DETAILS", e.getMessage());
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        if (error instanceof TimeoutError)
                            Toast.makeText(ViewRepairReport.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ViewRepairReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }, param);
    }

    private void setReportDetails(String model, String mb, String pr, String mon, String ram, String kb, String mouse, String vga, String hdd, int pc_no, String comp_status) {
        pc_kb.setText(kb);
        pc_mb.setText(mb);
        pc_mon.setText(mon);
        pc_model.setText(model);
        pc_mouse.setText(mouse);
        pc_vga.setText(vga);
        pc_hdd.setText(hdd);
        pc_ram.setText(ram);
        status.setText(comp_status);
        pc_pr.setText(pr);
        pc_name.setText("" + pc_no);
        getReportDetails();
    }

    private void getReportDetails() {
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
                                String remarks = obj.getString("remarks");
                                String reported_date = obj.getString("date");
                                String reported_time = obj.getString("time");

                                if (rep_id == id) {
                                    remark = remarks;
                                    date_reported.setText("Reported Date: " + reported_date + " " + reported_time);
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("JSON ERROR 1", "ReportFragment: " + e.getMessage());
                        }
                        progress.dismiss();
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        if (error instanceof TimeoutError)
                            Toast.makeText(ViewRepairReport.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ViewRepairReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remark_view_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                ViewRepairReport.this.finish();
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
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ViewRepairReport.this);
        builder.setMessage("" + remark);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        android.support.v7.app.AlertDialog alert = builder.create();
        if (remark.trim().length() == 0)
            Toast.makeText(this, "No remarks", Toast.LENGTH_SHORT).show();
        else
            alert.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
