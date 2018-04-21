package com.example.avendano.cp_scan.Network_Handler;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class Connection_Detector {
    Context context;

    public Connection_Detector(Context context){
        this.context = context;
    }

    public boolean isConnected(){
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivity != null){
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if(info != null){
                if(info.getState() == NetworkInfo.State.CONNECTED){
                    //snyc
                    return true;
                }
            }
        }
        return false;
    }
}
