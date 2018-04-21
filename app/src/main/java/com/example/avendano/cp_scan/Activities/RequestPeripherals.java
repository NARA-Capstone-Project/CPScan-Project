package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.service.autofill.SaveRequest;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class RequestPeripherals extends AppCompatActivity {
    Toolbar toolbar;
    ListView listView;
    EditText purpose, unit;
    FloatingActionButton add;
    int room_id;
    String room_name;
    String[] peripheralsList = new String[]{"Mouse", "Keyboard", "Power Supply", "Power Cord", "Memory", "Video Card"
            , "Motherboard", "VGA Cable", "UTP Cable", "Router Hub"};
    ArrayList<String> choices = new ArrayList<>();
    ArrayList<Integer> quantity = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_peripherals);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Request Peripherals");

        room_name = getIntent().getStringExtra("room_name");
        room_id = getIntent().getIntExtra("room_id", 0);

        listView = (ListView) findViewById(R.id.peripherals);
        purpose = (EditText) findViewById(R.id.purpose);
        add = (FloatingActionButton) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(peripheralsList.length > 0)
                    showPeripherals();
                else
                    Toast.makeText(RequestPeripherals.this, "You have already selected all peripherals", Toast.LENGTH_SHORT).show();
            }
        });
        showPeripherals();
    }

    private void showPeripherals() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Peripherals...")
                .setMultiChoiceItems(peripheralsList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            choices.add(peripheralsList[which]);
                            quantity.add(1);
                        } else if (choices.contains(peripheralsList[which])) {
                            int idx = choices.indexOf(peripheralsList[which]);
                            choices.remove(peripheralsList[which]);
                            quantity.remove(idx);
                        }
                    }
                })
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (choices.isEmpty())
                            RequestPeripherals.this.finish();
                    }
                })
                .setCancelable(false);
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button btn = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (choices.isEmpty()) {
                            Toast.makeText(RequestPeripherals.this, "No selected peripherals", Toast.LENGTH_SHORT).show();
                        } else {
                            for(int i = 0; i < choices.size(); i++){
                                String toRemove = choices.get(i);
                                List<String> list = new ArrayList<String>(Arrays.asList(peripheralsList));
                                list.remove(toRemove);
                                peripheralsList = list.toArray(new String[list.size()]);
                                dialog.dismiss();
                            }
                            PeripheralsAdapter adapter = new PeripheralsAdapter();
                            listView.setAdapter(adapter);
                        }
                    }
                });
            }
        });
        alert.show();
    }

    private class PeripheralsAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return choices.size();
        }

        @Override
        public Object getItem(int position) {
            return choices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int pos = position;
            convertView = getLayoutInflater().inflate(R.layout.request_peripherals_items, null);
            final TextView peripheral = (TextView) convertView.findViewById(R.id.peripheral);
            peripheral.setText(choices.get(position));
            ImageView delete = (ImageView) convertView.findViewById(R.id.remove);
            ScrollableNumberPicker qty = (ScrollableNumberPicker) convertView.findViewById(R.id.qty);

            unit = (EditText) convertView.findViewById(R.id.unit);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String toRemove = peripheral.getText().toString();
                    List<String> list = new ArrayList<String>(Arrays.asList(peripheralsList));
                    list.add(toRemove);
                    peripheralsList = list.toArray(new String[list.size()]);
                    int  idx = choices.indexOf(toRemove);
                    quantity.remove(idx);
                    choices.remove(toRemove);
                    PeripheralsAdapter.this.notifyDataSetChanged();
                }
            });

            qty.setListener(new ScrollableNumberPickerListener() {
                @Override
                public void onNumberPicked(int value) {
                    quantity.set(pos, value);
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
            case R.id.save:
                //check kung may napili
                if(choices.isEmpty()){
                    Toast.makeText(this, "You haven't select any peripherals", Toast.LENGTH_SHORT).show();
                }else{
                    if(purpose.getText().toString().trim().isEmpty()){
                        Toast.makeText(this, "Please write the purpose of your request", Toast.LENGTH_SHORT).show();
                        purpose.requestFocus();
                    }else
                        savePeripheralRequest();
                }
                break;
            case R.id.cancel:
                this.finish();
                break;
        }

        return true;
    }

    private void savePeripheralRequest() {
        class SaveRequest extends AsyncTask<Void, Void, String>{
            android.app.AlertDialog progress;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new SpotsDialog(RequestPeripherals.this, "Sending...");
                progress.setCancelable(false);
                progress.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                //httpurl connection
                JSONArray array = new JSONArray();
                HttpURLCon con =  new HttpURLCon();
                String reqPurpose = purpose.getText().toString().trim();
                String designation = SharedPrefManager.getInstance(RequestPeripherals.this).getName() + "/" + room_name;

                for(int i =0; i < choices.size(); i++){
                    JSONObject obj = new JSONObject();
                    try{
                        //qty, peripherals_desc (choices), unit, qty_issued
                        obj.put("qty", String.valueOf(quantity.get(i)));
                        obj.put("desc", choices.get(i));
                        obj.put("unit", unit.getText().toString().trim());
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    array.put(obj);
                }
                String details = array.toString();
                Map<String, String> param = new HashMap<>();
                param.put("purpose", reqPurpose);
                param.put("designation", designation);
                param.put("details", details);
                param.put("room_id" , String.valueOf(room_id));

                String response = con.sendPostRequest(AppConfig.SAVE_REQ_PERIPHERALS, param);
                Log.e("RESPONSE", response);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //json
                Log.e("REQUEST", s);
                progress.dismiss();
                try{
                    JSONObject obj = new JSONObject(s);
                    if(!obj.getBoolean("error")){
                        Toast.makeText(RequestPeripherals.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                        Log.e("MSG", obj.getString("msg"));
                        RequestPeripherals.this.finish();
                    }else{
                        Log.e("MSG", obj.getString("msg"));
                        Toast.makeText(RequestPeripherals.this, "Request Not Sent!", Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        new SaveRequest().execute();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}
