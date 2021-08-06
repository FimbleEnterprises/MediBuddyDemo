package com.fimbleenterprises.medimileage.objects_and_containers;

import android.util.Log;

import com.fimbleenterprises.medimileage.Helpers;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class GoalSummary {

    private static final String TAG = "GoalSummary";

    public GoalSummary(CrmEntities.Goals.Goal goal, DateTime curDate, DateTime startDate, DateTime endDate) {
        this.daysBetweenStartAndEnd = Days.daysBetween(startDate.toLocalDate(), endDate.toLocalDate()).getDays() +1;
        this.amtNeededPerDay = goal.target / daysBetweenStartAndEnd;
        this.daysBetweenStartAndNow = Days.daysBetween(startDate.toLocalDate(), curDate.toLocalDate()).getDays() + 1;
        this.targetAmtForToday = amtNeededPerDay * daysBetweenStartAndNow;
        this.actualAmtForToday = goal.actual;
        this.goal = goal;
        Log.i(TAG, "getToDatePerformance Amt/Day: " + Helpers.Numbers.convertToCurrency(this.amtNeededPerDay));
        Log.i(TAG, "getToDatePerformance Today target: " + Helpers.Numbers.convertToCurrency(this.targetAmtForToday));
        Log.i(TAG, "getToDatePerformance Today actual: " + Helpers.Numbers.convertToCurrency(goal.actual));
    }

    float daysBetweenStartAndEnd;
    float amtNeededPerDay;
    CrmEntities.Goals.Goal goal;

    public String getDaysBetweenStartAndEnd() {
        return Helpers.Numbers.convertToCurrency(daysBetweenStartAndEnd);
    }

    public String getAmtNeededPerDay() {
        return Helpers.Numbers.convertToCurrency(amtNeededPerDay);
    }

    public String getDaysBetweenStartAndNow() {
        return Helpers.Numbers.convertToCurrency(daysBetweenStartAndNow);
    }

    public String getTargetAmtForToday() {
        return Helpers.Numbers.convertToCurrency(targetAmtForToday);
    }

    public String getActualAmtForToday() {
        return Helpers.Numbers.convertToCurrency(actualAmtForToday);
    }

    public String getPrettyPctAcheivedAsOfToday() {
        return Helpers.Numbers.formatAsOneDecimalPointNumber((this.actualAmtForToday / this.targetAmtForToday) * 100) + "%";
    }

    public float getPctAcheivedAsOfToday() {
        return (float) (Helpers.Numbers.formatAsOneDecimalPointNumber(
                (this.actualAmtForToday / this.targetAmtForToday) * 100));
    }

    float daysBetweenStartAndNow;
    float targetAmtForToday;
    float actualAmtForToday;



    @Override
    public String toString() {
        return "Target for today: " + Helpers.Numbers.convertToCurrency(this.targetAmtForToday) +
                ", Actual: " + Helpers.Numbers.convertToCurrency(this.actualAmtForToday) +
                ", Goal pct: " + Helpers.Numbers.formatAsOneDecimalPointNumber(this.goal.pct) + "%" +
                ", Calc pct: " + this.getPrettyPctAcheivedAsOfToday() ;
    }
}
