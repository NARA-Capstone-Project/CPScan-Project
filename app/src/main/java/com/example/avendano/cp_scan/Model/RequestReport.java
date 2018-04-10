package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 2 Apr 2018.
 */

public class RequestReport {
    private int  rep_id;
    private String date, category, name;

    public RequestReport(int rep_id, String date, String category, String name) {
        this.rep_id = rep_id;
        this.date = date;
        this.category = category;
        this.name = name;
    }

    public int getRep_id() {
        return rep_id;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
}