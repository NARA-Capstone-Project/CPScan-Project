package com.example.avendano.cp_scan.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.avendano.cp_scan.R;

import java.io.FileInputStream;

public class ScheduleActivity extends AppCompatActivity {

    private int room_id;
    private ImageView sched;
    String image_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sched = (ImageView) findViewById(R.id.room_sched);

        room_id = getIntent().getIntExtra("room_id", 0);
        image_path = getIntent().getStringExtra("image");

        if (!image_path.isEmpty()) {
                //decode
            Bitmap bmp = null;
            String filename = getIntent().getStringExtra("image");
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                sched.setImageBitmap(bmp);
                sched.setBackgroundResource(0);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            ImageRequest req = new ImageRequest(image_path, new Response.Listener<Bitmap>() {
//                @Override
//                public void onResponse(Bitmap response) {
//                    sched.setImageBitmap(response);
//                    sched.setBackgroundResource(0);
//                }
//            }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
//                    new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            Toast.makeText(ScheduleActivity.this, "Something went wrong " +
//                                    "on loading image, check your connection", Toast.LENGTH_SHORT).show();
//                            error.printStackTrace();
//                        }
//                    });
//
//            RequestQueueHandler.getInstance(ScheduleActivity.this).addToRequestQueue(req);
        }else{
            Toast.makeText(this, "No Schedule", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here
//                Intent intent = new Intent(this, ViewRoom.class);
//                intent.putExtra("room_id", room_id);
//                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent intent = new Intent(this, ViewRoom.class);
//        intent.putExtra("room_id", room_id);
//        startActivity(intent);
        finish();
    }
}
