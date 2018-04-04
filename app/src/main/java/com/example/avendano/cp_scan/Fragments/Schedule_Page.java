package com.example.avendano.cp_scan.Fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.avendano.cp_scan.Activities.TaskActivity;
import com.example.avendano.cp_scan.Adapter.TaskAdapter;
import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Database.AppConfig;
import com.example.avendano.cp_scan.Database.RequestQueueHandler;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
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

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class Schedule_Page extends Fragment {

    String TAG = "TASK";
    private FloatingActionButton add;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private AlertDialog progress;
    List<Task> taskList;
    TaskAdapter taskAdapter;
    Connection_Detector connection_detector;
    SQLiteHandler db;

    public Schedule_Page() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule__page, container, false);

        progress = new SpotsDialog(getContext(), "Loading...");
        progress.show();
        swiper = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swiper.setRefreshing(true);
                new loadSchedule().execute();
            }
        });
        add = (FloatingActionButton) view.findViewById(R.id.add_sched);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_task_sched);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection_detector.isConnected())
                    goToTaskActivity();
                else
                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            }
        });

        new loadSchedule().execute();
        Log.e("CREATE", "create");
        return view;
    }

    private void goToTaskActivity() {
        Intent intent = new Intent(getContext(), TaskActivity.class);
        intent.putExtra("sched_id", 0);
        intent.putExtra("type", "add");
        getActivity().startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                new loadSchedule().execute();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskList = new ArrayList<>();
        connection_detector = new Connection_Detector(getContext());
        db = new SQLiteHandler(getContext());
    }

    private class loadSchedule extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            taskList.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (connection_detector.isConnected())
                loadScheduleFrmServer();
//            else
//                loadScheduleFrmLocal();
            return null;
        }
    }

    private void loadScheduleFrmServer() {
        StringRequest str = new StringRequest(Request.Method.POST
                , AppConfig.URL_GET_TASK
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                        taskAdapter = new TaskAdapter(getContext(), getActivity(), taskList, swiper, db);
                        recyclerView.setAdapter(taskAdapter);
                    } else {
                        Toast.makeText(getContext(), "No Tasks", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swiper.setRefreshing(false);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Can't connect to the server", Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<>();
                param.put("tech_id", SharedPrefManager.getInstance(getContext()).getUserId());
                return param;
            }
        };
        RequestQueueHandler.getInstance(getContext()).addToRequestQueue(str);
    }

}
