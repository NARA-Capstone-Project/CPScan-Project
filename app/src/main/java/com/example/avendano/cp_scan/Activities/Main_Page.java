package com.example.avendano.cp_scan.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Connection_Detector.NetworkStateChange;
import com.example.avendano.cp_scan.Database.AddCompFrmServer;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main_Page extends AppCompatActivity {
    NotificationBadge badge;
    CardView account, room, report, req_sched, req, scan, inventory;
    Toolbar toolbar;
    SQLiteHelper db;
    VolleyRequestSingleton volley;
    TextView display_date;

    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_custodian);

        db = new SQLiteHelper(this);
        SQLiteHandler handler = new SQLiteHandler(this);
        AddCompFrmServer comp = new AddCompFrmServer(this, handler);
        comp.SyncFunction();
        volley = new VolleyRequestSingleton(this);
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
                getReqCount();

                inventory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //assessment
                        //startactivity for result
                        //dialog muna
//                        AssessmentDialog assessmentDialog = new AssessmentDialog();
//                        assessmentDialog.show(getSupportFragmentManager(), "");
                        getRoomToAssess();
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

        display_date = (TextView) findViewById(R.id.date);
        display_date.setText(new SimpleDateFormat("MMM dd, yyyy").format(new Date()));
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
                if (isNetworkAvailable) {
                    Snackbar.make(findViewById(android.R.id.content), "Network " + networkStat,
                            Snackbar.LENGTH_SHORT).show();
                    //background
                    Intent i = new Intent(Main_Page.this, BackgroundService.class);
                    startService(i);
                } else
                    Snackbar.make(findViewById(android.R.id.content), "No Internet Connection",
                            Snackbar.LENGTH_INDEFINITE).show();
            }
        }, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String role = SharedPrefManager.getInstance(this).getUserRole();
        if (!role.equalsIgnoreCase("custodian")) {
            getReqCount();
        }
    }

    private void getRoomToAssess() {
        final ArrayList<String> tempRooms = new ArrayList<>();
        final ArrayList<Integer> room_ids = new ArrayList<>();
        final ArrayList<Integer> req_ids = new ArrayList<>();
        Map<String, String> param = new HashMap<>();
        param.put("user_id", SharedPrefManager.getInstance(this).getUserId());
        volley.sendStringRequestPost(AppConfig.GET_INVENTORY_REQ, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                Log.e("RESPONSe", response);
                try {
                    //sched_id, cat, desc, room_pc_id, tech_id, data,time, name, task_status
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        //roomid rooms req id
                        int room_id = obj.getInt("room_id");
                        int req_id = obj.getInt("req_id");
                        String room_name = obj.getString("room_name");
                        String set_date = obj.getString("date");
                        String req_status = obj.getString("req_status");

                        if(req_status.equalsIgnoreCase("accepted")){
                            Date task_date = new SimpleDateFormat("yyyy-MM-dd").parse(set_date);
                            if(task_date.equals(new Date()) || task_date.before(new Date())){
                                tempRooms.add(room_name);
                                room_ids.add(room_id);
                                req_ids.add(req_id);
                            }
                        }
                    }
                    if (tempRooms.size() != 0)
                        showRoomsInDialog(tempRooms, room_ids, req_ids);
                    else
                        Toast.makeText(Main_Page.this, "No scheduled inventory for today", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.getMessage());
            }
        }, param);
    }

    private void showRoomsInDialog(final ArrayList<String> tempRooms, final ArrayList<Integer> room_ids, final ArrayList<Integer> req_ids) {
        String[] rooms = tempRooms.toArray(new String[tempRooms.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Room...")
                .setSingleChoiceItems(rooms, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        position = which;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //pass room id to inventory activity
                        addPcToAssessFrmServer(room_ids.get(position), req_ids.get(position));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getReqCount() {
        VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        volley.sendStringRequestGet(AppConfig.COUNT_REQ, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    int inv = obj.getInt("inventory");
                    int rep = obj.getInt("repair");
                    int per = obj.getInt("peripherals");

                    int sum = inv + per + rep;
                    badge.setNumber(0);
                    setBadgeNumber(sum);
                } catch (Exception e) {

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

    private void clearDb() {
        db.deleteAssessedPc();
        db.deletePcToAssess();
    }

    private void addPcToAssessFrmServer(final int id, final int req_id) {
        clearDb();
        volley.sendStringRequestGet(AppConfig.GET_COMPUTERS, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int comp_id = obj.getInt("comp_id");
                        int room_id = 0;
                        if (!obj.isNull("room_id")) {
                            room_id = obj.getInt("room_id");
                        }
                        int pc_no = obj.getInt("pc_no");
                        String model = obj.getString("model");
                        String mb = obj.getString("mb");
                        String pr = obj.getString("pr");
                        String monitor = obj.getString("monitor");
                        String ram = obj.getString("ram");
                        String kboard = obj.getString("kboard");
                        String mouse = obj.getString("mouse");
                        String vga = obj.getString("vga");
                        String hdd = obj.getString("hdd");
                        String comp_status = obj.getString("comp_status");

                        if (room_id == id) {
                            long in = db.addPctoAssess(comp_id, mb, pr, monitor, ram, kboard, mouse, comp_status, vga, hdd, pc_no, model);
                        }
                    }
                    goToAssessment(id, req_id);
                } catch (JSONException e) {
                    Toast.makeText(Main_Page.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    private void goToAssessment(int room_id, int req_id) {
        Intent intent = new Intent(Main_Page.this, InventoryActivty.class);
        intent.putExtra("room_id", room_id);
        intent.putExtra("req_id", req_id);
        startActivity(intent);
        //start activity for result
    }
}

