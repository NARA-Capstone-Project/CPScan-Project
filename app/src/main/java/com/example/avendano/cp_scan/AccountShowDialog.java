package com.example.avendano.cp_scan;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Activities.LogInActivity;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

/**
 * Created by Avendano on 19 Mar 2018.
 */

@SuppressLint("ValidFragment")
public class AccountShowDialog extends AppCompatDialogFragment {
    String current_data, data_to_change;
    SQLiteHandler db;
    EditText current;
    EditText new_data;
    EditText confirmation;
    android.app.AlertDialog dialog;

    public AccountShowDialog(String data_to_change, String current_data) {
        this.data_to_change = data_to_change;
        this.current_data = current_data;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        db = new SQLiteHandler(getContext());
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.edit_profile_dialog, null);

        dialog = new SpotsDialog(getContext(), "Updating...");

        current = (EditText) v.findViewById(R.id.current_data);
        new_data = (EditText) v.findViewById(R.id.new_data);
        confirmation = (EditText) v.findViewById(R.id.confirm_data);
        Button save = (Button) v.findViewById(R.id.save);
        Button cancel = (Button) v.findViewById(R.id.cancel);

        if (data_to_change.equalsIgnoreCase("phone"))
            new_data.setFilters(new InputFilter[]{
                    new InputFilter.LengthFilter(11)
            });
        if (data_to_change.equalsIgnoreCase("password")) {
            current.setVisibility(View.GONE);
            current.setText("Current " + data_to_change + ":");
            new_data.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            new_data.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            current.setText("Current " + data_to_change + ": " + current_data);
            current.setFocusable(false);
        }

        new_data.setHint("New " + data_to_change);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmation.getText().toString().trim().length() == 0) {
                    confirmation.setError("Empty Field");
                } else if (new_data.getText().toString().length() == 0) {
                    new_data.setError("Empty Field");
                } else if (current_data.equals(new_data.getText().toString().trim()))
                    Toast.makeText(getContext(), "New " + data_to_change + " is same as your current " + data_to_change
                            , Toast.LENGTH_LONG).show();
                else if(current.isShown())
                    Log.w("CURRENT", "ISSHOWN");
                else
                    validate(confirmation.getText().toString().trim(), new_data.getText().toString().trim(), data_to_change);
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountShowDialog.this.dismiss();
            }
        });
        alertDialog.setView(v);
        alertDialog.setTitle("Change " + data_to_change);

        return alertDialog.create();
    }

    private void validate(String password, String new_data, String data_to_change) {
        String table = "";
        if (data_to_change.equalsIgnoreCase("name") ||
                data_to_change.equalsIgnoreCase("phone"))
            table = "users";
        else
            table = "accounts";
        new EditProfile().execute(table.trim(), data_to_change.trim(), new_data.trim(),
                SharedPrefManager.getInstance(getContext()).getUserId().trim(), password.trim());

    }

    class EditProfile extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            // table, new_data, column, user id
            String table = strings[0];
            String column = strings[1].toLowerCase();
            String new_data = strings[2];
            String user_id = strings[3];
            String password = strings[4];

            Log.w("PARAMS", "table: " + table + " col: " + column + " new_data: " + new_data +
                    " user_id: " + user_id + " password " + password);

            updateUserData(table, column, new_data, user_id, password);

            return null;
        }

    }

    private void updateUserData(final String table, final String column, final String new_data, final String user_id, final String password) {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_EDIT_PROFILE
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.e("RESPONSE", response.toString());
                    JSONObject obj = new JSONObject(response);
                    dialog.dismiss();
                    if (!obj.getBoolean("error")) {
                        Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        logout();
                    } else {
                        Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Error occured, please try again later", Toast.LENGTH_SHORT).show();
                    Log.e("EDITERROR", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Toast.makeText(getContext(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", user_id);
                params.put("table", table);
                params.put("column", column);
                params.put("new_data", new_data);
                params.put("password", password);
                params.put("username", SharedPrefManager.getInstance(getContext()).getKeyUsername());
                return params;
            }
        };
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }


    private void logout() {
        db.deleteAllComp();
        db.deleteReport();
        db.deleteReportDetails();
        db.deleteRooms();
        db.close();
        SharedPrefManager.getInstance(getContext()).logout();
        startActivity(new Intent(getContext(), LogInActivity.class));
        getActivity().finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }
}
