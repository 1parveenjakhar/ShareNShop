package com.puteffort.sharenshop.fragments;

import static com.puteffort.sharenshop.utils.UtilFunctions.isEmailValid;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentMyProfileBinding;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.viewmodels.MyProfileFragmentViewModel;

import java.util.Arrays;

public class MyProfileFragment extends Fragment {
    private FragmentMyProfileBinding binding;
    private MyProfileFragmentViewModel model;

    private UserProfile user;
    private String userID;

    public MyProfileFragment() {}
    public MyProfileFragment(UserProfile user) {
        this.user = user;
    }
    public MyProfileFragment(String userID) {
        this.userID = userID;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_profile, container, false);
        model = new ViewModelProvider(this).get(MyProfileFragmentViewModel.class);

        binding.progressBar.setVisibility(View.GONE);

        setUpComponents();
        addObservers();

        return binding.getRoot();
    }

    private void setUpComponents() {
        // Load only if user does not exist previously
        if (model.getUser().getValue() == null) {
            if (user != null) {
                // must be through some intent
                model.setUser(user);
            } else if (userID != null) {
                // must be through some intent
                model.setUser(userID);
            } else {
                // need to fetch current user
                model.setUser();
            }
        }

        if (model.isOwner()) {
            binding.updateProfile.setOnClickListener(this::updateProfile);
            binding.resetButton.setOnClickListener(this::resetViews);
        } else {
            // hiding views
            for (View view: Arrays.asList(binding.userDetails,
                    binding.updateProfile, binding.resetButton)) {
                view.setVisibility(View.GONE);
            }
            binding.userImage.setClickable(false);
        }
    }

    private void updateProfile(View view) {
        if (model.getUser().getValue() == null) return;

        String email = binding.emailEditText.getText().toString();
        String name = binding.nameEditText.getText().toString();
        String currentPassword = binding.currentPasswordEditText.getText().toString().trim();

        // Email check
        if (!isEmailValid(email)) {
            showToast(requireContext(), "Email is not valid !");
            return;
        }

        // Name Check
        if (name.trim().isEmpty()) {
            showToast(requireContext(), "Name should not be empty!");
            return;
        }

        // Current Pass Check
        if (currentPassword.trim().isEmpty()) {
            showToast(requireContext(), "Please fill your current password.");
            return;
        }

        String newPassword = binding.passwordEditText.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        model.updateDetails(name, email, currentPassword, newPassword).observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            if (message.equals(model.SUCCESS)) {
                showToast(requireContext(), "Details Updated Successfully :)");
            } else {
                showToast(requireContext(), "Some error occurred:\n" + message);
            }
            binding.progressBar.setVisibility(View.INVISIBLE);
        });
    }

    private void resetViews(View view) {
        if (user == null) return;
        setUserDetails();

        if (model.isOwner()) {
            binding.passwordEditText.setText("");
            binding.currentPasswordEditText.setText("");
        }
    }

    private void addObservers() {
        model.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                binding.progressBar.setVisibility(View.VISIBLE);
                return;
            }
            this.user = user;
            setUserDetails();
        });
    }

    private void setUserDetails() {
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.userName.setText(user.getName());
        binding.userEmail.setText(user.getEmail());
        Glide.with(requireContext()).load(user.getImageURL())
                .error(R.drawable.default_person_icon)
                .circleCrop().into(binding.userImage);

        binding.nameEditText.setText(user.getName());
        binding.emailEditText.setText(user.getEmail());
    }
}