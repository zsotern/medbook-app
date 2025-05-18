package com.example.medbook;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotificationHandler {
    private static final String CHANNEL_ID = "medbook_notification_channel";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager mManager;
    private Context mContext;
    FirebaseUser user;

    public NotificationHandler(Context context){
        this.mContext = context;
        this.mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "MedBook Notification",
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("Notifications from MedBook app");

        this.mManager.createNotificationChannel(channel);

    }

    public void send(String message){
        user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;

        if (user != null) {
            // Ha be van jelentkezve, nyissuk meg a HistoryListActivity-t
            intent = new Intent(mContext, HistoryListActivity.class);
        } else {
            // Ha nincs bejelentkezve, irányítsuk a LoginActivity-re
            intent = new Intent(mContext, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.foglalj_dokit_icon)
                .setContentTitle("FoglaljDokit")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        this.mManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void cancel(){
        this.mManager.cancel(NOTIFICATION_ID);
    }
}
