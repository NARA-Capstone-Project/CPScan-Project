package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;

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
    Connection_Detector connection_detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pc);

        comp_id = getIntent().getIntExtra("comp_id", 0);
        room_id = getIntent().getIntExtra("room_id", 0);
        connection_detector = new Connection_Detector(this);
        db = new SQLiteHandler(this);
        progressDialog = new SpotsDialog(this, "Loading...");
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

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = rGroup.findViewById(checkedId);
                String stat = btn.getText().toString().trim();
                if (stat.equalsIgnoreCase("missing"))
                    instr.setText("Check the missing peripherals");
                else if (stat.equalsIgnoreCase("defective"))
                    instr.setText("Check the defective peripherals");
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
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connection_Detector connection_detector = new Connection_Detector(ViewPc.this);
//                    if (cancel.getVisibility() == View.VISIBLE) {
//                        if (SharedPrefManager.getInstance(ViewPc.this).getUserRole().equalsIgnoreCase("technician")) {
//                            //check kung may nagrequest for repair tas imamark as done na automatically
//                        } else {
//                            Intent intent = new Intent(ViewPc.this, RequestForRepair.class);
//                            intent.putExtra("comp_id", comp_id);
//                            startActivity(intent);
//                        }
//                    } else {
//                        instr.setVisibility(View.VISIBLE);
//                        cancel.setVisibility(View.VISIBLE);
//                        monitor.setVisibility(View.VISIBLE);
//                        mb.setVisibility(View.VISIBLE);
//                        pr.setVisibility(View.VISIBLE);
//                        ram.setVisibility(View.VISIBLE);
//                        hdd.setVisibility(View.VISIBLE);
//                        keyboard.setVisibility(View.VISIBLE);
//                        mouse.setVisibility(View.VISIBLE);
//                        vga.setVisibility(View.VISIBLE);
//                        rGroup.setVisibility(View.VISIBLE);
//
//                        monitor.setChecked(false);
//                        mb.setChecked(false);
//                        pr.setChecked(false);
//                        ram.setChecked(false);
//                        hdd.setChecked(false);
//                        keyboard.setChecked(false);
//                        mouse.setChecked(false);
//                        vga.setChecked(false);
//                    }
                if (report.getText().toString().equalsIgnoreCase("reported"))
                    checkLastReqRepair(true);
                else {
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

            }
        });
        showDialog();
        new loadDetails().execute();
    }

    class loadDetails extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            loadPcDetails();
            checkLastReqRepair(false);
            return null;
        }
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
                    Log.e("RESPONSE", response);
                    if (!obj.getBoolean("error")) {
                        if (!obj.getBoolean("pending")) { //kapag hindi pa nagrerequest
                            report.setText("Report");
                            report.setBackgroundResource(R.color.darkorange);
                            report.setTextColor(getResources().getColor(R.color.white));
                        } else { //kapag na request na
                            report.setText("Reported");
                            report.setBackgroundResource(R.drawable.style_button_white);
                            report.setTextColor(getResources().getColor(R.color.darkorange));
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
                                updateSQlite(req_id);
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

    private void updateSQlite(int req_id) {
        db.updateReqRepStatus(req_id);
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
            View v = ViewPc.this.findViewById(android.R.id.content);
            Snackbar.make(v, "No data retrieved.", Snackbar.LENGTH_SHORT).show();
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
