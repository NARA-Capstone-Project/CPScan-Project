package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 1 Apr 2018.
 */

public class Search {
    String name;
    int id;

    public Search(String name, int id) {
        this.name = name;
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
