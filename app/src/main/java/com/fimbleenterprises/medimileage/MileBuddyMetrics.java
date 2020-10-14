package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;

import cz.msebera.android.httpclient.Header;

public class MileBuddyMetrics {

    public static final String TAG = "MileBuddyMetrics";

    public enum MetricName {
        LAST_OPENED_APP,
        LAST_ACCESSED_TERRITORY_DATA,
        LAST_ACCESSED_TERRITORY_CHANGER,
        LAST_ACCESSED_OTHER_USER_TRIPS,
        LAST_ACCESSED_GENERATE_RECEIPT,
        LAST_ACCESSED_MILEAGE_SYNC,
        LAST_ACCESSED_MILEAGE_STATS,
        LAST_ACCESSED_IS_DRIVING,
        LAST_ACCESSED_APP_SETTINGS;
    }

    private static String getMetricName(MetricName metricName) {
        switch (metricName) {
            case LAST_ACCESSED_TERRITORY_DATA:
                return "msus_milebuddy_last_accessed_territory_data";
            case LAST_ACCESSED_APP_SETTINGS:
                return "msus_last_opened_settings";
            case LAST_ACCESSED_TERRITORY_CHANGER:
                return "msus_milebuddy_last_accessed_territory_changer";
            case LAST_ACCESSED_OTHER_USER_TRIPS:
                return "msus_last_accessed_other_user_trips";
            case LAST_ACCESSED_GENERATE_RECEIPT:
                return "msus_last_generated_receipt";
            case LAST_ACCESSED_MILEAGE_SYNC:
                return "msus_last_synced_mileage";
            case LAST_ACCESSED_MILEAGE_STATS:
                return "msus_last_viewed_mileage_stats";
            case LAST_OPENED_APP:
            default:
                return "msus_last_accessed_milebuddy";
        }
    }

    public static void updateMetric(Context context, final MetricName metricName, DateTime dateAndTime, final MyInterfaces.MetricUpdateListener listener) {

        Log.i(TAG, "updateMetric: " + metricName.name() + " executing...");

        Requests.Request request = new Requests.Request(Requests.Request.Function.UPDATE);

        try {
            Containers.EntityContainer container = new Containers.EntityContainer();
            container.entityFields.add(new Containers.EntityField(getMetricName(metricName), dateAndTime.toLocalDateTime().toString()));

            request.arguments.add(new Requests.Argument("entityid", MediUser.getMe().systemuserid));
            request.arguments.add(new Requests.Argument("entity", "systemuser"));
            request.arguments.add(new Requests.Argument("container", container.toJson()));
            request.arguments.add(new Requests.Argument("as_userid", MediUser.getMe().systemuserid));

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "updateMetric: " + metricName.name() + new String(responseBody));
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "updateMetric: " + metricName.name() + error.getLocalizedMessage());
                    if (listener != null) {
                        listener.onFailure(error.getLocalizedMessage());
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateMetric(Context context, MetricName metricName, DateTime dateAndTime) {
        updateMetric(context, metricName, dateAndTime, null);
    }

}
