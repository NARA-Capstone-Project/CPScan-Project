package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    String type, image_path;
    SQLiteHandler db;
    EditText message;
    TextView date, time, peripherals, label;
    Spinner date_type, time_type;
    Toolbar toolbar;
    AlertDialog progress;
    ImageView photo1;
    RadioGroup status;
    RadioButton missing, not_working;
    private final int IMG_REQUEST = 1, CAMERA_REQUEST = 0;
    private Bitmap bitmap;
    boolean checkBox = false; // true missing false not working
    boolean setImage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_date_time);

        id = getIntent().getIntExtra("id", 0);  //req_id
        room_pc_id = getIntent().getIntExtra("room_pc_id", 0);  //comp or room id
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

        //FOR REPAIR

        photo1 = (ImageView) findViewById(R.id.photo1);
        status = (RadioGroup) findViewById(R.id.comp_status);
        peripherals = (TextView) findViewById(R.id.peripherals);
        label = (TextView) findViewById(R.id.label);
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
        image_path = "";

        photo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //photo
                showDialog();
            }
        });

        setDetails();

    }

    private void showDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
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
        android.support.v7.app.AlertDialog alert = builder.create();
        alert.show();
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
                    Log.e("REPAIR123", "bitmap: " + e.getMessage());
                }
            }
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void peripheralsDialog() {
        final ArrayList<String> checked = new ArrayList<>();
        final String[] items = {"Monitor", "Keyboard", "Mouse", "System Unit"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
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
        final android.support.v7.app.AlertDialog alert = builder.create();
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
                            Toast.makeText(EditRequestSchedule.this, "No selected peripherals", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        alert.show();
    }

    private void setDetails() {
        //set time and date image(kung repair)
        //check kung repair or inventory
        ///if cant connect sa server babalik tas toast ng cant connect
        class loadDetails extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if (type.equalsIgnoreCase("inventory"))
                    getReqInvDetails();
                else if (type.equalsIgnoreCase("repair")) {
                    peripherals.setVisibility(View.VISIBLE);
                    photo1.setVisibility(View.VISIBLE);
                    status.setVisibility(View.VISIBLE);
                    label.setVisibility(View.VISIBLE);
                    getReqRepDetails();
                }
                return null;
            }
        }

        new loadDetails().execute();
    }

    private void getReqRepDetails() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_ALL_REPAIR_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int req_id = obj.getInt("req_id");
                        int rep_id = 0;
                        if (!obj.isNull("rep_id"))
                            rep_id = obj.getInt("rep_id");
                        int comp_id = obj.getInt("comp_id");
                        String cust_id = obj.getString("custodian");
                        String tech_id = obj.getString("technician");
                        String date = obj.getString("date");
                        String time = obj.getString("time");
                        String msg = obj.getString("msg");
                        String req_details = obj.getString("req_details");
                        String path = obj.getString("image");

                        if (req_id == id) {
                            if (!date.equalsIgnoreCase("anytime")) {
                                date_type.setSelection(1);
                                EditRequestSchedule.this.date.setText(date);
                            }
                            if (!time.equalsIgnoreCase("anytime")) {
                                time_type.setSelection(1);
                                EditRequestSchedule.this.time.setText(time);
                            }
                            message.setText(msg);
                            String new_details = "";
                            if (req_details.contains("Missing")) {
                                new_details = req_details.replace("Missing", "");
                                missing.setChecked(true);
                                checkBox = true;
                            } else if (req_details.contains("Not Working")) {
                                new_details = req_details.replace("Not Working", "");
                                not_working.setChecked(true);
                                checkBox = false;
                            }
                            peripherals.setText(new_details.trim());
                            if (obj.isNull("image"))
                                image_path = "";
                            else
                                image_path = AppConfig.ROOT_URL + path;

                            Log.e("PATH", path + " IMAGE: " + image_path);
                            break;
                        }

                    }
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("IMAGE" , image_path);
                            if (image_path.length() != 0)
                                getImage();
                            progress.dismiss();
                        }
                    }, 5000);

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
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("comp_id", String.valueOf(room_pc_id));
                return param;
            }
        };
        RequestQueueHandler.getInstance(EditRequestSchedule.this).addToRequestQueue(str);

    }

    private void getImage() {
        ImageRequest req = new ImageRequest(image_path, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                photo1.setImageBitmap(response);
                photo1.setBackgroundResource(0);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EditRequestSchedule.this, "Something went wrong " +
                                "in loading image", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });

        RequestQueueHandler.getInstance(EditRequestSchedule.this).addToRequestQueue(req);
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

                        if (req_id == id) {
                            if (!date.equalsIgnoreCase("anytime")) {
                                date_type.setSelection(1);
                                EditRequestSchedule.this.date.setText(date);
                            }
                            if (!time.equalsIgnoreCase("anytime")) {
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
        });
        RequestQueueHandler.getInstance(EditRequestSchedule.this).addToRequestQueue(str);
    }

    private boolean checkSchedule() {
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

    private void goToViewPc() {
        int room_id = 0;
        Intent intent = new Intent(EditRequestSchedule.this, ViewPc.class);
        intent.putExtra("comp_id", room_pc_id);
        //search comp id by req_id
        Cursor c = db.getCompDetails(room_pc_id);
        if (c.moveToFirst()) {
            room_id = c.getInt(c.getColumnIndex(db.ROOMS_ID));
        }
        intent.putExtra("room_id", room_id);
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
                progress.show();
                progress.setCancelable(false);
                if (checkSchedule()) {
                    new editRequests().execute();
                } else {
                    progress.dismiss();
                }
                Log.w("SEND REQUEST", "User request for inventory");
                break;
            }
            case R.id.cancel: {
                if (type.equalsIgnoreCase("inventory"))
                    goToViewRoom();
                else
                    goToViewPc();
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
        if (type.equalsIgnoreCase("inventory"))
            goToViewRoom();
    }

    private class editRequests extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
            progress.setCancelable(false);
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

        final String finalSetDate = setDate;
        final String finalSetTime = setTime;

        String rep_msg = "";
        if (missing.isChecked())
            rep_msg = missing.getText().toString().trim() + " " + peripherals.getText().toString().trim();
        else
            rep_msg = not_working.getText().toString().trim() + " " + peripherals.getText().toString().trim();

        String image = "";
        if (setImage)
            image = imageToString();

        final String finalImage = image;
        final String finalRep_msg = rep_msg;
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_UPDATE_REPAIR_REQUEST
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("ERROR", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        //update sqlite
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                updateReqRepDetails(id, finalSetDate, finalSetTime, getMsg, finalRep_msg);
                                Toast.makeText(EditRequestSchedule.this, "Request Updated!", Toast.LENGTH_SHORT).show();
                                progress.dismiss();
                                goToViewPc();
                            }
                        }, 5000);
                    } else
                        Toast.makeText(EditRequestSchedule.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(EditRequestSchedule.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Toast.makeText(EditRequestSchedule.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("msg", getMsg);
                params.put("date", finalSetDate);
                params.put("time", finalSetTime);
                params.put("rep_details", finalRep_msg);
                params.put("old_path", image_path);
                params.put("image", finalImage);
                params.put("req_id", String.valueOf(id));
                return params;
            }
        };
        RequestQueueHandler.getInstance(EditRequestSchedule.this).addToRequestQueue(str);
    }

    private void updateReqRepDetails(int id, String finalSetDate, String finalSetTime, String getMsg, String finalRep_msg) {
        db.updateReqRepairDetails(id, finalSetDate,finalSetTime, getMsg,finalRep_msg);
    }


    private String imageToString() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imgBytes = outputStream.toByteArray();

        return Base64.encodeToString(imgBytes, Base64.DEFAULT);
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

        final String finalSetDate = setDate;
        final String finalSetTime = setTime;

        final String query = "UPDATE request_inventory SET date = '" + setDate + "', time ='"
                + setTime + "', message = '" + getMsg + "' WHERE req_id = ?";

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
                    } else
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
        }) {
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
