package com.example.avendano.cp_scan.Pages;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.avendano.cp_scan.Activities.AssessmentActivity;
import com.example.avendano.cp_scan.Activities.PcAssessment;
import com.example.avendano.cp_scan.Adapter.AssessAdapter;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.SQLiteHelper;
import com.example.avendano.cp_scan.Model.Assess_Computers;
import com.example.avendano.cp_scan.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Avendano on 10 Apr 2018.
 */

public class InventoryActivty extends AppCompatActivity{

    SQLiteHelper dbHelper;
    Toolbar toolbar;
    Button scan;
    EditText serial_edttxt, remark;
    RecyclerView recyclerView;
    List<Assess_Computers> pcList;
    SQLiteHandler db;
    AssessAdapter adapter;
    int room_id, request_inventory;
    android.app.AlertDialog dialog;
    String room_name;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);
        dbHelper = new SQLiteHelper(this);
        db = new SQLiteHandler(this);
        room_id = getIntent().getIntExtra("room_id", 0);
        request_inventory = getIntent().getIntExtra("request", 0);

        scan = (Button) findViewById(R.id.scan);
        serial_edttxt = (EditText) findViewById(R.id.serial_number);
        recyclerView = (RecyclerView) findViewById(R.id.scan_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = new SQLiteHandler(this);
        pcList = new ArrayList<>();
        remark = (EditText) findViewById(R.id.remark);

//        room_name = getRoomName();
//        getSupportActionBar().setTitle(room_name);

        serial_edttxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (serial_edttxt.getRight() - serial_edttxt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        String uniq = serial_edttxt.getText().toString().trim();
                        if (uniq.length() > 0) {
                            if (checkSerial(uniq)) {
                                if (!checkIfScanned(uniq)) {
                                    int comp_id = getCompId(uniq);
                                    if (comp_id != 0) {
                                        //intent to assessment comp id model room id serial
                                        String model = getModel(uniq);
                                        Intent intent = new Intent(getApplicationContext(), PcAssessment.class);
                                        intent.putExtra("room_id", room_id);
                                        intent.putExtra("comp_id", comp_id);
                                        intent.putExtra("model", model);
                                        intent.putExtra("manual", false);
                                        intent.putExtra("request", request_inventory);
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(InventoryActivty.this, "PC already assessed", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                searchSerial(uniq);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Empty Field", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        //scanned
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(InventoryActivty.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Place QR code to scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        //inflate pc
        loadPc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Scanning cancelled", Toast.LENGTH_SHORT).show();
            } else {
                //split # offline or online
                String content = result.getContents();
                String[] parts = content.split("#");
                String serial = parts[0];
                String conn = parts[1];
                if (checkSerial(serial)) {
                    if (!checkIfScanned(serial)) {
                        int comp_id = getCompId(serial);
                        if (comp_id != 0) {
                            String model = getModel(serial);
                            Intent intent = new Intent(getApplicationContext(), PcAssessment.class);
                            intent.putExtra("room_id", room_id);
                            intent.putExtra("comp_id", comp_id);
                            intent.putExtra("model", model);
                            intent.putExtra("request", request_inventory);
                            if (conn.equalsIgnoreCase("online"))
                                intent.putExtra("manual", true);
                            else
                                intent.putExtra("manual", false);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(InventoryActivty.this, "PC already assessed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    searchSerial(serial);
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean checkIfScanned(String serial) {
        Cursor c = dbHelper.getPcToAssessAsc();
        if (c.moveToFirst()) {
            do {
                String monitor = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(db.COMP_MB));
                int scanned = c.getInt(c.getColumnIndex(db.COLUMN_SCANNED));

                if (serial.equals(monitor) || serial.equals(mb)) {
                    if (scanned == 1)
                        return true;
                    else
                        return false;
                }
            } while (c.moveToNext());
        }
        return false;
    }

    private boolean checkSerial(String serial) {
        Cursor c = dbHelper.getPcToAssessAsc();
        if (c.moveToFirst()) {
            do {
                String monitor = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(db.COMP_MB));
                Log.w("MONITOR: ", "Serial: " + monitor);
                Log.w("MB: ", "Serial: " + mb);
                if (serial.equals(monitor) || serial.equals(mb)) {
                    return true;
                }
            } while (c.moveToNext());
        }
        return false;
    }
    private int getCompId(String serial) {
        Cursor c = dbHelper.getPcToAssessAsc();
        if (c.moveToFirst()) {
            do {
                String monitor = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(db.COMP_MB));
                int comp_id = c.getInt(c.getColumnIndex(db.COMP_ID));

                if (serial.equals(monitor) || serial.equals(mb)) {
                    return comp_id;
                }
            } while (c.moveToNext());
        }
        return 0;
    }

    private String getModel(String serial) {

        Cursor c = dbHelper.getPcToAssessAsc();
        if (c.moveToFirst()) {
            do {
                String monitor = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(db.COMP_MB));
                String model = c.getString(c.getColumnIndex(db.COMP_MODEL));

                if (serial.equals(monitor) || serial.equals(mb)) {
                    return model;
                }
            } while (c.moveToNext());
        }
        return "";
    }
    private void loadPc() {
        pcList.clear();
        Cursor cursor = dbHelper.getPcToAssessAsc();
        if (cursor.moveToFirst()) {
            do {
                Assess_Computers scan = new Assess_Computers(
                        cursor.getString(cursor.getColumnIndex(db.COMP_MODEL)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_MB)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_PR)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_MONITOR)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_RAM)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_KBOARD)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_MOUSE)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_VGA)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_HDD)),
                        cursor.getString(cursor.getColumnIndex(db.COMP_STATUS)),
                        cursor.getInt(cursor.getColumnIndex(db.COMP_ID)),
                        cursor.getInt(cursor.getColumnIndex(db.COMP_NAME)),
                        cursor.getInt(cursor.getColumnIndex(db.COLUMN_SCANNED))
                );

                pcList.add(scan);
            } while (cursor.moveToNext());
        }
        adapter = new AssessAdapter(this, pcList);
        recyclerView.setAdapter(adapter);
    }
    private void searchSerial(String serial) {
        class SearchComp extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... strings) {
                String serial = strings[0];
                String msg = "";
                Cursor c = db.getCompIdAndModel(serial);
                if (c.moveToFirst()) {
                    int comp_id = c.getInt(c.getColumnIndex(db.COMP_ID));
                    Cursor c1 = db.getCompDetails(comp_id);
                    if (c1.moveToFirst()) {
                        String room_name = "";
                        int pc_no = c1.getInt(c1.getColumnIndex(db.COMP_NAME));
                        int room_id = c1.getInt(c1.getColumnIndex(db.ROOMS_ID));
                        Cursor c2 = db.getRoomDetails(room_id);
                        if (c2.moveToFirst()) {
                            room_name = c2.getString(c2.getColumnIndex(db.ROOMS_NAME));
                        }
                        msg = "This is PC " + pc_no + " of " + room_name + " room";
                    }
                } else {
                    msg = "Computer not found in database";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivty.this);
                builder.setMessage(s);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send sms to custodians
                        Toast.makeText(InventoryActivty.this, "Alert sent to custodians", Toast.LENGTH_SHORT).show();
                        serial_edttxt.setText("");
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        new SearchComp().execute(serial);
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
                //compare scanned to total pc  to assess
                break;
            case R.id.cancel:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("");
                builder.setMessage("Assessed computers will be discarded once you exit. Continue?")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearDb();
                                InventoryActivty.this.finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false);

                AlertDialog alert = builder.create();
                alert.show();

                break;
        }

        return true;

    }

    private void clearDb() {
        //delete assessed pc and pc to assess
        dbHelper.deleteAssessedPc();
        dbHelper.deletePcToAssess();
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearDb();
        db.close();
        dbHelper.close();
    }
}
