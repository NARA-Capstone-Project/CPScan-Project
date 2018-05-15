package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.Database.SQLiteHelper;
import com.example.avendano.cp_scan.R;

import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 11 Apr 2018.
 */

public class AssessPc extends AppCompatActivity {

    int pc_no = 0, scan = 0;
    int comp_id, room_id, req_id;
    Toolbar toolbar;
    RadioGroup mon_stat, mb_stat, pr_stat, ram_stat, hdd_stat, kb_stat, vga_stat, mouse_stat;
    TextView mon, mb, pr, ram, hdd, kb, vga, mouse, pc_serial, pc_model;
    String WORKING = "OK", NOT_WORKING = "None/Not Working", MISSING = "Missing";
    String COMP_STATUS_NOT_WORKING = "Defective", comp_status;
    String mon_status, mb_status, pr_status, ram_status, hdd_status, kb_status, vga_status, mouse_status;
    SQLiteHelper db;
    AlertDialog progress;

    ArrayList<String> details;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_assessment);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        details = new ArrayList<>();
        progress = new SpotsDialog(this, "Loading...");
        progress.setCancelable(false);
        progress.show();
        comp_id = getIntent().getIntExtra("comp_id", 0);
        room_id = getIntent().getIntExtra("room_id", 0);
        req_id = getIntent().getIntExtra("req_id", 0);
        scan = getIntent().getIntExtra("scanned", 0);
        details = getIntent().getStringArrayListExtra("details");

        //textviews
        mon = (TextView) findViewById(R.id.monitor);
        mb = (TextView) findViewById(R.id.mb);
        pr = (TextView) findViewById(R.id.pr);
        ram = (TextView) findViewById(R.id.ram);
        hdd = (TextView) findViewById(R.id.hdd);
        kb = (TextView) findViewById(R.id.kboard);
        vga = (TextView) findViewById(R.id.vga);
        mouse = (TextView) findViewById(R.id.mouse);
        pc_serial = (TextView) findViewById(R.id.comp_serial);
        pc_model = (TextView) findViewById(R.id.comp_model);

        //radiogroup
        mon_stat = (RadioGroup) findViewById(R.id.btn_mon);
        mb_stat = (RadioGroup) findViewById(R.id.btn_mb);
        pr_stat = (RadioGroup) findViewById(R.id.btn_pr);
        ram_stat = (RadioGroup) findViewById(R.id.btn_ram);
        hdd_stat = (RadioGroup) findViewById(R.id.btn_hdd);
        kb_stat = (RadioGroup) findViewById(R.id.btn_kb);
        vga_stat = (RadioGroup) findViewById(R.id.btn_vga);
        mouse_stat = (RadioGroup) findViewById(R.id.btn_mouse);

        mon_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    mon_status = WORKING;
                else if (stat.equalsIgnoreCase("not working"))
                    mon_status = NOT_WORKING;
                else
                    mon_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        mb_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    mb_status = WORKING;
                else if (stat.equalsIgnoreCase("not working"))
                    mb_status = NOT_WORKING;
                else
                    mb_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        pr_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    pr_status = pr.getText().toString().trim();
                else if (stat.equalsIgnoreCase("not working"))
                    pr_status = NOT_WORKING;
                else
                    pr_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        ram_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    ram_status = ram.getText().toString().trim();
                else if (stat.equalsIgnoreCase("not working"))
                    ram_status = NOT_WORKING;
                else
                    ram_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        hdd_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    hdd_status = hdd.getText().toString().trim();
                else if (stat.equalsIgnoreCase("not working"))
                    hdd_status = NOT_WORKING;
                else
                    hdd_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        kb_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    kb_status = WORKING;
                else if (stat.equalsIgnoreCase("not working"))
                    kb_status = NOT_WORKING;
                else
                    kb_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        vga_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    vga_status = "Built-In";
                else if (stat.equalsIgnoreCase("not working"))
                    vga_status = NOT_WORKING;
                else
                    vga_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        mouse_stat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                String stat = btn.getTag().toString();
                if (stat.equalsIgnoreCase("working"))
                    mouse_status = WORKING;
                else if (stat.equalsIgnoreCase("not working"))
                    mouse_status = NOT_WORKING;
                else
                    mouse_status = MISSING;
                Log.e("CHECKID", " " + stat);
            }
        });
        db = new SQLiteHelper(this);
        loadPcDetails();
    }

    private void loadPcDetails() {
        class loadDetails extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                Cursor c = db.getPcToAssess(comp_id);
                if (c.moveToFirst()) {
                    pc_model.setText(c.getString(c.getColumnIndex(db.COMP_MODEL)));
                    pc_serial.setText(c.getString(c.getColumnIndex(db.COMP_SERIAL)));
                    mon.setText(c.getString(c.getColumnIndex(db.COMP_MONITOR)));
                    mb.setText(c.getString(c.getColumnIndex(db.COMP_MB)));
                    pr.setText(c.getString(c.getColumnIndex(db.COMP_PR)));
                    ram.setText(c.getString(c.getColumnIndex(db.COMP_RAM)));
                    hdd.setText(c.getString(c.getColumnIndex(db.COMP_HDD)));
                    kb.setText(c.getString(c.getColumnIndex(db.COMP_KBOARD)));
                    vga.setText(c.getString(c.getColumnIndex(db.COMP_VGA)));
                    mouse.setText(c.getString(c.getColumnIndex(db.COMP_MOUSE)));
                    pc_no = c.getInt(c.getColumnIndex(db.COMP_NAME));
                } else {
                    goToInventoryAct();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progress.dismiss();
                if (scan == 1) {
                    //check ung mga ok
                    //mb_serial, hdd, mouse ram kboard
                    Log.e("SCANNED", "YES");
                    Log.e("CHILD COUNT", "" + mb_stat.getChildCount());
                    if (!mb.getText().toString().trim().equalsIgnoreCase(details.get(0).toString()) ||
                            hdd.getText().toString().trim().equalsIgnoreCase(details.get(1).toString()) ||
                            ram.getText().toString().trim().equalsIgnoreCase(details.get(3).toString())) {
                        String msg = "";
                        if (!mb.getText().toString().trim().equalsIgnoreCase(details.get(0).toString()))
                            msg = msg + "\nMotherboard: " + "\n\tValue: " + mb.getText().toString() + "\n\tGot: " + details.get(0).toString();

                        if (!ram.getText().toString().trim().equalsIgnoreCase(details.get(3).toString()))
                            msg = msg + "\nRAM: " + "\n\tValue: " + ram.getText().toString() + "\n\tGot: " + details.get(3).toString();

                        if (!hdd.getText().toString().trim().equalsIgnoreCase(details.get(1).toString()))
                            msg = msg + "\nHDD: " + "\n\tValue: " + hdd.getText().toString() + "\n\tGot: " + details.get(1).toString();

                        showAlertDialogMismatch(msg);
                    }


                    if (mb.getText().toString().trim().equals(details.get(0).toString()))
                        mb_stat.check(mb_stat.getChildAt(0).getId());
                    else
                        mb_stat.check(mb_stat.getChildAt(2).getId());

                    if (hdd.getText().toString().trim().equals(details.get(1).toString()))
                        hdd_stat.check(hdd_stat.getChildAt(0).getId());
                    else
                        hdd_stat.check(hdd_stat.getChildAt(2).getId());

                    if (details.get(2).toString().equalsIgnoreCase("ok"))
                        mouse_stat.check(mouse_stat.getChildAt(0).getId());
                    else if (kb.getText().toString().trim().equalsIgnoreCase("ok") &&
                            details.get(2).equalsIgnoreCase("Missing"))//none
                        mouse_stat.check(mouse_stat.getChildAt(2).getId());
                    else
                        mouse_stat.check(mouse_stat.getChildAt(1).getId());

                    if (ram.getText().toString().trim().equals(details.get(3).toString()))
                        ram_stat.check(ram_stat.getChildAt(0).getId());
                    else
                        ram_stat.check(ram_stat.getChildAt(2).getId());


                    if (details.get(4).toString().equalsIgnoreCase("ok"))
                        kb_stat.check(kb_stat.getChildAt(0).getId());
                    else if (kb.getText().toString().trim().equalsIgnoreCase("ok") &&
                            details.get(4).equalsIgnoreCase("Missing"))//none
                        kb_stat.check(kb_stat.getChildAt(2).getId());
                    else
                        kb_stat.check(kb_stat.getChildAt(1).getId());

                }
                getSupportActionBar().setTitle("PC " + pc_no);
            }
        }

        new loadDetails().execute();
    }

    private void showAlertDialogMismatch(String msg) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AssessPc.this);
        builder.setTitle("Mismatch: ")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assess_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                //update snyc
                progress.show();
                new AddAssessedPc().execute();
                break;
            case R.id.cancel:
                goToInventoryAct();
                break;
        }

        return true;
    }


    class AddAssessedPc extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (checkInput())
                return true;
            else
                return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean)
                addAssessedPc();
            else
                progress.dismiss();
        }

        private void addAssessedPc() {
            progress.dismiss();
            if (mb_status.equalsIgnoreCase(NOT_WORKING) || mb_status.equalsIgnoreCase(MISSING) || pr_status.equalsIgnoreCase(NOT_WORKING) || pr_status.equalsIgnoreCase(MISSING) ||
                    ram_status.equalsIgnoreCase(NOT_WORKING) || ram_status.equalsIgnoreCase(MISSING)
                    || hdd_status.equalsIgnoreCase(NOT_WORKING) || hdd_status.equalsIgnoreCase(MISSING)) {
                comp_status = COMP_STATUS_NOT_WORKING;
            } else if (mon_status.equalsIgnoreCase(MISSING) && mb_status.equalsIgnoreCase(MISSING) && pr_status.equalsIgnoreCase(MISSING) && ram_status.equalsIgnoreCase(MISSING)
                    && hdd_status.equalsIgnoreCase(MISSING) && kb_status.equalsIgnoreCase(MISSING) && vga_status.equalsIgnoreCase(MISSING) && mouse_status.equalsIgnoreCase(MISSING)) {
                comp_status = MISSING;
            } else {
                comp_status = "Working";
            }
            long insert = db.addAssessedPc(comp_id, pc_no, pc_model.getText().toString().trim(),pc_serial.getText().toString().trim()
                    , mb_status, mb.getText().toString(),
                    pr_status, mon_status, mon.getText().toString(), ram_status, kb_status, mouse_status,
                    comp_status, vga_status, hdd_status
            );
            if (insert < 0) {
                Toast.makeText(AssessPc.this, "Error occured", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("INSERT", " " + insert);
                db.updateScannedStatus(1, comp_id);
                goToInventoryAct();
            }
        }

        private boolean checkInput() {

            if (mon_status.isEmpty() && mb_status.isEmpty() && pr_status.isEmpty() && ram_status.isEmpty()
                    && hdd_status.isEmpty() && kb_status.isEmpty() && vga_status.isEmpty() && mouse_status.isEmpty()) {
                Toast.makeText(AssessPc.this, "You haven't checked anything", Toast.LENGTH_SHORT).show();
                return false;
            } else if (mon_status.isEmpty() || mb_status.isEmpty() || pr_status.isEmpty() || ram_status.isEmpty()
                    || hdd_status.isEmpty() || kb_status.isEmpty() || vga_status.isEmpty() || mouse_status.isEmpty()) {
                Toast.makeText(AssessPc.this, "Some parts of the computer was not checked", Toast.LENGTH_SHORT).show();
                return false;
            } else
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToInventoryAct();
    }

    private void goToInventoryAct() {
        Intent intent = new Intent(getApplicationContext(), InventoryActivty.class);
        intent.putExtra("room_id", room_id);
        intent.putExtra("req_id", req_id);
        startActivity(intent);
        finish();
    }
}
