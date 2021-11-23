package com.puteffort.sharenshop.services;

import static com.puteffort.sharenshop.utils.UtilFunctions.INTENT_TAG;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.Notification;

import java.util.Map;

public class ShareNShopMessagingService extends FirebaseMessagingService {
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }


    @Override
    public void onNewToken(@NonNull String token) {}

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String sender = remoteMessage.getFrom();
        Log.d("notification", "Got a notification !");
        if (sender != null) {
            String topic = sender.substring("/topics/".length());
            Map<String, String> data = remoteMessage.getData();

            Log.d("notification", "Topic = " + topic);
            Log.d("notification", "Got data = " + data);

            String postID = data.get("postID");
            String title = data.get("title");
            String message = data.get("message");
            Notification notification = new Notification(title, message, postID);
            NotificationDatabase.getInstance(this).notificationDao().addNotification(notification);

            // For updating notification icon
            Intent intent = new Intent(INTENT_TAG);
            intent.putExtra("postID", postID);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            broadcaster.sendBroadcast(intent);

            // For opening new ACTIVITY
            intent = new Intent(this, MainActivity.class);
            intent.putExtra("postID", postID);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            @SuppressLint("UnspecifiedImmutableFlag")
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            String CHANNEL_ID = "ShareNShop_Channel";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
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
