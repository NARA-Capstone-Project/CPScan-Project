package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.R;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class PeripheralsIssuance extends AppCompatActivity {

    int req_id;

    ListView listView;
    ArrayList<String> peripherals = new ArrayList<>();
    ArrayList<Integer> quantity = new ArrayList<>();
    ArrayList<Integer> issued = new ArrayList<>();

    android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripherals_issuance);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request Peripherals");

        listView = (ListView) findViewById(R.id.peripherals);

        req_id = getIntent().getIntExtra("req_id", 0);
        if (req_id != 0) {
            //load
            getPeripherals();
        } else {
            this.finish();
        }
    }

    private void getPeripherals() {
        class loadPeripherals extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                Map<String, String> param = new HashMap<>();
                param.put("req_id", String.valueOf(req_id));
                String response = con.sendPostRequest(AppConfig.GET_PERIPHERALS_DETAILS, param);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.equalsIgnoreCase("ERROR")) {
                    Toast.makeText(PeripheralsIssuance.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONArray array = new JSONArray(s);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int q = obj.getInt("qty");
                            String description = obj.getString("desc");

                            peripherals.add(description);
                            quantity.add(q);
                            issued.add(0);
                        }
                        PeripheralsAdapter adapter = new PeripheralsAdapter();
                        listView.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(PeripheralsIssuance.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        new loadPeripherals().execute();
    }

    class PeripheralsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return peripherals.size();
        }

        @Override
        public Object getItem(int position) {
            return peripherals.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            convertView = getLayoutInflater().inflate(R.layout.issuance_item, null);
            TextView peripheral = (TextView) convertView.findViewById(R.id.peripheral);
            TextView qty = (TextView) convertView.findViewById(R.id.qty);
            ScrollableNumberPicker qty_issue = (ScrollableNumberPicker) convertView.findViewById(R.id.qty_issue);
            qty_issue.setMaxValue(quantity.get(position));
            qty.setText("QTY.: " + quantity.get(position));
            peripheral.setText(peripherals.get(position));
            qty_issue.setListener(new ScrollableNumberPickerListener() {
                @Override
                public void onNumberPicked(int value) {
                    Log.e("QTY", "position: " + pos + " value: " + value);
                    issued.set(pos, value);
                    for (int i = 0; i < issued.size(); i++) {
                        Log.e("QTYLIST", "position: " + i + " value: " + issued.get(i));
                    }
                }
            });

            return convertView;
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
                saveIssuance();
                break;
            }
            case R.id.cancel: {
                finish();
                break;
            }
        }

        return true;
    }

    private void saveIssuance() {
        class Issue extends AsyncTask<Void, Void, String>{
            AlertDialog progress;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new SpotsDialog(PeripheralsIssuance.this, "Saving...");
                progress.setCancelable(false);
                progress.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                JSONArray array = new JSONArray();
                for (int i = 0; i < peripherals.size(); i++) {
                    JSONObject obj = new JSONObject();
                    try {
                        //qty, peripherals_desc (choices), unit, qty_issued
                        obj.put("qty", String.valueOf(issued.get(i)));
                        obj.put("desc", peripherals.get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    array.put(obj);
                }
                String details = array.toString();
                Map<String,String> params = new HashMap<>();
                params.put("issued", details);
                params.put("req_id", String.valueOf(req_id));

                HttpURLCon con = new HttpURLCon();
                String response = con.sendPostRequest(AppConfig.ISSUE_PERIPHERALS, params);

                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONSE", s);
                if (s.equalsIgnoreCase("ERROR")) {
                    Toast.makeText(PeripheralsIssuance.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
                } else {
                    try{
                        JSONObject obj = new JSONObject(s);
                        if(!obj.getBoolean("error")){
                            Toast.makeText(PeripheralsIssuance.this, "Success!", Toast.LENGTH_SHORT).show();
                            PeripheralsIssuance.this.finish();
                        }else{
                            Log.e("ERROR", obj.getString("msg"));
                            Toast.makeText(PeripheralsIssuance.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        Toast.makeText(PeripheralsIssuance.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                progress.dismiss();
            }
        }
        new Issue().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
