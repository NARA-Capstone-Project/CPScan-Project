package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Avendano on 8 Apr 2018.
 */

public class ProfileActivity extends AppCompatActivity {
    SQLiteHandler db;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account");

        TextView tName = (TextView) findViewById(R.id.name);
        TextView tRole = (TextView) findViewById(R.id.role);
        TextView tAccExpire = (TextView) findViewById(R.id.date_expire);

        tAccExpire.setText("Account Expire on: " + SharedPrefManager.getInstance(ProfileActivity.this).getAcc_Expire());
        tName.setText(SharedPrefManager.getInstance(ProfileActivity.this).getName());
        tRole.setText(SharedPrefManager.getInstance(ProfileActivity.this).getUserRole());

        db = new SQLiteHandler(ProfileActivity.this);

        final ListView listView = (ListView) findViewById(R.id.listview);
        String[] strings = new String[]{"Profile", "Sign Out", "Deactivate"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        editProfileActivity();
                        break;
                    case 1:
                        logout();
                        break;
                    case 2:
                        promptUser();
                        break;
                }
            }
        });
    }

    private void promptUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setMessage("Are you sure you want to deactivate your account?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deactivateAccount();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deactivateAccount() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_DEACTIVATE_ACC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Toast.makeText(ProfileActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        logout();
                    } else
                        Toast.makeText(ProfileActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e("JSONEXCEPTIONACC", e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ProfileActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                Log.e("RESPONSEACCOUNT", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(ProfileActivity.this).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(ProfileActivity.this).addToRequestQueue(str);
    }

    private void editProfileActivity() {
        startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
    }

    private void logout() {
//        db = new SQLiteHandler(ProfileActivity.this);
//        db.deleteAllComp();
//        db.deleteReport();
//        db.deleteReportDetails();
//        db.deleteRooms();
//        db.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
        .setMessage("Are you sure you want to sign out?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefManager.getInstance(ProfileActivity.this).logout();
                startActivity(new Intent(ProfileActivity.this, LogInActivity.class));
                finish();
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent i = new Intent(this, Main_Page.class);
                startActivity(i);
                ProfileActivity.this.finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, Main_Page.class);
        startActivity(i);
        ProfileActivity.this.finish();
    }
}
