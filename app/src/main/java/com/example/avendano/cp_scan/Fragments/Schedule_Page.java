package com.example.avendano.cp_scan.Fragments;


import android.app.AlertDialog;
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

import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;
import com.example.avendano.cp_scan.Model.Task;
import com.example.avendano.cp_scan.R;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class Schedule_Page extends Fragment {

    private FloatingActionButton add;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private AlertDialog progress;
    List<Task> taskList;
    Connection_Detector connection_detector;
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
                //loadSchedule - asynctask
            }
        });
        add = (FloatingActionButton) view.findViewById(R.id.add_sched);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_task_sched);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Add schedule", Toast.LENGTH_SHORT).show();
            }
        });

        new loadSchedule().execute();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskList = new ArrayList<>();
        connection_detector = new Connection_Detector(getContext());
        //sqqlite db
    }

    private class loadSchedule extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            if(connection_detector.isConnected())
                loadScheduleFrmServer();
//            else
//                loadScheduleFrmLocal();
            return null;
        }
    }

    private void loadScheduleFrmServer() {

    }
}
