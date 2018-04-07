package com.example.avendano.cp_scan;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/**
 * Created by Avendano on 25 Mar 2018.
 */

public class DatePicker extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(),
                year, month, day);
        android.widget.DatePicker datePicker = datePickerDialog.getDatePicker();

        c.add(Calendar.WEEK_OF_MONTH, +1);
        long oneMonthAhead = c.getTimeInMillis();
        datePicker.setMaxDate(oneMonthAhead);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);

        return datePickerDialog;
    }
}
