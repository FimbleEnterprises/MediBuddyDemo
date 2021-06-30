package com.fimbleenterprises.medimileage.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fimbleenterprises.medimileage.R;

public abstract class MyOkayOnlyDialog {

    private static String message = "PUT A MSG HERE, DUMMY";
    private static Dialog dialog;

    public interface OkayListener {
        void onOkay();
    }

    private static void build(final Context context, String msg, final OkayListener listener) {
        message = msg;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        listener.onOkay();
                        break;
                }
            }
        };

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.generic_app_dialog);
        TextView txtMain = dialog.findViewById(R.id.txtMainText);
        txtMain.setText(message);
        Button btnOkay = dialog.findViewById(R.id.btnOkay);
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onOkay();
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    public static void show(Context context, OkayListener listener) {
        build(context, message, listener);
        dialog.show();
    }

    public static void show(Context context, String msg, OkayListener listener) {
        message = msg;
        build(context, message, listener);
        dialog.show();
    }
}
