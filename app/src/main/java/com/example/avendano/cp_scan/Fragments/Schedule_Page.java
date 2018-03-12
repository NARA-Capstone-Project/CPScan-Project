package com.example.avendano.cp_scan.Fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.avendano.cp_scan.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Schedule_Page extends Fragment {

    private FloatingActionButton add;
    private RecyclerView recyclerView;

    public Schedule_Page() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule__page, container, false);

        add = (FloatingActionButton) view.findViewById(R.id.add_sched);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_task_sched);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "You clicked the floating action button!", Snackbar.LENGTH_SHORT);
            }
        });
        return view;
    }

}
