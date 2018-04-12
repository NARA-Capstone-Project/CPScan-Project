package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class Task {
    String date, time, desc,title, status;
    int room_pc_id, req_id;


    public int getReqid() {
        return req_id;
    }

    public int getRoom_pc_id() {
        return room_pc_id;
    }

    public Task(String date, String time, String desc, String title, int room_pc_id, int req_id, String status) {

        this.date = date;
        this.time = time;
        this.desc = desc;
        this.title = title;
        this.room_pc_id = room_pc_id;
        this.req_id = req_id;
        this.status = status;
    }


    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDesc() {
        return desc;
    }

    public String getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }
}
