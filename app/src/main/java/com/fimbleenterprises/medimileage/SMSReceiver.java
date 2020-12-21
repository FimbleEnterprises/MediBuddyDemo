package com.fimbleenterprises.medimileage;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG ="SMSSentReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive  ");

        if (intent != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            handleReceivedSms(context, intent);
        } else if (intent != null && intent.getAction().equals("android.provider.Telephony.SMS_SENT")) {
            handleSentSms(context, intent);
        }
    }

    void handleSentSms(Context context, Intent intent) {
        Log.i(TAG, "handleSentSms ");
    }

    void handleReceivedSms(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();
                    try {
                        if (senderNum.contains("MOB_NUMBER")) {
                            Toast.makeText(context,"",Toast.LENGTH_SHORT).show();

                            Intent intentCall = new Intent(context, MainActivity.class);
                            intentCall.putExtra("message", currentMessage.getMessageBody());

                            PendingIntent pendingIntent= PendingIntent.getActivity(context, 0, intentCall, PendingIntent.FLAG_UPDATE_CURRENT);
                            pendingIntent.send();
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }

        int resultCode = getResultCode();
        switch (resultCode) {
            case Activity.RESULT_OK:
                Toast.makeText(context, "SMS sent", Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Toast.makeText(context, "Generic failure", Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                Toast.makeText(context, "No service", Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Toast.makeText(context, "Null PDU", Toast.LENGTH_LONG).show();
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Toast.makeText(context, "Radio off", Toast.LENGTH_LONG).show();
                break;
        }
    }


}
