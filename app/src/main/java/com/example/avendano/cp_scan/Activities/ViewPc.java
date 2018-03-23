package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
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

public class ViewPc extends AppCompatActivity {

    private int comp_id;
    SQLiteHandler db;
    ProgressDialog progressDialog;

    TextView pcno, room_name, comp_status, instr, pc_os;
    TextView pc_model, pc_mb, pc_monitor, pc_processor, pc_ram, pc_hdd, pc_mouse, pc_vga, pc_kb;
    CheckBox monitor, mb, pr, ram, hdd, keyboard, mouse, vga;
    Button report, cancel;
    RadioGroup rGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pc);

        comp_id = getIntent().getIntExtra("comp_id", 0);

        db = new SQLiteHandler(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
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
                if (connection_detector.isConnected()) {
                    if(cancel.getVisibility() == View.VISIBLE){
                        //add room_btn
                    }else {
                        instr.setVisibility(View.VISIBLE);
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

                        monitor.setChecked(false);
                        mb.setChecked(false);
                        pr.setChecked(false);
                        ram.setChecked(false);
                        hdd.setChecked(false);
                        keyboard.setChecked(false);
                        mouse.setChecked(false);
                        vga.setChecked(false);
                    }
                }else{
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
        });
        showDialog();
        loadDetails();
    }

    private void loadDetails() {
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
