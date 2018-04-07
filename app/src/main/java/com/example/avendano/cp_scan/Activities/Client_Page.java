package com.example.avendano.cp_scan.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.avendano.cp_scan.BottomNavigationHelper;
import com.example.avendano.cp_scan.Connection_Detector.NetworkStateChange;
import com.example.avendano.cp_scan.Database.AddCompFrmServer;
import com.example.avendano.cp_scan.Database.AddInventoryRequestFrmServer;
import com.example.avendano.cp_scan.Database.AddRepairRequestFrmServer;
import com.example.avendano.cp_scan.Database.AddRoomsFrmServer;
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Fragments.AccountFragment;
import com.example.avendano.cp_scan.Fragments.ReportFragment;
import com.example.avendano.cp_scan.Fragments.RoomFragment;
import com.example.avendano.cp_scan.Fragments.TaskFragment;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Client_Page extends AppCompatActivity {
    private BottomNavigationView navigationView;
    SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client__page);

        db = new SQLiteHandler(getApplicationContext());
        //Fragments
        final AccountFragment accountFragment = new AccountFragment();
        final RoomFragment roomFragment = new RoomFragment();
        final TaskFragment taskFragment = new TaskFragment();
        final ReportFragment reportFragment = new ReportFragment();

        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationHelper.disableShiftMode(navigationView);
        Menu menu = navigationView.getMenu();
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                SharedPreferences pref = getSharedPreferences("FRAGMENT", MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                switch (item.getItemId()) {
                    case R.id.navigation_report:
                        edit.putString("SELECTED", "report");
                        edit.apply();
                        setFragment(reportFragment);
                        return true;
                    case R.id.navigation_room:
                        edit.putString("SELECTED", "room");
                        edit.apply();
                        setFragment(roomFragment);
                        return true;
                    case R.id.navigation_scan:
                        scanPc();
                        return true;
                    case R.id.navigation_task:
                        edit.putString("SELECTED", "task");
                        edit.apply();
                        setFragment(taskFragment);
                        return true;
                    case R.id.navigation_account:
                        edit.putString("SELECTED", "account");
                        edit.apply();
                        setFragment(accountFragment);
                        return true;
                    default:
                        edit.putString("SELECTED", "room");
                        edit.apply();
                        return false;
                }
            }
        });
        //if log in
        if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
            startActivity(new Intent(getApplicationContext(), LogInActivity.class));
            finish();
        } else {
            setFragment(roomFragment);
            String user_role = SharedPrefManager.getInstance(this).getUserRole();
            if (user_role.equalsIgnoreCase("user")) {
                menu.removeItem(R.id.navigation_task);
                menu.removeItem(R.id.navigation_report);
                menu.removeItem(R.id.navigation_scan);
            } else if (user_role.equalsIgnoreCase("custodian") || user_role.equalsIgnoreCase("admin")) {
                menu.removeItem(R.id.navigation_task);
                menu.removeItem(R.id.navigation_scan);
            }
        }

        //internet checker
        IntentFilter intentFilter = new IntentFilter(NetworkStateChange.NETWORK_AVEILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(NetworkStateChange.IS_NETWORK_AVAILABLE, false);
                String networkStat = isNetworkAvailable ? "connected" : "disconnected";
                Snackbar.make(findViewById(R.id.navigation), "Network " + networkStat,
                        Snackbar.LENGTH_SHORT).show();
            }
        }, intentFilter);

        //background sync //snackbar
        AddInventoryRequestFrmServer add = new AddInventoryRequestFrmServer(this, db);
        add.SyncFunction();
        AddRepairRequestFrmServer req = new AddRepairRequestFrmServer(this, db);
        req.SyncFunction();
        AddRoomsFrmServer rooms = new AddRoomsFrmServer(this, db);
        rooms.SyncFunction();
        AddCompFrmServer comps = new AddCompFrmServer(this, db);
        comps.SyncFunction();
    }//oncreate

    private void scanPc() {
        IntentIntegrator integrator = new IntentIntegrator(Client_Page.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Place QR code to scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "Scanning cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String content = result.getContents();
                String[] parts = content.split("#");
                String serial = parts[0];
                if (getCompId(serial) != 0) {
                    Intent intent = new Intent(Client_Page.this, ViewPc.class);
                    intent.putExtra("comp_id", getCompId(serial));
                    Client_Page.this.startActivity(intent);
                } else {
                    Toast.makeText(Client_Page.this, "Computer is not found in database", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private int getCompId(String serial) {
        Cursor c = db.getCompIdAndModel(serial);
        if (c.moveToFirst()) {
            int comp_id = c.getInt(c.getColumnIndex(db.COMP_ID));
            return comp_id;
        }
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref = getSharedPreferences("FRAGMENT", MODE_PRIVATE);
        String fragment = pref.getString("SELECTED", "room");
        if(fragment.equalsIgnoreCase("room"))
            navigationView.setSelectedItemId(R.id.navigation_room);
        else if(fragment.equalsIgnoreCase("report"))
            navigationView.setSelectedItemId(R.id.navigation_report);
        else if(fragment.equalsIgnoreCase("account"))
            navigationView.setSelectedItemId(R.id.navigation_account);
        else if(fragment.equalsIgnoreCase("task"))
            navigationView.setSelectedItemId(R.id.navigation_task);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
}//class
