package com.example.avendano.cp_scan.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.avendano.cp_scan.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Request_Page extends Fragment {

    RecyclerView recyclerView;

    public Request_Page() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_request__page, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_task_request);
        return v;
    }

}
