package com.example.avendano.cp_scan.BackgroundServices;

import android.app.Service;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Avendano on 21 Apr 2018.
 */

public class NotificationService extends Service{

    public static final String BROADCAST_ACTION = "com.example.avendano.GET_NOTIFS";
    private final Handler handler = new Handler();
    Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        handler.removeCallbacks(sendNotifsCount);
        handler.postDelayed(sendNotifsCount, 1000); // 1 second
    }
    private Runnable sendNotifsCount = new Runnable() {
        @Override
        public void run() {
            sendNotifsCount();
            handler.postDelayed(this, 10000);
        }
    };

    private void sendNotifsCount(){
        class NotifCount extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                Map<String, String> param = new HashMap<>() ;
                param.put("user_id", SharedPrefManager.getInstance(NotificationService.this).getUserId());
                String s = con.sendPostRequest(AppConfig.NOTIF_COUNTER, param);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONsE", s);
                try{

                }catch (Exception e){

                }
            }
        }

        new NotifCount().execute();
    }
}
