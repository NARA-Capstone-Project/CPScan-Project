package com.example.avendano.cp_scan.Getter_Setter;

/**
 * Created by Avendano on 6 Mar 2018.
 */

public class RoomSchedule {

    String fromTime, toTime, prof;

    public RoomSchedule(String fromTime, String toTime, String prof) {
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.prof = prof;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public String getProf() {
        return prof;
    }
}
