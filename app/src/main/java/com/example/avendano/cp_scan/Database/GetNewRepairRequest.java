package com.example.avendano.cp_scan.Database;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.VolleyError;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class GetNewRepairRequest extends Service {
    private Timer timer = new Timer();
    public static boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //event function here
        this.runService();
        return START_STICKY;
    }

    private void runService(){
        if(!isRunning){
            callFetchNewRequests();
        }
    }

    private void callFetchNewRequests() {
        final VolleyRequestSingleton volley = new VolleyRequestSingleton(this);
        TimerTask timerTask = null;
        timerTask = new TimerTask() {
            @Override
            public void run() {
               volley.sendStringRequestGet(AppConfig.GET_REPAIR_REQ
                       , new VolleyCallback() {
                           @Override
                           public void onSuccessResponse(String response) {

                           }

                           @Override
                           public void onErrorResponse(VolleyError error) {

                           }
                       });
            }
        };
        timer.schedule(timerTask, 1000, 2000);
    }
}
