package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.fimbleenterprises.medimileage.objects_and_containers.MileBuddyUpdate;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

import static com.fimbleenterprises.medimileage.objects_and_containers.Requests.*;

public class MileBuddyUpdater {
    private static final String TAG = "MileBuddyUpdater";

    private Context context;
    private double appVersion;

    public interface UpdateCheckListener {
        public void onAvailable(MileBuddyUpdate updateObject);
        public void onNotAvailable();
        public void onError(String msg);
    }

    public MileBuddyUpdater(Context context) {
        this.context = context;

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            this.appVersion = Double.parseDouble(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void checkForUpdate(final UpdateCheckListener listener) {

        QueryFactory factory = new QueryFactory("msus_milebuddyupdate");
        factory.addColumn("msus_name");
        factory.addColumn("msus_milebuddyupdateid");
        factory.addColumn("msus_changelog");
        factory.addColumn("msus_download_link");
        factory.addColumn("msus_releasedate");
        factory.addColumn("msus_version");
        String query = factory.construct();

        Requests.Request request = new Request(Request.Function.GET);
        request.arguments.add(new Argument("query", query));

        Crm crm = new Crm();
        try {
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String results = new String(responseBody);
                    ArrayList<MileBuddyUpdate> updates = new ArrayList<>();
                    try {
                        JSONArray array = new JSONObject(results).getJSONArray("value");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            MileBuddyUpdate update = new MileBuddyUpdate(jsonObject);
                            if (update.version > appVersion) {
                                listener.onAvailable(update);
                                return;
                            }
                        }
                        listener.onNotAvailable();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "onSuccess ");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.i(TAG, "onFailure ");
                    listener.onError(error.getLocalizedMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError(e.getLocalizedMessage());
        }
    }

}























































