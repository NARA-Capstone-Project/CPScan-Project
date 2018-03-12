package com.example.avendano.cp_scan.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

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
    public static final String ROOMS_LAST_ASSESS = "last_assess";

    //TABLE ROOM SCHED
    public static final String TABLE_ROOM_SCHED = "room_sched";
    public static final String SCHED_DAY = "room_sched";
    public static final String SCHED_FROM_TIME = "from_time";
    public static final String SCHED_TO_TIME = "to_time";

    //TABLE TASKS


    //TABLE COMPUTERS
    //foreign key room_id
    public static final String TABLE_COMPUTERS = "computers";
    public static final String COMP_ID = "comp_id";
    public static final String COMP_MODEL = "model";
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
    public static final String REPORT_SIGNED = "cust_signed";
    public static final String REPORT_REMARKS = "remarks"; // sa assessment details din may remarks for room


    //TABLE REPORT DETAILS
    public static final String TABLE_REPORT_DETAILS = "assessment_details";
    public static final String REPORT_MB_SERIAL = "mb_serial";
    public static final String REPORT_MON_SERIAL = "mon_serial";

    //TABLE UNSYNC REPORT DETAILS PAG NAGLOGOUT
    public static final String TABLE_UNSYNC_DETAILS = "unsync_details";
    //same ng a_details

    //TEMPORARY TABLES
    public static final String ASSESSED_PC = "assessed_pc";
    //id hindi isasama sa pag save sa server
    public static final String PC_TO_ASSESS = "pc_to_assess";


    public static final String COLUMN_SYNC = "sync_state"; // 0 nasa sqlite plang 1 pag nasa server na 2 kapag nasign na and kailngan isync ulit
    public static final String COLUMN_SCANNED = "scanned"; // if assessed/scanned 0 n 1
    public static final String COLUMN_TECH_ID = "technician_id";
    public static final String COLUMN_CUST_ID = "custodian_id";

    //CREATE QUERIES
    //ROOMS
    String createRooms = "CREATE TABLE " + TABLE_ROOMS + "("
            + ROOMS_ID + " INTEGER,"
            + ROOMS_NAME + " VARCHAR,"  //dept + room name
            + ROOMS_CUSTODIAN + " VARCHAR,"
            + COLUMN_CUST_ID + " VACHAR,"
            + ROOMS_TECHNICIAN + " VARCHAR,"
            + COLUMN_TECH_ID + " VACHAR,"
            + ROOMS_BUILDING + " VARCHAR,"
            + ROOMS_FLOOR + " INTEGER, "
            + ROOMS_PC_COUNT + " INTEGER, "
            + ROOMS_PC_WORKING + " INTEGER, "
            + ROOMS_LAST_ASSESS + " VARCHAR"
            + ")";//to sync check kung ung room_id na nasa server is wala sa local

    //SCHED
    String createSched = "CREATE TABLE " + TABLE_ROOM_SCHED + " ( "
            + ROOMS_ID + " INTEGER, "
            + ROOMS_CUSTODIAN + " VARCHAR, "
            + SCHED_DAY + " VARCHAR, "
            + SCHED_FROM_TIME + " VARCHAR, "
            + SCHED_TO_TIME + " VARCHAR "
            + ");";

    //COMPUTERS
    String createComputers = "CREATE TABLE " + TABLE_COMPUTERS + "("
            + COMP_ID + " INTEGER, " //FOREIGN KEY
            + ROOMS_ID + " INTEGER, "
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
            + COMP_STATUS + " VARCHAR "
            + " );";//to sync check kung ung comp_id na nasa server is wala sa local

    //LOCAL REPORT
    String createReport = "CREATE TABLE " + TABLE_ASSESSMENT_REPORT + " ( "
            + REPORT_ID + " INTEGER ," //PRIMARY KEY
            + ROOMS_ID + " INTEGER,"
            + ROOMS_NAME + " varchar, "
            + COLUMN_CUST_ID + " VARCHAR,"
            + COLUMN_TECH_ID + " VARCHAR,"  // yung nagassess
            + REPORT_CATEGORY + " varchar, " // hindi ipapasa sa server
            + REPORT_DATE + " DATE,"
            + REPORT_TIME + " TIME,"
            + REPORT_SIGNED + " TINYINT,"
            + REPORT_REMARKS + " TEXT"
            + ");";

    //LOCAL REPORT DETAILS
    String createReportDetails = "CREATE TABLE " + TABLE_REPORT_DETAILS + " ( "
            + REPORT_ID + " INTEGER, " //FOREIGN KEY
            + COMP_ID + " INTEGER, " //FOREIGN KEY
            + COMP_MODEL + " VARCHAR, "
            + COMP_NAME + " INTEGER,"
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

    //TEMPORARY TABLE
    String createAssessedPc = "CREATE TABLE " + ASSESSED_PC + " ( "
            + COMP_ID + " INTEGER, " //FOREIGN KEY
            + COMP_NAME + " INTEGER,"
            + COMP_MODEL + " VARCHAR, "
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
    public static final String DB_NAME = "db_temp";
    public static final int DB_VERSION = 4;


    public SQLiteHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createPcToAssess);
        db.execSQL(createAssessedPc);
        db.execSQL(createRooms);
        db.execSQL(createComputers);
        db.execSQL(createReport);
        db.execSQL(createReportDetails);
        db.execSQL(createSched);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DATABASE : ", "DB: " + DB_NAME + "DB VER: " + DB_VERSION);
        db.execSQL("DROP TABLE IF EXISTS " + PC_TO_ASSESS);
        db.execSQL("DROP TABLE IF EXISTS " + ASSESSED_PC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSESSMENT_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM_SCHED);
        onCreate(db);
    }

    //SCHED FUNCTIONS
    ////////////////////////////////////////////////////////////////////////////////////
    public long addSched(int room_id, String to_time, String from_time, String room_user, String day) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ROOMS_ID, room_id);
        values.put(ROOMS_CUSTODIAN, room_user);
        values.put(SCHED_TO_TIME, to_time);
        values.put(SCHED_FROM_TIME, from_time);
        values.put(SCHED_DAY, day);

        long insert = db.insert(TABLE_ROOM_SCHED, null, values);
        return insert;
    }

    public long getSchedCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long c = DatabaseUtils.queryNumEntries(db, TABLE_ROOM_SCHED);
        return c;
    }
    public void deleteSched() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ROOM_SCHED);
        db.close();
    }
    public Cursor getRoomSched(int room_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {SCHED_TO_TIME, SCHED_FROM_TIME, ROOMS_CUSTODIAN, SCHED_DAY};
        Cursor cursor = db.query(TABLE_ROOM_SCHED, columns, ROOMS_ID + " = ?"
                , new String[]{String.valueOf(room_id)}, null, null, null);
        return cursor;
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //COMPUTER FUNCTIONS
    ///////////////////////////////////////////////////////////////////////////////////
    public void updateComp(int comp_id, String vga, String kboard, String mouse, String status) {   //after assessment uupdate ung o.devices
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMP_KBOARD, kboard);
        values.put(COMP_MOUSE, mouse);
        values.put(COMP_VGA, vga);
        values.put(COMP_STATUS, status);
        db.update(TABLE_COMPUTERS, values, COMP_ID + " = " + comp_id, null);
        db.close();
    }

    public Cursor getCompInARoom(int room_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COMP_ID, COMP_MODEL, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_NAME, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, ROOMS_ID};
        Cursor cursor = db.query(TABLE_COMPUTERS, columns, ROOMS_ID + " = ?", new String[]{String.valueOf(room_id)}, null, null, COMP_NAME);
        return cursor;
    }

    public Cursor getAllComp() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COMP_ID, COMP_MODEL, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_NAME, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, ROOMS_ID};
        Cursor cursor = db.query(TABLE_COMPUTERS, columns, null, null, null, null, null);
        return cursor;
    }

    public Cursor getCompDetails(int comp_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COMP_ID, COMP_MODEL, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_NAME, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, ROOMS_ID};
        Cursor cursor = db.query(TABLE_COMPUTERS, columns, COMP_ID + " = ?", new String[]{String.valueOf(comp_id)}, null, null, COMP_NAME);
        return cursor;
    }

    public void deleteAllComp() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COMPUTERS);
        db.close();
    }

    public long addComputers(int comp_id, int room_id, int pc_name, String model, String mb, String processor,
                             String monitor, String ram, String kboard,
                             String mouse, String status, String vga, String hdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COMP_ID, comp_id);
        values.put(ROOMS_ID, room_id);
        values.put(COMP_NAME, pc_name);
        values.put(COMP_MODEL, model);
        values.put(COMP_MB, mb);
        values.put(COMP_PR, processor);
        values.put(COMP_MONITOR, monitor);
        values.put(COMP_RAM, ram);
        values.put(COMP_KBOARD, kboard);
        values.put(COMP_MOUSE, mouse);
        values.put(COMP_VGA, vga);
        values.put(COMP_HDD, hdd);
        values.put(COMP_STATUS, status);

        long rowInserted = db.insert(TABLE_COMPUTERS, null, values);
        return rowInserted;
    }

    public long getCompCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long c = DatabaseUtils.queryNumEntries(db, TABLE_COMPUTERS);
        return c;
    }

    public Cursor getCompIdAndModel(String serial) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_COMPUTERS, new String[]{COMP_ID, COMP_MODEL},
                COMP_MB + " = ? OR " + COMP_MONITOR + " = ?",
                new String[]{serial, serial}, null, null, null);

        return c;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //ROOM FUNCTIONS
    public long addRooms(int room_id, String room_name, String cust, String cust_id, String tech, String tech_id, String build,
                         int floor, int pc_count, int pc_working, String lastAssess) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ROOMS_ID, room_id);
        values.put(ROOMS_NAME, room_name);
        values.put(ROOMS_CUSTODIAN, cust);
        values.put(COLUMN_CUST_ID, cust_id);
        values.put(ROOMS_TECHNICIAN, tech);
        values.put(COLUMN_TECH_ID, tech_id);
        values.put(ROOMS_BUILDING, build);
        values.put(ROOMS_FLOOR, floor);
        values.put(ROOMS_PC_COUNT, pc_count);
        values.put(ROOMS_PC_WORKING, pc_working);
        values.put(ROOMS_LAST_ASSESS, lastAssess);

        long rowInserted = db.insert(TABLE_ROOMS, null, values);
        return rowInserted;
    }

    public long getRoomCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long c = DatabaseUtils.queryNumEntries(db, TABLE_ROOMS);
        return c;
    }

    public Cursor getAllRoom() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{ROOMS_PC_WORKING, ROOMS_PC_COUNT, ROOMS_ID, COLUMN_CUST_ID,
                COLUMN_TECH_ID, ROOMS_FLOOR, ROOMS_BUILDING, ROOMS_TECHNICIAN, ROOMS_CUSTODIAN, ROOMS_NAME};
        Cursor c = db.query(TABLE_ROOMS, cols, null, null, null
                , null, null);
        return c;
    }

    public Cursor getCustodianRoom(String user_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{ROOMS_PC_WORKING, ROOMS_PC_COUNT, ROOMS_ID, COLUMN_CUST_ID, COLUMN_TECH_ID, ROOMS_FLOOR, ROOMS_BUILDING, ROOMS_TECHNICIAN, ROOMS_CUSTODIAN, ROOMS_NAME};
        Cursor c = db.query(TABLE_ROOMS, cols, COLUMN_CUST_ID + " = ?", new String[]{user_id}, null
                , null, null);
        return c;
    }

    public void deleteRooms() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ROOMS);
        db.close();
    }

    public Cursor getRoomDetails(int room_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{ROOMS_PC_WORKING, ROOMS_PC_COUNT, ROOMS_ID, COLUMN_CUST_ID,
                COLUMN_TECH_ID, ROOMS_FLOOR, ROOMS_BUILDING, ROOMS_TECHNICIAN,
                ROOMS_CUSTODIAN, ROOMS_NAME, ROOMS_LAST_ASSESS};
        Cursor c = db.query(TABLE_ROOMS, cols, ROOMS_ID + " = ?", new String[]{String.valueOf(room_id)}, null
                , null, null);
        return c;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //REPORT FUNCTIONS
    public void deleteReport() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ASSESSMENT_REPORT);
        db.close();
    }


    public long addReport(int rep_id, int room_id, String cust_id, String category, String user_id, String date, String time
            , int signed, String remarks, String room_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(REPORT_ID, rep_id);
        values.put(ROOMS_ID, room_id);
        values.put(ROOMS_NAME, room_name);
        values.put(COLUMN_CUST_ID, cust_id);
        values.put(COLUMN_TECH_ID, user_id);
        values.put(REPORT_CATEGORY, category);
        values.put(REPORT_DATE, date);
        values.put(REPORT_TIME, time);
        values.put(REPORT_SIGNED, signed);
        values.put(REPORT_REMARKS, remarks);

        long rowInserted = db.insert(TABLE_ASSESSMENT_REPORT, null, values);
        db.close();
        return rowInserted;
    }

    public Cursor getReportByUserId(String user_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{ROOMS_NAME, COLUMN_TECH_ID, COLUMN_CUST_ID, REPORT_ID, ROOMS_ID,
                REPORT_REMARKS, REPORT_SIGNED, REPORT_DATE, REPORT_TIME, REPORT_CATEGORY};
        Cursor c = db.query(TABLE_ASSESSMENT_REPORT, cols, COLUMN_CUST_ID + " = ? OR " +
                COLUMN_TECH_ID + " = ?", new String[]{user_id, user_id}, null, null, REPORT_DATE);
        return c;
    }

    public Cursor getAllUnsyncReport() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_ASSESSMENT_REPORT + " WHERE " + COLUMN_SYNC + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public void updateReportSyncStatus(int rep_id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC, status);
        db.update(TABLE_ASSESSMENT_REPORT, values, REPORT_ID + " = ? "
                , new String[]{String.valueOf(rep_id)});
    }

    public Cursor getLastAssessment(int room_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{COLUMN_TECH_ID, COLUMN_CUST_ID, REPORT_ID, ROOMS_ID, REPORT_REMARKS, REPORT_SIGNED, REPORT_DATE, REPORT_TIME, REPORT_CATEGORY};
        Cursor c = db.query(TABLE_ASSESSMENT_REPORT, cols,
                ROOMS_ID + " = ?", new String[]{String.valueOf(room_id)}, null, null, REPORT_DATE + " DESC", "1");
        return c;
    }

    public Cursor getLastAssessment() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * from " + TABLE_ASSESSMENT_REPORT + " ORDER BY " + REPORT_DATE + " DESC LIMIT 1";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public long getReportCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_ASSESSMENT_REPORT);
        return count;
    }

    //////////////////////////////////////////////////////////////////////////////////
    //ASSESSMENT REPORT DETAILS FUNCTIONS
    public long addReportDetails(int rep_id, int comp_id, int pc_no, String model, String mb
            ,String mb_serial, String pr, String monitor,String mon_serial, String ram, String kboard, String mouse, String vga
            , String hdd, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(REPORT_ID, rep_id);
        values.put(COMP_ID, comp_id);
        values.put(COMP_NAME, pc_no);
        values.put(COMP_MODEL, model);
        values.put(COMP_MB, mb);
        values.put(REPORT_MB_SERIAL, mb_serial);
        values.put(COMP_PR, pr);
        values.put(COMP_MONITOR, monitor);
        values.put(REPORT_MON_SERIAL, mon_serial);
        values.put(COMP_RAM, ram);
        values.put(COMP_KBOARD, kboard);
        values.put(COMP_MOUSE, mouse);
        values.put(COMP_VGA, vga);
        values.put(COMP_HDD, hdd);
        values.put(COMP_STATUS, status);

        long rowInserted = db.insert(TABLE_REPORT_DETAILS, null, values);

        return rowInserted;
    }

    public Cursor getReportDetailsById(int rep_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{COMP_ID,COMP_NAME, COMP_MODEL, COMP_MB, COMP_PR, COMP_MONITOR,
                COMP_RAM, COMP_KBOARD, COMP_MOUSE, COMP_VGA, COMP_HDD, COMP_STATUS, REPORT_MB_SERIAL,REPORT_MON_SERIAL};
        Cursor c = db.query(TABLE_REPORT_DETAILS, cols, REPORT_ID + " = ?", new String[]{String.valueOf(rep_id)},
                null, null, null);
        return c;
    }

    public long getReportDetailsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_REPORT_DETAILS);
        return count;
    }

    public void deleteReportDetails() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_REPORT_DETAILS);
    }

    //////////////////////////////////////////////////////////////////////////////////
    //PC TO ASSESS FUNCTIONS
    public long addPctoAssess(int comp_id, String mb, String processor,
                                 String monitor, String ram, String kboard,
                                 String mouse, String status, String vga, String hdd, int pc_no, String model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COMP_ID, comp_id);
        values.put(COMP_NAME, pc_no);
        values.put(COMP_MODEL, model);
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

    public void updateScannedStatus(int scanned, int comp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCANNED, scanned);
        db.update(PC_TO_ASSESS, values, COMP_ID + " = ? ", new String[]{String.valueOf(comp)});
        db.close();
    }

    public void deletePcToAssess() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PC_TO_ASSESS);
    }
    public long pcToAssessCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        long c = DatabaseUtils.queryNumEntries(db,PC_TO_ASSESS, null,null);
        return c;
    }

    public Cursor getPcToAssessAsc(){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * from " + PC_TO_ASSESS + " ORDER BY " + COLUMN_SCANNED + " ASC";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public Cursor getPcToAssess(){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COMP_ID, COMP_MODEL,COMP_NAME, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, COLUMN_SCANNED};
        Cursor cursor = db.query(PC_TO_ASSESS, columns, null,null, null, null, null);
        return cursor;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //ASSESSED PC FUNCTIONS
    public long addAssessedPc(int comp_id, int pc_no,String model, String mb, String mb_serial, String processor,
                              String monitor,String mon_serial, String ram, String kboard,
                              String mouse, String status, String vga, String hdd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COMP_ID, comp_id);
        values.put(COMP_NAME, pc_no);
        values.put(COMP_MODEL, model);
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
        String[] columns = {COMP_ID, COMP_NAME,COMP_MODEL, COMP_MONITOR, COMP_HDD, COMP_KBOARD, COMP_MB, COMP_PR,
                COMP_RAM, COMP_STATUS, COMP_VGA, COMP_MOUSE, REPORT_MB_SERIAL, REPORT_MON_SERIAL};
        Cursor cursor = db.query(ASSESSED_PC, columns, null, null, null, null, null);
        return cursor;
    }

    public void deleteAssessedPc() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + ASSESSED_PC);
    }
    public long assessPcCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        long c = DatabaseUtils.queryNumEntries(db,ASSESSED_PC, null,null);
        return c;
    }


    public long getUnscannedCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, PC_TO_ASSESS, COLUMN_SCANNED
                + " = 1 ", null);
        return count;
    }
    public long getAssessPcCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, PC_TO_ASSESS);
        return count;
    }
    //////////////////////////////////////////////////////////////////////////////////
}
