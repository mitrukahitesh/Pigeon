package com.hitesh.whatsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hitesh.whatsapp.activities.MainActivity;

public class OnlineStatusBR extends BroadcastReceiver {

    public OnlineStatusBR() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (MainActivity.ONLINE_ACTION.equals(intent.getAction()))
            MainActivity.setLoginStatus(true);
    }
}
