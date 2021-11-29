package com.puteffort.sharenshop.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.puteffort.sharenshop.interfaces.NotificationDao;
import com.puteffort.sharenshop.models.Notification;

import java.util.ArrayList;
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

    // Called by Notification Fragment
    public void markNotificationAsRead(int index) {
        AsyncTask.execute(() -> {
            Notification notification = notifications.get(index);
            notification.markedAsRead = true;
            notificationDao.updateNotification(notification);
            handler.post(() -> unreadCountLiveData.setValue(--unreadCount));
        });
    }

    // Called from Main Activity, reference is not same for Notification
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

    public void deleteAllNotifications() {
        AsyncTask.execute(() -> {
            notificationDao.deleteAll();
            notifications.clear();
            handler.post(() -> notificationsLiveData.setValue(notifications));
        });
    }

    public void markAllAsRead() {
        AsyncTask.execute(() -> {
            List<Notification> tmpList = new ArrayList<>();
            for (int i = 0; i < notifications.size(); i++) {
                Notification notification = notifications.get(i);
                tmpList.add(new Notification(notification));
                if (!notification.markedAsRead) {
                    tmpList.get(i).markedAsRead = true;
                    notificationDao.updateNotification(tmpList.get(i));
                }
            }
            notifications = tmpList;
            handler.post(() -> {
                notificationsLiveData.setValue(notifications);
                unreadCountLiveData.setValue(0);
            });
        });
    }
}
