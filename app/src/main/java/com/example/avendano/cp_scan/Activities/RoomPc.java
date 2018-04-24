package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.ComputerAdapter;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.Network_Handler.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Model.Computers;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomPc extends AppCompatActivity {

    SwipeRefreshLayout refresh;
    RecyclerView recyclerView;
    private List<Computers> compList;
    private int room_id;
    private SQLiteHandler db;
    private ComputerAdapter adapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_pc);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        showDialog();
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("room_name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new SQLiteHandler(this);
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);

        room_id = getIntent().getIntExtra("room_id", 0);

        compList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getComputersFromServer();
    }

    private void getComputersFromServer() {
        compList.clear();
        class getComps extends AsyncTask<Void, Void, String>{
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                String response = con.sendGetRequest(AppConfig.GET_COMPUTERS);
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONSE", s);
                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int comp_id = obj.getInt("comp_id");
                        int room_id = 0;
                        if (!obj.isNull("room_id")) {
                            room_id = obj.getInt("room_id");
                        }
                        int pc_no = 0;
                        if(!obj.isNull("pc_no")){
                            pc_no = obj.getInt("pc_no");
                        }
                        String model = obj.getString("model");
                        String comp_status = obj.getString("comp_status");

                        if (room_id == RoomPc.this.room_id) {
                            Computers computers = new Computers(comp_status, pc_no, model, comp_id, room_id);
                            compList.add(computers);
                        }
                    }
                    adapter = new ComputerAdapter(RoomPc.this, compList, refresh);
                    recyclerView.setAdapter(adapter);
                    hideDialog();
                } catch (JSONException e) {
                    Log.e("JSONERROR", "ROOMPC: " + e.getMessage());
                }
            }
        }
        new getComps().execute();
    }
    public void goBackToRoom() {
        Intent intent = new Intent(RoomPc.this, ViewRoom.class);
        intent.putExtra("room_id", room_id);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBackToRoom();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                goBackToRoom();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

}
