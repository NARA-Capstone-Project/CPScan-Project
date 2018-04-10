package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class Reports {
    private int room_id, rep_id;
    private String date, category, room_name;

    public String getRoom_name() {
        return room_name;
    }

    public Reports(String date, String category, String room_name, int room_id, int rep_id) {
        this.date = date;
        this.category = category;
        this.room_id = room_id;
        this.rep_id = rep_id;
        this.room_name = room_name;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public int getRoom_id() {
        return room_id;
    }

    public int getRep_id() {
        return rep_id;
    }
}
