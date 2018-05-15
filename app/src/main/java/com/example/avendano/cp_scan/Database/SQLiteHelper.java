package com.example.avendano.cp_scan.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.avendano.cp_scan.Model.Rooms;

import java.sql.RowId;
import java.util.Locale;

/**
 * Created by Avendano on 9 Apr 2018.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    //TABLE ROOMS
    public static final String TABLE_ROOMS = "rooms";
    public static final String ROOMS_ID = "room_id";
    public static final String ROOMS_NAME = "room_name";
    public static final String ROOMS_CUSTODIAN = "room_custodian";
    public static final String ROOMS_TECHNICIAN = "room_technician";
    public static final String ROOMS_BUILDING = "building";
    public static final String ROOMS_FLOOR = "floor";
    public static final String ROOMS_PC_WORKING = "pc_working";
    public static final String ROOMS_PC_COUNT = "pc_count";
    public static final String ROOMS_TECH_PHONE = "tech_phone";

    //TABLE ROOM SCHED
    public static final String TABLE_ROOM_SCHED = "room_sched";
    public static final String SCHED_DAY = "room_sched";
    public static final String SCHED_FROM_TIME = "from_time";
    public static final String SCHED_TO_TIME = "to_time";

    //TABLE COMPUTERS
    //foreign key room_id
    public static final String TABLE_COMPUTERS = "computers";
    public static final String COMP_ID = "comp_id";
    public static final String COMP_OS = "comp_os";
    public static final String COMP_MODEL = "model";
    public static final String COMP_SERIAL = "comp_serial";
    public static final String COMP_NAME = "pc_no";
    public static final String COMP_PR = "processor";
    public static final String COMP_MB = "motherboard";
    public static final String COMP_MONITOR = "monitor";
    public static final String COMP_RAM = "ram";
    public static final String COMP_KBOARD = "kboard";
    public static final String COMP_MOUSE = "mouse";
    public static final String COMP_VGA = "vga";
    public static final String COMP_HDD = "hdd";
    public static final String COMP_STATUS = "comp_status";

    //TABLE ASSESSMENT_REPORTS
    public static final String TABLE_ASSESSMENT_REPORT = "assessment_reports";
    public static final String REPORT_ID = "rep_id";
    public static final String REPORT_CATEGORY = "category";
    public static final String REPORT_DATE = "date";
    public static final String REPORT_TIME = "time";
    public static final String REPORT_CUST_SIGNED = "cust_signed";
    public static final String REPORT_REMARKS = "remarks";
    public static final String REPORT_HTECH_SIGNED = "htech_signed";
    public static final String REPORT_ADMIN_SIGNED = "admin_signed";

    public static final String TABLE_REQ_INVENTORY = "request_inventory";
    //req_id room id custodian technician date string time string reqstatus rep id;
    public static final String REQ_ID = "request_id"; //auto inc
    public static final String REQ_DATE = "req_date";   //string
    public static final String REQ_TIME = "req_time";   //string
    public static final String REQ_STATUS = "req_status";
    public static final String REQ_MESSAGE = "req_msg";
    public static final String DATE_OF_REQ = "date_of_req";
    public static final String TIME_OF_REQ = "time_of_req";

    public static final String TABLE_REQ_REPAIR = "request_repair";
    public static final String REQ_DETAILS = "req_details";
//req id comp_id, rep_id,message, custodian, technician, date, time,images, req_status


    //TABLE REPORT DETAILS
    public static final String TABLE_REPORT_DETAILS = "assessment_details";
    public static final String REPORT_MB_SERIAL = "mb_serial";
    public static final String REPORT_MON_SERIAL = "mon_serial";

    //TEMPORARY TABLES
    public static final String ASSESSED_PC = "assessed_pc";
    //id hindi isasama sa pag save sa server
    public static final String PC_TO_ASSESS = "pc_to_assess";


    //TABLE REQUEST PERPHERALS
    public static final String TABLE_REQ_PERIPHERALS = "request_peripherals";
    public static final String DEPT_ID = "dept_id";
    public static final String DESIGNATION = "designation";
    public static final String PURPOSE = "purpose";
    public static final String DATE_APPROVED = "date_approved";

    //PERIPHERALS DETAILS
    public static final String TABLE_PERIPHERALS_DETAILS = "peripherals_details";
    public static final String QTY = "qty";
    public static final String UNIT = "unit";
    public static final String DESCRIPTION = "description";
    public static final String QTY_ISSUED = "qty_issued";

    //TASK SCHEDULE
//    public static final String TABLE_TASK_SCHEDULE = "task_schedule";
//    public static final String SCHED_ID = "sched_id";
//    public static final String ROOM_PC_ID = "room_pc_id";
//    public static final String DATE = "date";
//    public static final String TIME = "time";
//    public static final String TASK_STATUS = "task_status";

    public static final String COLUMN_SCANNED = "scanned"; // if assessed/scanned 0 n 1
    public static final String COLUMN_TECH_ID = "technician_id";
    public static final String COLUMN_CUST_ID = "custodian_id";
    public static final String COLUMN_SYNC = "sync";
    public static final String COLUMN_TOGGLE = "toggle";
    public static final String COLUMN_REF_ID = "ref_id"; //auto increment

    //create
    String createComputers = "CREATE TABLE " + TABLE_COMPUTERS + "("
            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COMP_ID + " INTEGER, " //PRIMARY KEY
            + ROOMS_ID + " INTEGER, "
            + COMP_OS + " VARCHAR, "
            + COMP_NAME + " INTEGER, " // pc_no
            + COMP_MODEL + " VARCHAR, "
            + COMP_MB + " VARCHAR, "
            + COMP_PR + " VARCHAR, "
            + COMP_MONITOR + " VARCHAR, "
            + COMP_RAM + " VARCHAR, "
            + COMP_KBOARD + " VARCHAR, "
            + COMP_MOUSE + " VARCHAR, "
            + COMP_VGA + " VARCHAR, "
            + COMP_HDD + " VARCHAR, "
            + COMP_STATUS + " VARCHAR ,"
            + COLUMN_TOGGLE + " TINYINT,"
            + COLUMN_SYNC + " TINYINT"
            + " );";
    String createRooms = "CREATE TABLE " + TABLE_ROOMS + "("
            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ROOMS_ID + " INTEGER,"    //PRIMARY KEY
            + ROOMS_NAME + " VARCHAR,"  //dept + room name
            + ROOMS_BUILDING + " VARCHAR,"
            + ROOMS_FLOOR + " INTEGER, "
            + ROOMS_CUSTODIAN + " VARCHAR,"
            + COLUMN_CUST_ID + " VACHAR,"
            + ROOMS_TECHNICIAN + " VARCHAR,"
            + COLUMN_TECH_ID + " VACHAR,"
            + ROOMS_TECH_PHONE + " VACHAR,"
            + ROOMS_PC_COUNT + " INTEGER, "
            + ROOMS_PC_WORKING + " INTEGER, "
            + COLUMN_TOGGLE + " TINYINT,"
            + COLUMN_SYNC + " TINYINT"
            + ")";//to sync check kung ung room_id na nasa server is wala sa local
    String createReport = "CREATE TABLE " + TABLE_ASSESSMENT_REPORT + " ( "
            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + REPORT_ID + " INTEGER ,"  //PRIMARY KEY
            + ROOMS_ID + " INTEGER,"
            + ROOMS_NAME + " varchar, "
            + COLUMN_CUST_ID + " VARCHAR,"
            + COLUMN_TECH_ID + " VARCHAR,"  // yung nagassess
            + REPORT_CATEGORY + " varchar, " // hindi ipapasa sa server
            + REPORT_DATE + " DATE,"
            + REPORT_TIME + " TIME,"
            + REPORT_CUST_SIGNED + " TINYINT,"
            + REPORT_HTECH_SIGNED + " TINYINT,"
            + REPORT_ADMIN_SIGNED + " TINYINT,"
            + REPORT_REMARKS + " TEXT,"
            + COLUMN_TOGGLE + " TINYINT,"
            + COLUMN_SYNC + " TINYINT"
            + ");";
    //LOCAL REPORT DETAILS
    String createReportDetails = "CREATE TABLE " + TABLE_REPORT_DETAILS + " ( "
            + REPORT_ID + " INTEGER, " //FOREIGN KEY
            + COMP_ID + " INTEGER, " //FOREIGN KEY
            + COMP_MODEL + " VARCHAR, "
            + COMP_NAME + " INTEGER,"
            //add comp serial here
            + COMP_MB + " VARCHAR, "
            + REPORT_MB_SERIAL + " varchar,"
            + COMP_PR + " VARCHAR, "
            + COMP_MONITOR + " VARCHAR, "
            + REPORT_MON_SERIAL + " varchar,"
            + COMP_RAM + " VARCHAR, "
            + COMP_KBOARD + " VARCHAR, "
            + COMP_MOUSE + " VARCHAR, "
            + COMP_VGA + " VARCHAR, "
            + COMP_HDD + " VARCHAR, "
            + COMP_STATUS + " VARCHAR"
            + ");";
    //req_id, rep_id, comp_id, cust_id, tech_id, date, time, msg, images,status
    String createRequestRepair = "CREATE TABLE " + TABLE_REQ_REPAIR + "("
            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + REQ_ID + " INTEGER, " //PRIMARY KEY
            + REPORT_ID + " INTEGER, "
            + COMP_ID + " INTEGER, "
            + COLUMN_CUST_ID + " VARCHAR, "
            + COLUMN_TECH_ID + " VARCHAR, "
            + REQ_DATE + " VARCHAR, "
            + REQ_TIME + " VARCHAR, "
            + REQ_MESSAGE + " TEXT, "
            + REQ_DETAILS + " TEXT, "
            + DATE_OF_REQ + " DATE, "
            + TIME_OF_REQ + " TIME, "
            + REQ_STATUS + " VARCHAR,"
            + COLUMN_TOGGLE + " TINYINT,"
            + COLUMN_SYNC + " TINYINT)";

    //req_id, rep_id, room_id, cust_id, tech_id, date, time, msg, status
    String createRequestInventory = "CREATE TABLE " + TABLE_REQ_INVENTORY + "("
            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + REQ_ID + " INTEGER, " //PRIMARY KEY
            + REPORT_ID + " INTEGER, "
            + ROOMS_ID + " INTEGER, "
            + COLUMN_CUST_ID + " VARCHAR, "
            + COLUMN_TECH_ID + " VARCHAR, "
            + REQ_DATE + " VARCHAR, "
            + REQ_TIME + " VARCHAR, "
            + REQ_MESSAGE + " TEXT, "
            + DATE_OF_REQ + " DATE, "
            + TIME_OF_REQ + " TIME, "
            + REQ_STATUS + " VARCHAR,"
            + COLUMN_TOGGLE + " TINYINT,"
            + COLUMN_SYNC + " TINYINT)";

//    String createTaskSched = "CREATE TABLE " + TABLE_TASK_SCHEDULE + " ( "
//            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
//            + SCHED_ID + " INTEGER,"
//            + REPORT_CATEGORY + " VARCHAR,"
//            + DESCRIPTION + " TEXT,"
//            + ROOM_PC_ID + " INTEGER,"
//            + DATE + " DATE,"
//            + TIME + " TIME,"
//            + COLUMN_TECH_ID + " VARCHAR,"
//            + TASK_STATUS + " VARCHAR, "
//            + COLUMN_TOGGLE + " TINYINT,"
//            + COLUMN_SYNC + " TINYINT)";

    String createReqPeripherals = "CREATE TABLE " + TABLE_REQ_PERIPHERALS + " ( "
            + COLUMN_REF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + REQ_ID + " INTEGER, " //PRIMARY KEY
            + DEPT_ID + " INTEGER,"
            + COLUMN_CUST_ID + " VARCHAR,"
            + COLUMN_TECH_ID + " VARCHAR,"
            + DESIGNATION + " TEXT,"
            + PURPOSE + " TEXT,"
            + DATE_OF_REQ + " DATE,"
            + DATE_APPROVED + " TIME,"
            + REQ_STATUS + " VARCHAR,"
            + COLUMN_TOGGLE + " TINYINT,"
            + COLUMN_SYNC + " TINYINT)";

    String createReqPeripheralsDetails = "CREATE TABLE " + TABLE_PERIPHERALS_DETAILS + " ( "
            + REQ_ID + " INTEGER, "
            + QTY + " INTEGER, "
            + UNIT + " TEXT, "
            + DESCRIPTION + " TEXT, "
            + QTY_ISSUED + " INTEGER"
            + ")";

    //TEMPORARY TABLE
    String createAssessedPc = "CREATE TABLE " + ASSESSED_PC + " ( "
            + COMP_ID + " INTEGER, " //FOREIGN KEY
            + COMP_NAME + " INTEGER,"
            + COMP_MODEL + " VARCHAR, "
            + COMP_SERIAL + " VARCHAR, "
            + COMP_MB + " VARCHAR, "
            + REPORT_MB_SERIAL + " varchar,"
            + COMP_PR + " VARCHAR, "
            + REPORT_MON_SERIAL + " varchar,"
            + COMP_MONITOR + " VARCHAR, "
            + COMP_RAM + " VARCHAR, "
            + COMP_KBOARD + " VARCHAR, "
            + COMP_MOUSE + " VARCHAR, "
            + COMP_VGA + " VARCHAR, "
            + COMP_HDD + " VARCHAR, "
            + COMP_STATUS + " VARCHAR)";


    //queries
//    comp_id room_id pc_no model processor monitor ram kboard mouse vga hdd comp_status
    String createPcToAssess = "CREATE TABLE " + PC_TO_ASSESS + " ( "
            + COMP_ID + " INTEGER, " //FOREIGN KEY
            + COMP_NAME + " INTEGER, "
            + COMP_MODEL + " VARCHAR, "
            + COMP_SERIAL + " VARCHAR, "
            + COMP_MB + " VARCHAR, "
            + COMP_PR + " VARCHAR, "
            + COMP_MONITOR + " VARCHAR, "
            + COMP_RAM + " VARCHAR, "
            + COMP_KBOARD + " VARCHAR, "
            + COMP_MOUSE + " VARCHAR, "
            + COMP_VGA + " VARCHAR, "
            + COMP_HDD + " VARCHAR, "
            + COMP_STATUS + " VARCHAR, "
            + COLUMN_SCANNED + " TINYINT"
            + " );";
    //DB DETAILS
    public static final String DB_NAME = "mySQL";
    public static final int DB_VERSION = 9;

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createRooms);
        db.execSQL(createComputers);
        db.execSQL(createReport);
        db.execSQL(createReportDetails);
        db.execSQL(createRequestRepair);
        db.execSQL(createRequestInventory);
//        db.execSQL(createTaskSched);
        db.execSQL(createReqPeripherals);
        db.execSQL(createReqPeripheralsDetails);
        db.execSQL(createPcToAssess);
        db.execSQL(createAssessedPc);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTERS);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENT_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERIPHERALS_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQ_PERIPHERALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQ_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REQ_REPAIR);
        db.execSQL("DROP TABLE IF EXISTS " + PC_TO_ASSESS);
        db.execSQL("DROP TABLE IF EXISTS " + ASSESSED_PC);
        onCreate(db);
    }

    //computers
    public void addComputers(int comp_id, int room_id, String os, int comp_name
            , String model, String mb, String pr, String mon, String ram, String kb, String mouse
            , String vga, String hdd, String comp_status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COMP_ID, comp_id);
        v.put(ROOMS_ID, room_id);
        v.put(COMP_OS, os);
        v.put(COMP_NAME, comp_name);
        v.put(COMP_MODEL, model);
        v.put(COMP_MB, mb);
        v.put(COMP_PR, pr);
        v.put(COMP_MONITOR, mon);
        v.put(COMP_RAM, ram);
        v.put(COMP_KBOARD, kb);
        v.put(COMP_MOUSE, mouse);
        v.put(COMP_VGA, vga);
        v.put(COMP_HDD, hdd);
        v.put(COMP_STATUS, comp_status);

        db.insert(TABLE_COMPUTERS, null, v);
    }

    public Cursor getComputers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = {COMP_ID, COMP_HDD, COMP_KBOARD, COMP_MB, COMP_MODEL, COMP_MONITOR
                , COMP_MOUSE, COMP_NAME, ROOMS_ID, COMP_OS, COMP_PR, COMP_RAM, COMP_VGA, COMP_STATUS
                , COLUMN_TOGGLE, COLUMN_SYNC, COLUMN_REF_ID};
        Cursor c = db.query(TABLE_COMPUTERS, cols, null, null, null, null, COMP_NAME + " DESC", null);
        return c;
    }

    //rooms
    public void addRooms(int room_id, String room_name, String building, int flr, String custodian,
                         String cust_id, String technician, String tech_id,String tech_phone, int pc_count, int pc_working) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(ROOMS_ID, room_id);
        v.put(ROOMS_NAME, room_name);
        v.put(ROOMS_BUILDING, building);
        v.put(ROOMS_FLOOR, flr);
        v.put(ROOMS_CUSTODIAN, custodian);
        v.put(COLUMN_CUST_ID, cust_id);
        v.put(ROOMS_TECHNICIAN, technician);
        v.put(COLUMN_TECH_ID, tech_id);
        v.put(ROOMS_TECH_PHONE, tech_phone);
        v.put(ROOMS_PC_COUNT, pc_count);
        v.put(ROOMS_PC_WORKING, pc_working);
        db.insert(TABLE_ROOMS, null, v);
    }

    public Cursor getRooms() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = {COLUMN_REF_ID, ROOMS_ID, ROOMS_NAME, ROOMS_BUILDING, ROOMS_FLOOR,
                ROOMS_CUSTODIAN, COLUMN_CUST_ID, ROOMS_TECH_PHONE,ROOMS_TECHNICIAN, COLUMN_TECH_ID, ROOMS_PC_COUNT, ROOMS_PC_WORKING
                , COLUMN_TOGGLE, COLUMN_SYNC};
        Cursor c = db.query(TABLE_ROOMS, cols, null, null, null, null, ROOMS_NAME + " DESC", null);
        return c;
    }

    //reports
    public void addReports(int rep_id, int rooms_id, String room_name, String cust_id, String tech_id
            , String cat, String date, String time, int cust_signed, int head_signed, int admin_signed
            , String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(REPORT_ID, rep_id);
        v.put(ROOMS_ID, rooms_id);
        v.put(ROOMS_NAME, room_name);
        v.put(COLUMN_CUST_ID, cust_id);
        v.put(COLUMN_TECH_ID, tech_id);
        v.put(REPORT_CATEGORY, cat);
        v.put(REPORT_DATE, date);
        v.put(REPORT_TIME, time);
        v.put(REPORT_CUST_SIGNED, cust_signed);
        v.put(REPORT_HTECH_SIGNED, head_signed);
        v.put(REPORT_ADMIN_SIGNED, admin_signed);
        v.put(REPORT_REMARKS, remarks);
        db.insert(TABLE_ASSESSMENT_REPORT, null, v);
    }

    public Cursor getReports() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = {COLUMN_REF_ID, ROOMS_ID, ROOMS_NAME, REPORT_ID, COLUMN_CUST_ID,
                COLUMN_TECH_ID, REPORT_CATEGORY, REPORT_DATE, REPORT_TIME, REPORT_CUST_SIGNED, REPORT_HTECH_SIGNED
                , REPORT_ADMIN_SIGNED, REPORT_REMARKS, COLUMN_TOGGLE, COLUMN_SYNC};
        Cursor c = db.query(TABLE_ASSESSMENT_REPORT, cols, null, null, null, null, REPORT_DATE + " DESC, " + REPORT_TIME + " DESC", null);
        return c;
    }

    //report details
    public void addReportDetails(int comp_id, int rep_id, int comp_name
            , String model, String mb, String mb_serial, String pr, String mon, String mon_serial
            , String ram, String kb, String mouse
            , String vga, String hdd, String comp_status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COMP_ID, comp_id);
        v.put(REPORT_ID, rep_id);
        v.put(COMP_NAME, comp_name);
        v.put(COMP_MODEL, model);
        v.put(COMP_MB, mb);
        v.put(REPORT_MB_SERIAL, mb_serial);
        v.put(COMP_PR, pr);
        v.put(COMP_MONITOR, mon);
        v.put(REPORT_MON_SERIAL, mon_serial);
        v.put(COMP_RAM, ram);
        v.put(COMP_KBOARD, kb);
        v.put(COMP_MOUSE, mouse);
        v.put(COMP_VGA, vga);
        v.put(COMP_HDD, hdd);
        v.put(COMP_STATUS, comp_status);

        db.insert(TABLE_REPORT_DETAILS, null, v);
    }

    public Cursor getReportDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = {COMP_ID, COMP_HDD, REPORT_ID, COMP_KBOARD, COMP_MB, COMP_MODEL, COMP_MONITOR
                , COMP_MOUSE, COMP_NAME, ROOMS_ID, COMP_OS, COMP_PR, COMP_RAM, COMP_VGA, COMP_STATUS
                , COLUMN_TOGGLE, COLUMN_SYNC, COLUMN_REF_ID, REPORT_MB_SERIAL, REPORT_MON_SERIAL};
        Cursor c = db.query(TABLE_REPORT_DETAILS, cols, null, null, null, null, null, null);
        return c;
    }

    //inventory request
    public long addReqInventory(int req_id, int rep_id, int room_id, String cust_id, String tech_id,
                                String date, String time, String msg, String date_req, String time_req, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROOMS_ID, room_id);
        values.put(REQ_ID, req_id);
        values.put(REPORT_ID, rep_id);
        values.put(COLUMN_CUST_ID, cust_id);
        values.put(COLUMN_TECH_ID, tech_id);
        values.put(REQ_DATE, date);
        values.put(REQ_TIME, time);
        values.put(REQ_MESSAGE, msg);
        values.put(DATE_OF_REQ, date_req);
        values.put(TIME_OF_REQ, time_req);
        values.put(REQ_STATUS, status);
        values.put(COLUMN_SYNC, 1);
        long insert = db.insert(TABLE_REQ_INVENTORY, null, values);
        return insert;
    }

    //req repair
    public long addReqRepair(int req_id, int rep_id, int comp_id, String cust_id, String tech_id,
                             String date, String time, String msg, String req_details,
                             String date_req, String time_req, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(REQ_ID, req_id);
        values.put(COMP_ID, comp_id);
        values.put(REPORT_ID, rep_id);
        values.put(COLUMN_CUST_ID, cust_id);
        values.put(COLUMN_TECH_ID, tech_id);
        values.put(REQ_DATE, date);
        values.put(REQ_TIME, time);
        values.put(REQ_MESSAGE, msg);
        values.put(REQ_DETAILS, req_details);
        values.put(DATE_OF_REQ, date_req);
        values.put(TIME_OF_REQ, time_req);
        values.put(REQ_STATUS, status);
        values.put(COLUMN_SYNC, 1);
        long insert = db.insert(TABLE_REQ_REPAIR, null, values);
        return insert;
    }


    //addpctoassess
    public long addPctoAssess(int comp_id,String comp_serial, String mb, String processor,
                              String monitor, String ram, String kboard,
                              String mouse, String status, String vga, String hdd, int pc_no, String model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COMP_ID, comp_id);
        values.put(COMP_NAME, pc_no);
        values.put(COMP_MODEL, model);
        values.put(COMP_SERIAL, comp_serial);
        values.put(COMP_MB, mb);
        values.put(COMP_PR, processor);
        values.put(COMP_MONITOR, monitor);
        values.put(COMP_RAM, ram);
        values.put(COMP_KBOARD, kboard);
        values.put(COMP_MOUSE, mouse);
        values.put(COMP_VGA, vga);
        values.put(COMP_HDD, hdd);
        values.put(COMP_STATUS, status);
        values.put(COLUMN_SCANNED, 0);


        long in = db.insert(PC_TO_ASSESS, null, values);
        return in;
    }

    public long getUnscannedCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, PC_TO_ASSESS, COLUMN_SCANNED
                + " = 1 ", null);
        return count;
    }

    public long pcToAssessCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, PC_TO_ASSESS);
        return count;
    }

    public void deletePcToAssess() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PC_TO_ASSESS);
    }
    public void updateScannedStatus(int scanned, int comp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCANNED, scanned);
        db.update(PC_TO_ASSESS, values, COMP_ID + " = ? ", new String[]{String.valueOf(comp)});
        db.close();
    }

    public Cursor getPcToAssessAsc(){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * from " + PC_TO_ASSESS + " ORDER BY " + COLUMN_SCANNED + " ASC, " + COMP_NAME + " ASC";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public Cursor getPcToAssess(int comp_id){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COMP_ID, COMP_SERIAL, COMP_MODEL,COMP_NAME, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, COLUMN_SCANNED};
        Cursor cursor = db.query(PC_TO_ASSESS, columns, COMP_ID + " = ?", new String[]
                {String.valueOf(comp_id)}, null, null, null);
        return cursor;
    }




    //assessed pc
    public long addAssessedPc(int comp_id, int pc_no,String model, String comp_serial, String mb,
                              String mb_serial, String processor,
                              String monitor,String mon_serial, String ram, String kboard,
                              String mouse, String status, String vga, String hdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COMP_ID, comp_id);
        values.put(COMP_NAME, pc_no);
        values.put(COMP_MODEL, model);
        values.put(COMP_SERIAL, comp_serial);
        values.put(COMP_MB, mb);
        values.put(REPORT_MB_SERIAL, mb_serial);
        values.put(COMP_PR, processor);
        values.put(COMP_MONITOR, monitor);
        values.put(REPORT_MON_SERIAL, mon_serial);
        values.put(COMP_RAM, ram);
        values.put(COMP_KBOARD, kboard);
        values.put(COMP_MOUSE, mouse);
        values.put(COMP_VGA, vga);
        values.put(COMP_HDD, hdd);
        values.put(COMP_STATUS, status);

        long rowInserted = db.insert(ASSESSED_PC, null, values);
        db.close();
        return rowInserted;
    }

    public Cursor getAssessedPc(){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COMP_ID, COMP_SERIAL, COMP_NAME,COMP_MODEL, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, REPORT_MB_SERIAL, REPORT_MON_SERIAL};
        Cursor cursor = db.query(ASSESSED_PC, columns, null, null, null, null, null);
        return cursor;
    }

    public void deleteAssessedPc() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + ASSESSED_PC);
    }
    public long assessedPcCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        long c = DatabaseUtils.queryNumEntries(db,ASSESSED_PC, null,null);
        return c;
    }
}
