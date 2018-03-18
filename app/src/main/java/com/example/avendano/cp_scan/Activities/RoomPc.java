package com.example.avendano.cp_scan.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
import com.example.avendano.cp_scan.Adapter.RoomAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Getter_Setter.Computers;
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
        StringRequest str = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_PC
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int comp_id = obj.getInt("comp_id");
                        int room_id = 0;
                        if (!obj.isNull("room_id")) {
                            room_id = obj.getInt("room_id");
                        }
                        int pc_no = obj.getInt("pc_no");
                        String model = obj.getString("model");
                        String mb = obj.getString("mb");
                        String pr = obj.getString("pr");
                        String monitor = obj.getString("monitor");
                        String ram = obj.getString("ram");
                        String kboard = obj.getString("kboard");
                        String mouse = obj.getString("mouse");
                        String vga = obj.getString("vga");
                        String hdd = obj.getString("hdd");
                        String comp_status = obj.getString("comp_status");
                        String os = obj.getString("os");

                        if (room_id == RoomPc.this.room_id) {
                            Computers computers = new Computers(comp_status, pc_no, model, comp_id, room_id);
                            compList.add(computers);
                        }
                        checkComputers(comp_id, room_id, pc_no, os,model
                                , mb, pr, monitor, ram, kboard, mouse, vga, hdd, comp_status);
                    }
                    adapter = new ComputerAdapter(RoomPc.this, compList, refresh);
                    recyclerView.setAdapter(adapter);
                    hideDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new CompLocalLoader().execute();
            }
        });
        RequestQueueHandler.getInstance(this).addToRequestQueue(str);
    }

    private void checkComputers(int comp_id, int room_id, int pc_no,String os, String model
            , String mb, String pr, String monitor, String ram, String kboard, String mouse, String vga, String hdd, String comp_status) {

        Cursor c = db.getCompDetails(comp_id);
        if (!c.moveToFirst()) {
            long insert = db.addComputers(comp_id, room_id, pc_no, os, model, mb, pr
                    , monitor, ram, kboard, mouse, comp_status, vga, hdd);
            Log.w("COMP INSERT TO SQLITE: ", "Status : " + insert);
        }
    }

    class CompLocalLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            loadAllPc();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            hideDialog();
            adapter = new ComputerAdapter(RoomPc.this, compList, refresh);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private void loadAllPc() {
        compList.clear();
        Cursor c = db.getCompInARoom(room_id);
        if (c.moveToFirst()) {
            do {
                String status = c.getString(c.getColumnIndex(db.COMP_STATUS));
                int pcno = c.getInt(c.getColumnIndex(db.COMP_NAME));
                int compid = c.getInt(c.getColumnIndex(db.COMP_ID));
                String model = c.getString(c.getColumnIndex(db.COMP_MODEL));

                Computers computers = new Computers(status, pcno, model, compid, room_id);
                compList.add(computers);
            } while (c.moveToNext());
        }
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
