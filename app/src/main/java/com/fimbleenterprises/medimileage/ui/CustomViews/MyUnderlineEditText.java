package com.fimbleenterprises.medimileage.ui.CustomViews;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.fimbleenterprises.medimileage.Helpers;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.dialogs.MyDatePicker;

import org.joda.time.DateTime;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class MyUnderlineEditText extends AppCompatEditText implements View.OnClickListener {

    private MyDatePicker datePicker;
    private DateTime dateSelected;
    private Context context;
    private boolean isDatePicker = false;

    public MyUnderlineEditText(Context context) {
        super(context);
        dateSelected = DateTime.now();
        this.context = context;
        underline();
    }

    public MyUnderlineEditText(Context context, MyDatePicker datePicker) {
        super(context);
        dateSelected = DateTime.now();
        this.context = context;
        this.datePicker = datePicker;
    }

    public MyUnderlineEditText(Context context, AttributeSet attrs, MyDatePicker datePicker) {
        super(context, attrs);
        dateSelected = DateTime.now();
        this.context = context;
        this.datePicker = datePicker;
    }

    public MyUnderlineEditText(Context context, AttributeSet attrs, int defStyleAttr, MyDatePicker datePicker) {
        super(context, attrs, defStyleAttr);
        dateSelected = DateTime.now();
        this.datePicker = datePicker;
        this.context = context;
    }

    public MyUnderlineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dateSelected = DateTime.now();
        this.context = context;
    }

    public MyUnderlineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        dateSelected = DateTime.now();
        this.context = context;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {

        super.setOnClickListener(l);
    }

    /**
     * Underlines and colors blue the text in an psuedo hyperlink fashion.
     */
    private void underline() {
        this.setPaintFlags(this.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        this.setTextColor(Color.BLUE);
    }

    /**
     * Adds a DatePicker that is shown OnClick.
     * @param val Whether to make onClicks show a DatePicker
     */
    public void setAsDatePicker(boolean val) {

        this.isDatePicker = val;

        if (this.isDatePicker) {

            // Set up the DatePicker
            this.datePicker = new MyDatePicker(this.context, new MyInterfaces.DateSelector() {
                @Override
                public void onDateSelected(DateTime selectedDate, String selectedDateStr) {
                    setDate(selectedDate);
                }
            });

            // Set up an OnClickListener
            this.setOnClickListener(this);
            underline();

        } // end isDatePicker
    }

    @Override
    public void onClick(View v) {
        if (this.isDatePicker) {
            this.datePicker.show();
        }
    }

    /**
     *
     * @return Whether or not this EditText is a DatePicker as well
     */
    public boolean isDatePicker() {
        return this.isDatePicker;
    }

    private void setDate(DateTime date) {
        this.dateSelected = date;
        this.setText(Helpers.DatesAndTimes.getPrettyDate(date));
    }

    public DateTime getDateSelectedAsDateTime() {
        return this.dateSelected;
    }

    /**
     * Gets the date chosen
     * @return The chosen date
     */
    public String getDateSelectedAsString() {
        return Helpers.DatesAndTimes.getPrettyDate(this.dateSelected);
    }
}
