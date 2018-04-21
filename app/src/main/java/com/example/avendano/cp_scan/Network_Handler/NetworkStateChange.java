package com.example.avendano.cp_scan.Network_Handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Avendano on 9 Mar 2018.
 */

public class NetworkStateChange extends BroadcastReceiver {
    public static final String NETWORK_AVEILABLE_ACTION = "com.example.avendano.NetworkAvailable";
    public static final String IS_NETWORK_AVAILABLE = "isNetworkAvailable";

    @Override
    public void onReceive(Context context, Intent intent) {
        Connection_Detector connection_detector = new Connection_Detector(context);
        Intent networkStateIntent = new Intent(NETWORK_AVEILABLE_ACTION);
        networkStateIntent.putExtra(IS_NETWORK_AVAILABLE, connection_detector.isConnected());
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);
    }
}
