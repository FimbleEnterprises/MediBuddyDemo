package com.fimbleenterprises.medimileage.objects_and_containers;

import android.util.Log;

import com.fimbleenterprises.medimileage.Helpers;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

public class AggregateStats extends ArrayList<AggregateStats.AggregateStat> {
    public static final int AGGREGATE_TOTALS_SHEET = 0;
    public static final int THIS_MONTH_SHEET = 1;
    public static final int LAST_MONTH_SHEET = 2;
    public static final int LAST_LAST_MONTH_SHEET = 3;
    public static final int RAW_DATA_SHEET = 4;
    
    private static final String TAG = "AggregateStats";
    ArrayList<String> uniqueUserids = new ArrayList<>();

    public float thisMonthTotalPayout;
    public float lastMonthTotalPayout;
    public float lastLastMonthTotalPayout;

    public float thisMonthTotalMiles;
    public float lastMonthTotalMiles;
    public float lastLastMonthTotalMiles;

    public float thisMonthTripCount;
    public float lastMonthTripCount;
    public float lastLastMonthTripCount;

    public float thisMonthManTripCount;
    public float thisMonthEditedTripCount;
    public float thisMonthTripMinderKillCount;

    public float lastMonthManTripCount;
    public float lastMonthEditedTripCount;
    public float lastMonthTripMinderKillCount;

    public float lastLastMonthManTripCount;
    public float lastLastMonthEditedTripCount;
    public float lastLastMonthTripMinderKillCount;
    
    public float thisMonthAverageTripLength;
    public float lastMonthAverageTripLength;
    public float lastLastMonthAverageTripLength;

    public float thisMonthAverageReimbursement;
    public float lastMonthAverageReimbursement;
    public float lastLastMonthAverageReimbursement;

    public float thisMonthEditedPct;
    public float lastMonthEditedPct;
    public float lastLastMonthEditedPct;
    
    public float thisMonthManualPct;
    public float lastMonthManualPct;
    public float lastLastMonthManualPct;

    public float thisMonthAutoKillPct;
    public float lastMonthAutoKillPct;
    public float lastLastMonthAutoKillPct;
    
    public ArrayList<UserTotals> topUserMilesThisMonth = new ArrayList<>();
    public ArrayList<UserTotals> topUserMilesLastMonth = new ArrayList<>();
    public ArrayList<UserTotals> topUserMilesLastLastMonth = new ArrayList<>();

    public float convertToPct(float val) {
        String rslt = Helpers.Numbers.convertToPercentage((double) val, false);
        return Float.parseFloat(rslt);
    }

    public String convertToPct(float val, boolean includeSymbol) {
        return Helpers.Numbers.convertToPercentage((double) val, includeSymbol);
    }

    public float convertToCurrency(float val) {
        String rslt = Helpers.Numbers.convertToCurrency((double) val, false);
        return Float.parseFloat(rslt);
    }

    public String convertToCurrency(float val, boolean includeSymbol) {
        return Helpers.Numbers.convertToCurrency((double) val, includeSymbol);
    }

