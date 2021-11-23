package com.puteffort.sharenshop.viewmodels;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.puteffort.sharenshop.interfaces.NotificationDao;
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.services.NotificationDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragmentViewModel extends AndroidViewModel {
    private final List<Notification> notifications;
    private final MutableLiveData<List<Notification>> notificationsLiveData;
    private NotificationDao notificationDao;
    private final Handler handler;

    public NotificationFragmentViewModel(@NonNull Application application) {
        super(application);
        notifications = new ArrayList<>();
        notificationsLiveData = new MutableLiveData<>();
        handler = new Handler(Looper.getMainLooper());

        new Thread(this::loadNotifications).start();
    }

    private void loadNotifications() {
        notificationDao = NotificationDatabase.getInstance(getApplication()).notificationDao();
        notifications.addAll(notificationDao.getAllNotifications());
        Collections.reverse(notifications);
        handler.post(() -> notificationsLiveData.setValue(notifications));
    }

    public void updateNotification(int index) {
        AsyncTask.execute(() -> {
            Notification notification = notifications.get(index);
            notification.markedAsRead = true;
            notificationDao.updateNotification(notification);
            handler.post(() -> notificationsLiveData.setValue(notifications));
        });
    }


    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

}
