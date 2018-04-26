package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.DatePicker;
import com.example.avendano.cp_scan.TimePicker;
import com.example.avendano.cp_scan.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 25 Mar 2018.
 */

public class RequestForInventory extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    EditText message;
    TextView date, time;
    Spinner date_type;
    int room_id;
    Toolbar toolbar;
    AlertDialog progress;
    String tech_id;
    String room_name;
    VolleyRequestSingleton volley;
    boolean dateSet = false, timeSet = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Request Inventory");

        volley = new VolleyRequestSingleton(this);

        progress = new SpotsDialog(this, "Requesting...");
        room_id = getIntent().getIntExtra("room_id", 0);

        message = (EditText) findViewById(R.id.message);
        date = (TextView) findViewById(R.id.custom_date);
        time = (TextView) findViewById(R.id.custom_time);
        date_type = (Spinner) findViewById(R.id.date);
        String[] type = new String[]{"Anytime", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, type);
        date_type.setAdapter(adapter);
        date_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        date.setVisibility(View.GONE);
                        dateSet = false;
                        break;
                    }
                    case 1: {
                        DialogFragment datePicker = new DatePicker();
                        datePicker.show(getSupportFragmentManager(), "date picker");
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //date picker
                DialogFragment datePicker = new DatePicker();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //time picker
                DialogFragment timePicker = new TimePicker();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assess_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                if (checkSchedule()) {
                    //check tech sched
                    saveInventoryRequest();
                } else {
                    progress.dismiss();
                }
                Log.w("SEND REQUEST", "User request for inventory");
                break;
            }

            case R.id.cancel: {
                goToViewRoom();
            }
        }

        return true;
    }

    private boolean checkSchedule() {
        progress.show();
        progress.setCancelable(false);
        if (date_type.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                date.getText().toString().equalsIgnoreCase("yyyy-mm-dd")) {
            date.setError("Set date!");
            return false;
        } else if (time.getText().toString().equalsIgnoreCase("HH:mm:ss")) {
            time.setError("Set date!");
            return false;
        } else {
            return true;
        }
    }

    private void saveInventoryRequest() {
        String setDate = "";
        String setTime = "";
        final String msg = message.getText().toString().trim();
        if (date_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setDate = "Anytime";
        else
            setDate = date.getText().toString();

        setTime = time.getText().toString();

        final String finalSetDate = setDate;
        final String finalSetTime = setTime;

        String date_req = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String time_req = new SimpleDateFormat("HH:mm:ss").format(new Date());

        Map<String, String> params = new HashMap<>();
        params.put("room_id", String.valueOf(room_id));
        params.put("set_time", finalSetTime);
        params.put("set_date", finalSetDate);
        params.put("message", msg);
        params.put("date_req", date_req);
        params.put("time_req", time_req);

        volley.sendStringRequestPost(AppConfig.SAVE_REQ_INVENTORY, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                Log.e("RESPONSe", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Log.e("SMS", obj.getString("sms"));
                        Toast.makeText(RequestForInventory.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                goToViewRoom();
                            }
                        }, 2000);
                    } else {
                        progress.dismiss();
                        Toast.makeText(RequestForInventory.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progress.dismiss();
                    Toast.makeText(RequestForInventory.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progress.dismiss();
                Toast.makeText(RequestForInventory.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }, params);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToViewRoom();
    }

    private void goToViewRoom() {
        Intent intent = new Intent(RequestForInventory.this, ViewRoom.class);
        intent.putExtra("room_id", room_id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date getdate = c.getTime();
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(getdate);
        if (date.getVisibility() == View.GONE) {
            date.setVisibility(View.VISIBLE);
            date.setText(dateString);
        }
        dateSet = true;
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        timeSet = true;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        Date getTime = c.getTime();
        String timeString = new SimpleDateFormat("HH:mm:ss").format(getTime);
        Date pickedTime;
        try {
            pickedTime = new SimpleDateFormat("HH:mm:ss").parse(timeString);

            Date am = new SimpleDateFormat("HH:mm:ss").parse("07:59:00");
            Date pm = new SimpleDateFormat("HH:mm:ss").parse("16:01:00");

            Log.e("TIME", "GETTIME: " + pickedTime);
            Log.e("TIME", "AM: " + am);
            Log.e("TIME", "PM: " + pm);
            Log.e("TIME", "TIME B4 AM: " + pickedTime.before(am));
            Log.e("TIME", "AM B4 TIME: " + am.before(pickedTime));
            Log.e("TIME", "TIME after AM: " + pickedTime.after(am));
            Log.e("TIME", "AM after TIME: " + am.after(pickedTime));
            Log.e("TIME", "TIME B4 PM: " + pickedTime.before(pm));
            Log.e("TIME", "PM B4 TIME: " + pm.before(pickedTime));
            Log.e("TIME", "TIME after PM: " + pickedTime.after(pm));
            Log.e("TIME", "PM after TIME: " + pm.after(pickedTime));

            if (pickedTime.after(am) && pickedTime.before(pm)) {
                if (dateSet)    //hindi anytime ung time
                {
                    checkTime(pickedTime, date.getText().toString());
                } else
                    time.setText(new SimpleDateFormat("HH:mm:ss").format(pickedTime));
            } else {
                Toast.makeText(this, "Pick time between 8AM and 4PM", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkTime(final Date pickedTime, final String date) {
        final Calendar pickCal = Calendar.getInstance();
        final Calendar setTimePlusHour = Calendar.getInstance();
        pickCal.setTime(pickedTime);
        final Date picked = pickCal.getTime();
        final Calendar setCal = Calendar.getInstance();

        volley.sendStringRequestGet(AppConfig.GET_INVENTORY_REQ,
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            int count = 0;
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                int room = obj.getInt("room_id");

                                if (room_id == room) {
                                    if (obj.getString("req_status").equalsIgnoreCase("accepted")) {
                                        String setDate = obj.getString("date");
                                        if (setDate.equals(date) || setDate.equalsIgnoreCase("Anytime")) {
                                            String time = obj.getString("time");
                                            Date setTime = new SimpleDateFormat("HH:mm:ss").parse(time);
                                            setCal.setTime(setTime);
                                            setTimePlusHour.setTime(setTime);
                                            setTimePlusHour.add(Calendar.HOUR_OF_DAY, 1);

                                            if ((picked.after(setCal.getTime()) && picked.before(setTimePlusHour.getTime())) || (picked.equals(setCal) || picked.equals(setTimePlusHour))) {
                                                Toast.makeText(RequestForInventory.this, "Set time is not available", Toast.LENGTH_SHORT).show();
                                                break;
                                            } else {
                                                count++;
                                            }
                                        }
                                    }
                                }
                            }
                            if (count == array.length()) {
                                time.setText(new SimpleDateFormat("HH:mm:ss").format(pickedTime));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RequestForInventory.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        String string = (error instanceof TimeoutError) ? "Server took too long to respond" : "Can't Connect to the server";
                        Toast.makeText(RequestForInventory.this, string, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
