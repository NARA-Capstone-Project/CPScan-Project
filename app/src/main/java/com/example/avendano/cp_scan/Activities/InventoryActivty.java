package com.example.avendano.cp_scan.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.AssessAdapter;
import com.example.avendano.cp_scan.Adapter.InventoryAdapter;
import com.example.avendano.cp_scan.BackgroundServices.GetNewRepairRequest;
import com.example.avendano.cp_scan.BackgroundServices.QRCodeScan;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Database.SQLiteHelper;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Assess_Computers;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 10 Apr 2018.
 */

public class InventoryActivty extends AppCompatActivity {


    Intent receiverIntent;
    BroadcastReceiver qrScanReceiver;
    SQLiteHelper dbHelper;
    Toolbar toolbar;
    EditText serial_edttxt, remark;
    RecyclerView recyclerView;
    List<Assess_Computers> pcList;
    AssessAdapter adapter;
    int room_id, req_id;
    android.app.AlertDialog dialog;
    int SYNC = 1;
    int NOT_SYNC = 0;
    String room_name, custodian, phone, cust_id;
    VolleyRequestSingleton volley;
    android.app.AlertDialog progress;
    android.app.AlertDialog prog;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        long pc_to_assess = dbHelper.pcToAssessCount();
        long assessed_pc = dbHelper.assessedPcCount();
        if (assessed_pc != 0) {
            if (pc_to_assess != assessed_pc) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(room_name);
                builder.setMessage("Some computer has not been assessed, continue to exit?")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
            } else {
                //if naassess na lahat pero mageexit
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
            }
        } else {
            clearDb();
            InventoryActivty.this.finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);


        volley = new VolleyRequestSingleton(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getRoomName();

        dbHelper = new SQLiteHelper(this);
        room_id = getIntent().getIntExtra("room_id", 0);
        req_id = getIntent().getIntExtra("req_id", 0);

        serial_edttxt = (EditText) findViewById(R.id.serial_number);
        recyclerView = (RecyclerView) findViewById(R.id.scan_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pcList = new ArrayList<>();
        remark = (EditText) findViewById(R.id.remark);

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
                                        Intent intent = new Intent(InventoryActivty.this, AssessPc.class);
                                        intent.putExtra("comp_id", comp_id);
                                        intent.putExtra("room_id", room_id);
                                        intent.putExtra("req_id", req_id);
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

        //inflate pc
        loadPc();
        progress = new SpotsDialog(this, "Saving...");
        progress.setCancelable(false);

        prog = new SpotsDialog(this, "Loading...");
        prog.setCancelable(false);

    }

    private void getRoomName() {
        volley.sendStringRequestGet(AppConfig.GET_ROOMS, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                //deptid dept_name room_name
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String dept_name = obj.getString("dept_name");
                        String r_name = obj.getString("room_name");
                        custodian = obj.getString("cust_name");
                        phone = obj.getString("cust_phone");
                        cust_id = obj.getString("cust_id");
                        int r_id = obj.getInt("room_id");
                        if (r_id == room_id) {
                            if (obj.isNull("dept_id")) {
                                room_name = dept_name + " " + r_name;
                                getSupportActionBar().setTitle(r_name);
                            } else {
                                room_name = dept_name + " " + r_name;
                                getSupportActionBar().setTitle(dept_name + " " + r_name);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.getMessage());
            }
        });
    }

    private void sendDetailsToAssess(String serial, Intent intent) {
        int comp_id = getCompId(serial);
        if (comp_id != 0) {
            //unregister receiver
            ArrayList<String> details = new ArrayList<>();
            details.add(intent.getStringExtra("mb_serial"));
            details.add(intent.getStringExtra("hdd"));
            details.add(intent.getStringExtra("mouse"));
            details.add(intent.getStringExtra("ram"));
            details.add(intent.getStringExtra("kboard"));
            for (int i = 0; i < details.size(); i++) {
                Log.e("DETAILSINV", details.get(i));
            }
            unregisterReceiver(qrScanReceiver);
            stopService(receiverIntent);
            deleteDataFromTemp(comp_id,details);
        }
    }

    private void deleteDataFromTemp(final int comp_id, final ArrayList<String> details) {
        class DeleteFunction extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                String query = "DELETE FROM temporary WHERE pc_serial = '" +details.get(0) +"'";
                Map<String, String> param = new HashMap<>();
                param.put("query", query);
                String response = con.sendPostRequest(AppConfig.UPDATE_QUERY, param);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                prog.dismiss();
                try {
                    JSONObject obj = new JSONObject(s);
                    if (!obj.getBoolean("error")) {
                        Intent i = new Intent(InventoryActivty.this, AssessPc.class);
                        i.putExtra("comp_id", comp_id);
                        i.putExtra("room_id", room_id);
                        i.putExtra("req_id", req_id);
                        i.putExtra("scanned", 1);
                        i.putExtra("details", details);
                        startActivity(i);
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(InventoryActivty.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        }
        new DeleteFunction().execute();
    }

    private void sendSerialToTrigger(final String serial) {
        class sendSerial extends AsyncTask<Void, Void, String> {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                prog.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                Map<String, String> param = new HashMap<>();
                param.put("serial", serial);

                String response = con.sendPostRequest(AppConfig.SEND_SERIAL, param);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONSe", "SCAN: " + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    if (!obj.getBoolean("error")) {
                        //register receiver
                        startService(receiverIntent);
                        registerReceiver(qrScanReceiver, new IntentFilter(QRCodeScan.BROADCAST_ACTION));
                    } else {
                        progress.dismiss();
                        Toast.makeText(InventoryActivty.this, "Error occurred while getting computer info using QR", Toast.LENGTH_SHORT).show();
                        int comp_id = getCompId(serial);
                        if (comp_id != 0) {
                            Intent intent = new Intent(InventoryActivty.this, AssessPc.class);
                            intent.putExtra("comp_id", comp_id);
                            intent.putExtra("room_id", room_id);
                            intent.putExtra("req_id", req_id);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (Exception e) {
                    progress.dismiss();
                    e.printStackTrace();
                    Toast.makeText(InventoryActivty.this, "An error occured", Toast.LENGTH_SHORT).show();
                }
            }
        }
        new sendSerial().execute();
    }

    private boolean checkIfScanned(String serial) {
        Cursor c = dbHelper.getPcToAssessAsc();
        if (c.moveToFirst()) {
            do {
                String monitor = c.getString(c.getColumnIndex(dbHelper.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(dbHelper.COMP_MB));
                int scanned = c.getInt(c.getColumnIndex(dbHelper.COLUMN_SCANNED));
                String comp_serial = c.getString(c.getColumnIndex(dbHelper.COMP_SERIAL));

                if (serial.equals(monitor) || serial.equals(mb) || serial.equals(comp_serial)) {
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
                String monitor = c.getString(c.getColumnIndex(dbHelper.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(dbHelper.COMP_MB));
                String comp_serial = c.getString(c.getColumnIndex(dbHelper.COMP_SERIAL));
                Log.w("MONITOR: ", "Serial: " + monitor);
                Log.w("MB: ", "Serial: " + mb);
                if (serial.equals(monitor) || serial.equals(mb) || serial.equals(comp_serial)) {
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
                String monitor = c.getString(c.getColumnIndex(dbHelper.COMP_MONITOR));
                String mb = c.getString(c.getColumnIndex(dbHelper.COMP_MB));
                String comp_serial = c.getString(c.getColumnIndex(dbHelper.COMP_SERIAL));
                int comp_id = c.getInt(c.getColumnIndex(dbHelper.COMP_ID));

                if (serial.equals(monitor) || serial.equals(mb) || serial.equals(comp_serial)) {
                    return comp_id;
                }
            } while (c.moveToNext());
        }
        return 0;
    }

    private void loadPc() {
        pcList.clear();
        Cursor cursor = dbHelper.getPcToAssessAsc();
        if (cursor.moveToFirst()) {
            do {
                Assess_Computers scan = new Assess_Computers(
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_MODEL)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_MB)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_PR)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_MONITOR)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_RAM)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_KBOARD)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_MOUSE)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_VGA)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_HDD)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_STATUS)),
                        cursor.getInt(cursor.getColumnIndex(dbHelper.COMP_ID)),
                        cursor.getInt(cursor.getColumnIndex(dbHelper.COMP_NAME)),
                        cursor.getInt(cursor.getColumnIndex(dbHelper.COLUMN_SCANNED)),
                        cursor.getString(cursor.getColumnIndex(dbHelper.COMP_SERIAL))
                );

                pcList.add(scan);
            } while (cursor.moveToNext());
        }
        adapter = new AssessAdapter(this, pcList);
        recyclerView.setAdapter(adapter);
        Log.e("COUNT", " " + dbHelper.pcToAssessCount() + " " + dbHelper.assessedPcCount());
    }

    private void searchSerial(String serial) {  //kapag wala sa list
        class SearchComp extends AsyncTask<String, Void, Void> {
            @Override
            protected Void doInBackground(String... strings) {
                String serial = strings[0];
                searchComputer(serial);
                return null;
            }

            private void searchComputer(final String serial) {
                volley.sendStringRequestGet(AppConfig.GET_COMPUTERS, new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            int len = array.length();
                            int x = 0;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String monitor = obj.getString("monitor");
                                String mb = obj.getString("mb");
                                String comp_serial = obj.getString("comp_serial");

                                if (serial.equals(monitor) || serial.equals(mb) || serial.equals(comp_serial)) {
                                    if (obj.isNull("room_id")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivty.this);
                                        builder.setMessage("This computer is not assigned to any room");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                serial_edttxt.setText("");
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();

                                    } else {
                                        int id = obj.getInt("room_id"); //room id nung pc n naligaw para makuha ung user id then kukunin ung phone
                                        String room_name = obj.getString("room_name");
                                        int pc_no = obj.getInt("pc_no");
                                        showAlert("This is PC " + pc_no + " of " + room_name + " room", true, id, room_name, pc_no);
                                    }
                                    break;
                                } else {
                                    x++;
                                }
                            }
                            if (x == len)
                                showAlert("Computer not found in database", false, 0, "", 0);
                        } catch (Exception e) {
                            Log.e("ERROR", e.getMessage());
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", error.getMessage());
                    }
                });
            }

            private void showAlert(String msg, final boolean sendSMS, final int pc_room_id, final String pc_room_name, final int pc_no) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivty.this);
                builder.setMessage(msg);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //send sms to custodians
                        if (sendSMS) {
                            sendSms(pc_room_id, pc_room_name, pc_no);
                        }
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

    private void sendSms(int pc_room_id, String pc_room_name, int pc_no) {
        Map<String, String> params = new HashMap<>();
        params.put("room_cust", SharedPrefManager.getInstance(this).getUserId());
        params.put("room_cust_phone", SharedPrefManager.getInstance(this).getUserPhone());
        params.put("room_name", room_name);
        params.put("pc_room_name", pc_room_name);
        params.put("pc_room_id", String.valueOf(pc_room_id));
        params.put("pc_no", String.valueOf(pc_no));

        volley.sendStringRequestPost(AppConfig.SEND_SMS,
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getString("sms").equalsIgnoreCase("not sent!")) {
                                Log.e("SMS", "NOT SENT!");
                            } else {
                                Log.e("SMS", obj.getString("sms"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }
                , params);
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
                long pc_to_assess = dbHelper.pcToAssessCount();
                long assessed_pc = dbHelper.assessedPcCount();
                if (pc_to_assess != assessed_pc) {
                    Toast.makeText(getApplicationContext(), "Some PC has not been assessed", Toast.LENGTH_SHORT).show();
                } else {
                    progress.show();
                    saveReport();
                }
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

    private void saveReport() {
        //date time room_id cust_id tech_id remarks jsonarray

        class saveReport extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                saveInventoryReport();
                return null;
            }

            private void saveInventoryReport() {

                JSONArray details = convertDetailsToArray();

                String rem = remark.getText().toString().trim();
                Map<String, String> params = new HashMap<>();
                params.put("room_id", String.valueOf(room_id));
                params.put("req_id", String.valueOf(req_id));
                params.put("details", details.toString());
                params.put("remarks", rem);

                Log.e("JSONARRAY", details.toString());

                volley.sendStringRequestPost(AppConfig.SAVE_INVENTORY, new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                progress.dismiss();
                                Toast.makeText(InventoryActivty.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                InventoryActivty.this.finish();
                            } else {
                                progress.dismiss();
                                Log.e("DELETE STATUS", obj.getString("del"));
                                Toast.makeText(InventoryActivty.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("RESPONSE", e.getMessage() + response.toString());
                            e.printStackTrace();
                            progress.dismiss();
                            Toast.makeText(InventoryActivty.this, "Something went wrong in saving report", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e("RESPONSE", error.getMessage());
                        Log.e("RESPOnSE", error.getClass().toString());
                        progress.dismiss();
                        Toast.makeText(InventoryActivty.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }, params);

            }

            private JSONArray convertDetailsToArray() {
                Cursor c = dbHelper.getAssessedPc();
                JSONArray array = new JSONArray();
                if (c.moveToFirst()) {
                    Log.w("Get Assessed PC", "Ok");
                    do {
                        JSONObject obj = new JSONObject();
                        int comp_id = c.getInt(c.getColumnIndex(dbHelper.COMP_ID));
                        int pc_no = c.getInt(c.getColumnIndex(dbHelper.COMP_NAME));
                        String comp_serial = c.getString(c.getColumnIndex(dbHelper.COMP_SERIAL));
                        String model = c.getString(c.getColumnIndex(dbHelper.COMP_MODEL));
                        String mb = c.getString(c.getColumnIndex(dbHelper.COMP_MB));
                        String mb_serial = c.getString(c.getColumnIndex(dbHelper.REPORT_MB_SERIAL));
                        String monitor = c.getString(c.getColumnIndex(dbHelper.COMP_MONITOR));
                        String mon_serial = c.getString(c.getColumnIndex(dbHelper.REPORT_MON_SERIAL));
                        String pr = c.getString(c.getColumnIndex(dbHelper.COMP_PR));
                        String kb = c.getString(c.getColumnIndex(dbHelper.COMP_KBOARD));
                        String mouse = c.getString(c.getColumnIndex(dbHelper.COMP_MOUSE));
                        String ram = c.getString(c.getColumnIndex(dbHelper.COMP_RAM));
                        String hdd = c.getString(c.getColumnIndex(dbHelper.COMP_HDD));
                        String vga = c.getString(c.getColumnIndex(dbHelper.COMP_VGA));
                        String comp_status = c.getString(c.getColumnIndex(dbHelper.COMP_STATUS));

                        try {
                            obj.put("comp_id", comp_id);
                            obj.put("pc_no", pc_no);
                            obj.put("comp_serial", comp_serial);
                            obj.put("model", model);
                            obj.put("mb", mb);
                            obj.put("mb_serial", mb_serial);
                            obj.put("monitor", monitor);
                            obj.put("mon_serial", mon_serial);
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
                        array.put(obj);
                    } while (c.moveToNext());
                }
                return array;
            }
        }

        new saveReport().execute();
    }

    private void clearDb() {
        //delete assessed pc and pc to assess
        dbHelper.deleteAssessedPc();
        dbHelper.deletePcToAssess();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
    }

}
