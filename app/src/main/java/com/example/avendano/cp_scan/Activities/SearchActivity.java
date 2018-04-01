package com.example.avendano.cp_scan.Activities;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Adapter.RoomAdapter;
import com.example.avendano.cp_scan.Adapter.SearchAdapter;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Fragments.RoomFragment;
import com.example.avendano.cp_scan.Model.Rooms;
import com.example.avendano.cp_scan.Model.Search;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SearchActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText search;
    String type; // type kung room or pc = if room popup ung list ng pc pag ka click
    RecyclerView recycler;
    List<Search> searchResult;
    SearchAdapter searchAdapter;
    AlertDialog progress;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        type = getIntent().getStringExtra("type");
        searchResult = new ArrayList<>();
        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        progress = new SpotsDialog(this, "Loading...");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        search = (EditText) findViewById(R.id.search);
        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        String uniq = search.getText().toString().trim();
                        if (uniq.length() > 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            new loadData().execute("");
                        } else {
                            //load all
                            progressBar.setVisibility(View.VISIBLE);
                            new loadData().execute("all");
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        new loadData().execute("all");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                SearchActivity.this.finish();
                break;
            }
        }
        return true;
    }


    private class loadData extends AsyncTask<String,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(String... strings) {
            String type = strings[0];
            if(type.equalsIgnoreCase("all")){
                //load all
                loadAllRooms();
            }else{
                //load specific
                loadSearch();
            }
            return null;
        }
    }

    private void loadSearch() {
        searchResult.clear();
        String string = search.getText().toString().trim();
        Log.e("STRING", string);
        final String query = "select rooms.room_id,rooms.dept_name, rooms.room_name" +
                " from (select department.dept_name, r.room_id, r.room_name from " +
                "room r left join department on department.dept_id = r.dept_id) as rooms where " +
                "rooms.dept_name like '%" + string + "%' or rooms.room_name like '%" + string + "%'";
        StringRequest stringRequest = new StringRequest(Request.Method.POST
                , AppConfig.URL_SEARCH
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int room_id = obj.getInt("id");
                            String room_name = "";
                            if (obj.isNull("dept_name")) {
                                room_name = obj.getString("room_name");
                            } else {
                                room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                            }
                            Search search = new Search(room_name, room_id);
                            searchResult.add(search);
                        }
                        Log.w("LOADED", "Server rooms");
                        progressBar.setVisibility(View.GONE);
                        searchAdapter = new SearchAdapter(searchResult,SearchActivity.this, SearchActivity.this, type);
                        recycler.setAdapter(searchAdapter);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("JSON ERROR 1", "RoomFragment: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Volleyerror 1", "Load Room: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("query", query);
                return param;
            }
        };
        RequestQueueHandler.getInstance(SearchActivity.this).addToRequestQueue(stringRequest);
    }

    private void loadAllRooms() {
        searchResult.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET
                , AppConfig.URL_GET_ALL_ROOM
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);
                    if (array.length() > 0) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int room_id = obj.getInt("room_id");
                            String room_name = "";
                            if (obj.isNull("dept_name")) {
                                room_name = obj.getString("room_name");
                            } else {
                                room_name = obj.getString("dept_name") + " " + obj.getString("room_name");
                            }
                            Search search = new Search(room_name, room_id);
                            searchResult.add(search);
                        }
                        progressBar.setVisibility(View.GONE);
                        Log.w("LOADED", "Server rooms");
                        searchAdapter = new SearchAdapter(searchResult,SearchActivity.this, SearchActivity.this, type);
                        recycler.setAdapter(searchAdapter);
                    }else{
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, "No result", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("ROOM RESPONSE", response);
                } catch (JSONException e) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("JSON ERROR 1", "RoomFragment: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Log.e("Volleyerror 1", "Load Room: " + error.getMessage());
            }
        });
        RequestQueueHandler.getInstance(SearchActivity.this).addToRequestQueue(stringRequest);
    }
}
