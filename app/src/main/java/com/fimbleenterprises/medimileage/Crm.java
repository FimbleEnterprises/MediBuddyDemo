package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Crm {
    private static final String TAG = "Crm";
    private static final String BASE_URL = "https://mediproxyrestapi.azurewebsites.net/api/crm/PerformCrmAction/";
    private static final String FCM_URL = "https://mediproxyrestapi.azurewebsites.net/api/crm/Fcm/";
    // public static final String BASE_URL = "http://192.168.1.9:44341/";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public Crm() {

    }

    public RequestHandle makeCrmRequest(Context context, Requests.Request request,
                                        final AsyncHttpResponseHandler responseHandler) {

        String argString = "";
        for (Requests.Argument arg : request.arguments) {
            argString = arg.toString() + "\n";
        }

        Log.i(TAG, "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString);

        StringEntity entity = null;
        try {
            entity = new StringEntity(request.toJson());
        } catch (Exception e) {
            e.printStackTrace();
            responseHandler.onFailure(0, null, null, e);
        }

        return client.post(context, BASE_URL, entity, "application/json",
            new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "onSuccess : code = " + statusCode);
                    responseHandler.onSuccess(statusCode, headers, responseBody);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: code: " + statusCode);
                    responseHandler.onFailure(statusCode, headers, responseBody, error);
                }
            });
    }

    public static RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static RequestHandle post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
