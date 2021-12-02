package com.puteffort.sharenshop.services;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.puteffort.sharenshop.interfaces.NotificationDao;
import com.puteffort.sharenshop.models.Notification;

@Database(entities = {Notification.class}, exportSchema = false, version = 1)
public abstract class NotificationDatabase extends RoomDatabase {
    public abstract NotificationDao notificationDao();

    private static NotificationDatabase instance;
    public static final String NOTIFICATION_DB_NAME = "notifications";
    private static final Object LOCK = new Object();

    public static NotificationDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        NotificationDatabase.class, NOTIFICATION_DB_NAME)
                        .build();
            }
        }
        return instance;
    }
}
