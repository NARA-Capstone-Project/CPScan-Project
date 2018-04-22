package com.example.avendano.cp_scan.Model;

/**
 * Created by Avendano on 22 Apr 2018.
 */

public class PeripheralDetails {
    int qty, qty_issued;
    String unit, desc;

    public PeripheralDetails(int qty, int qty_issued, String unit, String desc) {
        this.qty = qty;
        this.qty_issued = qty_issued;
        this.unit = unit;
        this.desc = desc;
    }

    public int getQty() {
        return qty;
    }

    public int getQty_issued() {
        return qty_issued;
    }

    public String getUnit() {
        return unit;
    }

    public String getDesc() {
        return desc;
    }
}
