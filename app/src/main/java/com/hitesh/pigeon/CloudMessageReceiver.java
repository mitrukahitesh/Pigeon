package com.hitesh.pigeon;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hitesh.pigeon.activities.MainActivity;

public class CloudMessageReceiver extends FirebaseMessagingService {

    public static final String TITLE = "Pigeon";
    public static final String TEXT = "New message received";
    public static final int ID = 1;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle(TITLE)
                .setContentText(TEXT)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(ID, notification);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i("TOKEN", s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference reference = db.getReference()
                    .child(MainActivity.USERS)
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(MainActivity.TOKEN);
            reference.setValue(s);
        }
    }
}
