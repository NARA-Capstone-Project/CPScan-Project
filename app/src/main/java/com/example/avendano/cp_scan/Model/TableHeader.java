package com.example.avendano.cp_scan.Model;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Avendano on 22 Apr 2018.
 */

public class TableHeader {
    Context mCtx;
    String[] headers ={"QTY", "UNIT", "PERIPHERALS DESCRIPTION", "QTY ISSUED"};
    String[][] data;

    public TableHeader(Context mCtx) {
        this.mCtx = mCtx;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[][] returnDataAsArray(ArrayList<PeripheralDetails> details) {
        PeripheralDetails peripheralDetails;
        data = new String[details.size()][4];

        for (int i = 0; i < details.size(); i++){
            peripheralDetails = details.get(i);

            data[i][0] = "" + peripheralDetails.getQty();
            data[i][1] = "" + peripheralDetails.getUnit();
            data[i][2] = "" + peripheralDetails.getDesc();
            data[i][3] = "" + peripheralDetails.getQty_issued();
        }
        return data;
    }
}
