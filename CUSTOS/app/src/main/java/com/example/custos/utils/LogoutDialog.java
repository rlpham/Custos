package com.example.custos.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.custos.R;

public class LogoutDialog {
    private Activity activity;
    private AlertDialog alertDialog;

    public LogoutDialog(Activity activity){
        this.activity = activity;
    }
    public void startDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.custom_dialog_logout,null));
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();

    }
    public void dismissDialog(){
        alertDialog.dismiss();
    }
}
