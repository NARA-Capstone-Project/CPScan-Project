package com.example.avendano.cp_scan.Database;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class AppConfig {

//    public static String ROOT_URL = "https://cp-scan.000webhostapp.com/android_api/"; //online
    public static String ROOT_URL = "http://192.168.0.11/android_api/"; // genymotion
//    public static String ROOT_URL = "http://192.168.0.11/android_api/"; //lan
    public static String ROOT = "http://192.168.0.11/api/";

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
    public static String URL_REQUEST_INVENTORY = ROOT_URL + "cict_request_inventory.php";
    public static String URL_GET_ALL_INVENTORY_REQUEST = ROOT_URL + "cict_getAllInventoryRequest.php";
    public static String URL_CHECK_LAST_INVENTORY_REQUEST = ROOT_URL + "cict_inventory_request_details.php";
    public static String URL_UPDATE_SCHEDULE = ROOT_URL + "cict_update_schedule.php";
    public static String URL_CANCEL_SCHEDULE = ROOT_URL + "cict_cancel_schedule.php";
    public static String URL_REQUEST_REPAIR = ROOT_URL + "cict_request_repair.php";
    public static String URL_CHECK_LAST_REPAIR_REQUEST = ROOT_URL + "cict_repair_request_details.php";
    public static String URL_GET_ALL_REPAIR_REQUEST = ROOT_URL + "cict_getAllRepairRequest.php";
    public static String URL_UPDATE_REPAIR_REQUEST = ROOT_URL + "cict_update_repair.php";
    public static String URL_GET_TASK = ROOT_URL + "cict_get_task_schedule.php";
    public static String URL_SAVE_TASK = ROOT_URL + "cict_save_task_schedule.php";
    public static String URL_SEARCH = ROOT_URL + "cict_search_database.php";
    public static String URL_DELETE_REPORT = ROOT_URL + "cict_delete_assessment.php";
    public static String URL_GET_REQ_REPORTS = ROOT_URL + "cict_get_request_reports.php";
    public static String URL_SEARCH_ROOM = ROOT_URL + "cict_search_room.php";
    public static String URL_GET_SIGNATURE = ROOT_URL + "cict_get_user.php";

    public static String LOGIN = ROOT + "login.php";
    public static String GET_ROOMS = ROOT + "get_room.php";
    public static String SEARCH_ROOMS = ROOT + "search_room.php";
    public static String COUNT_REQ = ROOT + "count_requests.php";
    public static String GET_INVENTORY_REQ = ROOT + "get_inventory_requests.php";
    public static String GET_REPAIR_REQ = ROOT + "get_repair_requests.php";
    public static String GET_COMPUTERS = ROOT + "get_computers.php";
    public static String SAVE_INVENTORY = ROOT + "save_inventory.php";
    public static String SAVE_REPAIR = ROOT + "save_repair_report.php";
    public static String SAVE_REQ_INVENTORY = ROOT + "save_request_inventory.php";
    public static String SAVE_REQ_REPAIR = ROOT + "save_request_repair.php";
    public static String PENDING_INVENTORY = ROOT + "pending_request_inventory.php";
    public static String GET_INVENTORY_REPORTS = ROOT + "get_inventory_reports.php";
}
