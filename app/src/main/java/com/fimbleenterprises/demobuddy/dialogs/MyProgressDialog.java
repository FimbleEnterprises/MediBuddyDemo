package com.fimbleenterprises.demobuddy.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by weber on 18-Sep-2018.
 */

public class MyProgressDialog extends SweetAlertDialog implements DialogInterface.OnShowListener {

    private static final String TAG = "MyProgressDialog";

    public MyProgressDialog(Context context) {
        super(context);
        changeAlertType(SweetAlertDialog.PROGRESS_TYPE);

        this.setOnShowListener(this);
    }

    public MyProgressDialog(Context context, String msg) {
        super(context);
        this.setContentText(msg);
        this.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        // this.setTitleText("");
        this.setOnShowListener(this);
    }

    public MyProgressDialog(Context context, int alertType) {
        super(context, alertType);
        this.setContentText("Working...");
        // this.setTitleText("");
        this.setOnShowListener(this);
    }

    public MyProgressDialog(Context context, String msg, int alertType) {
        super(context, alertType);
        this.setContentText(msg);
        // this.setTitleText("");
        this.setOnShowListener(this);
    }

    @Override
    public String getTitleText() {
        return super.getTitleText();
    }

    @Override
    public SweetAlertDialog setTitleText(String text) {
        return super.setTitleText(text);
    }

    @Override
    public String getContentText() {
        return super.getContentText();
    }

    @Override
    public SweetAlertDialog setContentText(String text) {
        return super.setContentText(text);
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {}


}
