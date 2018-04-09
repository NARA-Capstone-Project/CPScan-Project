package com.example.avendano.cp_scan.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.example.avendano.cp_scan.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaskFragment extends Fragment {
    TabLayout tabLayout;
    ViewPager viewPage;
    private ViewPagerAdapter viewPagerAdapter;

    public TaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        Log.e("TASK", "oncreate");
        tabLayout = (TabLayout) view.findViewById(R.id.tablayout);
        viewPage = (ViewPager) view.findViewById(R.id.viewPage);
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPage.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPage);
        return view;
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:{
                    Schedule_Page sched_page = new Schedule_Page();
                    return sched_page;
                }
                case 1:{
                    Request_Page req_page = new Request_Page();
                    return req_page;
                }
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                        return "Task List";
                case 1:
                    return "Request List";
            }
            return null;
        }
    }
}
