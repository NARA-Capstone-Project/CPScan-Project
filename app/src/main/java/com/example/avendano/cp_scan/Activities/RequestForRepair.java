package com.example.avendano.cp_scan.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.PoolingByteArrayOutputStream;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
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
    Spinner date_type, time_type;
    int comp_id, room_id;
    Toolbar toolbar;
    ImageView photo1;
    private final int IMG_REQUEST = 1, CAMERA_REQUEST = 0;
    private Bitmap bitmap;
    private String TAG = "RequestForRepair";
    RadioGroup status;
    android.app.AlertDialog progress;
    RadioButton missing, not_working;
    boolean checkBox = false; // true missing false not working
    boolean setImage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


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
        time_type = (Spinner) findViewById(R.id.time);
        status = (RadioGroup) findViewById(R.id.comp_status);
        peripherals = (TextView) findViewById(R.id.peripherals);
        label = (TextView) findViewById(R.id.label);
        label.setVisibility(View.VISIBLE);
        missing = (RadioButton) findViewById(R.id.missing);
        missing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peripheralsDialog();
            }
        });
        not_working = (RadioButton) findViewById(R.id.not_working);
        not_working.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peripheralsDialog();
            }
        });
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
                        if (peripherals.getText().toString().isEmpty()) {
                            missing.setChecked(false);
                            not_working.setChecked(false);
                        } else {
                            if (checkBox) {
                                not_working.setChecked(false);
                                missing.setChecked(true);
                            } else {
                                missing.setChecked(false);
                                not_working.setChecked(true);
                            }
                        }
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
                            if (missing.isChecked())
                                checkBox = true;
                            else
                                checkBox = false;
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

        if (time_type.getSelectedItem().toString().equalsIgnoreCase("anytime"))
            setTime = "Anytime";
        else
            setTime = time.getText().toString();

        final String add_msg = message.getText().toString().trim();
        String rep_msg = "";
        if (missing.isChecked())
            rep_msg = missing.getText().toString().trim() + " " + peripherals.getText().toString().trim();
        else
            rep_msg = not_working.getText().toString().trim() + " " + peripherals.getText().toString().trim();

        String image = "";
        if (setImage)
            image = imageToString();


        String tech_id = "";
        Cursor c = db.getRoomDetails(room_id);
        if (c.moveToFirst()) {
            tech_id = c.getString(c.getColumnIndex(db.COLUMN_TECH_ID));
        }

        final String finalRep_msg = rep_msg;
        final String finalSetTime = setTime;
        final String finalSetDate = setDate;
        final String finalTech_id = tech_id;

        final String finalImage = image;
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_REQUEST_REPAIR
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("RESPONSE", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Log.e("IMAGE", obj.getString("image"));
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                Toast.makeText(RequestForRepair.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                                gotoViewPc();
                            }
                        }, 5000);
                    } else {
                        Toast.makeText(RequestForRepair.this, "An error occured", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    progress.dismiss();
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Log.e("ERROR", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("comp_id", String.valueOf(comp_id));
                params.put("msg", add_msg);
                params.put("cust_id", SharedPrefManager.getInstance(RequestForRepair.this).getUserId());
                params.put("tech_id", finalTech_id);
                params.put("date", finalSetDate);
                params.put("time", finalSetTime);
                params.put("image", finalImage);
                params.put("rep_details", finalRep_msg);
                return params;
            }
        };
        RequestQueueHandler.getInstance(RequestForRepair.this).addToRequestQueue(str);
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
        } else if (time_type.getSelectedItem().toString().equalsIgnoreCase("custom") &&
                time.getText().toString().equalsIgnoreCase("HH:mm:ss")) {
            time.setError("Set date!");
            return false;
        } else if (peripherals.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(this, "Select computer status and peripherals", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}
