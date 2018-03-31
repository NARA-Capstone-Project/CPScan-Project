package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.example.avendano.cp_scan.DatePicker;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dmax.dialog.SpotsDialog;

public class TaskActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

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


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //intent
        type = getIntent().getStringExtra("type");
        sched_id = getIntent().getIntExtra("sched_id", 0);

        choose_layout = (LinearLayout) findViewById(R.id.choose_layout);
        choose = (TextView) findViewById(R.id.choose);
        desc = (EditText) findViewById(R.id.desc);
        progress = new SpotsDialog(this, "Loading...");
        custom_date = (TextView) findViewById(R.id.custom_date);
        custom_time = (TextView) findViewById(R.id.custom_time);
        date = (Spinner) findViewById(R.id.date);
        time = (Spinner) findViewById(R.id.time);
        String[] type = new String[]{"Anytime", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, type);
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
        , items){
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
                    if(choose_layout.getVisibility() == View.GONE)
                        choose_layout.setVisibility(View.VISIBLE);
                }else{
                    if(choose_layout.getVisibility() == View.VISIBLE)
                        choose_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                //check type if edit or add
                break;
            }

            case R.id.cancel: {
                TaskActivity.this.finish();
            }
        }

        return true;
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
        } else {
            return true;
        }
    }
}
