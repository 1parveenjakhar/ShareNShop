package com.puteffort.sharenshop.viewmodels;

import android.annotation.SuppressLint;
import android.app.Application;

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
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.services.NotificationRepository;

public class MainActivityViewModel extends AndroidViewModel {
    private final Fragment homeFragment;
    private final Fragment newPostFragment;
    private final Fragment accountFragment;
    private final Fragment notificationFragment;
    private final NotificationRepository notificationRepository;

    private int lastSelectedTab;
    private final MutableLiveData<Integer> unreadCount;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        lastSelectedTab = -1;

        homeFragment = new HomeContainerFragment();
        newPostFragment = new NewPostFragment();
        accountFragment = new AccountFragment();
        notificationFragment = new NotificationContainerFragment();
        unreadCount = new MutableLiveData<>();

        notificationRepository = NotificationRepository.getInstance(application);

        addObservers();
    }

    private void addObservers() {
        notificationRepository.getUnreadCount().observeForever(this.unreadCount::setValue);
    }

    public void markNotificationAsRead(Notification notification) {
        notificationRepository.markNotificationAsRead(notification);
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
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
}