    public AggregateStats(String crmResponse) {
        try {
            ArrayList<String> userids = new ArrayList<>();
            JSONObject rootObject = new JSONObject(crmResponse);
            JSONArray array = rootObject.getJSONArray("value");
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                AggregateStat stat = new AggregateStat(jsonObject);
                this.add(stat);
                userids.add(stat.ownerid);
            }
            setUniqueUserids(userids);
            aggregateUserMetrics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUniqueUserids(ArrayList<String> tempUsers) {
        this.uniqueUserids.clear();
        Set<String> uniqueUsers = new HashSet<>(tempUsers);
        for (String userid : uniqueUsers) {
            this.uniqueUserids.add(userid);
        }
        Log.i(TAG, "setUniqueUsers | " + uniqueUsers.size() + " unique users");

    }

    private void aggregateUserMetrics() {

        // Reset values just cus
        topUserMilesThisMonth.clear();
        topUserMilesLastMonth.clear();
        topUserMilesLastLastMonth.clear();
        thisMonthTripCount = 0;
        lastMonthTripCount = 0;
        lastLastMonthTripCount = 0;

        int thisMonth = DateTime.now().getMonthOfYear();
        int lastMonth = DateTime.now().minusMonths(1).getMonthOfYear();
        int lastLastMonth = DateTime.now().minusMonths(2).getMonthOfYear();

        // Do cpy-wide totals
        for (AggregateStat s : this) {
            // Do cpy-wide totals
            if (s.tripDate.getMonthOfYear() == thisMonth) {
                thisMonthTotalPayout += s.reimbursement;
                thisMonthTotalMiles += s.distanceMiles;
                thisMonthTripCount += 1;
                thisMonthEditedTripCount += (s.isEdited ? 1 : 0);
                thisMonthManTripCount += (s.isManual ? 1 : 0);
                thisMonthTripMinderKillCount += (s.tripMinderKilled ? 1 : 0);
            } else if (s.tripDate.getMonthOfYear() == lastMonth) {
                lastMonthTotalPayout += s.reimbursement;
                lastMonthTotalMiles += s.distanceMiles;
                lastMonthTripCount += 1;
                lastMonthEditedTripCount += (s.isEdited ? 1 : 0);
                lastMonthManTripCount += (s.isManual ? 1 : 0);
                lastMonthTripMinderKillCount += (s.tripMinderKilled ? 1 : 0);
            } else if (s.tripDate.getMonthOfYear() == lastLastMonth) {
                lastLastMonthTotalPayout += s.reimbursement;
                lastLastMonthTotalMiles += s.distanceMiles;
                lastLastMonthTripCount += 1;
                lastLastMonthEditedTripCount += (s.isEdited ? 1 : 0);
                lastLastMonthManTripCount += (s.isManual ? 1 : 0);
                lastLastMonthTripMinderKillCount += (s.tripMinderKilled ? 1 : 0);
            }
        }
        
        // Calculate averages
        thisMonthAverageTripLength = (float) (thisMonthTotalMiles / thisMonthTripCount);
        lastMonthAverageTripLength = (float) (lastMonthTotalMiles / lastMonthTripCount);
        lastLastMonthAverageTripLength = (float) (lastLastMonthTotalMiles / lastLastMonthTripCount);

        thisMonthAverageReimbursement = (float) (thisMonthTotalPayout / thisMonthTripCount);
        lastMonthAverageReimbursement = (float) (lastMonthTotalPayout / lastMonthTripCount);
        lastLastMonthAverageReimbursement = (float) (lastLastMonthTotalPayout / lastLastMonthTripCount);

        // Calculate percentages
        thisMonthEditedPct = thisMonthEditedTripCount / thisMonthTripCount;
        lastMonthEditedPct = lastMonthEditedTripCount / lastMonthTripCount;
        lastLastMonthEditedPct = lastLastMonthEditedTripCount / lastLastMonthTripCount;

        thisMonthManualPct = thisMonthManTripCount / thisMonthTripCount;
        lastMonthManualPct = lastMonthManTripCount / lastMonthTripCount;
        lastLastMonthManualPct = lastLastMonthManTripCount / lastLastMonthTripCount;

        thisMonthAutoKillPct = thisMonthTripMinderKillCount / thisMonthTripCount;
        lastMonthAutoKillPct = lastMonthTripMinderKillCount / lastMonthTripCount;
        lastLastMonthAutoKillPct = lastLastMonthTripMinderKillCount / lastLastMonthTripCount;
        
        // Do user totals
        for (String systemuserid : uniqueUserids) {
            Log.i(TAG, "aggregateUserMetrics analyzing user: " + systemuserid);
            UserTotals thisUsersTotalsThisMonth = new UserTotals();
            UserTotals thisUsersTotalsLastMonth = new UserTotals();
            UserTotals thisUsersTotalsLastLastMonth = new UserTotals();

            for (AggregateStat stat : this) {

                if (stat.ownerid.equals(systemuserid) && stat.ownerid != null && systemuserid != null) {
                    if (stat.tripDate.getMonthOfYear() == thisMonth) {
                        thisUsersTotalsThisMonth.fullname = stat.ownerName;
                        thisUsersTotalsThisMonth.systemuserid = stat.ownerid;
                        thisUsersTotalsThisMonth.totalMiles += Helpers.Numbers.formatAsZeroDecimalPointNumber
                                (stat.distanceMiles, RoundingMode.UP);
                        thisUsersTotalsThisMonth.totalReimbursement += stat.reimbursement;
                    } else if (stat.tripDate.getMonthOfYear() == lastMonth) {
                        thisUsersTotalsLastMonth.fullname = stat.ownerName;
                        thisUsersTotalsLastMonth.systemuserid = stat.ownerid;
                        thisUsersTotalsLastMonth.totalMiles += Helpers.Numbers.formatAsZeroDecimalPointNumber
                                (stat.distanceMiles, RoundingMode.UP);
                        thisUsersTotalsLastMonth.totalReimbursement += stat.reimbursement;
                    } else if (stat.tripDate.getMonthOfYear() == lastLastMonth) {
                        thisUsersTotalsLastLastMonth.fullname = stat.ownerName;
                        thisUsersTotalsLastLastMonth.systemuserid = stat.ownerid;
                        thisUsersTotalsLastLastMonth.totalMiles += Helpers.Numbers.formatAsZeroDecimalPointNumber
                                (stat.distanceMiles, RoundingMode.UP);
                        thisUsersTotalsLastLastMonth.totalReimbursement += stat.reimbursement;
                    }
                }
            } // each stat

            // Apply the aggregated totals for the current user in the loop to the all user's totals
            if (systemuserid != null && thisUsersTotalsThisMonth.systemuserid != null) {
                topUserMilesThisMonth.add(thisUsersTotalsThisMonth);
            }
            if (systemuserid != null && thisUsersTotalsLastMonth.systemuserid != null) {
                topUserMilesLastMonth.add(thisUsersTotalsLastMonth);
            }
            if (systemuserid != null && thisUsersTotalsLastLastMonth.systemuserid != null) {
                topUserMilesLastLastMonth.add(thisUsersTotalsLastLastMonth);
            }
            Log.i(TAG, "aggregateUserMetrics | Added this month's metrics for: " + thisUsersTotalsThisMonth.fullname + "(Miles: " + thisUsersTotalsThisMonth.totalMiles
                    + ", Reimbursement: " + Helpers.Numbers.convertToCurrency(thisUsersTotalsThisMonth.totalReimbursement));
            Log.i(TAG, "aggregateUserMetrics | Added last month's metrics for: " + thisUsersTotalsLastMonth.fullname + "(Miles: " + thisUsersTotalsLastMonth.totalMiles
                    + ", Reimbursement: " + Helpers.Numbers.convertToCurrency(thisUsersTotalsLastMonth.totalReimbursement));
            Log.i(TAG, "aggregateUserMetrics | Added last last month's metrics for: " + thisUsersTotalsLastLastMonth.fullname + "(Miles: " + thisUsersTotalsLastMonth.totalMiles
                    + ", Reimbursement: " + Helpers.Numbers.convertToCurrency(thisUsersTotalsLastLastMonth.totalReimbursement));
        } // each user

        sortUserTotalsByDistance(topUserMilesThisMonth, true);
        sortUserTotalsByDistance(topUserMilesLastMonth, true);
        sortUserTotalsByDistance(topUserMilesLastLastMonth, true);

    }

    public ExcelSpreadsheet exportToExcel() {
        String monthYear = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear())
            .toLowerCase().replace(" ", "") + "_" + DateTime.now().getYear();
        return exportToExcel("milebuddy_aggregate_mileage_export_" + monthYear + ".xls");
    }

