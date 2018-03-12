package com.example.avendano.cp_scan.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.avendano.cp_scan.Adapter.ScheduleAdapter;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Getter_Setter.RoomSchedule;
import com.example.avendano.cp_scan.R;

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
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
                        loadSchedule("Sunday");
                        break;
                    }
                    case 1: {
                        loadSchedule("Monday");
                        break;
                    }
                    case 2: {
                        loadSchedule("Tuesday");
                        break;
                    }
                    case 3: {
                        loadSchedule("Wednesday");
                        break;
                    }
                    case 4: {
                        loadSchedule("Thursday");
                        break;
                    }
                    case 5: {
                        loadSchedule("Friday");
                        break;
                    }
                    case 6: {
                        loadSchedule("Saturday");
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

    private void loadSchedule(String day) {
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
