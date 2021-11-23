package com.puteffort.sharenshop.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.fragments.AccountFragment;
import com.puteffort.sharenshop.fragments.HomeContainerFragment;
import com.puteffort.sharenshop.fragments.NewPostFragment;
import com.puteffort.sharenshop.fragments.NotificationContainerFragment;
import com.puteffort.sharenshop.interfaces.NotificationDao;
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.services.NotificationDatabase;

public class MainActivityViewModel extends AndroidViewModel {
    private final Fragment homeFragment;
    private final Fragment newPostFragment;
    private final Fragment accountFragment;
    private final Fragment notificationFragment;
    private NotificationDao notificationDao;

    private int lastSelectedTab, unreadCount = 0;
    private final MutableLiveData<Integer> unreadNotifications;
    private final Handler handler;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        lastSelectedTab = -1;

        homeFragment = new HomeContainerFragment();
        newPostFragment = new NewPostFragment();
        accountFragment = new AccountFragment();
        notificationFragment = new NotificationContainerFragment();
        unreadNotifications = new MutableLiveData<>(unreadCount);

        handler = new Handler(Looper.getMainLooper());

        AsyncTask.execute(() -> {
            notificationDao = NotificationDatabase.getInstance(application).notificationDao();
            findUnreadNotifications();
        });
    }

    private void findUnreadNotifications() {
        for (Notification notification: notificationDao.getAllNotifications()) {
            if (!notification.markedAsRead)
                handler.post(() -> unreadNotifications.setValue(++unreadCount));
        }
    }

    public void reduceUnreadCount() {
        unreadNotifications.setValue(--unreadCount);
    }
    public void increaseUnreadCount() {
        unreadNotifications.setValue(++unreadCount);
    }

    public LiveData<Integer> getUnreadNotificationsCount() {
        return unreadNotifications;
    }
    public int getLastSelectedTab() {
        return lastSelectedTab;
    }
    public void setLastSelectedTab(int id) {
        lastSelectedTab = id;
    }

    @SuppressLint("NonConstantResourceId")
    public Fragment getFragment (int id) {
        Fragment fragment;
        switch (id) {
            case R.id.homeMenuItem: {
                fragment = homeFragment;
                break;
            }
            case R.id.newPostMenuItem: {
                fragment = newPostFragment;
                break;
            }
            case R.id.accountMenuItem: {
                fragment = accountFragment;
                break;
            }
            case R.id.notificationItem: {
                fragment = notificationFragment;
                break;
            }
            default: fragment = null;
        }
        return fragment;
    }

    public void updateNotification(Notification notification) {
        AsyncTask.execute(() -> notificationDao.updateNotification(notification));
        unreadNotifications.setValue(--unreadCount);
    }
}
