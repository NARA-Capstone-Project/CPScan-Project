package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.SearchAdapter;
import com.example.avendano.cp_scan.Adapter.TaskAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.DatePicker;
import com.example.avendano.cp_scan.Model.Search;
import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
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

public class TaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    Toolbar toolbar;
    Spinner title;
    LinearLayout choose_layout;
    TextView choose, custom_date, custom_time;
    EditText desc;
    Spinner date, time;
    AlertDialog progress;
    private String TAG = "TASK";
    int sched_id;
    String type;
    SQLiteHandler db;
    int room_comp_id;
    String room_;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select Room...");
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        //intent
        type = getIntent().getStringExtra("type");
        sched_id = getIntent().getIntExtra("sched_id", 0);
        room_comp_id = 0;
        room_ = "";

        db = new SQLiteHandler(this);

        choose_layout = (LinearLayout) findViewById(R.id.choose_layout);
        choose = (TextView) findViewById(R.id.choose);
        choose.setFocusable(false);
        desc = (EditText) findViewById(R.id.desc);
        progress = new SpotsDialog(this, "Loading...");
        custom_date = (TextView) findViewById(R.id.custom_date);
        custom_time = (TextView) findViewById(R.id.custom_time);
        date = (Spinner) findViewById(R.id.date);
        time = (Spinner) findViewById(R.id.time);
        String[] spinner_item = new String[]{"Anytime", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinner_item);
        date.setAdapter(adapter);
        time.setAdapter(adapter);
        time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        custom_time.setVisibility(View.GONE);
                        break;
                    }
                    case 1: {
                        custom_time.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        custom_date.setVisibility(View.GONE);
                        break;
                    }
                    case 1: {
                        custom_date.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        custom_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //date picker
                DialogFragment datePicker = new DatePicker();
                datePicker.show(getSupportFragmentManager(), "date picker");

            }
        });
        custom_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //time picker
                DialogFragment timePicker = new TimePicker();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        //choose room or repair
        String[] items = new String[]{"Choose Title...", "Schedule Inventory", "Schedule Repair"};
        ArrayAdapter<String> cAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item
                , items) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        title = (Spinner) findViewById(R.id.title);
        title.setAdapter(cAdapter);
        title.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // Notify the selected item text
                    if (choose_layout.getVisibility() == View.GONE) {
                        choose_layout.setVisibility(View.VISIBLE);
                    }
                    choose.setText("");
                } else {
                    if (choose_layout.getVisibility() == View.VISIBLE)
                        choose_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (type.equalsIgnoreCase("edit")) {
            progress.show();
            progress.setCancelable(false);
            choose_layout.setVisibility(View.VISIBLE);
            title.setClickable(false);
            title.setFocusable(false);
            title.setEnabled(false);
            loadDetails();
        }

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type.equalsIgnoreCase("edit")){

                }else{
                    Intent intent = new Intent(TaskActivity.this, SearchActivity.class);
                    String string = title.getSelectedItem().toString().trim();
                    intent.putExtra("type", string);
                    startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int returnedResult = data.getIntExtra("id", 0);
                if (title.getSelectedItem().toString().trim().contains("Inventory")) {
                    room_comp_id = returnedResult;
                    getRoom(returnedResult, 0, true);
                } else {
                    room_comp_id = returnedResult;
                    getComputers(returnedResult);
                }
            }
        }
    }

    private void getComputers(final int id) {
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

                        if (comp_id == id) {
                            getRoom(room_id, pc_no, false);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("RESULT", "Error: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(TaskActivity.this).addToRequestQueue(str);
    }

    private void getRoom(final int id, final int pc_no, final boolean room) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_ROOM
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int room_id = obj.getInt("room_id");
                            String room_name = "";
                            if (obj.isNull("dept_name")) {
                                room_name = obj.getString("room_name");
                            } else {
                                room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                            }

                            if (room_id == id) {
                                if (room)
                                    choose.setText(room_name);
                                else {
                                    room_ = room_name;
                                    choose.setText("PC " + pc_no + " of " + room_);
                                }
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(TaskActivity.this, "No result", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("ROOM RESPONSE", response);
                } catch (JSONException e) {
                    Log.e("JSON ERROR 1", "RoomFragment: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volleyerror 1", "Load Room: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(TaskActivity.this).addToRequestQueue(stringRequest);
    }

    private void loadDetails() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_TASK
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {
                        Log.e(TAG, "Task got from server");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.getInt("sched_id");
                            final String obj_title = obj.getString("category");
                            String obj_desc = obj.getString("desc");
                            String obj_date = obj.getString("date");
                            String obj_time = obj.getString("time");
                            final int room_pc_id = obj.getInt("id");

                            if (sched_id == id) {
                                if (!obj_date.equalsIgnoreCase("anytime")) {
                                    date.setSelection(1);
                                    custom_date.setText(obj_date);
                                }
                                if (!obj_time.equalsIgnoreCase("anytime")) {
                                    time.setSelection(1);
                                    custom_time.setText(obj_time);
                                }
                                if (obj_title.equalsIgnoreCase("Schedule Repair"))
                                    title.setSelection(2);
                                else
                                    title.setSelection(1);

                                desc.setText(obj_desc);

                                Handler h = new Handler();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setValue(room_pc_id, obj_title);
                                        progress.dismiss();
                                    }
                                }, 5000);
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(TaskActivity.this, "No Tasks", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    progress.dismiss();
                    Toast.makeText(TaskActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                    TaskActivity.this.finish();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(TaskActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                TaskActivity.this.finish();
                Log.e(TAG, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("tech_id", SharedPrefManager.getInstance(TaskActivity.this).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(TaskActivity.this).addToRequestQueue(str);
    }

    private void setValue(int room_pc_id, String cat) {
        String value = "";
        if (cat.contains("Repair")) {
            Cursor c = db.getCompDetails(room_pc_id);
            if (c.moveToFirst()) {
                String pc_name = "PC " + c.getString(c.getColumnIndex(db.COMP_NAME));
                int room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
                String room_name = "";
                Cursor c1 = db.getRoomDetails(room_id);
                if (c1.moveToFirst()) {
                    room_name = c1.getString(c1.getColumnIndex(db.ROOMS_NAME));
                }
                value = pc_name + " of " + room_name;
            }
        } else {
            Cursor c = db.getRoomDetails(room_pc_id);
            String room_name = "";
            if (c.moveToFirst()) {
                room_name = c.getString(c.getColumnIndex(db.ROOMS_NAME));
            }
            value = room_name + " Room";
        }

        choose.setText(value);
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
                //check type if edit or add
                progress.show();
                progress.setCancelable(false);
                if (checkInput()) {
                    new processTask().execute();
                } else {
                    progress.dismiss();
                }
                break;
            }
            case R.id.cancel: {
                TaskActivity.this.finish();
            }
        }

        return true;
    }

    private void editTask() {
        String setDate = "";
        String setTime = "";
        final String description = desc.getText().toString().trim();
        if (date.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setDate = "Anytime";
        else
            setDate = custom_date.getText().toString();

        if (time.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setTime = "Anytime";
        else
            setTime = custom_time.getText().toString();

        final String query = "UPDATE task_schedule SET date = '" + setDate + "', time ='"
                + setTime + "', description = '" + description + "' WHERE sched_id = ?";

        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_UPDATE_SCHEDULE
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("ERROR", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        //update sqlite
//                        updateSqliteInventory(finalSetDate, finalSetTime, getMsg);
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TaskActivity.this, "Schedule Updated!", Toast.LENGTH_SHORT).show();
                                TaskActivity.this.finish();
                                progress.dismiss();
                            }
                        }, 5000);
                    } else
                        Toast.makeText(TaskActivity.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(TaskActivity.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("query", query);
                params.put("id", String.valueOf(sched_id));
                return params;
            }
        };
        RequestQueueHandler.getInstance(TaskActivity.this).addToRequestQueue(str);
    }

    private void addTask() {
        String setDate = "";
        String setTime = "";
        final String description = desc.getText().toString().trim();
        final String category = title.getSelectedItem().toString().trim();

        if (date.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setDate = "Anytime";
        else
            setDate = custom_date.getText().toString();

        if (time.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setTime = "Anytime";
        else
            setTime = custom_time.getText().toString();

        final String finalSetDate = setDate;
        final String finalSetTime = setTime;

        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_SAVE_TASK
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                Toast.makeText(TaskActivity.this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
                                TaskActivity.this.finish();
                            }
                        }, 3000);

                    } else {
                        progress.dismiss();
                        Toast.makeText(TaskActivity.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    progress.dismiss();
                    e.printStackTrace();
                    Log.e("RESPONSE", response);
                    Toast.makeText(TaskActivity.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(TaskActivity.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("tech_id", SharedPrefManager.getInstance(TaskActivity.this).getUserId());
                params.put("tech_name", SharedPrefManager.getInstance(TaskActivity.this).getName());
                params.put("category", category);
                params.put("description", description);
                params.put("date", finalSetDate);
                params.put("time", finalSetTime);
                params.put("room_comp_id", String.valueOf(room_comp_id));
                return params;
            }
        };
        RequestQueueHandler.getInstance(TaskActivity.this).addToRequestQueue(str);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TaskActivity.this.finish();
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date getdate = c.getTime();
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(getdate);
        custom_date.setText(dateString);
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        Date getTime = c.getTime();
        String timeString = new SimpleDateFormat("HH:mm:ss").format(getTime);
        custom_time.setText(timeString);
    }

    private boolean checkInput() {
        progress.show();
        progress.setCancelable(false);
        if (date.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                custom_date.getText().toString().equalsIgnoreCase("yyyy-mm-dd")) {
            custom_date.setError("Set date!");
            return false;
        } else if (time.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                custom_time.getText().toString().equalsIgnoreCase("HH:mm:ss")) {
            custom_time.setError("Set date!");
            return false;
        } else if (choose.getText().toString().length() == 0) {
            if (title.getSelectedItem().toString().contains("Choose"))
                Toast.makeText(this, "Select title", Toast.LENGTH_SHORT).show();
            else
                choose.setError("No selected Computer/Room");
            return false;
        } else {
            return true;
        }
    }

    private class processTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (type.equalsIgnoreCase("edit")) {
                editTask();
            } else {
                addTask();
            }
            return null;
        }
    }
}
