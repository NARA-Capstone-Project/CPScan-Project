package com.example.avendano.cp_scan.Activities;

import android.content.SharedPreferences;
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

import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.example.avendano.cp_scan.AccountShowDialog;

public class EditProfileActivity extends AppCompatActivity {

    String[] titles = { "Phone","Name", "Username", "Password", "Signature"};
    String[] userData = {SharedPrefManager.getInstance(EditProfileActivity.this).getUserPhone(),
            SharedPrefManager.getInstance(EditProfileActivity.this).getName()
            , SharedPrefManager.getInstance(EditProfileActivity.this).getKeyUsername(), "" ,""};
    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new SQLiteHandler(EditProfileActivity.this);

        ListView list = (ListView) findViewById(R.id.listview);
        ProfileAdapter adapter = new ProfileAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //check kung ung signature ung nakiclick
                if (position == 4) {
                    //canvas to capture signature
                }else if(position == 3){ // update password
                    AccountShowDialog dialog = new AccountShowDialog(titles[position], "");
                    dialog.show(getSupportFragmentManager(), "");
                }
            }
        });
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
            if (title.getText().toString().equalsIgnoreCase("signature") ||
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
