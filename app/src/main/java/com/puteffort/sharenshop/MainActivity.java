package com.puteffort.sharenshop;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.puteffort.sharenshop.databinding.ActivityMainBinding;
import com.puteffort.sharenshop.fragments.NewPostFragment;
import com.puteffort.sharenshop.viewmodels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity implements NewPostFragment.NewPostReset {
    private ActivityMainBinding binding;
    private MainActivityViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        model = new ViewModelProvider(this).get(MainActivityViewModel.class);

        binding.bottomNav.setOnItemSelectedListener(model);
        model.getCurrentFragment().observe(this, fragment -> getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit());
    }

    @Override
    public void resetNewPostFragment() {
        binding.bottomNav.setSelectedItemId(R.id.homeMenuItem);
    }
}