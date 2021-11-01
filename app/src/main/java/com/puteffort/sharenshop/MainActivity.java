package com.puteffort.sharenshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.puteffort.sharenshop.databinding.ActivityMainBinding;
import com.puteffort.sharenshop.fragments.AccountFragment;
import com.puteffort.sharenshop.fragments.HomeFragment;
import com.puteffort.sharenshop.fragments.NewPostFragment;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityMainBinding binding;
    private Fragment homeFragment, newPostFragment, accountFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        homeFragment = new HomeFragment();
        newPostFragment = new NewPostFragment();
        accountFragment = new AccountFragment();

        binding.bottomNav.setOnItemSelectedListener(this);
        binding.bottomNav.setSelectedItemId(R.id.homeMenuItem);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.homeMenuItem: fragment = homeFragment; break;
            case R.id.newPostMenuItem: fragment = newPostFragment; break;
            case R.id.accountMenuItem: fragment = accountFragment; break;
            default: fragment = null;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }

        return false;
    }
}