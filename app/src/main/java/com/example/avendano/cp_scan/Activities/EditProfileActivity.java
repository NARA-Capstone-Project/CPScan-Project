package com.example.avendano.cp_scan.Activities;

import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EditProfileActivity extends AppCompatActivity {

    String[] titles = {"Name","Username", "Password", "Signature"};
    String[] userData = {SharedPrefManager.getInstance(EditProfileActivity.this).getName()
    , SharedPrefManager.getInstance(EditProfileActivity.this).getKeyUsername(),"",""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView list = (ListView) findViewById(R.id.listview);
        ProfileAdapter adapter = new ProfileAdapter();
        list.setAdapter(adapter);
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
            EditText user = (EditText) convertView.findViewById(R.id.userData);
            if(title.getText().toString().trim().equalsIgnoreCase("password")){
                user.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                user.setTransformationMethod(PasswordTransformationMethod.getInstance());
                user.setHint(getKeyPassword());
            }
            else{
                user.setHint(userData[position]);
                user.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            final int item = position;
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String getTitle = titles[item].toString();
                    //dialog
                    Toast.makeText(EditProfileActivity.this, getTitle, Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;
        }
    }

    private String getKeyPassword() {
        String decoded = "";
        try {
            decoded = decrypt(SharedPrefManager.getInstance(EditProfileActivity.this).getEncrypted(), "userdataencrypted");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decoded;
    }

    private String decrypt(String password, String Data) throws Exception{
        String AES = "AES";
        SecretKeySpec key = SharedPrefManager.getInstance(EditProfileActivity.this).generateKey(Data);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decVal = Base64.decode(password, Base64.DEFAULT);
        byte[] decoded = cipher.doFinal(decVal);
        String decryptedValue = new String(decoded);
        return decryptedValue;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EditProfileActivity.this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                EditProfileActivity.this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class PopupDialog extends DialogFragment{

    }
}
