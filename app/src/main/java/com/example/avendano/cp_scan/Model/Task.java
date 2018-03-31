package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class Task {
    String date, time, desc,title;
    int sched_id, room_pc_id;

    public int getSched_id() {
        return sched_id;
    }

    public int getRoom_pc_id() {
        return room_pc_id;
    }

    public Task(String date, String time, String desc, String title, int sched_id, int room_pc_id) {

        this.date = date;
        this.time = time;
        this.desc = desc;
        this.title = title;
        this.sched_id = sched_id;
        this.room_pc_id = room_pc_id;
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

    public String getTitle() {
        return title;
    }
}
