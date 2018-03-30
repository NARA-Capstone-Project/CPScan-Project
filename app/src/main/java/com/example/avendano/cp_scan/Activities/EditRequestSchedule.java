package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.RadioGroup;
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
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 27 Mar 2018.
 */

public class EditRequestSchedule extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private int id, room_pc_id;
    String type;
    SQLiteHandler db;
    EditText message;
    TextView date, time;
    Spinner date_type, time_type;
    Toolbar toolbar;
    AlertDialog progress;
    RadioGroup status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

        id = getIntent().getIntExtra("id", 0);
        room_pc_id = getIntent().getIntExtra("room_pc_id", 0);
        type = getIntent().getStringExtra("type");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        db = new SQLiteHandler(this);
        message = (EditText) findViewById(R.id.message);
        date = (TextView) findViewById(R.id.custom_date);
        time = (TextView) findViewById(R.id.custom_time);
        date_type = (Spinner) findViewById(R.id.date);
        time_type = (Spinner) findViewById(R.id.time);
        if(type.equalsIgnoreCase("repair"))
            status.setVisibility(View.VISIBLE);
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

        setDetails();

    }

    private void setDetails() {
        //set time and date image(kung repair)
        //check kung repair or inventory
        ///if cant connect sa server babalik tas toast ng cant connect
        class loadDetails extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(type.equalsIgnoreCase("inventory"))
                    getReqInvDetails();
                else if(type.equalsIgnoreCase("repair"))
                    getReqRepDetails();
                return null;
            }
        }

        new loadDetails().execute();
    }

    private void getReqRepDetails() {
    }

    private void getReqInvDetails() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_ALL_INVENTORY_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int req_id = obj.getInt("req_id");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        String msg = obj.getString("msg");

                        if(req_id == id){
                            if(!date.equalsIgnoreCase("anytime")){
                                date_type.setSelection(1);
                                EditRequestSchedule.this.date.setText(date);
                            }
                            if(!time.equalsIgnoreCase("anytime")){
                                time_type.setSelection(1);
                                EditRequestSchedule.this.time.setText(time);
                            }
                            message.setText(msg);
                            break;
                        }

                    }
                    progress.dismiss();

                } catch (JSONException e) {
                    progress.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(EditRequestSchedule.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("room_id", String.valueOf(room_pc_id));
                return param;
            }
        };
        RequestQueueHandler.getInstance(EditRequestSchedule.this).addToRequestQueue(str);
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
        } else {
            return true;
        }
    }

    private void goToViewRoom() {
        Intent intent = new Intent(EditRequestSchedule.this, ViewRoom.class);
        intent.putExtra("room_id", room_pc_id);
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
                    new editRequests().execute();
                } else {
                    progress.dismiss();
                }
                Log.w("SEND REQUEST", "User request for inventory");
                break;
            }
            case R.id.cancel: {
                if(type.equalsIgnoreCase("inventory"))
                    goToViewRoom();
            }
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(type.equalsIgnoreCase("inventory"))
            goToViewRoom();
    }

    private class editRequests extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (type.equalsIgnoreCase("inventory"))
                editRequestInventory();
            else if (type.equalsIgnoreCase("repair"))
                editRequestRepair();
            else
                editSchedule();
            return null;
        }
    }

    private void editSchedule() {
    }

    private void editRequestRepair() {
    }

    private void editRequestInventory() {
        String setDate = "";
        String setTime = "";
        final String getMsg = message.getText().toString().trim();
        if (date_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setDate = "Anytime";
        else
            setDate = date.getText().toString();

        if (time_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setTime = "Anytime";
        else
            setTime = time.getText().toString();
        final String query = "UPDATE request_inventory SET date = '"+setDate+"', time ='"
                +setTime +"', message = '"+getMsg+"' WHERE req_id = ?";

        final String finalSetDate = setDate;
        final String finalSetTime = setTime;
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_UPDATE_SCHEDULE
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("ERROR", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if(!obj.getBoolean("error")){
                        //update sqlite
                        updateSqliteInventory(finalSetDate, finalSetTime, getMsg);
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(EditRequestSchedule.this, "Request Updated!", Toast.LENGTH_SHORT).show();
                                progress.dismiss();
                                goToViewRoom();
                            }
                        }, 5000);
                    }else
                        Toast.makeText(EditRequestSchedule.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(EditRequestSchedule.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("query", query);
                params.put("id", String.valueOf(id));
                return params;
            }
        };
        RequestQueueHandler.getInstance(EditRequestSchedule.this).addToRequestQueue(str);
    }

    private void updateSqliteInventory(String finalSetDate, String finalSetTime, String getMsg) {
        db.updateReqInventoryDetails(id, finalSetDate, finalSetTime, getMsg);
    }
}
