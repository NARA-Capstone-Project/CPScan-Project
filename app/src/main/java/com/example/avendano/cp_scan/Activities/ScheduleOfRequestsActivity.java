package com.example.avendano.cp_scan.Activities;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.Adapter.TaskAdapter;
import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleOfRequestsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_schedules);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //instantiate
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new InventorySchedules();
                case 1:
                    return new RepairSchedule();
            }

            return null;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Inventory Schedule";
                case 1:
                    return "Repair Schedule";
            }
            return null;
        }

        @Override
        public int getCount() {
            //shows 2 total pages
            return 2;
        }
    }

    public static class InventorySchedules extends Fragment {

        private RecyclerView recyclerView;
        List<Task> taskList;
        TaskAdapter taskAdapter;
        ProgressBar progressBar;
        SwipeRefreshLayout refresh;

        public InventorySchedules() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_request_schedules, container, false);
            //recycler view and other views
            recyclerView = (RecyclerView) view.findViewById(R.id.recycler_items);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new LoadInventorySchedule().execute();
                }
            });
            refresh.setRefreshing(true);
            new LoadInventorySchedule().execute();
            return view;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //arraylist and db
            taskList = new ArrayList<>();
        }

        class LoadInventorySchedule extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                taskList.clear();
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                String s = con.sendGetRequest(AppConfig.GET_INVENTORY_REQ);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                refresh.setRefreshing(false);
                Log.e("RESPONSE", s);
                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String set_date = obj.getString("date");
                        String set_time = obj.getString("time");
                        String msg = obj.getString("msg");
                        int room_id = obj.getInt("room_id");
                        int req_id = obj.getInt("req_id");
                        String status = obj.getString("req_status");
                        String tech_id = obj.getString("technician");

                        if (status.equalsIgnoreCase("accepted")) {
                            if (SharedPrefManager.getInstance(getContext()).getUserId().equalsIgnoreCase(tech_id)) {
                                Task task = new Task(set_date, set_time, msg,
                                        obj.getString("room_name"), room_id, req_id, status);
                                taskList.add(task);
                            }
                        }
                    }
                    if (taskList.size() != 0) {
                        taskAdapter = new TaskAdapter(getContext(), getActivity(), taskList, refresh);
                        recyclerView.setAdapter(taskAdapter);
                        taskAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No Inventory Schedule", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Toast.makeText(getContext(), "Timeout Connection, please retry again later", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(getContext(), "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }


    public static class RepairSchedule extends Fragment {

        private RecyclerView recyclerView;
        List<Task> taskList;
        TaskAdapter taskAdapter;
        ProgressBar progressBar;
        SwipeRefreshLayout refresh;

        public RepairSchedule() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_request_schedules, container, false);
            //recycler view and other views
            recyclerView = (RecyclerView) view.findViewById(R.id.recycler_items);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new LoadRepairSchedule().execute();
                }
            });
            refresh.setRefreshing(true);
            new LoadRepairSchedule().execute();
            return view;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //arraylist and db
            taskList = new ArrayList<>();
        }

        class LoadRepairSchedule extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                taskList.clear();
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                String s = con.sendGetRequest(AppConfig.GET_REPAIR_REQ);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                refresh.setRefreshing(false);
                Log.e("RESPONSE", s);
                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String set_date = obj.getString("set_date");
                        String set_time = obj.getString("set_time");
                        String msg = obj.getString("msg");
                        int comp_id = obj.getInt("comp_id");
                        int req_id = obj.getInt("req_id");
                        String status = obj.getString("req_status");
                        String tech_id = obj.getString("tech_id");

                        if (status.equalsIgnoreCase("accepted")) {
                            if (SharedPrefManager.getInstance(getContext()).getUserId().equalsIgnoreCase(tech_id)) {
                                Task task = new Task(set_date, set_time, msg,
                                        "PC " + obj.getInt("pc_no") + "/" + obj.getString("room_name"), comp_id, req_id, status);
                                taskList.add(task);
                            }
                        }
                    }
                    if (taskList.size() != 0) {
                        taskAdapter = new TaskAdapter(getContext(), getActivity(), taskList, refresh);
                        recyclerView.setAdapter(taskAdapter);
                        taskAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "No Repair Schedule", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Toast.makeText(getContext(), "Timeout Connection, please retry again later", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(getContext(), "Can't connect to the server, please try again later", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
