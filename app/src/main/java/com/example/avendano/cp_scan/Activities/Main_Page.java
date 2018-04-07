package com.example.avendano.cp_scan.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.avendano.cp_scan.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main_Page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__page);

        String date = new SimpleDateFormat("M dd, yyyy").format(new Date());
        String time = new SimpleDateFormat("HH:mm a").format(new Date());

        TextView setDate = findViewById(R.id.date);
        setDate.setText(date + " " + time);
    }
}
