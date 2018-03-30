package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.DatePicker;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.example.avendano.cp_scan.TimePicker;
import com.example.avendano.cp_scan.R;

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
    SQLiteHandler db;
    EditText message;
    TextView date, time;
    Spinner date_type, time_type;
    int room_id;
    Toolbar toolbar;
    AlertDialog progress;
    String tech_id;
    String room_name;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        progress = new SpotsDialog(this, "Requesting...");
        db = new SQLiteHandler(this);
        room_id = getIntent().getIntExtra("room_id", 0);
        Cursor c = db.getRoomDetails(room_id);
        if(c.moveToFirst()){
            room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
            tech_id = c.getString(c.getColumnIndex(db.COLUMN_TECH_ID));
        }
        message = (EditText) findViewById(R.id.message);
        date = (TextView) findViewById(R.id.custom_date);
        time = (TextView) findViewById(R.id.custom_time);
        date_type = (Spinner) findViewById(R.id.date);
        time_type = (Spinner) findViewById(R.id.time);
        String[] type = new String[]{"Anytime", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, type);
        date_type.setAdapter(adapter);
        time_type.setAdapter(adapter);
        time_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        time.setVisibility(View.GONE);
                        break;
                    }
                    case 1: {
                        time.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        date_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        date.setVisibility(View.GONE);
                        break;
                    }
                    case 1: {
                        date.setVisibility(View.VISIBLE);
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
                if(checkSchedule()){
                    saveRequestForInventory();
                }else{
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
        if (date_type.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                date.getText().toString().equalsIgnoreCase("yyyy-mm-dd")) {
            date.setError("Set date!");
            return false;
        } else if (time_type.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                time.getText().toString().equalsIgnoreCase("HH:mm:ss")) {
            time.setError("Set date!");
            return false;
        }else{
            return true;
        }
    }

    private void saveRequestForInventory() {
        String setDate = "";
        String setTime = "";
        final String msg = message.getText().toString().trim();
        if(date_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setDate = "Anytime";
        else
            setDate =  date.getText().toString();

        if(time_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setTime = "Anytime";
        else
            setTime =  time.getText().toString();

        final String finalSetDate = setDate;
        final String finalSetTime = setTime;
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_REQUEST_INVENTORY
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progress.dismiss();
                try {
                    JSONObject obj = new JSONObject(response);
                    if(!obj.getBoolean("error")){
                        Toast.makeText(RequestForInventory.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                        Log.e("REQ", "SMS: " + obj.getString("message"));
                        goToViewRoom();
                    }else{
                        Toast.makeText(RequestForInventory.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e("REQUESTINVENTORY", "JSON " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
//                Log.e("REQUESTINVENTORY", error.getMessage());
                Toast.makeText(RequestForInventory.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("room_id", String.valueOf(room_id));
                params.put("cust_id", SharedPrefManager.getInstance(RequestForInventory.this).getUserId());
                params.put("room_name", room_name);
                params.put("tech_id", tech_id);
                params.put("date", finalSetDate);
                params.put("time", finalSetTime);
                params.put("message", msg);
                return params;
            }
        };
        RequestQueueHandler.getInstance(RequestForInventory.this).addToRequestQueue(str);
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
        date.setText(dateString);
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        Date getTime = c.getTime();
        String timeString = new SimpleDateFormat("hh:mm a").format(getTime);
        time.setText(timeString);
    }

}
