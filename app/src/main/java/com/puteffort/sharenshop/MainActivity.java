package com.puteffort.sharenshop;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainActivityViewModel.class);

        binding.bottomNav.setOnItemSelectedListener(model);
        List<Class<?>> classes = Arrays.asList(HomeFragment.class, NewPostFragment.class, AccountFragment.class);
        model.getCurrentFragment().observe(this, fragment -> {
            if (classes.contains(fragment.getClass())) {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                fm.beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        DBOperations.getUserDetails();
    }

    public void changeFragment(int fragmentID) {
        binding.bottomNav.setSelectedItemId(fragmentID);
    }

    public void changeFragment(Fragment fragment) {
        model.addFragmentToBackStack(fragment);
    }
}