package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class Rooms {

    private String room_custodian;
    private String room_technician;

    public String getRoom_technician() {
        return room_technician;
    }

    private String room_name; //fk users = name from users
    private String room_building; //fk d = dept_name
    private int room_id;

    public Rooms(int room_id, String room_custodian, String room_technician, String room_name, String room_building) {
        this.room_id = room_id;
        this.room_custodian = room_custodian;
        this.room_technician = room_technician;
        this.room_name = room_name;
        this.room_building = room_building;
    }
    public int getRoom_id() {
        return room_id;
    }

    public String getRoom_custodian() {
        return room_custodian;
    }

    public String getRoom_name() {
        return room_name;
    }

    public String getRoom_building() {
        return room_building;
    }

}
