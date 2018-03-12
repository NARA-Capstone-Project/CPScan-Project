package com.example.avendano.cp_scan.Getter_Setter;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class Computers {
    private String pc_status;
    private String model;
    private int pc_no;
    private int comp_id;

    public int getRoom_id() {
        return room_id;
    }

    private int room_id;
    public String getModel() {
        return model;
    }


    public Computers(String pc_status, int pc_no, String model, int comp_id, int room_id) {
        this.pc_status = pc_status;
        this.pc_no = pc_no;
        this.comp_id = comp_id;
        this.model= model;
        this.room_id = room_id;
    }

    public String getPc_status() {
        return pc_status;
    }

    public int getPc_no() {
        return pc_no;
    }

    public int getComp_id() {
        return comp_id;
    }

}
