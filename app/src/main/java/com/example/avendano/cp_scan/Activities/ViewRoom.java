package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewRoom extends AppCompatActivity {

    ImageView room_sched, room_computers, room_image;
    private int room_id;
    private SQLiteHandler db;
    //VIEWS
    private String user_role;
    Button room_btn;
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
        room_image = (ImageView) findViewById(R.id.room_image);

        dialog.setMessage("Loading...");

        user_role = SharedPrefManager.getInstance(this).getUserRole();
        room_btn = (Button) findViewById(R.id.room_button);
        if (user_role.equalsIgnoreCase("technician")) {
            room_btn.setVisibility(View.VISIBLE);
        } else if (user_role.equalsIgnoreCase("custodian")) {
            room_btn.setText("Request");
        } else {
            room_btn.setVisibility(View.INVISIBLE);
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
        room_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_role.equalsIgnoreCase("custodian")) {

                    Toast.makeText(ViewRoom.this, "Custodian request for inventory", Toast.LENGTH_SHORT).show();
                } else if (user_role.equalsIgnoreCase("technician")){
                    int pc = Integer.parseInt(pc_count.getText().toString().trim());
                    if (pc > 0) {
                        if (checkDate()) {//naassess na ngayong week
                            if (connection_detector.isConnected()) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(ViewRoom.this);
                                builder.setMessage("This room has been assessed this week. Reassess?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                addPcToAssessFrmServer();
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
                                addPcToAssessFrmServer();
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
            }
        });
        clearDb();
        showDialog();
        roomDetails();
//        getImage();
    }

    private void addPcToAssessFrmServer() {
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_PC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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

                        if (room_id == ViewRoom.this.room_id) {
                            long in = db.addPctoAssess(comp_id, mb, pr, monitor, ram, kboard, mouse, comp_status, vga, hdd, pc_no, model);
                            Log.w("ADDED TO PCTOASSESS: ", "Status: " + in);
                            Log.w("ADDED TO PCTOASSESS: ", "MODEL: " + model);
                        }
                    }
                    goToAssessment();
                } catch (JSONException e) {
                    Toast.makeText(ViewRoom.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("RESULT", "Error: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(ViewRoom.this).addToRequestQueue(str);
    }

    private void goToAssessment() {
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
                Log.w("Volleyerror 1", "ViewRoom: " + error.getMessage());
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

    private void getImage() {
        class GetImage extends AsyncTask<Integer, Void, Bitmap> {

            @Override
            protected Bitmap doInBackground(Integer... integers) {
                int id = integers[0];
                String get = AppConfig.ROOT_URL + "cict_getRoom_Image.php?id=" + id;
                URL url = null;
                Bitmap image = null;
                try {
                    url = new URL(get);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                room_image.setImageBitmap(bitmap);
            }
        }
        GetImage gi = new GetImage();
        gi.execute(room_id);
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
