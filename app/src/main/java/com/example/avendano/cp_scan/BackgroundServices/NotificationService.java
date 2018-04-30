package com.example.avendano.cp_scan.BackgroundServices;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by Avendano on 21 Apr 2018.
 */

public class NotificationService extends Service {
    ArrayList<Integer> req_ids = new ArrayList<>();
    Timer mTimer;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(timerTask, 2000, 2000);
    }
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            getNotifsCount();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            mTimer.cancel();
            timerTask.cancel();
        }catch(Exception e){
            e.printStackTrace();
        }
        //broadcast
        Intent intent = new Intent("com.example.avendano.GET_NOTIF_COUNT");
        intent.putExtra("req_ids", req_ids);
        sendBroadcast(intent);
    }

    private void getNotifsCount() {
        class NotifCount extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                req_ids.clear();
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLCon con = new HttpURLCon();
                Map<String, String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(NotificationService.this).getUserId());
                String s = con.sendPostRequest(AppConfig.NOTIF_COUNTER, param);
                return s;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONsE", s);
                try {
                    JSONArray array = new JSONArray(s);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        int req_id = obj.getInt("req_id");
                        req_ids.add(req_id);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new NotifCount().execute();
    }
}
