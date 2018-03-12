package com.example.avendano.cp_scan.Activities;

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

import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;

public class PcAssessment extends AppCompatActivity {

    private Toolbar toolbar;
    private int room_id;
    String serial, model;
    Boolean manual;
    RadioGroup rGroup;
    CheckBox monitor, mb, pr, ram, hdd, keyboard, mouse, vga;
    Button save;
    SQLiteHandler db;
    int comp_id, pc_no;

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

        new GetCompDetails().execute();

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
        model = getIntent().getStringExtra("model");

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new addAssessment().execute();
            }
        });
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.missing) {
                    monitor.setEnabled(false);
                    mb.setEnabled(false);
                    pr.setEnabled(false);
                    hdd.setEnabled(false);
                    vga.setEnabled(false);
                    ram.setEnabled(false);
                    keyboard.setEnabled(false);
                    mouse.setEnabled(false);
                } else {
                    monitor.setEnabled(true);
                    mb.setEnabled(true);
                    pr.setEnabled(true);
                    hdd.setEnabled(true);
                    vga.setEnabled(true);
                    ram.setEnabled(true);
                    keyboard.setEnabled(true);
                    mouse.setEnabled(true);
                }
            }
        });

    }
    public class GetCompDetails extends AsyncTask<Void,Void,Void>{
        String mon_serial, mb_serial, pr_type, ram_size,hdd_size;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mon_serial = "";
            mb_serial = "";
            pr_type = "";
            ram_size ="";
            hdd_size = "";
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor c = db.getCompDetails(comp_id);
            if(c.moveToFirst()){
                this.mon_serial = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                this.mb_serial =  c.getString(c.getColumnIndex(db.COMP_MB));
                this.pr_type =  c.getString(c.getColumnIndex(db.COMP_PR));
                this.ram_size =  c.getString(c.getColumnIndex(db.COMP_RAM));
                this.hdd_size =  c.getString(c.getColumnIndex(db.COMP_HDD));
                pc_no =  c.getInt(c.getColumnIndex(db.COMP_NAME));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            toolbar.setTitle("PC " + pc_no);
            monitor.setText(mon_serial);
            mb.setText(mb_serial);
            pr.setText(pr_type);
            ram.setText(ram_size);
            hdd.setText(hdd_size);
        }
    }

    public class addAssessment extends AsyncTask<Void, Void, Void> {

        int idx;
        RadioButton btn;
        String status;

        @Override
        protected Void doInBackground(Void... voids) {
            idx = rGroup.getCheckedRadioButtonId();
            btn = rGroup.findViewById(idx);
            status = btn.getText().toString().trim();

            if (status.equalsIgnoreCase("missing")) {
                long in = db.addAssessedPc(comp_id, pc_no, model, "Missing", mb.getText().toString().trim()
                        ,pr.getText().toString().trim(), "Missing"
                        , monitor.getText().toString().trim(),ram.getText().toString().trim(), "Missing",
                        "Missing", "Missing", "Missing", hdd.getText().toString().trim());
                db.updateScannedStatus(1, comp_id);
                Log.w("DETAILS INSERTED: ", "Insert STATUS: " + in);
//            Log.w("Added assessed pc", "Count: " + db.assessPcCount());
            } else {
                String mon, motherboard, processor, comp_ram, comp_hdd, comp_kb, comp_mouse, comp_vga;
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
                long in = db.addAssessedPc(comp_id,pc_no, model, motherboard,mb.getText().toString().trim()
                        , processor, mon,monitor.getText().toString().trim(), comp_ram, comp_kb, comp_mouse, status
                        , comp_vga, comp_hdd);
                db.updateScannedStatus(1, comp_id);
                Log.w("DETAILS INSERTED: ", "Insert STATUS: " + in);
                Log.w("DETAILS Count: ", "Count: " + db.assessPcCount());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
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
