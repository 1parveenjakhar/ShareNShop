package com.puteffort.sharenshop.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.puteffort.sharenshop.interfaces.NotificationDao;
import com.puteffort.sharenshop.models.Notification;

import java.util.Collections;
import java.util.List;

public class NotificationRepository {
    private static NotificationRepository instance;

    private final MutableLiveData<List<Notification>> notificationsLiveData;
    private List<Notification> notifications;

    private final MutableLiveData<Integer> unreadCountLiveData;
    private int unreadCount;

    private NotificationDao notificationDao;
    private final Handler handler;

    private NotificationRepository(Context context) {
        notificationsLiveData = new MutableLiveData<>();
        handler = new Handler(Looper.getMainLooper());

        unreadCountLiveData = new MutableLiveData<>();
        unreadCount = 0;

        loadNotifications(context);
    }

    private void loadNotifications(Context context) {
        AsyncTask.execute(() -> {
            notificationDao = NotificationDatabase.getInstance(context).notificationDao();
            notifications = notificationDao.getAllNotifications();
            for (Notification notification: notifications)
                if (!notification.markedAsRead)
                    unreadCount++;
            Collections.reverse(notifications);
            handler.post(() -> {
                notificationsLiveData.setValue(notifications);
                unreadCountLiveData.setValue(unreadCount);
            });

        });
    }

    public void markNotificationAsRead(int index) {
        markNotificationAsRead(notifications.get(index));
    }

    public void markNotificationAsRead(Notification notification) {
        AsyncTask.execute(() -> {
            notification.markedAsRead = true;
            notificationDao.updateNotification(notification);
            int index = notifications.indexOf(notification);
            notifications.get(index).markedAsRead = true;
            handler.post(() -> {
                notificationsLiveData.setValue(notifications);
                unreadCountLiveData.setValue(--unreadCount);
            });
        });
    }

    public void addNotification(Notification notification) {
        notification.id = notificationDao.addNotification(notification);
        notifications.add(0, notification);
        handler.post(() -> {
            notificationsLiveData.setValue(notifications);
            unreadCountLiveData.setValue(++unreadCount);
        });
    }

    public static NotificationRepository getInstance(Context context) {
        if (instance == null)
            instance = new NotificationRepository(context);
        return instance;
    }

    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCountLiveData;
    }
}
