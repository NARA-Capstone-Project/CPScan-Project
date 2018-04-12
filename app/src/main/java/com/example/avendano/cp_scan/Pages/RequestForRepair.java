package com.example.avendano.cp_scan.Pages;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.DatePicker;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.example.avendano.cp_scan.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 25 Mar 2018.
 */

public class RequestForRepair extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    SQLiteHandler db;
    EditText message;
    TextView date, time, peripherals, label;
    Spinner date_type;
    int comp_id, room_id;
    Toolbar toolbar;
    ImageView photo1;
    private final int IMG_REQUEST = 1, CAMERA_REQUEST = 0;
    private Bitmap bitmap;
    private String TAG = "RequestForRepair";
    android.app.AlertDialog progress;
    boolean checkBox = false; // true missing false not working
    boolean setImage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Request Repair");

        comp_id = getIntent().getIntExtra("comp_id", 0);
        room_id = getIntent().getIntExtra("room_id", 0);
        db = new SQLiteHandler(this);

        progress = new SpotsDialog(this, "Requesting...");
        message = (EditText) findViewById(R.id.message);
        date = (TextView) findViewById(R.id.custom_date);
        time = (TextView) findViewById(R.id.custom_time);
        photo1 = (ImageView) findViewById(R.id.photo1);
        photo1.setVisibility(View.VISIBLE);
        date_type = (Spinner) findViewById(R.id.date);
        peripherals = (TextView) findViewById(R.id.peripherals);
        label = (TextView) findViewById(R.id.label);
        label.setVisibility(View.VISIBLE);
        peripherals.setVisibility(View.VISIBLE);
        peripherals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peripheralsDialog();
            }
        });
        String[] type = new String[]{"Anytime", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, type);
        date_type.setAdapter(adapter);
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

        photo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //photo
                showDialog();
            }
        });

    }

    private void peripheralsDialog() {
        final ArrayList<String> checked = new ArrayList<>();
        final String[] items = {"Monitor", "Keyboard", "Mouse", "System Unit"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Peripherals...")
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked)
                            checked.add(items[which]);
                        else if (checked.contains(items[which]))
                            checked.remove(items[which]);
                    }
                })
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checked.size() > 0) {
                            String content = "";
                            for (int i = 0; i < checked.size(); i++) {
                                content = "" + content + " > " + checked.get(i).toString().trim();
                            }
                            peripherals.setText(content);
                            peripherals.setVisibility(View.VISIBLE);
                            alert.dismiss();
                        } else {
                            Toast.makeText(RequestForRepair.this, "No selected peripherals", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        alert.show();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Action...");
        String[] items = {"Camera", "Gallery"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        //capture image
                        captureImage();
                        break;
                    }
                    case 1: {
                        selectImage();
                        break;
                    }
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            if (requestCode == CAMERA_REQUEST) {
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                photo1.setImageBitmap(bitmap);
                photo1.setBackgroundResource(0);
                setImage = true;
            } else if (requestCode == IMG_REQUEST) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                    photo1.setImageBitmap(bitmap);
                    photo1.setBackgroundResource(0);
                    setImage = true;
                } catch (Exception e) {
                    Log.e(TAG, "bitmap: " + e.getMessage());
                }
            }
        }
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
                //send sms and save
                //check time and date
                //check peripherals and status
                //check image
                if (checkInput())
                    requestRepair();
                else
                    progress.dismiss();
                break;
            }

            case R.id.cancel: {
                gotoViewPc();
            }
        }

        return true;
    }

    private void requestRepair() {
        String setDate = "";
        String setTime = "";

        if (date_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setDate = "Anytime";
        else
            setDate = date.getText().toString();

        setTime = time.getText().toString();

        final String add_msg = message.getText().toString().trim();
        String rep_msg = peripherals.getText().toString().trim();

        String image = "";
        if (setImage)
            image = imageToString();

        final String finalRep_msg = rep_msg;
        final String finalSetTime = setTime;
        final String finalSetDate = setDate;

        final String finalImage = image;

        String date_req = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String time_req = new SimpleDateFormat("HH:mm:ss").format(new Date());


        Map<String, String> params = new HashMap<>();
        params.put("comp_id", String.valueOf(comp_id));
        params.put("message", add_msg);
        params.put("set_date", finalSetDate);
        params.put("set_time", finalSetTime);
        params.put("date_req", date_req);
        params.put("time_req", time_req);
        params.put("image", finalImage);
        params.put("rep_details", finalRep_msg);

        VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        volley.sendStringRequestPost(AppConfig.SAVE_REQ_REPAIR, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String response) {
                Log.e("RESPONSe", response);
                try{
                    JSONObject obj = new JSONObject(response);
                    if(!obj.getBoolean("error")){
                        Log.e("Id", " " + obj.getInt("id"));
                        Toast.makeText(RequestForRepair.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                        gotoViewPc();
                    }else{
                        Toast.makeText(RequestForRepair.this, "An Error occurred, pleaase try again later", Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                }catch (Exception e){
                    Toast.makeText(RequestForRepair.this, "An Error occurred, pleaase try again later", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RequestForRepair.this, "Can't connect to the server, pleaase try again later", Toast.LENGTH_SHORT).show();
                progress.dismiss();
                error.printStackTrace();
            }
        }, params);
    }

    private String imageToString() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imgBytes = outputStream.toByteArray();

        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gotoViewPc();
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
        String timeString = new SimpleDateFormat("HH:mm:ss").format(getTime);
        time.setText(timeString);
    }

    private void gotoViewPc() {
        Intent intent = new Intent(RequestForRepair.this, ViewPc.class);
        intent.putExtra("comp_id", comp_id);
        startActivity(intent);
        finish();
    }

    private boolean checkInput() {
        progress.show();
        progress.setCancelable(false);
        if (date_type.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                date.getText().toString().equalsIgnoreCase("yyyy-mm-dd")) {
            date.setError("Set date!");
            return false;
        } else if (time.getText().toString().equalsIgnoreCase("HH:mm:ss")) {
            time.setError("Set date!");
            return false;
        } else if (peripherals.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(this, "Select peripherals", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}