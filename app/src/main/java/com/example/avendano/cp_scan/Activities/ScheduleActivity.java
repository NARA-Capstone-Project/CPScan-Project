package com.example.avendano.cp_scan.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.ScheduleAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.RoomSchedule;
import com.example.avendano.cp_scan.Pages.ViewRoom;
import com.example.avendano.cp_scan.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {

    private int room_id;
    RecyclerView rView;
    private List<RoomSchedule> scheduleList;
    ScheduleAdapter schedAdapter;
    LinearLayout container;
    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new SQLiteHandler(this);

        rView = (RecyclerView) findViewById(R.id.sched_recycler);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(new LinearLayoutManager(this));

        scheduleList = new ArrayList<>();

        container = (LinearLayout) findViewById(R.id.container);

        room_id = getIntent().getIntExtra("room_id", 0);
        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String[] days = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, days);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        new LoadSchedule().execute("server", "Sunday");
                        break;
                    }
                    case 1: {
                        new LoadSchedule().execute("server", "Monday");
                        break;
                    }
                    case 2: {
                        new LoadSchedule().execute("server", "Tuesday");
                        break;
                    }
                    case 3: {
                        new LoadSchedule().execute("server", "Wednesday");
                        break;
                    }
                    case 4: {
                        new LoadSchedule().execute("server", "Thursday");
                        break;
                    }
                    case 5: {
                        new LoadSchedule().execute("server", "Friday");
                        break;
                    }
                    case 6: {
                        new LoadSchedule().execute("server", "Saturday");
                        break;
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    class LoadSchedule extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... voids) {
            String method = voids[0];
            String day = voids[1];
            if(method.equals("server"))
                LoadFromServer(day);
            else
                loadScheduleFromLocal(day);
            return null;
        }
    }

    private void loadScheduleFromLocal(String day) {
        scheduleList.clear();
        Cursor c = db.getRoomSched(room_id);
        if (c.moveToFirst()) {
            do {
                String to = c.getString(c.getColumnIndex(db.SCHED_TO_TIME));
                String from = c.getString(c.getColumnIndex(db.SCHED_FROM_TIME));
                String prof = c.getString(c.getColumnIndex(db.ROOMS_CUSTODIAN));
                String getDay = c.getString(c.getColumnIndex(db.SCHED_DAY));

                if(day.equalsIgnoreCase(getDay)){
                    RoomSchedule sched = new RoomSchedule(from, to, prof);
                    scheduleList.add(sched);
                    break;
                }else{
                    Log.w("SCHED DETAILS", "GetDay " + getDay );
                    Log.w("SCHED DETAILS", "to " + to );
                    Log.w("SCHED DETAILS", "from " + from );
                    Log.w("SCHED DETAILS", "cust " + prof );
                }
            } while (c.moveToNext());
            schedAdapter = new ScheduleAdapter(ScheduleActivity.this, scheduleList);
            rView.setAdapter(schedAdapter);
        }else{
            Toast.makeText(this, "No Schedule", Toast.LENGTH_SHORT).show();
        }
    }

    private void LoadFromServer(final String spinner_day){
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_ROOM_SCHED
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for(int i=0; i< array.length();i++){
                        JSONObject obj = array.getJSONObject(i);
                        //room_id room name(dept + room no) custodian, custodian id technician technician id
                        //building, floor
                        int id = obj.getInt("room_id");
                        String prof = obj.getString("room_user");
                        String day = obj.getString("day");
                        String to_time = obj.getString("to");
                        String from_time = obj.getString("from");

                        if(room_id == id){
                            if(spinner_day.equalsIgnoreCase(day)){
                                RoomSchedule sched = new RoomSchedule(from_time, to_time, prof);
                                scheduleList.add(sched);
                                break;
                            }
                        }
                    }
                    schedAdapter = new ScheduleAdapter(ScheduleActivity.this, scheduleList);
                    rView.setAdapter(schedAdapter);
                } catch (JSONException e) {
                    new LoadSchedule().execute("server", spinner_day);
                    Log.w("RESULT SCHED", "Error: "+ e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new LoadSchedule().execute("server", spinner_day);
                Log.w("RESULT SCHED", "Error: "+ error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(ScheduleActivity.this).addToRequestQueue(str);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
                Intent intent = new Intent(this, ViewRoom.class);
                intent.putExtra("room_id", room_id);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, ViewRoom.class);
        intent.putExtra("room_id", room_id);
        startActivity(intent);
        finish();
    }
}
