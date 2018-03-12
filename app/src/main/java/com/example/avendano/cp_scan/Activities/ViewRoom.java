package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewRoom extends AppCompatActivity {

    ImageView room_sched, room_computers;
    Button report;
    private int room_id;
    private SQLiteHandler db;
    //VIEWS
    private String user_role;
    private String custodian;
    Button btn;
    TextView building, room, cust, floor, pc_count, pc_working, lastAssess;
    ProgressDialog dialog;
    Connection_Detector connection_detector;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLocalRoomDetails();
        clearDb();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_room);

        //db
        db = new SQLiteHandler(this);

        connection_detector = new Connection_Detector(this);

        //get room id
        room_id = getIntent().getIntExtra("room_id", 0);
        //VIEWS
        building = (TextView) findViewById(R.id.building);
        room = (TextView) findViewById(R.id.room);
        cust = (TextView) findViewById(R.id.custodian);
        floor = (TextView) findViewById(R.id.floor);
        pc_working = (TextView) findViewById(R.id.working_pc);
        pc_count = (TextView) findViewById(R.id.pc_count);
        lastAssess = (TextView) findViewById(R.id.date_assess);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        dialog.setMessage("Loading...");

        user_role = SharedPrefManager.getInstance(this).getUserRole();
        btn = (Button) findViewById(R.id.room_button);
        if (user_role.equalsIgnoreCase("technician")) {
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.INVISIBLE);
        }

        //BUTTONS
        room_sched = (ImageView) findViewById(R.id.calendar);
        room_sched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Room Schedule
                Intent intent = new Intent(getApplicationContext(), ScheduleActivity.class);
                intent.putExtra("room_id", room_id);
                startActivity(intent);
                finish();
            }
        });
        room_computers = (ImageView) findViewById(R.id.computers);
        room_computers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check kung may laman na pc
                int pc = Integer.parseInt(pc_count.getText().toString().trim());
                if (pc > 0) {
                    Intent intent = new Intent(ViewRoom.this, RoomPc.class);
                    intent.putExtra("room_id", room_id);
                    intent.putExtra("room_name", room.getText().toString().trim());
                    startActivity(intent);
                    finish();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ViewRoom.this);
                    builder.setMessage("No computers assigned in this room.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

            }
        });
        report = (Button) findViewById(R.id.room_button);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pc = Integer.parseInt(pc_count.getText().toString().trim());
                if (pc > 0) {
                    if (checkDate()) {//naassess na ngayong week
                        if (connection_detector.isConnected()) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ViewRoom.this);
                            builder.setMessage("This room has been assessed this week. Reassess?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addPcToAssess();
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
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ViewRoom.this);
                            builder.setMessage("No Internet Connection")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    } else {
                        if (connection_detector.isConnected()) {
                            addPcToAssess();
                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(ViewRoom.this);
                            builder.setMessage("No Internet Connection")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ViewRoom.this);
                    builder.setMessage("No computers assigned in this room.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
        clearDb();
        showDialog();
        roomDetails();
    }

    private void addPcToAssess() {
        Cursor c = db.getCompInARoom(room_id);
        if (c.moveToFirst()) {
            do {
                int comp_id = c.getInt(c.getColumnIndex(db.COMP_ID));
                int pc_no = c.getInt(c.getColumnIndex(db.COMP_NAME));
                String model = c.getString(c.getColumnIndex(db.COMP_MODEL));
                String pr = c.getString(c.getColumnIndex(db.COMP_PR));
                String mb = c.getString(c.getColumnIndex(db.COMP_MB));
                String monitor = c.getString(c.getColumnIndex(db.COMP_MONITOR));
                String kboard = c.getString(c.getColumnIndex(db.COMP_KBOARD));
                String mouse = c.getString(c.getColumnIndex(db.COMP_MOUSE));
                String ram = c.getString(c.getColumnIndex(db.COMP_RAM));
                String vga = c.getString(c.getColumnIndex(db.COMP_VGA));
                String hdd = c.getString(c.getColumnIndex(db.COMP_HDD));
                String comp_status = c.getString(c.getColumnIndex(db.COMP_STATUS));

                long in = db.addPctoAssess(comp_id, mb, pr, monitor, ram, kboard, mouse, comp_status, vga, hdd, pc_no, model);
                Log.w("ADDED TO PCTOASSESS: ", "Status: " + in);
                Log.w("ADDED TO PCTOASSESS: ", "MODEL: " + model);

            } while (c.moveToNext());
        }

        Intent intent = new Intent(ViewRoom.this, AssessmentActivity.class);
        intent.putExtra("room_id", room_id);
        startActivity(intent);
        finish();
    }

    private boolean checkDate() {
        String lastReportDate = lastAssess.getText().toString().trim();
        //compare kung naassess na this week
        if (!lastReportDate.equals("--")) {
            Date date1 = null;
            try {
                date1 = new SimpleDateFormat("yyyy-MM-dd").parse(lastReportDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar currentCalendar = Calendar.getInstance();
            int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
            Calendar targetCalendar = Calendar.getInstance();
            targetCalendar.setTime(date1);
            int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
            return week == targetWeek;
        } else {
            return false;
        }
    }

    private void roomDetails() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_ROOM
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    hideDialog();
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int room_id = obj.getInt("room_id");
                        int floor = obj.getInt("floor");
                        String room_name = "";
                        if (obj.isNull("dept_name")) {
                            room_name = obj.getString("room_name");
                        } else {
                            room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                        }
                        String custodian = obj.getString("room_custodian");
                        String building = obj.getString("building");
                        int pc_count = obj.getInt("pc_count");
                        int pc_working = obj.getInt("pc_working");

                        String lastAssess = "";
                        if (obj.isNull("lastAssess")) {
                            lastAssess = "--";
                        } else {
                            lastAssess = obj.getString("lastAssess");
                        }
                        if (room_id == ViewRoom.this.room_id) {
                            ViewRoom.this.lastAssess.setText(lastAssess);
                            ViewRoom.this.floor.setText("" + floor);
                            ViewRoom.this.building.setText(building);
                            ViewRoom.this.room.setText(room_name);
                            ViewRoom.this.cust.setText(custodian);
                            ViewRoom.this.pc_count.setText("" + pc_count);
                            ViewRoom.this.pc_working.setText("" + pc_working);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("JSON ERROR 1", "ViewRoom: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                getLocalRoomDetails();
                Log.w("Volleyerror 1", "ViewRoom: "+error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void getLocalRoomDetails() {
        Cursor c = db.getRoomDetails(room_id);
        if (c.moveToFirst()) {
            String building = c.getString(c.getColumnIndex(db.ROOMS_BUILDING));
            int flr = c.getInt(c.getColumnIndex(db.ROOMS_FLOOR));

            String room = c.getString(c.getColumnIndex(db.ROOMS_NAME));
            String custodian = c.getString(c.getColumnIndex(db.ROOMS_CUSTODIAN));
            int pc_count = c.getInt(c.getColumnIndex(db.ROOMS_PC_COUNT));
            int pc_working = c.getInt(c.getColumnIndex(db.ROOMS_PC_WORKING));
            String lasAssess = c.getString(c.getColumnIndex(db.ROOMS_LAST_ASSESS));

            lastAssess.setText(lasAssess);
            floor.setText("" + flr);
            this.building.setText(building);
            this.room.setText(room);
            cust.setText(custodian);
            this.pc_count.setText("" + pc_count);
            this.pc_working.setText("" + pc_working);
        }
    }

    private void clearDb() {
        db.deleteAssessedPc();
        db.deletePcToAssess();
    }

    private void showDialog() {
        if (!dialog.isShowing())
            dialog.show();
    }

    private void hideDialog() {
        if (dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}
