package com.puteffort.sharenshop.viewmodels;

import android.annotation.SuppressLint;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.fragments.AccountFragment;
import com.puteffort.sharenshop.fragments.HomeContainerFragment;
import com.puteffort.sharenshop.fragments.NewPostFragment;

public class MainActivityViewModel extends ViewModel {
    private final Fragment homeFragment;
    private final Fragment newPostFragment;
    private final Fragment accountFragment;

    private int lastSelectedTab;

    public MainActivityViewModel() {
        lastSelectedTab = -1;

        homeFragment = new HomeContainerFragment();
        newPostFragment = new NewPostFragment();
        accountFragment = new AccountFragment();
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
            default: fragment = null;
        }
        return fragment;
    }
}
