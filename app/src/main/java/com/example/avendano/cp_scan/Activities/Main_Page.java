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
import com.example.avendano.cp_scan.Network_Handler.NetworkStateChange;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.BackgroundServices.GetNewRepairRequest;
import com.example.avendano.cp_scan.Database.SQLiteHelper;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
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
    Intent receiverIntent, notifIntent;
    BroadcastReceiver reqCountReceiver, notifReceiver;

    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page_custodian);

        Intent intent = new Intent(this, GetNewRepairRequest.class);
        startService(intent);
        db = new SQLiteHelper(this);
        volley = new VolleyRequestSingleton(this);
        //if log in
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            finish();
        } else {
            //check if not a custodian
            String role = SharedPrefManager.getInstance(this).getUserRole();
            if (role.equalsIgnoreCase("technician")) {

                setContentView(R.layout.main_page_technician);
                req_sched = (CardView) findViewById(R.id.req_sched);
                inventory = (CardView) findViewById(R.id.start_inventory);
                badge = (NotificationBadge) findViewById(R.id.badge);
                badge.setAnimationEnabled(false);

                //get request count from service
                receiverIntent = new Intent(this, GetNewRepairRequest.class);
                reqCountReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        setNotifCount(intent);
                    }
                };

                inventory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getRoomToAssess();
                    }
                });

                req_sched.setOnClickListener(new View.OnClickListener() { //request schedules
                    @Override
                    public void onClick(View v) {
                        //satrtactivityforresult
                        Intent i = new Intent(Main_Page.this, ScheduleOfRequestsActivity.class);
                        startActivity(i);
                    }
                });
            } else if(role.equalsIgnoreCase("custodian")){
                setContentView(R.layout.main_page_custodian);
            }else{
                setContentView(R.layout.main_head_layout); //request, reports, profile
            }
            if(SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("custodian")||
                    SharedPrefManager.getInstance(this).getUserRole().equalsIgnoreCase("technician")){

                room = (CardView) findViewById(R.id.rooms);
                room.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Main_Page.this, RoomActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account");

        display_date = (TextView) findViewById(R.id.date);
        display_date.setText(new SimpleDateFormat("MMM dd, yyyy").format(new Date()));
        account = (CardView) findViewById(R.id.account);
        report = (CardView) findViewById(R.id.reports);
        req = (CardView) findViewById(R.id.requests);
//        db = new SQLiteHandler(this);

        //onclick listeners
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main_Page.this, ProfileActivity.class);
                startActivity(intent);
                finish();
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
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "No Internet Connection",
                            Snackbar.LENGTH_INDEFINITE).show();
                }
            }
        }, intentFilter);
    }

    private void setNotifCount(Intent intent) {
        int sum = intent.getIntExtra("number", 0);
        setBadgeNumber(sum);
    }

    private void getRoomToAssess() {
        //today
        final ArrayList<String> tempRooms = new ArrayList<>();
        final ArrayList<Integer> room_ids = new ArrayList<>();
        final ArrayList<Integer> req_ids = new ArrayList<>();
        //missed
        final ArrayList<String> tempRoomsM = new ArrayList<>();
        final ArrayList<Integer> room_idsM = new ArrayList<>();
        final ArrayList<Integer> req_idsM = new ArrayList<>();
        //merged
        final ArrayList<String> rooms = new ArrayList<>();
        final ArrayList<Integer> roomids = new ArrayList<>();
        final ArrayList<Integer> reqids = new ArrayList<>();

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
                        String set_time = obj.getString("time");
                        String req_status = obj.getString("req_status");

                        if (req_status.equalsIgnoreCase("accepted")) {
                            if (obj.getString("technician").equals(SharedPrefManager.getInstance(Main_Page.this).getUserId())) {
                                if (set_date.equalsIgnoreCase("anytime")) {
                                    room_name = room_name + " (Anytime)";
                                    tempRooms.add(room_name);
                                    room_ids.add(room_id);
                                    req_ids.add(req_id);
                                } else {
                                    Date task_date = new SimpleDateFormat("yyyy-MM-dd").parse(set_date);
                                    String strToday = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                                    Date today = new SimpleDateFormat("yyyy-MM-dd").parse(strToday);
                                    if (task_date.equals(today)) {
                                        room_name = room_name + " (Today -" + set_time + ")";
                                        tempRooms.add(room_name);
                                        room_ids.add(room_id);
                                        req_ids.add(req_id);
                                    }
                                }

                            }
                        }
                    }
                    if (tempRooms.size() != 0 || tempRoomsM.size() != 0) {
                        rooms.addAll(tempRoomsM);
                        rooms.addAll(tempRooms);
                        reqids.addAll(req_idsM);
                        reqids.addAll(req_ids);
                        roomids.addAll(room_idsM);
                        roomids.addAll(room_ids);
                        showRoomsInDialog(rooms, roomids, reqids);
                    } else
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

    private void setBadgeNumber(int sum) {
        badge.setNumber(sum);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Scanning cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String serial = result.getContents();
//                String[] parts = content.split("#");
//                String serial = parts[0];
                checkComputer(serial);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkComputer(final String serial) {

        volley.sendStringRequestGet(AppConfig.GET_COMPUTERS
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("RESPONSE", response);
                        Toast.makeText(Main_Page.this, serial, Toast.LENGTH_SHORT).show();
                        try {
                            JSONArray array = new JSONArray(response);
                            int counter = 0;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String mb = obj.getString("mb");
                                String mon = obj.getString("monitor");
                                int comp_id = obj.getInt("comp_id");

                                if (serial.equals(mb) || serial.equals(mon)) {
                                    Intent intent = new Intent(Main_Page.this, ViewPc.class);
                                    intent.putExtra("comp_id", comp_id);
                                    Main_Page.this.startActivity(intent);
                                    break;
                                } else {
                                    counter++;
                                }
                            }
                            if (counter == array.length())
                                Toast.makeText(Main_Page.this, "Scanned computer not recognized", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
        String role = SharedPrefManager.getInstance(this).getUserRole();

        if (role.equalsIgnoreCase("technician")) {
            unregisterReceiver(reqCountReceiver);
            stopService(receiverIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String role = SharedPrefManager.getInstance(this).getUserRole();
        if (role.equalsIgnoreCase("technician")) {
            startService(receiverIntent);
            registerReceiver(reqCountReceiver, new IntentFilter(GetNewRepairRequest.BROADCAST_ACTION));
        }
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
                Log.e("ROOMID", " " + id);
                Log.e("RESPONSE", "COMPS:" + response);
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int r_id = 0;
                        int comp_id = obj.getInt("comp_id");
                        String comp_serial = "";
                        if (!obj.isNull("room_id")) {
                            r_id = obj.getInt("room_id");
                        }
                        if (!obj.isNull("comp_serial")) {
                            comp_serial = obj.getString("comp_serial");
                        }
                        Log.e("RIDS", "comp: " + comp_id + " ROOM: " + r_id + " : " + id);
                        if (r_id == id) {
                            Log.e("COMP", " " + comp_id);
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

                            int pc_no = obj.getInt("pc_no");
                            //add comp serial
                            long in = db.addPctoAssess(comp_id, comp_serial, mb, pr, monitor, ram, kboard, mouse, comp_status, vga, hdd, pc_no, model);
                            Log.e("PCTOASSESS", "COUNT: " + db.pcToAssessCount());
                        }
                    }
                    goToAssessment(id, req_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Main_Page.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
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

