package com.example.avendano.cp_scan.BackgroundServices;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.VolleyError;
import com.example.avendano.cp_scan.Network_Handler.AppConfig;
import com.example.avendano.cp_scan.Network_Handler.HttpURLCon;
import com.example.avendano.cp_scan.Network_Handler.VolleyCallback;
import com.example.avendano.cp_scan.Network_Handler.VolleyRequestSingleton;
import com.example.avendano.cp_scan.SharedPref.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class GetNewRepairRequest extends Service {
    private Timer timer = new Timer();
    public static boolean isRunning = false;
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.example.avendano.GET_REQ_COUNT";
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;

//    private IBinder mBinder = new LocalBinder();
//    public class LocalBinder extends Binder{
//        public GetNewRepairRequest getService(){
//            return GetNewRepairRequest.this;
//        }
//    }

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
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        @Override
        public void run() {
            DisplayNotifNumber();
            handler.postDelayed(this, 10000);
        }
    };

    private void DisplayNotifNumber() {
        class NotifNumber extends AsyncTask<Void,Void,String>{

            @Override
            protected String doInBackground(Void... voids) {
                String response = "";
                Map<String, String> param = new HashMap<>();
                param.put("user_id", SharedPrefManager.getInstance(GetNewRepairRequest.this).getUserId());

                HttpURLCon con = new HttpURLCon();
                response = con.sendPostRequest(AppConfig.COUNT_REQ, param);

                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.e("RESPONSe", s);
                try{
                    JSONObject obj = new JSONObject(s);
                    int inv = obj.getInt("inventory");
                    int rep = obj.getInt("repair");
                    int per = obj.getInt("peripherals");
                    int sum  = inv + rep + per;
                    intent.putExtra("number", sum);
                    sendBroadcast(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new NotifNumber().execute();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

}
