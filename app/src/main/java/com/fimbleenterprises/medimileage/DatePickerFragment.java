package com.fimbleenterprises.medimileage;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import org.joda.time.DateTime;

import java.util.Calendar;

import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    MyInterfaces.DateSelector listener;
    DateTime showDate;

    public DatePickerFragment(MyInterfaces.DateSelector listener) {
        this.listener = listener;
    }

    public void setShowDate(DateTime dateTime) {
        this.showDate = dateTime;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, showDate.getYear(),
                showDate.getMonthOfYear(), showDate.getDayOfMonth());
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        DateTime selectedDate = new DateTime(year, month, day, 0, 0);
        listener.onDateSelected(selectedDate, Helpers.DatesAndTimes.getPrettyDate(selectedDate));
    }
}
