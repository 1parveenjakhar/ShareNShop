package com.puteffort.sharenshop;

import static com.puteffort.sharenshop.utils.UtilFunctions.INTENT_TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationBarView;
import com.puteffort.sharenshop.databinding.ActivityMainBinding;
import com.puteffort.sharenshop.fragments.HomeContainerFragment;
import com.puteffort.sharenshop.interfaces.NotificationHandler;
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.services.NotificationDatabase;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.UtilFunctions;
import com.puteffort.sharenshop.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, NotificationHandler {
    private ActivityMainBinding binding;
    private MainActivityViewModel model;
    private NavigationBarView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> UtilFunctions.showToast(this, "Exception: " + e.getMessage()));
        if (setOrientation()) return;

        // Load user details (UserProfile and UserActivity)
        DBOperations.getUserDetails();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setListenersAndObservers();
        if (getIntent() != null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INTENT_TAG));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("postID") != null) {
                model.increaseUnreadCount();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setOrientation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra("postID") != null) {
            // A notification has been clicked
            Notification notification = new Notification(intent.getStringExtra("title"),
                    intent.getStringExtra("message"),
                    intent.getStringExtra("postID"));
            notification.markedAsRead = true;
            model.updateNotification(notification);

            AsyncTask.execute(() -> NotificationDatabase.getInstance(this).notificationDao()
                    .updateNotification(notification));

            changeFragment(new HomeContainerFragment(intent.getStringExtra("postID")));
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private boolean setOrientation() {
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
        }
        return false;
    }

    private void setListenersAndObservers() {
        // Do not remove this cast, as it will crash the app in tablet mode
        navBar = ((NavigationBarView)binding.bottomNav);
        navBar.setOnItemSelectedListener(this);

        int lastTab = model.getLastSelectedTab();
        if (lastTab == -1) lastTab = R.id.homeMenuItem;
        navBar.setSelectedItemId(lastTab);

        model.getUnreadNotificationsCount().observe(this, unreadCount -> {
            if (unreadCount == null) return;
            if (unreadCount == 0)
                navBar.getOrCreateBadge(R.id.notificationItem).setVisible(false);
            else {
                BadgeDrawable badge = navBar.getOrCreateBadge(R.id.notificationItem);
                badge.setVisible(true);
                badge.setNumber(unreadCount);
            }
        });
    }

    // Use if need to change the tab i.e. delete all fragments in stack
    public void changeFragment(int fragmentID) {
        navBar.setSelectedItemId(fragmentID);
    }

    // Use in case it is needed to change fragment inside same tab
    public void changeFragment(Fragment fragment) {
        applyFragmentTransaction(fragment, fragment.getClass() != HomeContainerFragment.class);
    }

    private void applyFragmentTransaction(Fragment fragment, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        } else {
            for(int i = 0; i < fm.getBackStackEntryCount(); i++) {
                fm.popBackStack();
            }
        }
        fragmentTransaction.commit();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (model.getLastSelectedTab() != id) {
            model.setLastSelectedTab(id);
            applyFragmentTransaction(model.getFragment(id), false);
        }
        return true;
    }

    @Override
    public void reduceUnreadCount() {
        model.reduceUnreadCount();
    }
}