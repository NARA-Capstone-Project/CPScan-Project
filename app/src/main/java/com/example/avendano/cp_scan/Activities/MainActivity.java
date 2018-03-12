package com.example.avendano.cp_scan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.avendano.cp_scan.Database.SQLiteHandler;
import com.example.avendano.cp_scan.Fragments.AccountFragment;
import com.example.avendano.cp_scan.Fragments.RoomFragment;
import com.example.avendano.cp_scan.R;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

public class MainActivity extends AppCompatActivity {
    private FrameLayout main_frame;
    private BottomNavigationView navigation;
    SQLiteHandler db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new SQLiteHandler(getApplicationContext());
         Log.w("Room Count: ", "" + db.getRoomCount());
        Log.w("Report Count: ", "" + db.getReportCount());
        Log.w("Comp Count: ", "" + db.getCompCount());
        Log.w("Details Count: ", "" + db.getReportDetailsCount());
        Log.w("Sched Count: ", "" + db.getSchedCount());

        //Fragments
        final AccountFragment accountFragment = new AccountFragment();
        final RoomFragment roomFragment = new RoomFragment();

        //oncreate
        main_frame = (FrameLayout) findViewById(R.id.main_frame);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_report:
                        return true;
                    case R.id.navigation_room:
                        setFragment(roomFragment);
                        return true;
                    case R.id.navigation_task:
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
            //check role of user
            String user_role = SharedPrefManager.getInstance(this).getUserRole();
            if (user_role.equalsIgnoreCase("user")) {
                menu.removeItem(R.id.navigation_task);
                menu.removeItem(R.id.navigation_report);
            } else if (user_role.equalsIgnoreCase("custodian")) {
                menu.removeItem(R.id.navigation_task);
            }
        }

    }

    private void setView(View view) {
        main_frame.removeAllViews();
        main_frame.addView(view);
    }
    private void setFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_frame, fragment);
        transaction.commit();
    }
}
