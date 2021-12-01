package com.puteffort.sharenshop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.puteffort.sharenshop.databinding.ActivityMainBinding;
import com.puteffort.sharenshop.fragments.HistoryContainerFragment;
import com.puteffort.sharenshop.fragments.HomeContainerFragment;
import com.puteffort.sharenshop.fragments.MyProfileFragment;
import com.puteffort.sharenshop.models.Notification;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.UtilFunctions;
import com.puteffort.sharenshop.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityMainBinding binding;
    private MainActivityViewModel model;
    private NavigationBarView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // For intents like Google Assistant
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // not logged in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setTheme();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> UtilFunctions.showToast(this, "Exception: " + e.getMessage()));
        if (setOrientation()) return;

        // Load user details (UserProfile and UserActivity)
        DBOperations.getUserDetails();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setListenersAndObservers();
        onNewIntent(getIntent());
    }

    private void setTheme() {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        // Theme change
        int themeVal = sharedPrefs.getInt(getString(R.string.shared_pref_theme), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (themeVal != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(themeVal);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOrientation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent == null)
            return;

        if (intent.getSerializableExtra("notification") != null) {
            // A notification has been clicked
            Notification notification = (Notification) intent.getSerializableExtra("notification");
            model.markNotificationAsRead(notification);
            changeFragment(new HomeContainerFragment(notification.postID));
        }
        else if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            this.handleDeepLink(intent.getData());
        }
    }

    private void handleDeepLink(Uri data) {
        if(data.getPath().equals("/open")) {
            String featureType = data.getQueryParameter("featureName");
            if(featureType == null) {
                featureType = "";
            }
            navigateToActivity(featureType);
        }
    }

    private void navigateToActivity(String featureType) {
        switch (featureType) {
            case "account":
                changeFragment(R.id.accountMenuItem);
                break;
            case "new post":
                changeFragment(R.id.newPostMenuItem);
                break;
            case "notifications":
                changeFragment(R.id.notificationItem);
                break;
            case "profile":
            case "my profile":
                changeFragment(R.id.accountMenuItem);
                changeFragment(new MyProfileFragment());
                break;
            case "history":
                changeFragment(R.id.accountMenuItem);
                changeFragment(new HistoryContainerFragment());
                break;
            default:
                changeFragment(R.id.homeMenuItem);
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

        model.getUnreadCount().observe(this, unreadCount -> {
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
    public void onBackPressed() {
        Fragment fragment = model.getFragment(model.getLastSelectedTab());
        try {
            if (fragment.getChildFragmentManager().getBackStackEntryCount() > 0)
                fragment.getChildFragmentManager().popBackStackImmediate();
            else
                super.onBackPressed();
        } catch (Exception e) {
            super.onBackPressed();
        }
    }
}