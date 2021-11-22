package com.puteffort.sharenshop.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;

import java.util.Map;

public class ShareNShopMessagingService extends FirebaseMessagingService {
    private final String CHANNEL_ID = "ShareNShop_Channel";
    @Override
    public void onNewToken(@NonNull String token) {}

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String sender = remoteMessage.getFrom();
        if (sender != null) {
            String topic = sender.substring("/topics/".length());
            Map<String, String> data = remoteMessage.getData();

            // TODO() "Switch case based on topic/from"
            if (topic.contains("_")) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("postID", data.get("postID"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                @SuppressLint("UnspecifiedImmutableFlag")
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(data.get("title"))
                        .setContentText(data.get("message"))
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID,
                            NotificationManager.IMPORTANCE_DEFAULT);
                    managerCompat.createNotificationChannel(channel);
                    builder.setChannelId(CHANNEL_ID);
                }
                managerCompat.notify(0, builder.build());
            }

        }
    }
}
