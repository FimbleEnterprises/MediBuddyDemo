package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Crm {
    private static final String TAG = "Crm";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    // public static final String DEFAULT_BASE_URL = MyApp.getAppContext().getString(R.string.base_server_url);
    private static final String FCM_URL = "https://mediproxyrestapi.azurewebsites.net/api/crm/Fcm/";
    // public static final String BASE_URL = "http://192.168.16.135:44341/";
    private static AsyncHttpClient client = new AsyncHttpClient();
    private MySettingsHelper options;

    public Crm() {
        options = new MySettingsHelper(MyApp.getAppContext());
    }

    public static void userCanAuthenticate(String username, String password, final MyInterfaces.AuthenticationResult result) {

        final MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());

        Requests.Request request = new Requests.Request(Requests.Request.Function.CAN_AUTHENTICATE);
        request.arguments.add(new Requests.Argument(null, username));
        request.arguments.add(new Requests.Argument(null, password));
        Crm crm = new Crm();
        try {
            crm.makeCrmRequest(MyApp.getAppContext(), request, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String strResponse = new String(responseBody);
                    Log.d(TAG, "onSuccess " + strResponse);

                    // Added 1.5 - was authenticating everyone prior
                    if (strResponse != null && strResponse.equals(TRUE)) {
                        result.onSuccess();
                    } else if (strResponse != null && strResponse.equals(FALSE)) {
                        result.onFailure();
                    } else {
                        result.onError("UNKNOWN ERROR", null);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getMessage());
                    result.onError(error.getMessage(), error);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onClick: " + e.getMessage());
        }
    }

    public RequestHandle makeCrmRequest(Context context, Requests.Request request,
                                        final AsyncHttpResponseHandler responseHandler) {

        String argString = "";
        for (Requests.Argument arg : request.arguments) {
            argString = arg.toString() + "\n";
        }

        Log.i(TAG, "makeCrmRequest: Request function: " + request.function + " Request arguments: " + argString);

        StringEntity payload = null;
        try {
            payload = new StringEntity(request.toJson());
        } catch (Exception e) {
            e.printStackTrace();
            responseHandler.onFailure(0, null, null, e);
        }

        return client.post(context, options.getServerBaseUrl(), payload, "application/json",
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
        MySettingsHelper options = new MySettingsHelper(MyApp.getAppContext());
        return options.getServerBaseUrl() + relativeUrl;
    }

}
