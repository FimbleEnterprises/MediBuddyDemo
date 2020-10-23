package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.sql.Time;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Crm {
    private static final String TAG = "Crm";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    // public static final String DEFAULT_BASE_URL = MyApp.getAppContext().getString(R.string.base_server_url);
    // private static final String FCM_URL = "https://mediproxyrestapi.azurewebsites.net/api/crm/Fcm/";
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
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        responseHandler.onProgress(bytesWritten, totalSize);
                        Log.d(TAG, "onProgress | bytesWritten: " + bytesWritten + ", totalSize: " + totalSize);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: code: " + statusCode);
                        responseHandler.onFailure(statusCode, headers, responseBody, error);
                    }
                });
    }

    public RequestHandle makeCrmRequest(Context context, Requests.Request request, Timeout timeout,
                                        final MyInterfaces.CrmRequestListener listener) {

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
        }

        if (timeout == Timeout.LONG) {
            client.setTimeout(1000000);
        } else if (timeout == Timeout.SHORT){
            client.setTimeout(10000);
        }

        return client.post(context, options.getServerBaseUrl(), payload, "application/json",
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.i(TAG, "onSuccess : code = " + statusCode);
                        listener.onComplete(new String(responseBody));
                    }

                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                        AsyncProgress proggy = new AsyncProgress(bytesWritten, totalSize);
                        listener.onProgress(proggy);
                        Log.d(TAG, "onProgress | KB written: " + proggy.getCompletedKb());
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.w(TAG, "onFailure: code: " + statusCode);
                        listener.onFail(error.getLocalizedMessage());
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

    public enum Timeout {
        SHORT, LONG
    }

    public class AsyncProgress {
        private double bytesWritten = 1;
        private double totalSize = 1;

        public AsyncProgress(double bytesWritten, double totalSize) {

            try {
                this.bytesWritten = bytesWritten;
                this.totalSize = totalSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public double getCompletedBytes() {
            try {
                return this.bytesWritten;
            } catch (Exception e) {
                Log.w(TAG, "getCompletedBytes: " + e.getLocalizedMessage());
                return -1;
            }
        }

        public double getCompletedKb() {
            try {
                double completed = Helpers.Numbers.formatAsTwoDecimalPointNumber(Helpers.Files.convertBytesToKb(this.bytesWritten));
                return completed;
            } catch (Exception e) {
                Log.w(TAG, "getCompletedBytes: " + e.getLocalizedMessage());
                return -1;
            }
        }

        public double getCompletedMb() {
            try {
                double completed = Helpers.Numbers.formatAsTwoDecimalPointNumber(Helpers.Files.convertBytesToMb(this.bytesWritten));
                return completed;
            } catch (Exception e) {
                Log.w(TAG, "getCompletedBytes: " + e.getLocalizedMessage());
                return -1;
            }
        }
    }

}
