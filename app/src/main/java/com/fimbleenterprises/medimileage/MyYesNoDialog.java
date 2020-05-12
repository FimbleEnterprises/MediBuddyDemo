package com.fimbleenterprises.medimileage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.Nullable;

public class MyYesNoDialog {

    private static String message = "Are you sure?";
    private static AlertDialog dialog;

    public interface YesNoListener {
        void onYes();
        void onNo();
    }

    private static void build(Context context, final YesNoListener listener) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        listener.onYes();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        listener.onNo();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialog = builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .create();
    }

    public static void showDialog(Context context, String msg, YesNoListener listener) {
        message = msg;
        build(context, listener);
        dialog.show();
    }
}
