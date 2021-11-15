package com.puteffort.sharenshop.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.puteffort.sharenshop.LoginActivity;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentAccountBinding;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPrefs;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);

        sharedPrefs = requireActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        setRadioButtons();
        addListeners();

        return binding.getRoot();
    }

    @SuppressLint("SwitchIntDef")
    private void setRadioButtons() {
        RadioButton themeButton;

        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeButton = binding.lightTheme;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeButton = binding.darkTheme;
                break;
            default:
                themeButton = binding.defaultTheme;
        }
        themeButton.setChecked(true);
    }

    @SuppressLint("NonConstantResourceId")
    private void addListeners() {
        binding.logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        binding.themeOptions.setOnCheckedChangeListener((group, checkedId) -> {
            int themeVal;
            switch (checkedId) {
                case R.id.lightTheme: themeVal = AppCompatDelegate.MODE_NIGHT_NO; break;
                case R.id.darkTheme: themeVal = AppCompatDelegate.MODE_NIGHT_YES; break;
                default: themeVal = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }
            AppCompatDelegate.setDefaultNightMode(themeVal);
            sharedPrefs.edit().clear().putInt(getString(R.string.shared_pref_theme), themeVal).apply();
        });

        binding.postsHistoryButton.setOnClickListener(view ->
                ((MainActivity)requireActivity()).changeFragment(new HistoryContainerFragment()));
    }
}