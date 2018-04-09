package com.example.avendano.cp_scan.Activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
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
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ViewRoom extends AppCompatActivity {

    ImageView room_sched, room_computers, room_image;
    private int room_id;
    private SQLiteHandler db;
    //VIEWS
    private String user_role;
    Button room_btn;
    TextView building, room, cust, floor, pc_count, pc_working, lastAssess;
    android.app.AlertDialog dialog;
    Connection_Detector connection_detector;
    String image_path;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new backgroundTasks().execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_room);

        //db
        db = new SQLiteHandler(this);
        Log.e("REQUEST COUNT", db.getRequestCount());

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
        dialog = new SpotsDialog(this, "Loading...");
        room_image = (ImageView) findViewById(R.id.room_image);

        user_role = SharedPrefManager.getInstance(this).getUserRole();
        room_btn = (Button) findViewById(R.id.room_button);
        if (user_role.equalsIgnoreCase("technician") || user_role.equalsIgnoreCase("custodian") ||
        user_role.equalsIgnoreCase("main technician")) {
            room_btn.setVisibility(View.VISIBLE);
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
                int pc = Integer.parseInt(pc_count.getText().toString().trim());
                if (pc > 0) {
                    if (user_role.equalsIgnoreCase("custodian")) {
                        //if hindi pa nagrerequest "Request"
                        //pag nkapagrequest na "Edit Request"
                        String request = room_btn.getText().toString().trim();
                        if (request.equalsIgnoreCase("request")) {
                            if (connection_detector.isConnected()) {
                                Intent intent = new Intent(ViewRoom.this, RequestForInventory.class);
                                intent.putExtra("room_id", room_id);
                                startActivity(intent);
                                finish();
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
                        } else {
                            checkLastInventoryRequestFrmServer(true);
                        }
                    } else if (user_role.equalsIgnoreCase("technician") || user_role.equalsIgnoreCase("main technician")) {
                        if (connection_detector.isConnected()) {
                            if (checkDate()) {//naassess na ngayong week
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
                                addPcToAssessFrmServer();
                            }
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
        dialog.show();
        new backgroundTasks().execute();
    }

    class backgroundTasks extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            clearDb();
            if (user_role.equalsIgnoreCase("custodian"))
                checkLastInventoryRequestFrmServer(false);
            roomDetails();
            return null;
        }
    }

    private void checkLastInventoryRequestFrmServer(final boolean popup) {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_CHECK_LAST_INVENTORY_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("RESPONSE", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        if (!obj.getBoolean("pending")) { //kapag hindi pa nagrerequest
                            room_btn.setText("Request");
                            room_btn.setBackgroundResource(R.color.button_color);
                            room_btn.setTextColor(getResources().getColor(R.color.white));
                        } else { //kapag na request na
                            room_btn.setText("Requested");
                            room_btn.setBackgroundResource(R.drawable.style_button_white);
                            room_btn.setTextColor(getResources().getColor(R.color.button_color));
                            int req_id = obj.getInt("req_id");
                            int rep_id = 0;
                            if (!obj.isNull("rep_id"))
                                rep_id = obj.getInt("rep_id");
                            int room_id = obj.getInt("room_id");
                            String cust_id = obj.getString("custodian");
                            String tech_id = obj.getString("technician");
                            String date = obj.getString("date");
                            String time = obj.getString("time");
                            String msg = obj.getString("msg");
                            String req_date = obj.getString("date_requested");
                            String req_time = obj.getString("time_requested");
                            String req_status = obj.getString("req_status");

                            if (popup)
                                showRequestDetails(req_id, rep_id, room_id, cust_id, tech_id, date, time,
                                        msg, req_date, req_time, req_status);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    checkLastInventoryRequestFrmLocal(popup);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VIEWROOM", "ERROR: " + error.getMessage());
                checkLastInventoryRequestFrmLocal(popup);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("room_id", String.valueOf(room_id));
                return param;
            }
        };
        RequestQueueHandler.getInstance(this).addToRequestQueue(str);
    }

    @SuppressLint("ResourceAsColor")
    private void checkLastInventoryRequestFrmLocal(boolean popup) {
        if (popup) {
            Cursor c = db.checkIfRequested(room_id);
            if (c.moveToFirst()) {
                int req_id = c.getInt(c.getColumnIndex(db.REQ_ID));
                int rep_id = c.getInt(c.getColumnIndex(db.REPORT_ID));
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                ;
                String cust_id = c.getString(c.getColumnIndex(db.COLUMN_CUST_ID));
                String tech_id = c.getString(c.getColumnIndex(db.COLUMN_TECH_ID));
                String date = c.getString(c.getColumnIndex(db.REQ_DATE));
                String time = c.getString(c.getColumnIndex(db.REQ_TIME));
                String msg = c.getString(c.getColumnIndex(db.REQ_MESSAGE));
                String req_date = c.getString(c.getColumnIndex(db.DATE_OF_REQ));
                String req_time = c.getString(c.getColumnIndex(db.TIME_OF_REQ));
                String req_status = c.getString(c.getColumnIndex(db.REQ_STATUS));

                showRequestDetails(req_id, rep_id, room_id, cust_id, tech_id, date, time,
                        msg, req_date, req_time, req_status);
            }
        } else {
            Cursor c = db.checkIfRequested(room_id);
            if (c.moveToFirst()) {
                room_btn.setText("Requested");
                room_btn.setBackgroundResource(R.drawable.style_button_white);
                room_btn.setTextColor(getResources().getColor(R.color.button_color));
            } else {
                room_btn.setText("Request");
                room_btn.setBackgroundResource(R.color.button_color);
                room_btn.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }

    private void showRequestDetails(final int req_id, int rep_id, final int room_id, String cust_id,
                                    String tech_id, String date, String time, String msg,
                                    String date_req, String time_req, String req_status) {
        //alertdialog
        String msg_body = "";
        if (msg.length() == 0) {
            msg_body = "Date requested: " + date_req + "\nTime Requested: " + time_req
                    + "\nAssigned Date: " + date + "\nAssigned Time: " + time + "\nRequest Status: " + req_status;
        } else {
            msg_body = "Date requested: " + date_req + "\nTime Requested: " + time_req
                    + "\nAssigned Date: " + date + "\nAssigned Time: " + time + "\nRequest Status: " + req_status
                    + "\n\nMessage: " + msg;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request Details");
        builder.setMessage(msg_body);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (connection_detector.isConnected()) {
                            Intent intent = new Intent(ViewRoom.this, EditRequestSchedule.class);
                            intent.putExtra("type", "inventory");
                            intent.putExtra("room_pc_id", room_id);
                            intent.putExtra("id", req_id);
                            ViewRoom.this.startActivity(intent);
                            finish();
                        } else
                            Toast.makeText(ViewRoom.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Cancel Request", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel
                        cancelRequestInventory(req_id);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelRequestInventory(final int req_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Request");
        builder.setMessage("Are you sure you want to cancel your request?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (connection_detector.isConnected())
                            cancelRequest(req_id);
                        else
                            Toast.makeText(ViewRoom.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void cancelRequest(final int req_id) {
        class cancel {
            void callCancel() {
                new cancelling().execute();
            }

            class cancelling extends AsyncTask<Void, Void, Void> {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    dialog.show();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    cancelRequest();
                    return null;
                }
            }

            private void cancelRequest() {

                StringRequest str = new StringRequest(Request.Method.POST
                        , AppConfig.URL_CANCEL_SCHEDULE
                        , new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            //update sqlite
                            if (!obj.getBoolean("error")) {
                                updateSQlite(req_id);
                                new backgroundTasks().execute();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        dialog.dismiss();
                                    }
                                }, 5000);
                            } else {
                                dialog.dismiss();
                                Toast.makeText(ViewRoom.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            dialog.dismiss();
                            Toast.makeText(ViewRoom.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                            Log.e("JSONERROR", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(ViewRoom.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("id", String.valueOf(req_id));
                        param.put("req_type", "inventory");
                        return param;
                    }
                };
                RequestQueueHandler.getInstance(ViewRoom.this).addToRequestQueue(str);
            }
        }
        new cancel().callCancel();
    }

    private void updateSQlite(int req_id) {
        db.updateReqInvStatus(req_id, "Cancel");
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
        intent.putExtra("request", 0);
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
                    dialog.dismiss();
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
                        String path = obj.getString("room_image");

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
                            if (obj.isNull("room_image"))
                                image_path = "";
                            else {
                                image_path = AppConfig.ROOT_URL + path;
                                getImage();
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e("JSON ERROR 1", "ViewRoom: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                getLocalRoomDetails();
                Log.w("Volleyerror 1", "ViewRoom: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void getImage() {
        ImageRequest req = new ImageRequest(image_path, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                room_image.setImageBitmap(response);
                room_image.setBackgroundResource(0);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewRoom.this, "Something went wrong " +
                                "in loading image", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });

        RequestQueueHandler.getInstance(ViewRoom.this).addToRequestQueue(req);
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

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}
