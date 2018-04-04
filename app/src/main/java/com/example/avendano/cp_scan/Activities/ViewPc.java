package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ViewPc extends AppCompatActivity {

    private int comp_id, room_id;
    SQLiteHandler db;
    android.app.AlertDialog progressDialog;

    TextView pcno, room_name, comp_status, instr, pc_os;
    TextView pc_model, pc_mb, pc_monitor, pc_processor, pc_ram, pc_hdd, pc_mouse, pc_vga, pc_kb;
    CheckBox monitor, mb, pr, ram, hdd, keyboard, mouse, vga;
    Button report, cancel;
    RadioGroup rGroup;
    RadioButton defective, missing, working;
    Connection_Detector connection_detector;
    EditText remark;
    int make_request_report;
    private static final String WORKING = "OK";
    private static final String NOT_WORKING = "NONE/NOT WORKING";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pc);

        comp_id = getIntent().getIntExtra("comp_id", 0);
        room_id = getIntent().getIntExtra("room_id", 0);
        make_request_report = getIntent().getIntExtra("request", 0);
        connection_detector = new Connection_Detector(this);
        db = new SQLiteHandler(this);
        progressDialog = new SpotsDialog(this, "Loading...");
        progressDialog.setCancelable(false);
        remark = (EditText) findViewById(R.id.remark);
        progressDialog.show();
        pcno = (TextView) findViewById(R.id.pc_no);
        room_name = (TextView) findViewById(R.id.pc_room);
        pc_model = (TextView) findViewById(R.id.pc_model);
        pc_monitor = (TextView) findViewById(R.id.pc_monitor);
        pc_mb = (TextView) findViewById(R.id.pc_mb);
        pc_processor = (TextView) findViewById(R.id.pc_processor);
        pc_ram = (TextView) findViewById(R.id.pc_ram);
        pc_hdd = (TextView) findViewById(R.id.pc_hdd);
        pc_mouse = (TextView) findViewById(R.id.pc_mouse);
        pc_vga = (TextView) findViewById(R.id.pc_vga);
        pc_kb = (TextView) findViewById(R.id.pc_kb);
        pc_os = (TextView) findViewById(R.id.pc_os);
        report = (Button) findViewById(R.id.pc_report);
        comp_status = (TextView) findViewById(R.id.pc_status);
        cancel = (Button) findViewById(R.id.cancel);
        monitor = findViewById(R.id.check_mon);
        mb = findViewById(R.id.check_mb);
        pr = findViewById(R.id.check_pr);
        ram = findViewById(R.id.check_ram);
        hdd = findViewById(R.id.check_hdd);
        instr = (TextView) findViewById(R.id.instr);
        keyboard = findViewById(R.id.check_kb);
        mouse = findViewById(R.id.check_mouse);
        vga = findViewById(R.id.check_vga);
        rGroup = findViewById(R.id.group);
        missing = findViewById(R.id.missing);
        defective = findViewById(R.id.defective);
        working = findViewById(R.id.working);

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = rGroup.findViewById(checkedId);
                String stat = btn.getText().toString().trim();
                if (stat.equalsIgnoreCase("missing")) {
                    if (instr.getVisibility() == View.GONE)
                        instr.setVisibility(View.VISIBLE);
                    instr.setText("Check the missing peripherals");
                    monitor.setVisibility(View.VISIBLE);
                    mb.setVisibility(View.VISIBLE);
                    pr.setVisibility(View.VISIBLE);
                    ram.setVisibility(View.VISIBLE);
                    hdd.setVisibility(View.VISIBLE);
                    keyboard.setVisibility(View.VISIBLE);
                    mouse.setVisibility(View.VISIBLE);
                    vga.setVisibility(View.VISIBLE);
                    remark.setVisibility(View.VISIBLE);
                } else if (stat.equalsIgnoreCase("defective")) {
                    if (instr.getVisibility() == View.GONE)
                        instr.setVisibility(View.VISIBLE);
                    instr.setText("Check the defective peripherals");
                    monitor.setVisibility(View.VISIBLE);
                    mb.setVisibility(View.VISIBLE);
                    pr.setVisibility(View.VISIBLE);
                    ram.setVisibility(View.VISIBLE);
                    hdd.setVisibility(View.VISIBLE);
                    keyboard.setVisibility(View.VISIBLE);
                    mouse.setVisibility(View.VISIBLE);
                    vga.setVisibility(View.VISIBLE);
                    remark.setVisibility(View.VISIBLE);
                } else {
                    if (instr.getVisibility() == View.VISIBLE)
                        instr.setVisibility(View.GONE);
                    monitor.setVisibility(View.GONE);
                    mb.setVisibility(View.GONE);
                    pr.setVisibility(View.GONE);
                    ram.setVisibility(View.GONE);
                    hdd.setVisibility(View.GONE);
                    keyboard.setVisibility(View.GONE);
                    mouse.setVisibility(View.GONE);
                    vga.setVisibility(View.GONE);
                    instr.setVisibility(View.GONE);
                }


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel.setVisibility(View.GONE);
                monitor.setVisibility(View.GONE);
                mb.setVisibility(View.GONE);
                pr.setVisibility(View.GONE);
                ram.setVisibility(View.GONE);
                hdd.setVisibility(View.GONE);
                keyboard.setVisibility(View.GONE);
                mouse.setVisibility(View.GONE);
                vga.setVisibility(View.GONE);
                rGroup.setVisibility(View.GONE);
                instr.setVisibility(View.GONE);
                remark.setVisibility(View.GONE);
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection_Detector connection_detector = new Connection_Detector(ViewPc.this);
                if (SharedPrefManager.getInstance(ViewPc.this).getUserRole().equalsIgnoreCase("custodian")) {
                    if (report.getText().toString().equalsIgnoreCase("reported")) {
                        progressDialog.show();
                        checkLastReqRepair(true);
                    } else {
                        if (connection_detector.isConnected()) {
                            Intent intent = new Intent(ViewPc.this, RequestForRepair.class);
                            intent.putExtra("comp_id", comp_id);
                            intent.putExtra("room_id", room_id);
                            startActivity(intent);
                            finish();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ViewPc.this);
                            builder.setMessage("No Internet Connection")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                } else if (SharedPrefManager.getInstance(ViewPc.this).getUserRole().equalsIgnoreCase("technician") ||
                        SharedPrefManager.getInstance(ViewPc.this).getUserRole().equalsIgnoreCase("main technician")) {
                    if (cancel.getVisibility() == View.VISIBLE) {
                        //check if may nag request and save report
                        //check inputs muna sa rbutn saka checkbox
                        progressDialog.show();
                        if (checkInput()) {
                            if (connection_detector.isConnected())
                                checkIfRequested();
                            else {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(ViewPc.this);
                                builder.setMessage("No Internet Connection")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(ViewPc.this, "You haven't check any computer status/peripherals", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        cancel.setVisibility(View.VISIBLE);
                        monitor.setVisibility(View.VISIBLE);
                        mb.setVisibility(View.VISIBLE);
                        pr.setVisibility(View.VISIBLE);
                        ram.setVisibility(View.VISIBLE);
                        hdd.setVisibility(View.VISIBLE);
                        keyboard.setVisibility(View.VISIBLE);
                        mouse.setVisibility(View.VISIBLE);
                        vga.setVisibility(View.VISIBLE);
                        rGroup.setVisibility(View.VISIBLE);
                        remark.setVisibility(View.VISIBLE);

                        monitor.setChecked(false);
                        mb.setChecked(false);
                        pr.setChecked(false);
                        ram.setChecked(false);
                        hdd.setChecked(false);
                        keyboard.setChecked(false);
                        mouse.setChecked(false);
                        vga.setChecked(false);
                    }
                } else {
                    report.setVisibility(View.GONE);
                }


            }
        });
        showDialog();
        new loadDetails().execute();
    }

    private boolean checkInput() {
        if (rGroup.getCheckedRadioButtonId() == -1) {
            return false;
        } else {
            //check kung may nakacheck sa checbox
            if (working.isChecked())
                return true;
            else {
                if (!monitor.isChecked()) {
                    if (!mb.isChecked()) {
                        if (!pr.isChecked()) {
                            if (!ram.isChecked()) {
                                if (!hdd.isChecked()) {
                                    if (!keyboard.isChecked()) {
                                        if (!mouse.isChecked()) {
                                            if (!vga.isChecked()) {
                                                return false;
                                            } else {
                                                return true;
                                            }
                                        } else {
                                            return true;
                                        }
                                    } else {
                                        return true;
                                    }
                                } else {
                                    return true;
                                }
                            } else {
                                return true;
                            }
                        } else {
                            return true;
                        }
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
    }

    class loadDetails extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loadPcDetails();
            checkLastReqRepair(false);
            return null;
        }
    }

    private void checkIfRequested() {
        class Checker extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                checking();
                return null;
            }

            private void checking() {
                final String msg = "This computer was requested for repair, do you want to make a report for this?";
                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_CHECK_LAST_REPAIR_REQUEST
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            Log.e("checking", response);
                            if (!obj.getBoolean("error")) {
                                //kapag may request na
                                if (make_request_report == 1) { //galing sa request_page
                                    if (obj.getBoolean("pending")) {
                                        int req_id = obj.getInt("req_id");
                                        Log.e("Array", details().toString());
                                        confirmReport(req_id, true);
                                    }
                                } else {
                                    if (obj.getBoolean("pending")) {
                                        if (obj.getString("technician").equals(SharedPrefManager.getInstance(ViewPc.this).getUserId())) {
                                            int req_id = obj.getInt("req_id");
                                            Log.e("Array", details().toString());
                                            String details = obj.getString("req_details");
                                            String path = obj.getString("image");
                                            if (obj.isNull("image"))
                                                path = "";
                                            if (details.trim().isEmpty())
                                                showAlert(req_id, msg, path);
                                            else
                                                showAlert(req_id, msg + " Request Details: " + obj.getString("req_details"), path);
                                        } else {
                                            confirmReport(0, false);
                                        }
                                    } else {
                                        Log.e("Array", details().toString());
                                        //savereportc
                                        confirmReport(0, false);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("RESPONSE", response);
                            Toast.makeText(ViewPc.this, "An error occurred, try again later", Toast.LENGTH_SHORT).show();
                            ViewPc.this.finish();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ViewPc.this, "Can't connect to the server, check your internet connection and try again", Toast.LENGTH_SHORT).show();
                        Log.e("VIEWROOM", "ERROR: " + error.getMessage());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("comp_id", String.valueOf(comp_id));
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(ViewPc.this).addToRequestQueue(str);
            }

            private String message() {
                String msg = "";
                //check kung may nakacheck sa checbox
                if (working.isChecked())
                    msg = "Confirm if computer is working and in good condition";
                else if (defective.isChecked()) {
                    msg = "The Following Peripherals are Defective: \n";
                    if (mb.isChecked() && pr.isChecked() && ram.isChecked() && hdd.isChecked() &&
                            monitor.isChecked() && keyboard.isChecked() && mouse.isChecked() && vga.isChecked())
                        msg = "Confirm if this computer is defective";
                    else {
                        if (monitor.isChecked())
                            msg = msg + "\tMonitor\n";
                        if (mb.isChecked() || pr.isChecked() || ram.isChecked() || hdd.isChecked())
                            msg = msg + "\tSystem Unit\n";

                        if (keyboard.isChecked())
                            msg = msg + "\tKeyboard\n";

                        if (mouse.isChecked())
                            msg = msg + "\tMouse\n";
                        if (vga.isChecked())
                            msg = msg + "\nVGA";
                    }
                } else {
                    msg = "The Following Peripherals are Missing: \n";
                    if (mb.isChecked() && pr.isChecked() && ram.isChecked() && hdd.isChecked() &&
                            monitor.isChecked() && keyboard.isChecked() && mouse.isChecked() && vga.isChecked())
                        msg = "Confirm if this computer is missing";
                    else {
                        if (monitor.isChecked())
                            msg = msg + "\tMonitor\n";
                        if (mb.isChecked() || pr.isChecked() || ram.isChecked() || hdd.isChecked())
                            msg = msg + "\tSystem Unit\n";

                        if (keyboard.isChecked())
                            msg = msg + "\tKeyboard\n";

                        if (mouse.isChecked())
                            msg = msg + "\tMouse\n";
                        if (vga.isChecked())
                            msg = msg + "\nVGA";
                    }
                }
                return msg;
            }

            private void confirmReport(final int req_id, final boolean req_save) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewPc.this);
                String msg = message();
                builder.setTitle("Confirm Report...")
                        .setMessage(msg)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveReport(req_id, req_save); //true = save din sa request_reports
                                progressDialog.show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                progressDialog.dismiss();
            }

            private void showAlert(final int req_id, String msg, final String image_path) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewPc.this);
                builder.setTitle("Report")
                        .setMessage(msg)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.show();
                                confirmReport(req_id, true);
                                Log.e("Array", details().toString());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.show();
                                confirmReport(req_id, false);
                                dialog.dismiss();
                                Log.e("Array", details().toString());
                            }
                        });

                if (image_path.isEmpty()) {

                } else
                    builder.setNeutralButton("View Image", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showImage(image_path);
                        }
                    });
                AlertDialog alert = builder.create();
                alert.show();
                progressDialog.dismiss();
            }

            private void showImage(String path) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.ROOT_URL + path))); /** replace with your own uri */
            }

            private void saveReport(final int req_id, final boolean req_save) {
                final String rem = remark.getText().toString().trim();
                final JSONArray array = details();
                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_SAVE_A_REPORT
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("RESPONSE", response);
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                Log.w("INSERT REPORT", "SUCCESS");
                                int rep = obj.getInt("rep_id");
                                setrep_id(rep, array, req_id, req_save);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(ViewPc.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(ViewPc.this, "An error occurred, please try again later.", Toast.LENGTH_SHORT).show();
                            Log.e("JSON ERROR", "SAVE REPORT: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.w("INSERT REPORT", "NOT SUCCESS");
                        Toast.makeText(ViewPc.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("tech_id", SharedPrefManager.getInstance(ViewPc.this).getUserId());
                        params.put("room_id", String.valueOf(room_id));
                        params.put("remarks", rem);
                        params.put("category", "Repair Report");
                        return params;
                    }
                };
                RequestQueueHandler.getInstance(ViewPc.this).addToRequestQueue(str);
            }

            private JSONArray details() {
                int idx;
                RadioButton btn;
                String status;

                //pag walang naka check ibig sabihin lahat working
                JSONObject obj = new JSONObject();
                int pc_no = Integer.parseInt(pcno.getText().toString().replace("PC", "").trim());
                String mb = "";
                String monitor = "";
                String pr = pc_processor.getText().toString().trim();
                String kb = pc_kb.getText().toString().trim();
                String mouse = pc_mouse.getText().toString().trim();
                String ram = pc_ram.getText().toString().trim();
                String hdd = pc_hdd.getText().toString().trim();
                String vga = pc_vga.getText().toString().trim();
                String comp_status = "";

                try {
                    idx = rGroup.getCheckedRadioButtonId();
                    btn = rGroup.findViewById(idx);
                    status = btn.getText().toString().trim();
                    if (status.equalsIgnoreCase("missing")) {
                        if (ViewPc.this.monitor.isChecked()) {
                            monitor = "Missing";
                        } else {
                            monitor = WORKING;
                        }
                        if (ViewPc.this.mb.isChecked()) {
                            mb = "Missing";
                        } else {
                            mb = WORKING;
                        }
                        if (ViewPc.this.pr.isChecked()) {
                            pr = "Missing";
                        }
                        if (ViewPc.this.ram.isChecked()) {
                            ram = "Missing";
                        }
                        if (ViewPc.this.hdd.isChecked()) {
                            hdd = "Missing";
                        }
                        if (ViewPc.this.keyboard.isChecked()) {
                            kb = "Missing";
                        }
                        if (ViewPc.this.mouse.isChecked()) {
                            mouse = "Missing";
                        }
                        if (ViewPc.this.vga.isChecked()) {
                            vga = "Missing";
                        }
                        if (ViewPc.this.pc_mb.getText().toString().trim().equalsIgnoreCase("missing") ||
                                pr.equalsIgnoreCase("missing") ||
                                ram.equalsIgnoreCase("missing")) {
                            comp_status = "Missing Components";
                        } else
                            comp_status = "Working";
                    } else if (status.equalsIgnoreCase("defective")) {
                        if (ViewPc.this.monitor.isChecked()) {
                            monitor = NOT_WORKING;
                        } else {
                            monitor = WORKING;
                        }
                        if (ViewPc.this.mb.isChecked()) {
                            mb = NOT_WORKING;
                        }
                        if (ViewPc.this.pr.isChecked()) {
                            pr = NOT_WORKING;
                        }
                        if (ViewPc.this.ram.isChecked()) {
                            ram = NOT_WORKING;
                        }
                        if (ViewPc.this.hdd.isChecked()) {
                            hdd = NOT_WORKING;
                        }
                        if (ViewPc.this.keyboard.isChecked()) {
                            kb = NOT_WORKING;
                        } else {
                            kb = WORKING;
                        }
                        if (ViewPc.this.mouse.isChecked()) {
                            mouse = NOT_WORKING;
                        } else {
                            mouse = WORKING;
                        }
                        if (ViewPc.this.vga.isChecked()) {
                            vga = NOT_WORKING;
                        } else {
                            vga = "BUILT-IN";
                        }
                        if (ViewPc.this.pc_mb.getText().toString().trim().equalsIgnoreCase(NOT_WORKING) ||
                                pr.equalsIgnoreCase(NOT_WORKING) ||
                                ram.equalsIgnoreCase(NOT_WORKING)) {
                            comp_status = "Defective";
                        } else
                            comp_status = "Working";
                    } else { //working
                        //lahat working
                        mb = WORKING;
                        monitor = WORKING;
                        kb = WORKING;
                        vga = WORKING;
                        mouse = WORKING;
                        comp_status = "Working";
                    }

                    obj.put("comp_id", comp_id);
                    obj.put("pc_no", pc_no);
                    obj.put("model", pc_model.getText().toString().trim());
                    obj.put("mb", mb);
                    obj.put("mb_serial", pc_mb.getText().toString().trim());
                    obj.put("monitor", monitor);
                    obj.put("mon_serial", pc_monitor.getText().toString().trim());
                    obj.put("pr", pr);
                    obj.put("kb", kb);
                    obj.put("mouse", mouse);
                    obj.put("ram", ram);
                    obj.put("hdd", hdd);
                    obj.put("vga", vga);
                    obj.put("comp_status", comp_status);
                } catch (JSONException e) {
                    Log.e("JSONEXEPTION", " " + e.getMessage());
                }
                Log.e("OBJECTS", "Details: " + comp_id + " " + pc_no + " " + pc_model.getText().toString().trim() + " " +
                        mb + " " + monitor + " " + pr + " " + kb + " " + mouse + " " + ram + " " + hdd + " " +
                        vga + " " + pc_mb.getText().toString().trim() + " " + pc_monitor.getText().toString().trim());
                JSONArray array = new JSONArray();
                array.put(obj);
                return array;
            }

            private void setrep_id(int rep, JSONArray array, int req_id, boolean req_save) {
                JSONArray newArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    try {
                        JSONObject newObj = new JSONObject();
                        JSONObject oldObj = array.getJSONObject(i);
                        newObj.put("comp_id", oldObj.getInt("comp_id"));
                        newObj.put("pc_no", oldObj.getInt("pc_no"));
                        newObj.put("model", oldObj.getString("model"));
                        newObj.put("mb", oldObj.getString("mb"));
                        newObj.put("mb_serial", oldObj.getString("mb_serial"));
                        newObj.put("monitor", oldObj.getString("monitor"));
                        newObj.put("mon_serial", oldObj.getString("mon_serial"));
                        newObj.put("pr", oldObj.getString("pr"));
                        newObj.put("kb", oldObj.getString("kb"));
                        newObj.put("mouse", oldObj.getString("mouse"));
                        newObj.put("ram", oldObj.getString("ram"));
                        newObj.put("hdd", oldObj.getString("hdd"));
                        newObj.put("vga", oldObj.getString("vga"));
                        newObj.put("comp_status", oldObj.getString("comp_status"));
                        newObj.put("rep_id", String.valueOf(rep));
                        //add objects to new jsoon array
                        newArray.put(newObj);
                    } catch (JSONException e) {
                        Log.e("JSONEXCEP", "" + e.getMessage());
                    }
                }
                Log.e("NEW JSONARRAY", "" + newArray.toString());
                saveReportDetails(rep, newArray, req_id, req_save);
            }

            private void saveReportDetails(final int rep, JSONArray newArray, final int req_id, final boolean req_save) {
                JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST
                        , AppConfig.URL_SAVE_A_DETAILS
                        , newArray
                        , new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject obj = response.getJSONObject(0);
                            if (!obj.getBoolean("error")) {
                                if (req_save) {
                                    saveRequestReport(req_id, rep);
                                } else {
                                    activityRecreate();
                                    progressDialog.dismiss();
                                    Toast.makeText(ViewPc.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                deleteReport(rep);
                            }
                        } catch (JSONException e) {
                            Log.e("error", " " + e.getMessage());
                            deleteReport(rep);
                        }
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        deleteReport(rep);
                        Log.w("save details", "NOT SUCCESS: " + error.getMessage());
                    }
                });
                RequestQueueHandler.getInstance(ViewPc.this).addToRequestQueue(req);
            }

            private void saveRequestReport(final int req_id, final int rep_id) {
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveRequest(req_id, rep_id);
                    }
                }, 3000);
            }

            private void saveRequest(final int req_id, int rep_id) {
                final String query = "UPDATE request_repair SET rep_id = " + rep_id + ", req_status = 'Done' WHERE req_id = ?";
                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_UPDATE_SCHEDULE
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(ViewPc.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                                updateSQlite(req_id, "Done");
                            }
                            activityRecreate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("714", error.getMessage());
                        progressDialog.dismiss();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put("query", query);
                        map.put("id", String.valueOf(req_id));
                        return map;
                    }
                };
                RequestQueueHandler.getInstance(ViewPc.this).addToRequestQueue(str);
            }

            private void deleteReport(final int rep_id) {
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteReportFrmServer(rep_id);
                    }
                }, 3000);
            }

            private void deleteReportFrmServer(final int rep_id) {
                final String query = "DELETE FROM assessment_report WHERE rep_id = ?";
                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_DELETE_REPORT
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            progressDialog.dismiss();
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(ViewPc.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                                Log.e("DELETED", "success");
                            } else {
                                Toast.makeText(ViewPc.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                                Log.e("DELETED", "not success");
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(ViewPc.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ViewPc.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        Log.e("ERROR", error.getMessage());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("query", query);
                        param.put("rep_id", String.valueOf(rep_id));
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(ViewPc.this).addToRequestQueue(str);
            }
        }
        new Checker().execute();
    }

    private void activityRecreate() {
        this.recreate();
    }

    private void checkLastReqRepair(final boolean popup) {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_CHECK_LAST_REPAIR_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    Log.e("last repair", response);
                    if (!obj.getBoolean("error")) {
                        if (!obj.getBoolean("pending")) { //kapag hindi pa nagrerequest
                            report.setText("Report");
                            report.setBackgroundResource(R.color.darkorange);
                            report.setTextColor(getResources().getColor(R.color.white));
                        } else { //kapag na request na
                            if (SharedPrefManager.getInstance(ViewPc.this).getUserRole().equalsIgnoreCase("custodian")) {
                                report.setText("Reported");
                                report.setBackgroundResource(R.drawable.style_button_white);
                                report.setTextColor(getResources().getColor(R.color.darkorange));
                            }
                            int req_id = obj.getInt("req_id");
                            int rep_id = 0;
                            if (!obj.isNull("rep_id"))
                                rep_id = obj.getInt("rep_id");
                            int compid = obj.getInt("comp_id");
                            String cust_id = obj.getString("custodian");
                            String tech_id = obj.getString("technician");
                            String date = obj.getString("date");
                            String time = obj.getString("time");
                            String msg = obj.getString("msg");
                            String req_date = obj.getString("date_requested");
                            String req_time = obj.getString("time_requested");
                            String req_status = obj.getString("req_status");
                            String req_details = obj.getString("req_details");

                            if (popup)
                                showRequestDetails(req_id, rep_id, compid, cust_id, tech_id, date, time,
                                        msg, req_date, req_time, req_status, req_details);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("RESPONSE", response);
                    Toast.makeText(ViewPc.this, "An error occurred, try again later", Toast.LENGTH_SHORT).show();
                    ViewPc.this.finish();
                    e.printStackTrace();
                    checkLastRepairRequestFrmLocal(popup);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.e("VIEWROOM", "ERROR: " + error.getMessage());
                checkLastRepairRequestFrmLocal(popup);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("comp_id", String.valueOf(comp_id));
                return param;
            }
        };
        RequestQueueHandler.getInstance(this).addToRequestQueue(str);
    }

    private void checkLastRepairRequestFrmLocal(boolean popup) {
        if (popup) {
            Cursor c = db.checkIfRepairRequested(comp_id);
            if (c.moveToFirst()) {
                int req_id = c.getInt(c.getColumnIndex(db.REQ_ID));
                int rep_id = c.getInt(c.getColumnIndex(db.REPORT_ID));
                int compid = c.getInt(c.getColumnIndex(db.COMP_ID));
                ;
                String cust_id = c.getString(c.getColumnIndex(db.COLUMN_CUST_ID));
                String tech_id = c.getString(c.getColumnIndex(db.COLUMN_TECH_ID));
                String date = c.getString(c.getColumnIndex(db.REQ_DATE));
                String time = c.getString(c.getColumnIndex(db.REQ_TIME));
                String msg = c.getString(c.getColumnIndex(db.REQ_MESSAGE));
                String req_date = c.getString(c.getColumnIndex(db.DATE_OF_REQ));
                String req_time = c.getString(c.getColumnIndex(db.TIME_OF_REQ));
                String req_status = c.getString(c.getColumnIndex(db.REQ_STATUS));
                String req_details = c.getString(c.getColumnIndex(db.REQ_DETAILS));

                showRequestDetails(req_id, rep_id, compid, cust_id, tech_id, date, time,
                        msg, req_date, req_time, req_status, req_details);
            }
        } else {
            Cursor c = db.checkIfRepairRequested(comp_id);
            if (c.moveToFirst()) {
                report.setText("Reported");
                report.setBackgroundResource(R.drawable.style_button_white);
                report.setTextColor(getResources().getColor(R.color.darkorange));
            } else {
                report.setText("Report");
                report.setBackgroundResource(R.color.darkorange);
                report.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    private void showRequestDetails(final int req_id, int rep_id, final int room_id, String cust_id,
                                    String tech_id, String date, String time, String msg,
                                    String date_req, String time_req, String req_status, String req_details) {
        //alertdialog
        String msg_body = "";
        if (msg.length() == 0) {
            msg_body = "Date requested: " + date_req + "\nTime Requested: " + time_req
                    + "\nAssigned Date: " + date + "\nAssigned Time: " + time + "\nRequest Details: "
                    + req_details + "\nRequest Status: " + req_status;
        } else {
            msg_body = "Date requested: " + date_req + "\nTime Requested: " + time_req
                    + "\nAssigned Date: " + date + "\nAssigned Time: " + time + "\nRequest Details: "
                    + req_details + "\nRequest Status: " + req_status
                    + "\n\nMessage: " + msg;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Report Details");
        builder.setMessage(msg_body);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (connection_detector.isConnected()) {
                            Intent intent = new Intent(ViewPc.this, EditRequestSchedule.class);
                            intent.putExtra("type", "repair");
                            intent.putExtra("room_pc_id", comp_id);
                            intent.putExtra("id", req_id);
                            ViewPc.this.startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(ViewPc.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Cancel Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        cancelRequestRepair(req_id);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelRequestRepair(final int req_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Request");
        builder.setMessage("Are you sure you want to cancel your request?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (connection_detector.isConnected())
                            cancelRequest(req_id);
                        else
                            Toast.makeText(ViewPc.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new loadDetails().execute();
    }

    private void cancelRequest(final int req_id) {
        class cancel {
            void callCancel() {
                new cancelling().execute();
            }

            class cancelling extends AsyncTask<Void, Void, Void> {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    progressDialog.show();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    cancelRequest();
                    return null;
                }
            }

            private void cancelRequest() {

                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_CANCEL_SCHEDULE
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            //update sqlite
                            if (!obj.getBoolean("error")) {
                                updateSQlite(req_id, "Cancel");
                                new loadDetails().execute();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                    }
                                }, 5000);
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(ViewPc.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            Toast.makeText(ViewPc.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                            Log.e("JSONERROR", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ViewPc.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("id", String.valueOf(req_id));
                        param.put("req_type", "repair");
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(ViewPc.this).addToRequestQueue(str);
            }
        }
        new cancel().callCancel();
    }

    private void updateSQlite(int req_id, String status) {
        db.updateReqRepStatus(req_id, status);
    }

    private void loadPcDetails() {
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_PC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int comp_id = obj.getInt("comp_id");
                        int room_id = 0;    //walang nakaassign na room
                        if (!obj.isNull("room_id")) {
                            room_id = obj.getInt("room_id");
                        }
                        if (comp_id == ViewPc.this.comp_id) {
                            //textview settext
                            if (room_id == 0) {
                                ViewPc.this.room_name.setText("No Assign Room");
                            } else {
                                getRoomName(room_id);
                            }
                            pcno.setText("PC " + obj.getInt("pc_no"));
                            pc_model.setText(obj.getString("model"));
                            pc_processor.setText(obj.getString("pr"));
                            pc_mb.setText(obj.getString("mb"));
                            pc_monitor.setText(obj.getString("monitor"));
                            pc_ram.setText(obj.getString("ram"));
                            pc_kb.setText(obj.getString("kboard"));
                            pc_mouse.setText(obj.getString("mouse"));
                            pc_vga.setText(obj.getString("vga"));
                            pc_hdd.setText(obj.getString("hdd"));
                            comp_status.setText(obj.getString("comp_status"));
                            pc_os.setText(obj.getString("os"));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e("json error", "viewpc: " + e.getMessage());
                    loadLocalPc();
                }
                hideDialog();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadLocalPc();
            }
        });
        RequestQueueHandler.getInstance(this).addToRequestQueue(str);
    }

    private void getRoomName(int room_id) {
        Cursor c = db.getRoomDetails(room_id);
        if (c.moveToFirst()) {
            room_name.setText(c.getString(c.getColumnIndex(db.ROOMS_NAME)));
        }
    }

    private void loadLocalPc() {
        Cursor c = db.getCompDetails(comp_id);
        if (c.moveToFirst()) {
            pcno.setText("PC " + c.getInt(c.getColumnIndex(db.COMP_NAME)));
            pc_model.setText(c.getString(c.getColumnIndex(db.COMP_MODEL)));
            pc_processor.setText(c.getString(c.getColumnIndex(db.COMP_PR)));
            pc_mb.setText(c.getString(c.getColumnIndex(db.COMP_MB)));
            pc_monitor.setText(c.getString(c.getColumnIndex(db.COMP_MONITOR)));
            pc_ram.setText(c.getString(c.getColumnIndex(db.COMP_RAM)));
            pc_kb.setText(c.getString(c.getColumnIndex(db.COMP_KBOARD)));
            pc_mouse.setText(c.getString(c.getColumnIndex(db.COMP_MOUSE)));
            pc_vga.setText(c.getString(c.getColumnIndex(db.COMP_VGA)));
            pc_hdd.setText(c.getString(c.getColumnIndex(db.COMP_HDD)));
            comp_status.setText(c.getString(c.getColumnIndex(db.COMP_STATUS)));
            pc_os.setText(c.getString(c.getColumnIndex(db.COMP_OS)));
        } else {
            pcno.setText("PC --");
            pc_model.setText("--");
            pc_processor.setText("--");
            pc_mb.setText("--");
            pc_monitor.setText("--");
            pc_ram.setText("--");
            pc_kb.setText("--");
            pc_mouse.setText("--");
            pc_vga.setText("--");
            pc_hdd.setText("--");
            comp_status.setText("--");
            pc_os.setText("--");
        }
        hideDialog();
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ViewPc.this.finish();
    }
}
