package com.puteffort.sharenshop.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.puteffort.sharenshop.LoginActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentAccountBinding;

import java.util.Objects;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);

        mAuth = FirebaseAuth.getInstance();
        addListeners();

        return binding.getRoot();
    }

    private void addListeners() {
        binding.logoutButton.setOnClickListener(view -> {
            mAuth.signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}