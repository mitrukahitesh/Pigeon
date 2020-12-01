package com.hitesh.whatsapp.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.hitesh.whatsapp.R;

public class LoadingDialog {

    Context context;
    AlertDialog dialog;

    public LoadingDialog(Context context) {
        this.context = context;
        dialog = new AlertDialog.Builder(context)
                .setView(LayoutInflater.from(context).inflate(R.layout.loading_dialog, null, false))
                .setCancelable(false)
                .create();
    }

    public void start() {
        dialog.show();
    }

    public void stop() {
        dialog.dismiss();
    }
}
