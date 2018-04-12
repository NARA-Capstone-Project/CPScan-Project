package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 1 Apr 2018.
 */

public class RequestRepair {
    int req_id, comp_id;
    String custodian, tech, date, time, msg, date_req, time_req, req_status, image_path, rep_details;

    public RequestRepair(int req_id, int comp_id, String custodian, String tech, String date,
                         String time, String msg, String date_req, String time_req, String req_status
            , String image_path, String rep_details) {
        this.req_id = req_id;
        this.comp_id = comp_id;
        this.custodian = custodian;
        this.tech = tech;
        this.date = date;
        this.time = time;
        this.msg = msg;
        this.date_req = date_req;
        this.time_req = time_req;
        this.req_status = req_status;
        this.image_path = image_path;
        this.rep_details = rep_details;
    }

    public String getImage_path() {
        return image_path;
    }

    public String getRep_details() {
        return rep_details;
    }

    public String getCategory() {
        return "Repair Request";
    }

    public int getReq_id() {
        return req_id;
    }

    public int getComp_id() {
        return comp_id;
    }

    public String getCustodian() {
        return custodian;
    }

    public String getTech() {
        return tech;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getMsg() {
        return msg;
    }

    public String getDate_req() {
        return date_req;
    }

    public String getTime_req() {
        return time_req;
    }

    public String getReq_status() {
        return req_status;
    }
}
