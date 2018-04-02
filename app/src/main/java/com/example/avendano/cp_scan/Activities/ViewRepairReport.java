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
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
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
    AlertDialog progress;
    String image_path;
    int rep_id;
    Toolbar toolbar;
    String remark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_repair_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rep_id = getIntent().getIntExtra("rep_id", 0);

        remark = "";

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

        new getData().execute();
    }

    class getData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loadRequestDetails();
            return null;
        }

        private void loadRequestDetails() {
            StringRequest str = new StringRequest(Request.Method.POST
                    , AppConfig.URL_GET_ALL_REPAIR_REQUEST
                    , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
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
                            String sched_date = obj.getString("date");
                            String sched_time = obj.getString("time");
                            String req_date = obj.getString("date_requested");
                            String req_time = obj.getString("time_requested");
                            String details = obj.getString("req_details");
                            String path = obj.getString("image");

                            if (rep_id == repid) {
                                if (obj.isNull("image"))
                                    image_path = "";
                                else
                                    image_path = AppConfig.ROOT_URL + path;
                                if(image_path.trim().isEmpty())
                                    image.setVisibility(View.GONE);

                                setRequestDetails(cust_name, tech_name, sched_date, sched_time, req_date,
                                        req_time, details);

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
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progress.dismiss();
                    ViewRepairReport.this.finish();
                    Toast.makeText(ViewRepairReport.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> param = new HashMap<>();
                    param.put("id", SharedPrefManager.getInstance(ViewRepairReport.this).getUserId());
                    return param;
                }
            };
            RequestQueueHandler.getInstance(ViewRepairReport.this).addToRequestQueue(str);
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

                                    setReportDetails(model, mb, pr, mon, ram, kb, mouse, vga, hdd, pc_no, comp_status);
                                }
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
                    Toast.makeText(ViewRepairReport.this, "Can't connect to the server, try again later", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> param = new HashMap<>();
                    param.put("rep_id", String.valueOf(rep_id));
                    return param;
                }
            };
            RequestQueueHandler.getInstance(ViewRepairReport.this).addToRequestQueue(str);
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
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progress.dismiss();
                }
            }, 3000);
        }

        private void getReportDetails() {
            String query = "";
            String role = SharedPrefManager.getInstance(ViewRepairReport.this).getUserRole();
            String user_id = SharedPrefManager.getInstance(ViewRepairReport.this).getUserId();
            Log.e("USER", role + " " + user_id);
            if(role.equalsIgnoreCase("main technician") || role.equalsIgnoreCase("admin"))
                query = "select * from assessment_reports where rep_id in (select rep_id from " +
                        "request_inventory as i where req_status = 'Done');";
            else
                query= "select * from assessment_reports where (rep_id in (select rep_id from " +
                        "request_inventory as i where req_status = 'Done')) and (technician_id = '"+user_id+"' " +
                        "or custodian_id = '"+user_id+"');";
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
                            String remarks = obj.getString("remarks");

                            if (rep_id == id) {
                                remark = remarks;
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
            RequestQueueHandler.getInstance(ViewRepairReport.this).addToRequestQueue(stringRequest);
        }

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
