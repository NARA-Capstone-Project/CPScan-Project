package com.example.avendano.cp_scan.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;

/**
 * Created by Avendano on 25 Mar 2018.
 */

public class RequestForInventory extends AppCompatActivity {
    SQLiteHandler db;
    EditText message;
    TextView date, time;
    Spinner date_type, time_type;
    int room_id;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

//        room_id = getIntent().getIntExtra("room_id", 0);
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
                switch (position){
                    case 0: {
                        time.setVisibility(View.GONE);
                        break;
                    }
                    case 1:{
                        //time picker

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
                switch (position){
                    case 0: {
                        time.setVisibility(View.GONE);
                        break;
                    }
                    case 1:{
                        //time picker

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.assess_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:{
                //send sms and save
                Toast.makeText(this, "SAVE REQUEST", Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.cancel:{
                RequestForInventory.this.finish();
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RequestForInventory.this.finish();
    }
}
