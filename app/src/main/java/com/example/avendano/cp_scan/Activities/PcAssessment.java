package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;

import dmax.dialog.SpotsDialog;

public class PcAssessment extends AppCompatActivity {

    private Toolbar toolbar;
    private int room_id;
    String model;
    Boolean manual;
    RadioGroup rGroup;
    CheckBox monitor, mb, pr, ram, hdd, keyboard, mouse, vga;
    Button save;
    SQLiteHandler db;
    int comp_id, pc_no;
    TextView instr;

    AlertDialog dialog;

    private static final String WORKING = "OK";
    private static final String NOT_WORKING = "NONE/NOT WORKING";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcassessment);


        db = new SQLiteHandler(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dialog = new SpotsDialog(PcAssessment.this, "Loading...");
        dialog.show();

        save = (Button) findViewById(R.id.save);
        monitor = findViewById(R.id.pc_monitor);
        mb = findViewById(R.id.pc_mb);
        pr = findViewById(R.id.pc_processor);
        ram = findViewById(R.id.pc_ram);
        hdd = findViewById(R.id.pc_hdd);

        keyboard = findViewById(R.id.kboard_check);
        mouse = findViewById(R.id.mouse_check);
        vga = findViewById(R.id.vga_check);
        rGroup = findViewById(R.id.group);

        comp_id = getIntent().getIntExtra("comp_id", 0);
        room_id = getIntent().getIntExtra("room_id", 0);
        manual = getIntent().getBooleanExtra("manual", false);
        model = getIntent().getStringExtra("model");

        instr = (TextView) findViewById(R.id.instr);

        if (manual) {

        } else {
            checkSerial();
        }

        new GetCompDetails().execute();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new addAssessment().execute();
            }
        });
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = rGroup.findViewById(checkedId);
                String stat = btn.getText().toString().trim();
                if (stat.equalsIgnoreCase("working"))
                    instr.setText("Check the working peripherals");
                else if (stat.equalsIgnoreCase("defective"))
                    instr.setText("Check the defective peripherals");
                else
                    instr.setText("Check the missing peripherals");
            }
        });
    }

    private void checkSerial() {
        //get serial n status from temporary tas check kung tama ung serial sa nkalagay sa comp table
    }

    public class GetCompDetails extends AsyncTask<Void, Void, Void> {
        String mon_serial, mb_serial, pr_type, ram_size, hdd_size, kb, mouse_attach, vga_attach;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mon_serial = "";
            mb_serial = "";
            pr_type = "";
            ram_size = "";
            hdd_size = "";
            vga_attach = "";
            mouse_attach = "";
            kb = "";
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor c = db.getPcToAssess(comp_id);
            if (c.moveToFirst()) {
                this.mon_serial = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                this.mb_serial = c.getString(c.getColumnIndex(db.COMP_MB));
                this.pr_type = c.getString(c.getColumnIndex(db.COMP_PR));
                this.ram_size = c.getString(c.getColumnIndex(db.COMP_RAM));
                this.hdd_size = c.getString(c.getColumnIndex(db.COMP_HDD));
                this.vga_attach = c.getString(c.getColumnIndex(db.COMP_VGA));
                this.kb = c.getString(c.getColumnIndex(db.COMP_KBOARD));
                this.mouse_attach = c.getString(c.getColumnIndex(db.COMP_MOUSE));
                pc_no = c.getInt(c.getColumnIndex(db.COMP_NAME));
            }
            toolbar.setTitle("PC " + pc_no);
            monitor.setText(mon_serial);
            mb.setText(mb_serial);
            pr.setText(pr_type);
            ram.setText(ram_size);
            hdd.setText(hdd_size);
            vga.setText(vga.getText() + "(" +vga_attach+ ")");
            keyboard.setText(keyboard.getText() + "(" +kb+ ")");
            mouse.setText(mouse.getText() + "(" +mouse_attach+ ")");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    public class addAssessment extends AsyncTask<Void, Void, Void> {
        int idx;
        RadioButton btn;
        String status, mon, motherboard, processor, comp_ram, comp_hdd, comp_kb, comp_mouse, comp_vga;
        AlertDialog saving;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            saving = new SpotsDialog(PcAssessment.this, "Saving...");
            saving.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            idx = rGroup.getCheckedRadioButtonId();
            btn = rGroup.findViewById(idx);
            status = btn.getText().toString().trim();
            String comp_status = "";
            if (status.equalsIgnoreCase("missing")) {
                if (monitor.isChecked()) {
                    mon = "Missing";
                } else {
                    mon = WORKING;
                }
                if (mb.isChecked()) {
                    motherboard = "Missing";
                } else {
                    motherboard = WORKING;
                }
                if (pr.isChecked()) {
                    processor = "Missing";
                } else {
                    processor = pr.getText().toString().trim();
                }
                if (ram.isChecked()) {
                    comp_ram = "Missing";
                } else {
                    comp_ram = ram.getText().toString().trim();
                }
                if (hdd.isChecked()) {
                    comp_hdd = "Missing";
                } else {
                    comp_hdd = hdd.getText().toString().trim();
                }
                if (keyboard.isChecked()) {
                    comp_kb = "Missing";
                } else {
                    comp_kb = WORKING;
                }
                if (mouse.isChecked()) {
                    comp_mouse = "Missing";
                } else {
                    comp_mouse = WORKING;
                }
                if (vga.isChecked()) {
                    comp_vga = "Missing";
                } else {
                    comp_vga = "BUILT-IN";
                }
                if(mb.getText().toString().trim().equalsIgnoreCase("missing") ||
                        processor.equalsIgnoreCase("missing") ||
                        comp_ram.equalsIgnoreCase("missing")){
                    comp_status = "Missing Components";
                }else
                    comp_status = "Working";
            }else if (status.equalsIgnoreCase("defective")){
                if (monitor.isChecked()) {
                    mon = NOT_WORKING;
                } else {
                    mon = WORKING;
                }
                if (mb.isChecked()) {
                    motherboard = NOT_WORKING;
                } else {
                    motherboard = WORKING;
                }
                if (pr.isChecked()) {
                    processor = NOT_WORKING;
                } else {
                    processor = pr.getText().toString().trim();
                }
                if (ram.isChecked()) {
                    comp_ram = NOT_WORKING;
                } else {
                    comp_ram = ram.getText().toString().trim();
                }
                if (hdd.isChecked()) {
                    comp_hdd = NOT_WORKING;
                } else {
                    comp_hdd = hdd.getText().toString().trim();
                }
                if (keyboard.isChecked()) {
                    comp_kb = NOT_WORKING;
                } else {
                    comp_kb = WORKING;
                }
                if (mouse.isChecked()) {
                    comp_mouse = NOT_WORKING;
                } else {
                    comp_mouse = WORKING;
                }
                if (vga.isChecked()) {
                    comp_vga = NOT_WORKING;
                } else {
                    comp_vga = "BUILT-IN";
                }
                if(mb.getText().toString().trim().equalsIgnoreCase(NOT_WORKING) ||
                        processor.equalsIgnoreCase(NOT_WORKING) ||
                        comp_ram.equalsIgnoreCase(NOT_WORKING)){
                    comp_status = "Defective";
                }else
                    comp_status = "Working";
            }else { //working
                if (monitor.isChecked()) {
                    mon = WORKING;
                } else {
                    mon = NOT_WORKING;
                }
                if (mb.isChecked()) {
                    motherboard = WORKING;
                } else {
                    motherboard = NOT_WORKING;
                }
                if (pr.isChecked()) {
                    processor = pr.getText().toString().trim();
                } else {
                    processor = NOT_WORKING;
                }
                if (ram.isChecked()) {
                    comp_ram = ram.getText().toString().trim();
                } else {
                    comp_ram = NOT_WORKING;
                }
                if (hdd.isChecked()) {
                    comp_hdd = hdd.getText().toString().trim();
                } else {
                    comp_hdd = NOT_WORKING;
                }
                if (keyboard.isChecked()) {
                    comp_kb = WORKING;
                } else {
                    comp_kb = NOT_WORKING;
                }
                if (mouse.isChecked()) {
                    comp_mouse = WORKING;
                } else {
                    comp_mouse = NOT_WORKING;
                }
                if (vga.isChecked()) {
                    comp_vga = "BUILT-IN";
                } else {
                    comp_vga = NOT_WORKING;
                }
                if(mb.getText().toString().trim().equalsIgnoreCase(NOT_WORKING) ||
                        processor.equalsIgnoreCase(NOT_WORKING) ||
                        comp_ram.equalsIgnoreCase(NOT_WORKING)){
                    comp_status = "Defective";
                }else
                    comp_status = "Working";
            }

            long in = db.addAssessedPc(comp_id, pc_no, model, motherboard, mb.getText().toString().trim()
                    , processor, mon, monitor.getText().toString().trim(), comp_ram, comp_kb,
                    comp_mouse, comp_status
                    , comp_vga, comp_hdd);

            db.updateScannedStatus(1, comp_id);
            Log.w("DETAILS INSERTED: ", "Insert STATUS: " + in);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saving.dismiss();
            Intent intent = new Intent(getApplicationContext(), AssessmentActivity.class);
            intent.putExtra("room_id", room_id);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), AssessmentActivity.class);
                intent.putExtra("room_id", room_id);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}
