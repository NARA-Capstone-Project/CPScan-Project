package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.service.autofill.SaveRequest;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.PeripheralAdapter;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.attribute.AclEntryPermission;
import java.sql.Time;
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
    int room_id, req_id;
    String room_name, method;//method = edit or request
    String[] peripheralsList = new String[]{"Mouse", "Keyboard", "Power Supply", "Power Cord", "Memory", "Video Card"
            , "Motherboard", "VGA Cable", "UTP Cable", "Router Hub"};
    String[] purposeList = new String[]{"Replacement", "Defective", "Missing", "Others..."};
    ArrayList<String> choices = new ArrayList<>();
    ArrayList<Integer> quantity = new ArrayList<>();
    ArrayList<String> unitValue = new ArrayList<>();
    Spinner list;
    TextView peripheral_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_peripherals);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        room_name = getIntent().getStringExtra("room_name");//purpose kapag ieedit
        room_id = getIntent().getIntExtra("room_id", 0);
        method = getIntent().getStringExtra("method");
        req_id = getIntent().getIntExtra("req_id", 0);

        listView = (ListView) findViewById(R.id.peripherals);
        peripheral_count = (TextView) findViewById(R.id.total_peripherals);
        peripheral_count.setText("Total No. of Requisition: " + choices.size());
        purpose = (EditText) findViewById(R.id.purpose); //default visibility = gone
        list = (Spinner) findViewById(R.id.list);
        ArrayAdapter<String> spin_adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, purposeList);
        list.setAdapter(spin_adapter);
        list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //"Missing","Defective","Replacement","Others..."
                if (position == 3) {
                    purpose.setVisibility(View.VISIBLE);
                } else
                    purpose.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        add = (FloatingActionButton) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (peripheralsList.length > 0)
                    showPeripherals();
                else
                    Toast.makeText(RequestPeripherals.this, "You have already selected all peripherals", Toast.LENGTH_SHORT).show();
            }
        });
        if (method.equalsIgnoreCase("request")) {
            getSupportActionBar().setTitle("Request Peripherals");
            showPeripherals();
        } else {
            getSupportActionBar().setTitle("Edit Request");
            int counter = 0;
            for (String val : purposeList) {
                if (val.equals(room_name)) {
                    list.setSelection(counter);
                    break;
                } else
                    counter++;
            }
            if (counter == purposeList.length) {
                purpose.setVisibility(View.VISIBLE);
                list.setSelection(3);
                purpose.setText(room_name);
            }
            getPeripheralRequest();
        }
    }

    private void showPeripherals() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Peripherals...")
                .setMultiChoiceItems(peripheralsList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            choices.add(peripheralsList[which]);
                            quantity.add(0);
                            unitValue.add("");
                            peripheral_count.setText("Total No. of Requisition: " + choices.size());
                        } else if (choices.contains(peripheralsList[which])) {
                            int idx = choices.indexOf(peripheralsList[which]);
                            choices.remove(peripheralsList[which]);
                            quantity.remove(idx);
                            unitValue.remove(idx);
                            peripheral_count.setText("Total No. of Requisition: " + choices.size());
                        }
                    }
                })
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (choices.isEmpty())
                            finish();
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
                            for (int i = 0; i < choices.size(); i++) {
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

    private class PeripheralsAdapter extends BaseAdapter {
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
            unit.setText(unitValue.get(position));
            unit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.e("UNIT", s.toString());
                    unitValue.set(pos, s.toString());
                    for (int i = 0; i < unitValue.size(); i++) {
                        Log.e("UNITLIST", "position: " + i + " value: " + unitValue.get(i));
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RequestPeripherals.this);
                    builder.setTitle("Confirmation")
                            .setMessage("Are you sure you want to remove this peripheral?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String toRemove = peripheral.getText().toString();
                                    List<String> list = new ArrayList<String>(Arrays.asList(peripheralsList));
                                    list.add(toRemove);
                                    peripheralsList = list.toArray(new String[list.size()]);
                                    int idx = choices.indexOf(toRemove);
                                    quantity.remove(idx);
                                    unitValue.remove(idx);
                                    choices.remove(toRemove);
                                    peripheral_count.setText("Total No. of Requisition: " + choices.size());
                                    PeripheralsAdapter.this.notifyDataSetChanged();
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
            });

            if (!quantity.isEmpty()) {
                qty.setValue(quantity.get(position));
            }
            if (!unitValue.isEmpty()) {
                unit.setText(unitValue.get(position));
            }

            qty.setListener(new ScrollableNumberPickerListener() {
                @Override
                public void onNumberPicked(int value) {
                    Log.e("QTY", "position: " + pos + " value: " + value);
                    quantity.set(pos, value);
                    for (int i = 0; i < quantity.size(); i++) {
                        Log.e("QTYLIST", "position: " + i + " value: " + quantity.get(i));
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
            case R.id.save:
                //check kung may napili
                if (choices.isEmpty()) {
                    Toast.makeText(this, "You haven't select any peripherals", Toast.LENGTH_SHORT).show();
                }else if(quantity.contains(0)){
                    Toast.makeText(this, "Quantity must not be 0", Toast.LENGTH_SHORT).show();
                } else {
                    if (list.getSelectedItemPosition() == 3) {
                        if (purpose.getText().toString().trim().length() == 0) {
                            Toast.makeText(this, "Please write the purpose of your request", Toast.LENGTH_SHORT).show();
                            purpose.requestFocus();
                        } else {
                            if (method.equalsIgnoreCase("request"))
                                savePeripheralRequest();
                            else
                                editPeripheralRequest();
                        }
                    } else {
                        if (method.equalsIgnoreCase("request"))
                            savePeripheralRequest();
                        else
                            editPeripheralRequest();
                    }
                }
                break;
            case R.id.cancel:
                finish();
                break;
        }

        return true;
    }

    private void savePeripheralRequest() {
        class SaveRequest extends AsyncTask<Void, Void, String> {
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
                HttpURLCon con = new HttpURLCon();
                String reqPurpose = "";
                if (list.getSelectedItemPosition() == 3)
                    reqPurpose = purpose.getText().toString().trim();
                else
                    reqPurpose = list.getSelectedItem().toString().trim();
                String designation = SharedPrefManager.getInstance(RequestPeripherals.this).getName() + "/" + room_name;

                for (int i = 0; i < choices.size(); i++) {
                    JSONObject obj = new JSONObject();
                    try {
                        //qty, peripherals_desc (choices), unit, qty_issued
                        obj.put("qty", String.valueOf(quantity.get(i)));
                        obj.put("desc", choices.get(i));
                        obj.put("unit", unitValue.get(i));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    array.put(obj);
                }
                String details = array.toString();
                Map<String, String> param = new HashMap<>();
                param.put("purpose", reqPurpose);
                param.put("designation", designation);
                param.put("details", details);
                param.put("room_id", String.valueOf(room_id));

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
                try {
                    JSONObject obj = new JSONObject(s);
                    if (!obj.getBoolean("error")) {
                        Toast.makeText(RequestPeripherals.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                        Log.e("SMS", obj.getString("sms"));
                        finish();
                    } else {
                        Log.e("MSG", obj.getString("msg"));
                        Toast.makeText(RequestPeripherals.this, "Request Not Sent!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(RequestPeripherals.this, "An error occurred  while processing your request", Toast.LENGTH_SHORT).show();
                }
            }
        }

        new SaveRequest().execute();
    }

    private void editPeripheralRequest() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < choices.size(); i++) {
            JSONObject obj = new JSONObject();
            try {
                //qty, peripherals_desc (choices), unit, qty_issued
                obj.put("qty", String.valueOf(quantity.get(i)));
                obj.put("desc", choices.get(i));
                obj.put("unit", unitValue.get(i));
            } catch (Exception e) {
                e.printStackTrace();
            }

            array.put(obj);
        }
        String details = array.toString();
        Map<String, String> param = new HashMap<>();
        String reqPurpose = "";
        if (list.getSelectedItemPosition() == 3)
            reqPurpose = purpose.getText().toString().trim();
        else
            reqPurpose = list.getSelectedItem().toString().trim();
        param.put("purpose", reqPurpose);
        param.put("details", details);
        param.put("req_id", String.valueOf(req_id));

        VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        volley.sendStringRequestPost(AppConfig.EDIT_PERIPHERALS,
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e("EDIT", response);
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(RequestPeripherals.this, "Updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RequestPeripherals.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RequestPeripherals.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String string = (error instanceof TimeoutError) ? "Server took too long to respond" : "Can't connect to the server";
                        Toast.makeText(RequestPeripherals.this, string, Toast.LENGTH_SHORT).show();
                    }
                }, param);
    }

    private void getPeripheralRequest() {
        final HttpURLCon con = new HttpURLCon();
        final Map<String, String> param = new HashMap<>();
        param.put("req_id", String.valueOf(req_id));

        class GetDetails extends AsyncTask<Void, Void, String> {
            android.app.AlertDialog progress;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new SpotsDialog(RequestPeripherals.this, "Loading...");
                progress.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                String response = con.sendPostRequest(AppConfig.GET_PERIPHERALS_DETAILS, param);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                choices.clear();
                unitValue.clear();
                quantity.clear();
                if (s.equalsIgnoreCase("ERROR")) {
                    Toast.makeText(RequestPeripherals.this, "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONArray array = new JSONArray(s);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int q = obj.getInt("qty");
                            String description = obj.getString("desc");
                            String u = obj.getString("unit");

                            choices.add(description);
                            unitValue.add(u);
                            quantity.add(q);
                        }
                        for (int i = 0; i < choices.size(); i++) {
                            String toRemove = choices.get(i);
                            List<String> list = new ArrayList<String>(Arrays.asList(peripheralsList));
                            list.remove(toRemove);
                            peripheralsList = list.toArray(new String[list.size()]);
                        }
                        peripheral_count.setText("Total No. of Requisition: " + choices.size());
                        PeripheralsAdapter adapter = new PeripheralsAdapter();
                        listView.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RequestPeripherals.this, "An error occured, please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
                progress.dismiss();
            }
        }

        new GetDetails().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
