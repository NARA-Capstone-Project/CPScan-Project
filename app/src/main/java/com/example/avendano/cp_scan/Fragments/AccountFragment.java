package com.example.avendano.cp_scan.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.avendano.cp_scan.Activities.LogInActivity;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    SQLiteHandler db;
    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        TextView tName = (TextView) view.findViewById(R.id.name);
        TextView tRole = (TextView) view.findViewById(R.id.role);
        TextView tAccExpire = (TextView) view.findViewById(R.id.date_expire);

        tAccExpire.setText("Account Expire on: " + SharedPrefManager.getInstance(getContext()).getAcc_Expire());
        tName.setText(SharedPrefManager.getInstance(getContext()).getName());
        tRole.setText(SharedPrefManager.getInstance(getContext()).getUserRole());

        final ListView listView = (ListView) view.findViewById(R.id.listview);
        String[] strings = new String[]{"Settings","Sign Out", "Deactivate"};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strings);
        listView.setAdapter(adapter);
        listView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (adapter.getItem(position)){
                            case "Sign Out":
                                logout();
                                break;
                        }
                    }
                });
        return view;
    }
    private void logout(){
        db = new SQLiteHandler(getContext());
        db.deleteAllComp();
        db.deleteReport();
        db.deleteReportDetails();
        db.deleteRooms();
        db.close();
        SharedPrefManager.getInstance(getContext()).logout();
        startActivity(new Intent(getActivity(), LogInActivity.class));
        getActivity().finish();
    }

    //add oncreate here to add to list<>
    //then adapter at oncreateview
}
