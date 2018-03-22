package com.example.avendano.cp_scan.Database;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AppConfig {

    //        public static String ROOT_URL = "https://cp-scan.000webhostapp.com/android_api/"; //online
    public static String ROOT_URL = "http://192.168.43.21/android_api/"; // lan network

    // Server user login url
    public static String URL_LOGIN = ROOT_URL + "cict_login.php";
    // Server user register url
    public static String URL_REQUEST_ACCOUNT = ROOT_URL + "cict_request_account.php"; //request an account
    public static String URL_GET_ALL_PC = ROOT_URL + "cict_get_all_pc.php"; //get all pc data of all pc
    public static String URL_GET_ALL_ROOM = ROOT_URL + "cict_get_all_rooms.php"; //get all room
    public static String URL_GET_REPORT = ROOT_URL + "cict_getInventoryReport.php"; //get report by user_id
    public static String URL_GET_REPORT_DETAILS = ROOT_URL + "cict_getInventoryDetails.php"; //get report details
    public static String URL_SAVE_A_REPORT = ROOT_URL + "cict_tech_a_report.php";   //save assessment report
    public static String URL_SAVE_A_DETAILS = ROOT_URL + "cict_save_report_details.php"; // save assessment details then update computers
    public static String URL_ROOM_SCHED = ROOT_URL + "cict_get_sched.php"; //get sched
    public static String URL_EDIT_PROFILE = ROOT_URL + "cict_edit_user.php"; // edit profile
    public static String URL_REACTIVATE_ACC = ROOT_URL + "cict_reactivate_account.php";
    public static String URL_DEACTIVATE_ACC = ROOT_URL + "cict_deactivate_user.php";
}
