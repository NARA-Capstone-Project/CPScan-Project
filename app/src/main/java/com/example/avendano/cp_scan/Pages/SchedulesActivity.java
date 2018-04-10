package com.example.avendano.cp_scan.Pages;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Adapter.TaskAdapter;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Database.VolleyCallback;
import com.example.avendano.cp_scan.Database.VolleyRequestSingleton;
import com.example.avendano.cp_scan.Model.Task;
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

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class SchedulesActivity extends AppCompatActivity {

    String TAG = "TASK";
    Spinner list_type;
    private FloatingActionButton add;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private AlertDialog progress;
    List<Task> taskList;
    TaskAdapter taskAdapter;
    Connection_Detector connection_detector;
    SQLiteHandler db;
    ProgressBar progressBar;
    VolleyRequestSingleton volley;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_and_spinner);

        list_type = findViewById(R.id.list_type);
        taskList = new ArrayList<>();
        volley = new VolleyRequestSingleton(this);
        progress = new SpotsDialog(this, "Loading...");
        progress.show();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        swiper = (SwipeRefreshLayout) findViewById(R.id.refresh);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                new loadSchedule().execute();
            }
        });
        connection_detector = new Connection_Detector(this);
        add = (FloatingActionButton) findViewById(R.id.add_btn);
        add.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection_detector.isConnected()) {

                }
//                    goToTaskActivity();
                else
                    Toast.makeText(SchedulesActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });
        String[] items = new String[]{"Inventory Schedule", "Repair Schedule"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, items);
        list_type.setAdapter(adapter);


        new loadSchedule().execute();
    }

    private class loadSchedule extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //load repair, inventory schedule
            if (connection_detector.isConnected())
                loadScheduleFrmServer();
//            else
//                loadScheduleFrmLocal();
            return null;
        }
    }

    private void loadScheduleFrmServer() {
        Map<String, String> param = new HashMap<>();
        param.put("tech_id", SharedPrefManager.getInstance(this).getUserId());

        volley.sendStringRequestPost(AppConfig.URL_GET_TASK
                , new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String response) {
                        Log.e(TAG, response);
                        try {
                            progress.dismiss();
                            JSONArray array = new JSONArray(response);
                            if (array.length() > 0) {
                                Log.e(TAG, "Task got from server");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    int sched_id = obj.getInt("sched_id");
                                    String title = obj.getString("category");
                                    String desc = obj.getString("desc");
                                    String date = obj.getString("date");
                                    String time = obj.getString("time");
                                    int room_pc_id = obj.getInt("id");

                                    Task task = new Task(date, time, desc, title, sched_id, room_pc_id);
                                    taskList.add(task);
                                }
                                taskAdapter = new TaskAdapter(SchedulesActivity.this, SchedulesActivity.this, taskList, swiper, db);
                                recyclerView.setAdapter(taskAdapter);
                            } else {
                                Toast.makeText(SchedulesActivity.this, "No Tasks", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        swiper.setRefreshing(false);

                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                    }
                }, param);
    }
}
