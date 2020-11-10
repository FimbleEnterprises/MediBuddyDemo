package com.fimbleenterprises.medimileage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public abstract class MyTwoChoiceDialog {

    private static String message = "PUT A MSG HERE, DUMMY";
    private static String btn1Text = "Button 1";
    private static String btn2Text = "Button 2";
    private static Dialog dialog;

    public interface TwoButtonListener {
        void onBtn1Pressed();
        void onBtn2Pressed();
    }

    private static void build(final Context context, final TwoButtonListener listener) {

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.generic_two_choice_dialog);
        TextView txtMain = dialog.findViewById(R.id.txtMainText);
        txtMain.setText(message);
        Button btn1 = dialog.findViewById(R.id.btnButton1);
        btn1.setText(btn1Text);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBtn1Pressed();
                dialog.dismiss();
            }
        });
        Button btn2 = dialog.findViewById(R.id.btnButton2);
        btn2.setText(btn2Text);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onBtn2Pressed();
                dialog.dismiss();
            }
        });
        dialog.setCancelable(true);
        dialog.show();
    }

    public static void show(Context context, String button1Text, String button2Text, String msg, TwoButtonListener listener) {
        message = msg;
        btn1Text = button1Text;
        btn2Text = button2Text;
        build(context, listener);
        dialog.show();
    }
}
