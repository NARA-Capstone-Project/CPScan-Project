package com.example.avendano.cp_scan.BackgroundServices;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Avendano on 23 Apr 2018.
 */

public class QRCodeScan extends Service {

    public static final String BROADCAST_ACTION = "com.example.avendano.SCAN";
    private final Handler handler = new Handler();
    Intent intent;
    String content;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 2000); // 1 second
    }


    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            GetScannedQR();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        content = intent.getStringExtra("content");
        return START_STICKY;
    }

    private void GetScannedQR() {
        class getScan extends AsyncTask<Void, Void, String>{
            private AsyncTask<Void,Void,String> task = null;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                task = this;
                if(content.isEmpty()){
                    task.cancel(true);
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                String response = "";
                if(isCancelled()){
                    response = "";
                }else{
                    Map<String, String> param = new HashMap<>();
                    param.put("serial", content);
                    HttpURLCon con = new HttpURLCon();
                    response = con.sendPostRequest(AppConfig.SCAN, param);
                }
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if(!s.isEmpty()){
                    try{
                        JSONObject obj = new JSONObject(s);
                        //mb serial mon serial, ram, kboard mouse and hdd
                    }catch(Exception e){

                    }
                }
            }
        }

        new getScan().execute();
    }
}
