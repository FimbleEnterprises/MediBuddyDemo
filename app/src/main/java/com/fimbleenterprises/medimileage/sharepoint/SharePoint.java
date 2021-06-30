package com.fimbleenterprises.medimileage.sharepoint;

import android.content.Context;
import android.util.Log;

import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import cz.msebera.android.httpclient.Header;

public class SharePoint {
    private static final String TAG = "SharePoint";


    public interface SearchListener {
        public void onSuccess(@Nullable SharePointItems items);
        public void onFailure(String msg);
    }

    public static class SharePointItems {
        public ArrayList<SharePointItem> list = new ArrayList<>();

        /**
         * Constructor consuming a raw json string as returned by our MediProxy server.
         * @param jsonArray The raw, unadulterated string returned by our MediProxy server.
         */
        public SharePointItems(String jsonArray) {
            try {
                JSONObject rootObject = new JSONObject(jsonArray);
                JSONArray rootArray = rootObject.getJSONArray("Values");
                for (int i = 0; i < rootArray.length(); i++) {
                    SharePointItem item = new SharePointItem(rootArray.getJSONObject(i));
                    this.list.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return this.list.size() + " items.";
        }

        public static class SharePointItem {
            public String guid;
            public String parentContainerUrl;
            public String title;
            public Boolean isContainer;
            public String path;
            public String hitHighlightSummary;
            public String docId;
            public String filename;
            public String fullurl;
            public DateTime modifiedOn;
            public String extension;
            public String authorName;
            public int bytes;

            /**
             * Constructor using JSONObjects parsed from our server's JSON response.
             * @param json JSONObjects as formatted by our MediProxy server.
             */
            public SharePointItem(JSONObject json) {
                try {
                    if (!json.isNull("uniqueGuid")) {
                        this.guid = (json.getString("uniqueGuid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("parentContainerUrl")) {
                        this.parentContainerUrl = (json.getString("parentContainerUrl"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("title")) {
                        this.title = (json.getString("title"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("isContainer")) {
                        this.isContainer = (json.getBoolean("isContainer"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("path")) {
                        this.path = (json.getString("path"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("hitHighlightSummary")) {
                        this.hitHighlightSummary = (json.getString("hitHighlightSummary"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("docid")) {
                        this.docId = (json.getString("docid"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("filename")) {
                        this.filename = (json.getString("filename"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("fullUrl")) {
                        this.fullurl = (json.getString("fullUrl"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                   if (!json.isNull("modifiedon")) {
                       this.modifiedOn = (new DateTime(json.getString("modifiedon")));
                   }
                } catch (JSONException e) {
                   e.printStackTrace();
                }
                try {
                    if (!json.isNull("extension")) {
                        this.extension = (json.getString("extension"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("authorName")) {
                        this.authorName = (json.getString("authorName"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (!json.isNull("size")) {
                        this.bytes = (json.getInt("size"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return this.title;
            }

        }

    }

    public static void searchSp(Context context, String query, final SearchListener listener) {

        ArrayList<Requests.Argument> args = new ArrayList<>();
        args.add(new Requests.Argument("query", query));
        Requests.Request request = new Requests.Request("searchsp", args);
        Crm crm = new Crm();
        crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                SharePointItems spItems = new SharePointItems(response);
                listener.onSuccess(spItems);
                Log.i(TAG, "onSuccess " + spItems.list.size() + " items returned");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.w(TAG, "onFailure: " + error.getLocalizedMessage());
                listener.onFailure(error.getLocalizedMessage());
            }
        });

    }

}