    public ExcelSpreadsheet exportToExcel(String filename) {

        if (this == null || this.size() < 1) {
            // Toast.makeText(this, "No stats found!", Toast.LENGTH_SHORT).show();
            return null;
        }

        ExcelSpreadsheet spreadsheet = null;

        // Create a new spreadsheet
        try {
            spreadsheet = new ExcelSpreadsheet(filename);
        } catch (Exception e) {
            // Toast.makeText(this, "Failed to create spreadsheet!\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }

        // Add the sheets that we will populate
        try {
            String monthname;

            // All raw trips last 2 months
            spreadsheet.createSheet("Agregated data", AGGREGATE_TOTALS_SHEET);
            ;

            // This month
            monthname = Helpers.DatesAndTimes.getMonthName(DateTime.now().getMonthOfYear());
            spreadsheet.createSheet(monthname + " " + DateTime.now().getYear(), THIS_MONTH_SHEET);

            // One month ago
            monthname = Helpers.DatesAndTimes.getMonthName(DateTime.now().minusMonths(1).getMonthOfYear());
            spreadsheet.createSheet(monthname + " " + DateTime.now().minusMonths(1).getYear(), LAST_MONTH_SHEET);

            // Two months ago
            monthname = Helpers.DatesAndTimes.getMonthName(DateTime.now().minusMonths(2).getMonthOfYear());
            spreadsheet.createSheet(monthname + " " + DateTime.now().minusMonths(2).getYear(), LAST_LAST_MONTH_SHEET);

            // All raw trips last 2 months
            spreadsheet.createSheet("All trips raw data", RAW_DATA_SHEET);
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Format header and content values
        WritableFont headerFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.BOLD);
        try {
            headerFont.setColour(Colour.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

        WritableFont contentFont = new WritableFont(WritableFont.TAHOMA, 10, WritableFont.NO_BOLD);
        try {
            contentFont.setColour(Colour.BLACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        WritableCellFormat contentFormat = new WritableCellFormat(contentFont);

        // *****************************************************************************************
        //                                 AGGREGATE TOTALS SHEET
        // *****************************************************************************************

        // trip count
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 0, "This month trips:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 0, this.thisMonthTripCount + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 1, "Last month trips:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 1, this.lastMonthTripCount + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 2, "Two months ago trips:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 2, this.lastLastMonthTripCount + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 3, "", contentFormat);

        // total miles
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 4, "This month total miles:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 4, this.thisMonthTotalMiles + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 5, "Last month total miles:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 5, this.lastMonthTotalMiles + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 6, "Two months ago total miles:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 6, this.lastLastMonthTotalMiles + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 7, "", contentFormat);

        // avg length
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 8, "This month average length:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 8, this.thisMonthAverageTripLength + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 9, "Last month average length:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 9, this.lastMonthAverageTripLength + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 10, "Two months ago average length:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 10, this.lastLastMonthAverageTripLength + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 11, "", contentFormat);

        // avg reimbursement
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 12, "This month avg payout:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 12, this.convertToCurrency(this.thisMonthAverageReimbursement) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 13, "Last month avg payout:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 13, this.convertToCurrency(this.lastMonthAverageReimbursement) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 14, "Two months ago avg payout:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 14, this.convertToCurrency(this.lastLastMonthAverageReimbursement) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 15, "", contentFormat);

        // man trip pct
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 16, "This month manual pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 16, this.convertToPct(this.thisMonthManualPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 17, "Last month manual pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 17, this.convertToPct(this.lastMonthManualPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 18, "Two months ago manual pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 18, this.convertToPct(this.lastLastMonthManualPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 19, "", contentFormat);

        // edited pct
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 20, "This month edited pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 20, this.convertToPct(this.thisMonthEditedPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 21, "Last month edited pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 21, this.convertToPct(this.lastMonthEditedPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 22, "Two months ago edited pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 22, this.convertToPct(this.lastLastMonthEditedPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 23, "", contentFormat);

        // auto killed pct
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 24, "This month auto kill pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 24, this.convertToPct(this.thisMonthAutoKillPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 25, "Last month auto kill pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 25, this.convertToPct(this.lastMonthAutoKillPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 26, "Two months ago auto kill pct:", contentFormat);
        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 1, 26, this.convertToPct(this.lastLastMonthAutoKillPct) + "", contentFormat);

        spreadsheet.addCell(AGGREGATE_TOTALS_SHEET, 0, 27, "", contentFormat);

        // *****************************************************************************************
        //                                 THIS MONTH TOTALS SHEET
        // *****************************************************************************************
        try {
            // Trip count
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 0, "Trip count:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 0, Float.toString(this.thisMonthTripCount), contentFormat);

            // Total miles
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 1, "Total miles:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 1, Float.toString(this.thisMonthTotalMiles), contentFormat);

            // Total payout
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 2, "Total payout:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 2, Float.toString(this.thisMonthTotalPayout), contentFormat);

            // Total edited trips
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 3, "Total edited trips:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 3, Float.toString(this.thisMonthEditedTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 4, "Total manual trips:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 4, Float.toString(this.thisMonthManTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 5, "Total auto-stopped trips:", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 5, Float.toString(this.thisMonthTripMinderKillCount), contentFormat);

            // By user header
            spreadsheet.addCell(THIS_MONTH_SHEET, 0, 7, "Name", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 1, 7, "Total miles", headerFormat);
            spreadsheet.addCell(THIS_MONTH_SHEET, 2, 7, "Total reimbursement", headerFormat);

            // By user trip counts
            for (int i = 0; i < this.topUserMilesThisMonth.size(); i++) {
                AggregateStats.UserTotals userTotals = this.topUserMilesThisMonth.get(i);
                spreadsheet.addCell(THIS_MONTH_SHEET, 0, i + 8, userTotals.fullname, contentFormat);
                spreadsheet.addCell(THIS_MONTH_SHEET, 1, i + 8, Double.toString(userTotals.totalMiles), contentFormat);
                spreadsheet.addCell(THIS_MONTH_SHEET, 2, i + 8, Double.toString(userTotals.totalReimbursement), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // *****************************************************************************************
        //                                 LAST MONTH TOTALS SHEET
        // *****************************************************************************************
        try {
            // Trip count
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 0, "Trip count:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 0, Float.toString(this.lastMonthTripCount), contentFormat);

            // Total miles
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 1, "Total miles:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 1, Float.toString(this.lastMonthTotalMiles), contentFormat);

            // Total payout
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 2, "Total payout:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 2, Float.toString(this.lastMonthTotalPayout), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 3, "Total edited trips:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 3, Float.toString(this.lastMonthEditedTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 4, "Total manual trips:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 4, Float.toString(this.lastMonthManTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 5, "Total auto-stopped trips:", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 5, Float.toString(this.lastMonthTripMinderKillCount), contentFormat);

            // By user header
            spreadsheet.addCell(LAST_MONTH_SHEET, 0, 7, "Name", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 1, 7, "Total miles", headerFormat);
            spreadsheet.addCell(LAST_MONTH_SHEET, 2, 7, "Total reimbursement", headerFormat);

            // By user trip counts
            for (int i = 0; i < this.topUserMilesLastMonth.size(); i++) {
                AggregateStats.UserTotals userTotals = this.topUserMilesLastMonth.get(i);
                spreadsheet.addCell(LAST_MONTH_SHEET, 0, i + 8, userTotals.fullname, contentFormat);
                spreadsheet.addCell(LAST_MONTH_SHEET, 1, i + 8, Double.toString(userTotals.totalMiles), contentFormat);
                spreadsheet.addCell(LAST_MONTH_SHEET, 2, i + 8, Double.toString(userTotals.totalReimbursement), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // *****************************************************************************************
        //                              LAST LAST MONTH TOTALS SHEET
        // *****************************************************************************************
        try {
            // Trip count
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 0, "Trip count:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 0, Float.toString(this.lastLastMonthTripCount), contentFormat);

            // Total miles
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 1, "Total miles:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 1, Float.toString(this.lastLastMonthTotalMiles), contentFormat);

            // Total payout
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 2, "Total payout:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 2, Float.toString(this.lastLastMonthTotalPayout), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 3, "Total edited trips:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 3, Float.toString(this.lastLastMonthEditedTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 4, "Total manual trips:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 4, Float.toString(this.lastLastMonthManTripCount), contentFormat);

            // Total edited trips
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 5, "Total auto-stopped trips:", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 5, Float.toString(this.lastLastMonthTripMinderKillCount), contentFormat);

            // By user header
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, 7, "Name", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, 7, "Total miles", headerFormat);
            spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 2, 7, "Total reimbursement", headerFormat);

            // By user trip counts
            for (int i = 0; i < this.topUserMilesLastLastMonth.size(); i++) {
                AggregateStats.UserTotals userTotals = this.topUserMilesLastLastMonth.get(i);
                spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 0, i + 8, userTotals.fullname, contentFormat);
                spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 1, i + 8, Double.toString(userTotals.totalMiles), contentFormat);
                spreadsheet.addCell(LAST_LAST_MONTH_SHEET, 2, i + 8, Double.toString(userTotals.totalReimbursement), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // *****************************************************************************************
        //                                 RAW DATA SHEET
        // *****************************************************************************************
        try {
            spreadsheet.addCell(RAW_DATA_SHEET, 0, 0, "Trip name", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 1, 0, "Date", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 2, 0, "Driver", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 3, 0, "Distance (mi)", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 4, 0, "Reimbursement", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 5, 0, "Manual trip", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 6, 0, "Edited trip", headerFormat);
            spreadsheet.addCell(RAW_DATA_SHEET, 7, 0, "Auto-stopped trip", headerFormat);

            for (int i = 0; i < this.size(); i++) {
                AggregateStats.AggregateStat stat = this.get(i);
                spreadsheet.addCell(RAW_DATA_SHEET, 0, i + 1, stat.tripName, contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 1, i + 1, Helpers.DatesAndTimes.getPrettyDateAndTime(stat.tripDate), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 2, i + 1, stat.ownerName, contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 3, i + 1, Double.toString(stat.distanceMiles), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 4, i + 1, Double.toString(stat.reimbursement), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 5, i + 1, Boolean.toString(stat.isManual), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 6, i + 1, Boolean.toString(stat.isEdited), contentFormat);
                spreadsheet.addCell(RAW_DATA_SHEET, 7, i + 1, Boolean.toString(stat.tripMinderKilled), contentFormat);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save the file
        spreadsheet.save();

        return spreadsheet;

    }

    public static class AggregateStat {

        public String etag;
        public DateTime tripDate;
        public String ownerid;
        public String ownerName;
        public double reimbursement;
        public double distanceMiles;
        public int durationMinutes;
        public String tripid;
        public String tripName;
        public boolean isEdited;
        public boolean isManual;
        public boolean tripMinderKilled;

        public AggregateStat(JSONObject json) {
            try {
                if (!json.isNull("etag")) {
                    this.etag = (json.getString("etag"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_dt_tripdate")) {
                    this.tripDate = (new DateTime(json.getString("msus_dt_tripdate")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_ownerid_value")) {
                    this.ownerid = (json.getString("_ownerid_value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("_ownerid_valueFormattedValue")) {
                    this.ownerName = (json.getString("_ownerid_valueFormattedValue"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_reimbursement")) {
                    this.reimbursement = (json.getDouble("msus_reimbursement"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_totaldistance")) {
                    this.distanceMiles = (json.getDouble("msus_totaldistance"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_trip_duration")) {
                    this.durationMinutes = (int) (Double.parseDouble(json.getString("msus_trip_duration")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_fulltripid")) {
                    this.tripid = (json.getString("msus_fulltripid"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_name")) {
                    this.tripName = (json.getString("msus_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /* metadata */
            try {
                if (!json.isNull("msus_edited")) {
                    this.isEdited = (json.getBoolean("msus_edited"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_trip_minder_killed")) {
                    this.tripMinderKilled = (json.getBoolean("msus_trip_minder_killed"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (!json.isNull("msus_is_manual")) {
                    this.isManual = (json.getBoolean("msus_is_manual"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return this.tripDate + " - " + this.ownerName + " - " + this.distanceMiles + " miles - $" + this.reimbursement;
        }

    }

    public class UserTotals {
        public String systemuserid;
        public String fullname;
        public double totalMiles;
        public double totalReimbursement;

        @Override
        public String toString() {
            try {
                return this.fullname + " - " + this.totalMiles + " miles - " + Helpers.Numbers.convertToCurrency(this.totalReimbursement);
            } catch (Exception e) {
                e.printStackTrace();
                return this.fullname;
            }
        }
    }

    private static class CompareMiles implements Comparator<UserTotals> {
        private int mod = 1;
        public CompareMiles(boolean desc) {
            if (desc) mod =-1;
        }
        @Override
        public int compare(UserTotals arg0, UserTotals arg1) {
            return mod* (Integer.valueOf((int)arg0.totalMiles).compareTo(Integer.valueOf((int)arg1.totalMiles)));
        }
    }

    public static void sortUserTotalsByDistance(ArrayList<UserTotals> totals, final boolean descending) {
        Collections.sort(totals, new CompareMiles(true));
        /*Collections.sort(totals, new Comparator<UserTotals>() {
            @Override public int compare(UserTotals p1, UserTotals p2) {
                if (descending) {
                    return ( (int) p1.totalMiles - (int) p2.totalMiles); // Ascending
                } else {
                    return ( (int) p2.totalMiles - (int) p1.totalMiles); // Descending
                }
            }

        });*/
    }
}
