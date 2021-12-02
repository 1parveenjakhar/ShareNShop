package com.puteffort.sharenshop.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.services.NotificationRepository;

import java.util.List;

public class NotificationFragmentViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Notification>> notifications;
    private NotificationRepository notificationRepository;

    public NotificationFragmentViewModel(@NonNull Application application) {
        super(application);
        notifications = new MutableLiveData<>();

        addObservers();
    }

    private void addObservers() {
        notificationRepository = NotificationRepository.getInstance(getApplication());
        notificationRepository.getNotifications().observeForever(this.notifications::setValue);
    }

    public void markNotificationAsRead(int index) {
        notificationRepository.markNotificationAsRead(index);
    }

    public void markAllAsRead() {
        notificationRepository.markAllAsRead();
    }

    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

}
