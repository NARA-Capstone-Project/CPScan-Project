package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestActivity extends AppCompatActivity {

    Toolbar toolbar;
    private Button submit, cancel;
    private EditText id, username, password;
    private ProgressDialog pDialog;
    private Connection_Detector connection_detector = new Connection_Detector(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request Account");
        //widgets
        submit = (Button) findViewById(R.id.submit_action);
        cancel = (Button) findViewById(R.id.cancel_action);
        id = (EditText) findViewById(R.id.user_id);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RequestActivity.this, LogInActivity.class));
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_id = id.getText().toString().trim();
                String pass = password.getText().toString().trim();
                String user = username.getText().toString().trim();
                if (checkInput()) {
                    submitRequest(user_id, user, pass);
                }
            }
        });
    }

    private void submitRequest(final String user_id, final String user, final String pass) {
        pDialog.setMessage("Requesting...");
        showDialog();

        //add request queue
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                AppConfig.URL_REQUEST_ACCOUNT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideDialog();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                String msg = jsonObject.getString("message");
                                openDialog(msg);
                            }else{
                                Toast.makeText(RequestActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON ERROR request: ", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                        Toast.makeText(getApplicationContext(), "Can't connect to the server, please try again.", Toast.LENGTH_LONG).show();
                    }
                }) {

            //ung ipapasa sa php
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", user_id);
                params.put("username", user);
                params.put("password", pass);
                return params;
            }
        };
        RequestQueueHandler.getInstance(this).addToRequestQueue(stringRequest);

    }

    private boolean checkInput() {
        int id_lth = id.getText().toString().trim().length();
        int user_lth = id.getText().toString().trim().length();
        int pass_lth = id.getText().toString().trim().length();
        if (id_lth > 0 && id_lth < 9) {
            if (user_lth > 0 && user_lth <= 16) {
                if (pass_lth > 0 && pass_lth <= 16) {
                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), "Password must be 0-16", Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Username must be 0-16 characters", Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            Toast.makeText(getApplicationContext(), "ID must be 0-8 characters", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void openDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request Sent")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearFields();
                        startActivity(new Intent(getApplicationContext(), LogInActivity.class));
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void clearFields() {
        id.setText("");
        username.setText("");
        password.setText("");
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RequestActivity.this, LogInActivity.class));
        finish();
    }

}
