package com.example.avendano.cp_scan.Pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Activities.LogInActivity;
import com.example.avendano.cp_scan.Activities.ViewPc;
import com.example.avendano.cp_scan.Connection_Detector.NetworkStateChange;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.BackgroundService;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.SQLiteHelper;
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nex3z.notificationbadge.NotificationBadge;

import org.json.JSONObject;

public class Main_Page extends AppCompatActivity {
    NotificationBadge badge;
    CardView account, room, report, req_sched, req, scan, inventory;
    Toolbar toolbar;
    SQLiteHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_custodian);

        db = new SQLiteHelper(this);

        //if log in
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            finish();
        } else {
            //check if not a custodian
            String role = SharedPrefManager.getInstance(this).getUserRole();
            if (!role.equalsIgnoreCase("custodian")) {
                setContentView(R.layout.main_page_technician);
                req_sched = (CardView) findViewById(R.id.req_sched);
                scan = (CardView) findViewById(R.id.quick_scan);
                inventory = (CardView) findViewById(R.id.start_inventory);
                badge = (NotificationBadge) findViewById(R.id.badge);
                badge.setNumber(0);
                getReqCount();

                inventory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //assessment
                        //startactivity for result
                    }
                });
                scan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scanPc();
                    }
                });

                req_sched.setOnClickListener(new View.OnClickListener() { //request schedules
                    @Override
                    public void onClick(View v) {
                        //satrtactivityforresult
                        Intent i = new Intent(Main_Page.this, SchedulesActivity.class);
                        startActivity(i);
                    }
                });
            } else {
                setContentView(R.layout.main_page_custodian);
            }
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account");

        account = (CardView) findViewById(R.id.account);
        room = (CardView) findViewById(R.id.rooms);
        report = (CardView) findViewById(R.id.reports);
        req = (CardView) findViewById(R.id.requests);
//        db = new SQLiteHandler(this);

        //onclick listeners
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Page.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Page.this, RoomActivity.class);
                startActivity(intent);
            }
        });
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Page.this, ReportActivity.class);
                startActivity(intent);
            }
        });

        req.setOnClickListener(new View.OnClickListener() { //list of requests - inventory, report, peripherals
            @Override
            public void onClick(View v) {
                //iba layout ng item
                //satrtactivityforresult
                Intent intent = new Intent(Main_Page.this, RequestListsActivity.class);
                startActivity(intent);
            }
        });

        //internet
        IntentFilter intentFilter = new IntentFilter(NetworkStateChange.NETWORK_AVEILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(NetworkStateChange.IS_NETWORK_AVAILABLE, false);
                String networkStat = isNetworkAvailable ? "connected" : "disconnected";
                if (isNetworkAvailable)
                {
                    Snackbar.make(findViewById(android.R.id.content), "Network " + networkStat,
                            Snackbar.LENGTH_SHORT).show();
                    //background
                    Intent i = new Intent(Main_Page.this, BackgroundService.class);
                    startService(i);
                }
                else
                    Snackbar.make(findViewById(android.R.id.content), "No Internet Connection",
                            Snackbar.LENGTH_INDEFINITE).show();
            }
        }, intentFilter);
    }

    private void getReqCount() {
        VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        volley.sendStringRequestGet(AppConfig.COUNT_REQ, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                try{
                    JSONObject obj = new JSONObject(response);
                    int inv = obj.getInt("inventory");
                    int rep = obj.getInt("repair");
                    int per = obj.getInt("peripherals");

                    int sum = inv + per + rep;

                    setBadgeNumber(sum);

                }catch(Exception e){

                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void setBadgeNumber(int sum) {
        badge.setNumber(sum);
    }

    private void scanPc() {
        IntentIntegrator integrator = new IntentIntegrator(Main_Page.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Place QR code to scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Scanning cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String content = result.getContents();
                String[] parts = content.split("#");
                String serial = parts[0];
                if (getCompId(serial) != 0) {
                    Intent intent = new Intent(Main_Page.this, ViewPc.class);
                    intent.putExtra("comp_id", getCompId(serial));
                    Main_Page.this.startActivity(intent);
                } else {
                    Toast.makeText(Main_Page.this, "Computer is not found in database", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private int getCompId(String serial) {
//        Cursor c = db.getCompIdAndModel(serial);
//        if (c.moveToFirst()) {
//            int comp_id = c.getInt(c.getColumnIndex(db.COMP_ID));
//            return comp_id;
//        }
        return 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
        this.stopService(new Intent(this, BackgroundService.class));
    }
}

