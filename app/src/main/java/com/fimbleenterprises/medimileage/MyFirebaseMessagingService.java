package com.fimbleenterprises.medimileage;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.AsyncHttpResponseHandler;

import androidx.annotation.NonNull;
import cz.msebera.android.httpclient.Header;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public MyFirebaseMessagingService() {
        Log.i(TAG, "MyFirebaseMessagingService Default constructor called.");
    }

    public interface FcmTokenRequestListener {
        void onSuccess(String token);
        void onFailure(String msg);
    }

    private static final String TAG = "MyFirebaseMessagingService";
    private MySettingsHelper options;
    private Context context;

    public MyFirebaseMessagingService(Context context) {
        super();
        this.context = context;
        options = new MySettingsHelper(context);
        requestNewFcmToken();
    }

    private String retrieveCachedFcmToken() {
        String token = options.getFcmToken();
        Log.i(TAG, "retrieveCachedFcmToken " + token);
        return token;
    }

    public void requestNewFcmToken() {
        try {
            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                       @Override
                       public void onComplete(@NonNull Task<InstanceIdResult> task) {
                           if (!task.isSuccessful()) {
                               Log.w(TAG, "getInstanceId failed", task.getException());
                               return;
                           }
                           // Get new Instance ID token
                           String token = task.getResult().getToken();
                           Log.i(TAG, "onComplete FCM token was received. (" + token + ")");
                           options.setFcmToken(token);
                           Log.i(TAG, "onComplete FCM token was saved locally. (" + token + ")");
                           updateCrmWithToken();
                       } // onComplete
                   } // add listener
                ) // new listener
            ; // get instanceid
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestNewFcmToken(final FcmTokenRequestListener listener) {
        try {
            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                       @Override
                       public void onComplete(@NonNull Task<InstanceIdResult> task) {
                           if (!task.isSuccessful()) {
                               Log.w(TAG, "getInstanceId failed", task.getException());
                               listener.onFailure(task.getException().getLocalizedMessage());
                               return;
                           }

                           // Get new Instance ID token
                           String token = task.getResult().getToken();
                           options.setFcmToken(token);
                           Log.i(TAG, "onComplete FCM token was set. (" + token + ")");

                           // Log and toast
                           String msg ="token: "+ token;
                           Log.d(TAG, msg);
                           listener.onSuccess(token);
                       }

                   }
                )
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCrmWithToken() {

        try {
            EntityContainers.EntityContainer container = new EntityContainers.EntityContainer();
            container.entityFields.add(new EntityContainers.EntityField("msus_fcm_token", options.getFcmToken()));

            Requests.Request request = new Requests.Request(Requests.Request.Function.UPDATE);
            request.arguments.add(new Requests.Argument("guid", MediUser.getMe().systemuserid));
            request.arguments.add(new Requests.Argument("entity", "systemuser"));
            request.arguments.add(new Requests.Argument("container", container.toJson()));
            request.arguments.add(new Requests.Argument("asuserid", MediUser.getMe().systemuserid));

            Crm crm = new Crm();
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "onSuccess Successfully updated CRM with our FCM token.");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: FAILED TO UPDATE CRM WITH FCM TOKEN!");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(@NonNull String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onSendError(@NonNull String s, @NonNull Exception e) {
        super.onSendError(s, e);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        try {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            options.setFcmToken(s);
            Log.i(TAG, "onNewToken New FCM token was set! (" + s + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onNewToken(s);
    }
}
