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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import dmax.dialog.SpotsDialog;

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
    String purpose;
    int SIGN_REQ = 0, ISSUED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_peripherals_details);

        req_id = getIntent().getIntExtra("req_id", 0);

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
            //status = pending, confirm, approved, issued, received - cancel ignore
            //check status
            //received = hide linear
            //technician and pending = button = confirm or ignore
            //custodian and issued = button = sign
            //admin and confirmed = button = approve or ignore
            //technician and approved = button = issue peripherals -> jump to another activity

            showRequestDetails(false);
            loadDetails();

            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (positive.getText().toString().equalsIgnoreCase("Edit")) {
                        Intent i = new Intent(ViewRequestPeripheralsDetails.this, RequestPeripherals.class);
                        i.putExtra("room_name", purpose);
                        i.putExtra("room_id", 0);
                        i.putExtra("req_id", req_id);
                        i.putExtra("method", "edit");
                        startActivity(i);
                    } else if (positive.getText().toString().equalsIgnoreCase("approve")) {
                        updateRequestStatus(req_id, "Approved");
                    } else if (positive.getText().toString().equalsIgnoreCase("Confirm")) {
                        updateRequestStatus(req_id, "Confirmed");
                    } else if (positive.getText().toString().equalsIgnoreCase("sign")) {
                        //check first kung may signature na si user
                        checkSignature();
                    } else if (positive.getText().toString().equalsIgnoreCase("Resend Request")) {
                        updateRequestStatus(req_id, "Pending");
                    } else if (positive.getText().toString().equalsIgnoreCase("Issue Peripherals")) {
                        //intent to issuance
                        Intent intent = new Intent(ViewRequestPeripheralsDetails.this, PeripheralsIssuance.class);
                        intent.putExtra("req_id", req_id);
                        startActivity(intent);
                    }
                }
            });
            negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negative.getText().toString().equalsIgnoreCase("cancel")) {
                        updateRequestStatus(req_id, "Cancel");
                    } else if (negative.getText().toString().equalsIgnoreCase("ignore")) {
                        updateRequestStatus(req_id, "Ignored");
                    }
                }
            });
        }
    }

    private void checkSignature() {
        Map<String, String> param = new HashMap<>();
        param.put("user_id", SharedPrefManager.getInstance(this).getUserId());

        volley.sendStringRequestPost(AppConfig.GET_USER_INFO
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            Log.e("SIGN", response);
                            JSONObject obj = new JSONObject(response);
                            if (obj.isNull("signature")) {
                                signReport();
                            } else {
                                updateRequestStatus(req_id, "Received");
                            }

                        } catch (JSONException e) {
                            Log.e("SIGN", response);
                            e.printStackTrace();
                            Toast.makeText(ViewRequestPeripheralsDetails.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError)
                            Toast.makeText(ViewRequestPeripheralsDetails.this, "Server took too long to respond", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ViewRequestPeripheralsDetails.this, "Can't connect to the server", Toast.LENGTH_SHORT).show();
                    }
                }, param);
    }

    private void signReport() {
        Intent intent = new Intent(this, SignatureActivity.class);
        intent.putExtra("from", "request");
        startActivityForResult(intent, SIGN_REQ); //result 0 not signed or 1 signed;
    }

    private void updateRequestStatus(final int req_id, final String update) {
        final android.app.AlertDialog progress = new SpotsDialog(this, "Updating...");
        progress.show();
        String query = "UPDATE request_peripherals SET req_status = '" + update + "' WHERE req_id = '" + req_id + "'";
        if(update.equalsIgnoreCase("approved")){
            String approve_date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
             query = "UPDATE request_peripherals SET req_status = '" + update + "' WHERE req_id = '" + req_id + "'";
        }
        Map<String, String> param = new HashMap<>();
        param.put("query", query);

        volley.sendStringRequestPost(AppConfig.UPDATE_QUERY,
                new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                Toast.makeText(ViewRequestPeripheralsDetails.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                                recreate();
                            } else
                                Toast.makeText(ViewRequestPeripheralsDetails.this, "Can't process update, please try again later", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ViewRequestPeripheralsDetails.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        error.printStackTrace();
                        String string = (error instanceof TimeoutError) ? "Server took too long to respond" : "Can't connect to the server";
                        Toast.makeText(ViewRequestPeripheralsDetails.this, string, Toast.LENGTH_SHORT).show();
                    }
                }, param);

    }

    private void loadDetails() {
        new TableAdapter().retrieve(tb);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDetails();
        showRequestDetails(false);
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
            case R.id.info: {
                showRequestDetails(true);
                break;
            }
        }
        return true;
    }

    private void showRequestDetails(final boolean popup) {
        class GetReqDetails extends AsyncTask<Void, Void, String> {

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
                if (s.equalsIgnoreCase("error")) {
                    Toast.makeText(ViewRequestPeripheralsDetails.this, "Can't Connect to the server", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        JSONArray array = new JSONArray(s);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.getInt("req_id");
                            if (id == req_id) {
                                String designation = obj.getString("designation");
                                String tech_name = obj.getString("tech_name");
                                String purpose = obj.getString("purpose");
                                String date_req = obj.getString("date_req");
                                req_status = obj.getString("req_status");
                                String msg = "Designation: " + designation + "\nPurpose: " +
                                        purpose + "\nDate Requested: " + date_req
                                        + "\nTechnician: " + tech_name
                                        + "\nStatus: " + req_status;
                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewRequestPeripheralsDetails.this);
                                builder.setTitle("Information....")
                                        .setMessage(msg)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                if (popup) {
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                                ViewRequestPeripheralsDetails.this.purpose = purpose;

                                String role = SharedPrefManager.getInstance(ViewRequestPeripheralsDetails.this).getUserRole();
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
                                    } else if (req_status.equalsIgnoreCase("received")) {
                                        //sa reports
                                        buttons.setVisibility(View.GONE);
                                    } else if (req_status.equalsIgnoreCase("cancel") || req_status.equalsIgnoreCase("ignored")) {
                                        positive.setText("Resend Request");
                                        negative.setVisibility(View.GONE);
                                    } else if (req_status.equalsIgnoreCase("confirmed")) {
                                        buttons.setVisibility(View.GONE);
                                    } else { //if pending
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
                                }else{
                                    buttons.setVisibility(View.GONE);
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ViewRequestPeripheralsDetails.this, "An error occurred, please try again later", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        new GetReqDetails().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int result = data.getIntExtra("result", 0);
        if (resultCode == RESULT_OK) {
            if (requestCode == SIGN_REQ) {
                if (result == 1) {  //signed
                    updateRequestStatus(req_id, "Received");
                }
            }
        }
    }

}
