package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class ReportDetails {
    String monitor, mb, pr, ram, hdd, vga, mouse, kb,status, model, serial;
    boolean isExpandable;
    int pc_no;

    public ReportDetails(String serial, int pc_no, String monitor, String mb, String pr, String ram, String hdd, String vga, String mouse, String kb, String status, String model, boolean isExpandable) {
        this.pc_no = pc_no;
        this.monitor = monitor;
        this.mb = mb;
        this.pr = pr;
        this.ram = ram;
        this.hdd = hdd;
        this.vga = vga;
        this.mouse = mouse;
        this.kb = kb;
        this.status = status;
        this.model = model;
        this.isExpandable = isExpandable;
        this.serial = serial;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public int getPc_no() {
        return pc_no;
    }

    public void setPc_no(int pc_no) {
        this.pc_no = pc_no;
    }

    public String getMonitor() {
        return monitor;
    }

    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }

    public String getMb() {
        return mb;
    }

    public void setMb(String mb) {
        this.mb = mb;
    }

    public String getPr() {
        return pr;
    }

    public void setPr(String pr) {
        this.pr = pr;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getHdd() {
        return hdd;
    }

    public void setHdd(String hdd) {
        this.hdd = hdd;
    }

    public String getVga() {
        return vga;
    }

    public void setVga(String vga) {
        this.vga = vga;
    }

    public String getMouse() {
        return mouse;
    }

    public void setMouse(String mouse) {
        this.mouse = mouse;
    }

    public String getKb() {
        return kb;
    }

    public void setKb(String kb) {
        this.kb = kb;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }
}
