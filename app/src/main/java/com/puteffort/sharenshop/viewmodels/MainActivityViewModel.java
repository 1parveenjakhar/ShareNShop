package com.puteffort.sharenshop.viewmodels;

import android.annotation.SuppressLint;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.navigation.NavigationBarView;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.fragments.AccountFragment;
import com.puteffort.sharenshop.fragments.HomeFragment;
import com.puteffort.sharenshop.fragments.NewPostFragment;

public class MainActivityViewModel extends ViewModel implements NavigationBarView.OnItemSelectedListener {
    private final MutableLiveData<Fragment> currentFragment;
    private final Fragment homeFragment;
    private Fragment newPostFragment;
    private Fragment accountFragment;

    public MainActivityViewModel() {
        currentFragment = new MutableLiveData<>();
        homeFragment = new HomeFragment();
        currentFragment.setValue(homeFragment);
    }

    public LiveData<Fragment> getCurrentFragment() {
        return currentFragment;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.homeMenuItem:
                fragment = homeFragment; break;
            case R.id.newPostMenuItem: {
                if (newPostFragment == null)
                    newPostFragment = new NewPostFragment();
                fragment = newPostFragment;
                break;
            }
            case R.id.accountMenuItem: {
                if (accountFragment == null)
                    accountFragment = new AccountFragment();
                fragment = accountFragment;
                break;
            }
            default: fragment = null;
        }

        if (fragment != null) {
            currentFragment.setValue(fragment);
            return true;
        }

        return false;
    }
}
