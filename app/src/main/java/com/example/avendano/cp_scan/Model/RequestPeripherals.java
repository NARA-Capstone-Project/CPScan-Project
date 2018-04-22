package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 21 Apr 2018.
 */

public class RequestPeripherals {
    int req_id;
    String category, room_name, status;

    public RequestPeripherals(int req_id,  String room_name, String status) {
        this.req_id = req_id;
        this.room_name = room_name;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getReq_id() {
        return req_id;
    }

    public String getCategory() {
        return "Peripheral Request";
    }

    public String getRoom_name() {
        return room_name;
    }
}
