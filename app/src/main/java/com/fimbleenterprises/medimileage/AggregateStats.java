package com.fimbleenterprises.medimileage;

import android.util.Log;

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

public class AggregateStats extends ArrayList<AggregateStats.AggregateStat> {
    private static final String TAG = "AggregateStats";
    ArrayList<String> uniqueUserids = new ArrayList<>();

    public double thisMonthTotalPayout = 0;
    public double lastMonthTotalPayout = 0;
    public double lastLastMonthTotalPayout = 0;

    public double thisMonthTotalMiles = 0;
    public double lastMonthTotalMiles = 0;
    public double lastLastMonthTotalMiles = 0;

    public int thisMonthTripCount = 0;
    public int lastMonthTripCount = 0;
    public int lastLastMonthTripCount = 0;

    public int thisMonthManTripCount = 0;
    public int thisMonthEditedTripCount = 0;
    public int thisMonthTripMinderKillCount = 0;

    public int lastMonthManTripCount = 0;
    public int lastMonthEditedTripCOunt = 0;
    public int lastMonthTripMinderKillCount = 0;

    public int lastLastMonthManTripCount = 0;
    public int lastLastMonthEditedTripCount = 0;
    public int lastLastMonthTripMinderKillCount = 0;

    public ArrayList<UserTotals> topUserMilesThisMonth = new ArrayList<>();
    public ArrayList<UserTotals> topUserMilesLastMonth = new ArrayList<>();
    public ArrayList<UserTotals> topUserMilesLastLastMonth = new ArrayList<>();


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
                lastMonthEditedTripCOunt += (s.isEdited ? 1 : 0);
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
