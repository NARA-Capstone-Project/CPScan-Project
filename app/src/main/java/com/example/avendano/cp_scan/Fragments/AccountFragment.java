package com.example.avendano.cp_scan.Fragments;


import android.app.VoiceInteractor;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.avendano.cp_scan.Activities.EditProfileActivity;
import com.example.avendano.cp_scan.Activities.LogInActivity;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    SQLiteHandler db;
    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        TextView tName = (TextView) view.findViewById(R.id.name);
        TextView tRole = (TextView) view.findViewById(R.id.role);
        TextView tAccExpire = (TextView) view.findViewById(R.id.date_expire);

        tAccExpire.setText("Account Expire on: " + SharedPrefManager.getInstance(getContext()).getAcc_Expire());
        tName.setText(SharedPrefManager.getInstance(getContext()).getName());
        tRole.setText(SharedPrefManager.getInstance(getContext()).getUserRole());

        db = new SQLiteHandler(getContext());

        final ListView listView = (ListView) view.findViewById(R.id.listview);
        String[] strings = new String[]{"Profile","Sign Out", "Deactivate"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(adapter);
        listView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position){
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
        return view;
    }

    private void promptUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
    }

    private void deactivateAccount() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_DEACTIVATE_ACC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if(!obj.getBoolean("error")){
                        Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        logout();
                    }
                    else
                        Toast.makeText(getContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e("JSONEXCEPTIONACC", e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
                Log.e("RESPONSEACCOUNT", error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(getContext()).getUserId());
                return param;
            }
        };RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }

    private void editProfileActivity() {
        startActivity(new Intent(getContext(), EditProfileActivity.class));
    }

    private void logout(){
        db = new SQLiteHandler(getContext());
        db.deleteAllComp();
        db.deleteReport();
        db.deleteReportDetails();
        db.deleteRooms();
        db.close();

        SharedPreferences pref = getActivity().getSharedPreferences("FRAGMENT", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("SELECTED", "room");
        edit.apply();

        SharedPrefManager.getInstance(getContext()).logout();
        startActivity(new Intent(getActivity(), LogInActivity.class));
        getActivity().finish();
    }
}
