package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.AssessAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Getter_Setter.Assess_Computers;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentActivity extends AppCompatActivity {

    Toolbar toolbar;
    Button scan;
    EditText serial_edttxt, remark;
    RecyclerView recyclerView;
    List<Assess_Computers> pcList;
    SQLiteHandler db;
    AssessAdapter adapter;
    int room_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        room_id = getIntent().getIntExtra("room_id", 0);
        scan = (Button) findViewById(R.id.scan);
        serial_edttxt = (EditText) findViewById(R.id.serial_number);
        recyclerView = (RecyclerView) findViewById(R.id.scan_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = new SQLiteHandler(this);
        pcList = new ArrayList<>();
        remark = (EditText) findViewById(R.id.remark);

        //serial
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
                                        startActivity(intent);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(AssessmentActivity.this, "PC already assessed", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(AssessmentActivity.this, "Computer-not belong in this room. ", Toast.LENGTH_SHORT).show();
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
    }

    private boolean checkIfScanned(String serial) {
        Cursor c = db.getPcToAssess();
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
        Cursor c = db.getPcToAssess();
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
        Cursor c = db.getPcToAssess();
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

        Cursor c = db.getPcToAssess();
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
        Cursor cursor = db.getPcToAssessAsc();
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
                long unscanned = db.getUnscannedCount();
                long allPc = db.getAssessPcCount();
                if (unscanned < allPc) {
                    Toast.makeText(getApplicationContext(), "Some PC has not been assessed", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Assessed Count", "Count: " + db.assessPcCount());
                    new AddReportToServer().execute();
                    goToViewRoom();
                }
                break;
            case R.id.cancel:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Assessed computers will be discarded once you exit. Continue?")
                        .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goToViewRoom();
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

    @Override
    public void onBackPressed() {
        long unscanned = db.getUnscannedCount();
        long allPc = db.getAssessPcCount();
        if (unscanned < allPc) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Some computer has not been assessed, continue to exit?")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goToViewRoom();
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
            goToViewRoom();
        }

    }

    private void saveReport() {
        final String rem= remark.getText().toString().trim();
        Calendar calendar = Calendar.getInstance();
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        final String time = new SimpleDateFormat("HH:mm:ss").format(calendar.getTime());
        final JSONArray array = details();
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_SAVE_A_REPORT
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Log.w("INSERT REPORT", "SUCCESS");
                        int rep = obj.getInt("rep_id");
                        setrep_id(rep, array);
                    }
                } catch (JSONException e) {
                    Log.e("JSON ERROR", "SAVE REPORT: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("INSERT REPORT", "NOT SUCCESS");
                Toast.makeText(AssessmentActivity.this, "Report not saved", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("tech_id", SharedPrefManager.getInstance(AssessmentActivity.this).getUserId());
                params.put("room_id", String.valueOf(room_id));
                params.put("date", date);
                params.put("time", time);
                params.put("remarks", rem);
                return params;
            }
        };
        RequestQueueHandler.getInstance(this).addToRequestQueue(str);
    }
    private JSONArray details() {
        Cursor c = db.getAssessedPc();
        JSONArray array = new JSONArray();
        if (c.moveToFirst()) {
            Log.w("Get Assessed PC", "Ok");
            do {
                JSONObject obj = new JSONObject();
                int comp_id = c.getInt(c.getColumnIndex(db.COMP_ID));
                int pc_no = c.getInt(c.getColumnIndex(db.COMP_NAME));
                String model = c.getString(c.getColumnIndex(db.COMP_MODEL));
                String mb = c.getString(c.getColumnIndex(db.COMP_MB));
                String mb_serial = c.getString(c.getColumnIndex(db.REPORT_MB_SERIAL));
                String monitor = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String mon_serial = c.getString(c.getColumnIndex(db.REPORT_MON_SERIAL));
                String pr = c.getString(c.getColumnIndex(db.COMP_PR));
                String kb = c.getString(c.getColumnIndex(db.COMP_KBOARD));
                String mouse = c.getString(c.getColumnIndex(db.COMP_MOUSE));
                String ram = c.getString(c.getColumnIndex(db.COMP_RAM));
                String hdd = c.getString(c.getColumnIndex(db.COMP_HDD));
                String vga = c.getString(c.getColumnIndex(db.COMP_VGA));
                String comp_status = c.getString(c.getColumnIndex(db.COMP_STATUS));

                try {
                    obj.put("comp_id", comp_id);
                    obj.put("pc_no", pc_no);
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

    private void setrep_id(int rep, JSONArray array) {
        Log.e("JSONARRAY", "" + array.toString());
        Log.e("REP_ID", "" + rep);
        //for new array
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject newObj = new JSONObject();
                JSONObject oldObj = array.getJSONObject(i);
                newObj.put("comp_id", oldObj.getInt("comp_id"));
                newObj.put("pc_no", oldObj.getInt("pc_no"));
                newObj.put("model", oldObj.getString("model"));
                newObj.put("mb",  oldObj.getString("mb"));
                newObj.put("mb_serial", oldObj.getString("mb_serial"));
                newObj.put("monitor",  oldObj.getString("monitor"));
                newObj.put("mon_serial", oldObj.getString("mon_serial"));
                newObj.put("pr",  oldObj.getString("pr"));
                newObj.put("kb",  oldObj.getString("kb"));
                newObj.put("mouse",  oldObj.getString("mouse"));
                newObj.put("ram",  oldObj.getString("ram"));
                newObj.put("hdd",  oldObj.getString("hdd"));
                newObj.put("vga",  oldObj.getString("vga"));
                newObj.put("comp_status",  oldObj.getString("comp_status"));
                newObj.put("rep_id", String.valueOf(rep));
                //add objects to new jsoon array
                newArray.put(newObj);
            } catch (JSONException e) {
                Log.e("JSONEXCEP", "" + e.getMessage());
            }
        }
        Log.e("NEW JSONARRAY", "" + newArray.toString());
        saveReportDetails(rep, newArray);
    }

    public class AddReportToServer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            saveReport();
            return null;
        }
    }

    //savereportdetails
    private void saveReportDetails(final int id, JSONArray array) {
        //request
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.POST
                , AppConfig.URL_SAVE_A_DETAILS
                , array
                , new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    JSONObject obj = response.getJSONObject(0);
                    if(!obj.getBoolean("error"))
                        Toast.makeText(AssessmentActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e("error", " "+ e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w("save details", "NOT SUCCESS: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(this).addToRequestQueue(req);
    }

    private void goToViewRoom() {
        Intent intent = new Intent(AssessmentActivity.this, ViewRoom.class);
        intent.putExtra("room_id", room_id);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}
