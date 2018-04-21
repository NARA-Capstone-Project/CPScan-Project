package com.example.avendano.cp_scan.BackgroundServices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Avendano on 21 Apr 2018.
 */

public class SyncingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //computers
    //assessments
    //request inv
    //request repair
    //request peripherals
}
