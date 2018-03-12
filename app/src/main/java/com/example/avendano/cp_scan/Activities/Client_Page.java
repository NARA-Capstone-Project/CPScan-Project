package com.example.avendano.cp_scan.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Fragments.AccountFragment;
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
        Log.w("Room Count: ", "" + db.getRoomCount());
        Log.w("Report Count: ", "" + db.getReportCount());
        Log.w("Comp Count: ", "" + db.getCompCount());
        Log.w("Details Count: ", "" + db.getReportDetailsCount());
        Log.w("Sched Count: ", "" + db.getSchedCount());

        //Fragments
        final AccountFragment accountFragment = new AccountFragment();
        final RoomFragment roomFragment = new RoomFragment();
        final TaskFragment taskFragment = new TaskFragment();

        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationHelper.disableShiftMode(navigationView);
        Menu menu = navigationView.getMenu();
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_report:
                        return true;
                    case R.id.navigation_room:
                        setFragment(roomFragment);
                        return true;
                    case R.id.navigation_scan:
                        scanPc();
                        return true;
                    case R.id.navigation_task:
                        setFragment(taskFragment);
                        return true;
                    case R.id.navigation_account:
                        setFragment(accountFragment);
                        return true;
                    default:
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
            } else if (user_role.equalsIgnoreCase("custodian")) {
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
                Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_SHORT).show();
                String content = result.getContents();
                //ViewPc
                //search serial then pass comp id sa viewpc
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setSelectedItemId(R.id.navigation_room);
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
