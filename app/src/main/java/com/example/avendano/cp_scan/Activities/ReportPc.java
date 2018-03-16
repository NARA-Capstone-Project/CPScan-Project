package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

/**
 * Created by Avendano on 14 Mar 2018.
 */

public class ReportPc extends AppCompatActivity {

    Button report;
    private Toolbar toolbar;
    private int room_id;
    Boolean manual;
    RadioGroup rGroup;
    CheckBox monitor, mb, pr, ram, hdd, keyboard, mouse, vga;
    Button save;
    SQLiteHandler db;
    int comp_id, pc_no;
    TextView instr;
    AlertDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcassessment);

        report = (Button) findViewById(R.id.save);
        report.setText("Report");

        db = new SQLiteHandler(this);

        dialog = new SpotsDialog(ReportPc.this, "Loading...");
        dialog.show();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        save = (Button) findViewById(R.id.save);
        monitor = findViewById(R.id.pc_monitor);
        mb = findViewById(R.id.pc_mb);
        pr = findViewById(R.id.pc_processor);
        ram = findViewById(R.id.pc_ram);
        hdd = findViewById(R.id.pc_hdd);
        instr = findViewById(R.id.instr);

        keyboard = findViewById(R.id.kboard_check);
        mouse = findViewById(R.id.mouse_check);
        vga = findViewById(R.id.vga_check);
        rGroup = findViewById(R.id.group);
        rGroup.check(R.id.defective);
        rGroup.findViewById(R.id.working).setVisibility(View.INVISIBLE);
        instr.setText("Check the defective peripherals");
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = rGroup.findViewById(checkedId);
                String status = btn.getText().toString().trim();
                if (status.equalsIgnoreCase("defective")) {
                    instr.setText("Check the defective peripherals");
                } else {
                    instr.setText("Check the missing peripherals");
                }
            }
        });

        comp_id = getIntent().getIntExtra("comp_id", 0);

        new GetCompDetails().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ReportPc.this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class GetCompDetails extends AsyncTask<Void, Void, Void> {
        String mon_serial, mb_serial, pr_type, ram_size, hdd_size;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mon_serial = "";
            mb_serial = "";
            pr_type = "";
            ram_size = "";
            hdd_size = "";
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor c = db.getCompDetails(comp_id);
            if (c.moveToFirst()) {
                this.mon_serial = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                this.mb_serial = c.getString(c.getColumnIndex(db.COMP_MB));
                this.pr_type = c.getString(c.getColumnIndex(db.COMP_PR));
                this.ram_size = c.getString(c.getColumnIndex(db.COMP_RAM));
                this.hdd_size = c.getString(c.getColumnIndex(db.COMP_HDD));
                pc_no = c.getInt(c.getColumnIndex(db.COMP_NAME));
                room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
            }
            toolbar.setTitle("PC " + pc_no);
            monitor.setText(mon_serial);
            mb.setText(mb_serial);
            pr.setText(pr_type);
            ram.setText(ram_size);
            hdd.setText(hdd_size);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}
