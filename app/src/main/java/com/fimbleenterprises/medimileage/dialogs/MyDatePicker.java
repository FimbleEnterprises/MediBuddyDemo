package com.fimbleenterprises.medimileage.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.DatePicker;

import com.fimbleenterprises.medimileage.MyInterfaces;

import org.joda.time.DateTime;

import java.util.Calendar;

import androidx.annotation.NonNull;


/**
 * Created by weber on 02-Jul-2019.
 */


@SuppressLint("ValidFragment")
public class MyDatePicker implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "MyDatePicker";

    private static String singleDayDate;
    private static int singleDayDay = 0;
    private static int singleDayMonth = 0;
    private static int singleDayYear = 0;
    Context context;
    DateTime selectedDate;
    MyInterfaces.DateSelector callback;

    public MyDatePicker(@NonNull Context context, MyInterfaces.DateSelector callback) {

        this.context = context;
        final Calendar calendar = Calendar.getInstance();
        this.callback = callback;

        Log.d(TAG, "(onCreateDialog) Creating the single day date picker...");

        singleDayYear = calendar.get(Calendar.YEAR);
        singleDayMonth = calendar.get(Calendar.MONTH) - 1;
        singleDayDay = calendar.get(Calendar.DAY_OF_MONTH);

    }

    MyDatePicker(@NonNull Context context, DateTime dateTime, MyInterfaces.DateSelector callback) {
        this.context = context;
        final Calendar calendar = Calendar.getInstance();
        this.callback = callback;

        Log.d(TAG, "(onCreateDialog) Creating the single day date picker...");

        if (dateTime != null) {
            singleDayYear = dateTime.getYear();
            singleDayMonth = dateTime.getMonthOfYear() - 1;
            singleDayDay = dateTime.getDayOfMonth();
            singleDayDate = convertToString(dateTime);
        } else {
            singleDayYear = calendar.get(Calendar.YEAR);
            singleDayMonth = calendar.get(Calendar.MONTH) - 1;
            singleDayDay = calendar.get(Calendar.DAY_OF_MONTH);

        }
    }

/*    MyDatePicker(@NonNull Context context, String date, MyInterfaces.DateSelector callback) {
        this.context = context;
        final Calendar calendar = Calendar.getInstance();
        this.callback = callback;

        try {
            DateTime dateTime = MyHelper.DatesAndTimes.parseCrmDateOnly(date);
            if (dateTime != null) {
                singleDayYear = dateTime.getYear();
                singleDayMonth = dateTime.getMonthOfYear() - 1;
                singleDayDay = dateTime.getDayOfMonth();
                singleDayDate = convertToString(dateTime);
            } else {
                singleDayYear = calendar.get(Calendar.YEAR);
                singleDayMonth = calendar.get(Calendar.MONTH) - 1;
                singleDayDay = calendar.get(Calendar.DAY_OF_MONTH);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void show() {
        DatePickerDialog dialog = new DatePickerDialog(context, this, singleDayYear, singleDayMonth, singleDayDay);
        dialog.show();
    }

    private String convertToString(DateTime dateTime) {
        return dateTime.getMonthOfYear() + 1 + "/" + dateTime.getDayOfMonth() + "/" + dateTime.getYear();
    }

    @Override
    public void onDateSet(DatePicker view, int yy, int mm, int dd) {
        // Save the mm/dd/yy values to our static vars so we can set the
        // custom date dialog to these values
        // the next time it's shown (if it's shown) as a time saver
        singleDayYear = yy;
        singleDayMonth = mm;
        singleDayDay = dd;
        singleDayDate = mm + 1 + "/" + dd + "/" + yy;
        this.selectedDate = new DateTime(yy,mm + 1,dd,0,0);

        Log.d(TAG, "(onDateSet) User selected '" + singleDayDate + "' as their single date to search.");
        callback.onDateSelected(selectedDate, singleDayDate);
    }

    public String getSelectedDateAsString() {
        return singleDayDate;
    }

    public DateTime getSelectedDate() {
        return selectedDate;
    }
}
