package com.example.avendano.cp_scan.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Model.PeripheralDetails;
import com.example.avendano.cp_scan.Model.TableHeader;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class ViewRequestPeripheralsDetails extends AppCompatActivity {

    int req_id;
    String req_status;
    VolleyRequestSingleton volley;
    PeripheralDetails peripheralDetails;
    private SimpleTableDataAdapter adapter;
    TableView<String[]> tb;
    TableHeader headers;
    LinearLayout buttons;
    Button positive, negative;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_peripherals_details);

        req_id = getIntent().getIntExtra("req_id", 0);
        req_status = getIntent().getStringExtra("status");

        volley = new VolleyRequestSingleton(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Request Details");

        headers = new TableHeader(this);
        tb = (TableView<String[]>) findViewById(R.id.tableView);
        tb.setColumnCount(4);
        tb.setHeaderBackground(R.drawable.table_border);
        tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this, headers.getHeaders()));
        tb.setColumnWeight(0, 1);
        tb.setColumnWeight(1, 1);
        tb.setColumnWeight(2, 3);
        tb.setColumnWeight(3, 1);

        positive = (Button) findViewById(R.id.positive);
        negative = (Button) findViewById(R.id.negative);
        buttons = (LinearLayout) findViewById(R.id.buttons);

        Log.e("REQID", "" + req_id);
        if (req_id == 0) {
            this.finish();
        } else {
            //check status
            //received = hide linear
            //technician and pending = button = confirm or ignore
            //custodian and issued = button = sign
            //admin and confirmed = button = approve or ignore
            //technician and approved = button = issue peripherals -> jump to another activity
            String role = SharedPrefManager.getInstance(this).getUserRole();
            if (role.equalsIgnoreCase("admin")) {
                if (req_status.equalsIgnoreCase("confirmed")) {
                    positive.setText("Approve");
                } else {
                    buttons.setVisibility(View.GONE);
                }
            } else if (role.equalsIgnoreCase("custodian")) {
                if (req_status.equalsIgnoreCase("issued")) {
                    positive.setText("Sign");
                    negative.setVisibility(View.GONE);
                } else if(req_status.equalsIgnoreCase("received")){
                    //sa reports
                    buttons.setVisibility(View.GONE);
                }else{
                    positive.setText("Edit");
                    negative.setText("Cancel");
                }
            } else if (role.equalsIgnoreCase("technician")) {
                if (req_status.equalsIgnoreCase("approved")) {
                    positive.setText("Issue Peripherals");
                    negative.setVisibility(View.GONE);
                } else if (req_status.equalsIgnoreCase("pending")) {
                    positive.setText("Confirm");
                } else {
                    buttons.setVisibility(View.GONE);
                }
            }
            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (positive.getText().toString().equalsIgnoreCase("Edit")) {
                        Intent i = new Intent(ViewRequestPeripheralsDetails.this, RequestPeripherals.class);
                        i.putExtra("room_name", "");
                        i.putExtra("room_id", 0);
                        i.putExtra("req_id", req_id);
                        i.putExtra("method", "edit");
                        startActivity(i);
                    }
                }
            });
            loadDetails();
        }
    }

    private void loadDetails() {
        new TableAdapter().retrieve(tb);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    //table adapter
    class TableAdapter {

        private void retrieve(final TableView v) {
            final ArrayList<PeripheralDetails> details = new ArrayList<>();

            Map<String, String> param = new HashMap<>();
            param.put("req_id", String.valueOf(req_id));

            volley.sendStringRequestPost(AppConfig.GET_PERIPHERALS_DETAILS,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessResponse(String response) {
                            Log.e("DETAILS", response);
                            try {
                                JSONArray array = new JSONArray(response);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    int qty = obj.getInt("qty");
                                    String unit = obj.getString("unit");
                                    String desc = obj.getString("desc");
                                    int qty_issued = obj.getInt("qty_issued");

                                    peripheralDetails = new PeripheralDetails(qty, qty_issued, unit, desc);
                                    details.add(peripheralDetails);
                                }

                                adapter = new SimpleTableDataAdapter(
                                        ViewRequestPeripheralsDetails.this,
                                        new TableHeader(ViewRequestPeripheralsDetails.this).returnDataAsArray(details));
                                v.setDataAdapter(adapter);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(ViewRequestPeripheralsDetails.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            String string = (error instanceof TimeoutError) ? "Server took too long to respond" : "Can't connect to the server";
                            Toast.makeText(ViewRequestPeripheralsDetails.this, string, Toast.LENGTH_SHORT).show();
                        }
                    }, param);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                ViewRequestPeripheralsDetails.this.finish();
            break;
            }
            case R.id.info:{
                showRequestDetails();
                break;
            }
        }
        return true;
    }

    private void showRequestDetails() {
        class GetReqDetails extends AsyncTask<Void, Void, String>{

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                String response = con.sendGetRequest(AppConfig.GET_PERIPHERALS);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONSE", s);
                if(s.equalsIgnoreCase("error")){
                    Toast.makeText(ViewRequestPeripheralsDetails.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                }else{
                    try{
                        JSONArray array = new JSONArray(s);
                        for (int i = 0 ; i <array.length(); i++){
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.getInt("req_id");
                            if(id == req_id){
                                String designation = obj.getString("designation");
                                String tech_name = obj.getString("tech_name");
                                String purpose = obj.getString("purpose");
                                String date_req = obj.getString("date_req");
                                String msg = "Designation: " + designation + "\nPurpose: " +
                                        purpose + "\nDate Requested: " + date_req
                                        + "\nTechnician: " + tech_name;

                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewRequestPeripheralsDetails.this);
                                builder.setTitle("Information....")
                                .setMessage(msg)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                                break;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(ViewRequestPeripheralsDetails.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        new GetReqDetails().execute();
    }
}
