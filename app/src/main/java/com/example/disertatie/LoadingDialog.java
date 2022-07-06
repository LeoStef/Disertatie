package com.example.disertatie;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog alertDialog;

    LoadingDialog(Activity myActivity){
        activity = myActivity;

    }

    void startLoadingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);
        Log.d("WOW","Starting Loading Dialog...");
        alertDialog = builder.create();
        alertDialog.show();
    }


    void stopLoadingDialog(){
        alertDialog.dismiss();
        Log.d("WOW","Loading Dialog dismissed!");
    }

}
