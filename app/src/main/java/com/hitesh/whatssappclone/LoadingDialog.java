package com.hitesh.whatssappclone;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;

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
