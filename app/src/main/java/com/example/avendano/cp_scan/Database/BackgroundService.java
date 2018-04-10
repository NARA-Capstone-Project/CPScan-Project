package com.example.avendano.cp_scan.Database;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.avendano.cp_scan.Connection_Detector.Connection_Detector;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class BackgroundService extends Service {
    private Timer timer = new Timer();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            }
        }, 1000, 5000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
