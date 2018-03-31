package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 31 Mar 2018.
 */

public class Task {
    String date, time, desc,title;

    public Task(String date, String time, String desc, String title) {
        this.date = date;
        this.time = time;
        this.desc = desc;
        this.title = title;
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
