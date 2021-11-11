package com.puteffort.sharenshop;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.puteffort.sharenshop.databinding.ActivityMainBinding;
import com.puteffort.sharenshop.fragments.AccountFragment;
import com.puteffort.sharenshop.fragments.HomeFragment;
import com.puteffort.sharenshop.fragments.NewPostFragment;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.viewmodels.MainActivityViewModel;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load user details (UserProfile and UserActivity)
        DBOperations.getUserDetails();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainActivityViewModel.class);

        setListenersAndObservers();
    }

    private void setListenersAndObservers() {
        binding.bottomNav.setOnItemSelectedListener(model);

        List<Class<?>> classes = Arrays.asList(HomeFragment.class, NewPostFragment.class, AccountFragment.class);

        model.getCurrentFragment().observe(this, fragment -> {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

            if (classes.contains(fragment.getClass())) {
                for(int i = 0; i < fm.getBackStackEntryCount(); i++) {
                    fm.popBackStack();
                }
            } else {
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        });
    }

    public void changeFragment(int fragmentID) {
        binding.bottomNav.setSelectedItemId(fragmentID);
    }

    public void changeFragment(Fragment fragment) {
        model.addFragmentToBackStack(fragment);
    }
}