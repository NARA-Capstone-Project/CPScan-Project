package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Pages.Main_Page;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class LogInActivity extends AppCompatActivity {

    //initialize

    private Button login;
    private EditText user, password;
    private TextView request_acc, error_alert;
    private ProgressDialog pDialog;
    private Connection_Detector connection_detector = new Connection_Detector(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        user = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        request_acc = (TextView) findViewById(R.id.login_request);
        login = (Button) findViewById(R.id.login);
        error_alert = (TextView) findViewById(R.id.error);
        //progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pDialog.setMessage("Logging in...");
                showDialog();
                if (connection_detector.isConnected()) {
                    if (checkInput()) {
                        String username = user.getText().toString().trim();
                        String pass = password.getText().toString().trim();
                        logUser(username, pass);
                    } else {
                        hideDialog();
                    }
                }else{
                    hideDialog();
                    Toast.makeText(LogInActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        request_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, RequestActivity.class));
                finish();
            }
        });
    }

    private boolean checkInput() {

        int user_lth = user.getText().toString().trim().length();
        int pass_lth = password.getText().toString().trim().length();

        if (user_lth == 0 && pass_lth == 0) {
            user.setError("Empty Field");
            user.requestFocus();
            password.setError("Empty Field");
        } else {
            if (user_lth > 0) {
                if (pass_lth > 0) {
                    return true;
                } else {
                    password.requestFocus();
                    password.setError("Empty Field");
                    return false;
                }
            } else {
                user.setError("Empty Field");
                user.requestFocus();
                return false;
            }
        }
        return false;
    }

    private void logUser(final String username, final String password) {

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideDialog();
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        SharedPrefManager.getInstance(getApplicationContext())
                                .userLogin(obj.getString("user_id"),
                                        obj.getString("email"),
                                        obj.getString("username"),
                                        obj.getString("name"),
                                        obj.getString("phone"),
                                        obj.getString("role"),
                                        obj.getString("date_expire"),
                                        obj.getString("acc_status")
                                );

                        startActivity(new Intent(getApplicationContext(), Main_Page.class));
                        finish();
                    } else {
                        error_alert.setVisibility(View.VISIBLE);
                        String msg = obj.getString("message");
                        error_alert.setText(msg);
//                        if (obj.getString("message").contains("deactivated"))
//                            reactivateAccount(obj.getString("user_id"));
//                        else
//                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e("JSONEXCEPTION: ", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Toast.makeText(getApplicationContext(), "Can't connect to the server, please try again.", Toast.LENGTH_LONG).show();
            }
        }) {
            //POST DATA TO PHP
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        RequestQueueHandler.getInstance(this).addToRequestQueue(strReq);
    }

    private void reactivateAccount(final String user_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
        builder.setMessage("Your account has been deactivated or expired, send request to reactivate account?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        user.setText("");
                        user.setFocusable(true);
                        password.setText("");
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendReactivateAccount(user_id);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void openDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        user.setText("");
                        user.setFocusable(true);
                        password.setText("");
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendReactivateAccount(final String user_id) {
        final android.app.AlertDialog requesting = new SpotsDialog(LogInActivity.this, "Requesting...");
        requesting.show();
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_REACTIVATE_ACC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                requesting.hide();
                try {
                    JSONObject obj = new JSONObject(response);
                    openDialog(obj.getString("message"));
                } catch (JSONException e) {
                    Log.e("JSONERRORLOGIN", e.getMessage());
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requesting.hide();
                Log.e("ERRORREQUESTING", error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", user_id);
                params.put("username", user.getText().toString().trim());
                params.put("password", password.getText().toString().trim());
                return params;
            }
        };RequestQueueHandler.getInstance(LogInActivity.this).addToRequestQueue(str);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
