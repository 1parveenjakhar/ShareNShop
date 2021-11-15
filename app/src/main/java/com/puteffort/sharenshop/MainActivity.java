package com.puteffort.sharenshop;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationBarView;
import com.puteffort.sharenshop.databinding.ActivityMainBinding;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{
    private ActivityMainBinding binding;
    private MainActivityViewModel model;
    private NavigationBarView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (setOrientation()) return;

        // Load user details (UserProfile and UserActivity)
        DBOperations.getUserDetails();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setListenersAndObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOrientation();
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
        navBar = ((NavigationBarView)binding.bottomNav);
        navBar.setOnItemSelectedListener(this);

        int lastTab = model.getLastSelectedTab();
        if (lastTab == -1) lastTab = R.id.homeMenuItem;
        navBar.setSelectedItemId(lastTab);
    }

    // Use if need to change the tab i.e. delete all fragments in stack
    public void changeFragment(int fragmentID) {
        navBar.setSelectedItemId(fragmentID);
    }

    // Use in case it is needed to change fragment inside same tab
    public void changeFragment(Fragment fragment) {
        applyFragmentTransaction(fragment, true);
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
}