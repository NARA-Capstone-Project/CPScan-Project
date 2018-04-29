package com.example.avendano.cp_scan.SharedPref;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.MultiAutoCompleteTextView;

/**
 * Created by Avendano on 28 Apr 2018.
 */

public class SharedPrefNotifCounter {

    private static SharedPrefNotifCounter mInstance;
    private static Context mCtx;

    private static final String SHARED_PREF_NAME = "notif_counter";
    private static final String A_REPORT_COUNT = "a_report_count";
    private static final String INV_REQ_COUNT = "inv_count";
    private static final String REP_REQ_COUNT = "rep_count";
    private static final String PER_REQ_COUNT = "per_count";
    private static final String MISSED_INV_COUNT = "missed_inv";
    private static final String MISSED_REP_COUNT = "missed_rep";
    private static final String PER_APPROVED_COUNT = "approved";
    private static final String PER_ISSUED_COUNT = "issued";
    private static final String PER_REC_COUNT = "received";

    public SharedPrefNotifCounter(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefNotifCounter getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefNotifCounter(context);
        }
        return mInstance;
    }

    public static String getaReportCount() {
        return A_REPORT_COUNT;
    }

    public static String getInvReqCount() {
        return INV_REQ_COUNT;
    }

    public static String getRepReqCount() {
        return REP_REQ_COUNT;
    }

    public static String getPerReqCount() {
        return PER_REQ_COUNT;
    }

    public static String getMissedInvCount() {
        return MISSED_INV_COUNT;
    }

    public static String getMissedRepCount() {
        return MISSED_REP_COUNT;
    }

    public static String getPerApprovedCount() {
        return PER_APPROVED_COUNT;
    }

    public static String getPerIssuedCount() {
        return PER_ISSUED_COUNT;
    }

    public static String getPerRecCount() {
        return PER_REC_COUNT;
    }

    public boolean logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        return true;
    }
}
