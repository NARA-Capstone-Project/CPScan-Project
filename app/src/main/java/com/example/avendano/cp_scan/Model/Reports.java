package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class Reports {
    private int room_pc_id, rep_id;
    private String date, category, name;

    public String getName() {
        return name;
    }

    public Reports(String date, String category, String name, int room_pc_id, int rep_id) {
        this.date = date;
        this.category = category;
        this.room_pc_id = room_pc_id;
        this.rep_id = rep_id;
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public int getRoom_pc_id() {
        return room_pc_id;
    }

    public int getRep_id() {
        return rep_id;
    }
}
