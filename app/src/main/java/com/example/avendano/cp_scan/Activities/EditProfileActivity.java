package com.example.avendano.cp_scan.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.example.avendano.cp_scan.AccountShowDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    String[] titles = { "Phone","Name","Email", "Username", "Password", "Add Signature"};
    String[] userData = {SharedPrefManager.getInstance(EditProfileActivity.this).getUserPhone(),
            SharedPrefManager.getInstance(EditProfileActivity.this).getName()
            , SharedPrefManager.getInstance(EditProfileActivity.this).getEmail()
            , SharedPrefManager.getInstance(EditProfileActivity.this).getKeyUsername(), "" ,""};
    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        db = new SQLiteHandler(EditProfileActivity.this);

        ListView list = (ListView) findViewById(R.id.listview);
        ProfileAdapter adapter = new ProfileAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //check kung ung signature ung nakiclick
                if (position == 5) {
                    //canvas to capture signature
                    Intent intent = new Intent(EditProfileActivity.this, SignatureActivity.class);
                    intent.putExtra("from", "profile");
                    startActivity(intent);
                    finish();
                }else if(position == 4){ // update password
                    AccountShowDialog dialog = new AccountShowDialog(titles[position], "");
                    dialog.show(getSupportFragmentManager(), "");
                }
            }
        });
        checkSignature();
    }

    private void checkSignature() {
        VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        Map<String, String> param = new HashMap<>();
        param.put("user_id", SharedPrefManager.getInstance(this).getUserId());

        volley.sendStringRequestPost(AppConfig.GET_USER_INFO
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            Log.e("SIGN", response);
                            JSONObject obj = new JSONObject(response);
                            if (!obj.isNull("signature")) {
                                List<String> list = new ArrayList<String>(Arrays.asList(titles));
                                int idx = list.indexOf("Add Signature");
                                list.remove(idx);
                                titles = list.toArray(new String[list.size()]);

                                List<String> data = new ArrayList<String>(Arrays.asList(userData));
                                data.remove(idx);
                                userData = data.toArray(new String[data.size()]);
                            }
                        } catch (JSONException e) {
                            Log.e("SIGN", response);
                            e.printStackTrace();
                            Toast.makeText(EditProfileActivity.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError)
                            Toast.makeText(EditProfileActivity.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(EditProfileActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }, param);

    }

    class ProfileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(titles[position]);
            final EditText user = (EditText) convertView.findViewById(R.id.userData);
            if (title.getText().toString().equalsIgnoreCase("add signature") ||
                    title.getText().toString().trim().equalsIgnoreCase("password")) {
                user.setVisibility(View.GONE);
                ImageView image = convertView.findViewById(R.id.arrow);
                image.setVisibility(View.VISIBLE);
            } else {
                user.setText(userData[position]);
                user.setInputType(InputType.TYPE_CLASS_TEXT);
            }

            final int item = position;
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.w("EDITTEXT", "Clicked!");
                    String title = titles[item];
                    AccountShowDialog dialog = new AccountShowDialog(title, user.getText().toString().trim());
                    //custom dialog
                    dialog.show(getSupportFragmentManager(), "");
                }
            });
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EditProfileActivity.this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                EditProfileActivity.this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }


}
