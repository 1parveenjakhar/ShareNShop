package com.puteffort.sharenshop.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentNewPostBinding;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;

import java.util.Objects;

// Does not require a ViewModel
// A user can bear that much data loss
public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private FirebaseFirestore db;

    public interface NewPostReset {
        void resetNewPostFragment();
    }

    public NewPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_post, container, false);
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.postButton.setOnClickListener(this::handlePostListener);

        return binding.getRoot();
    }

    private void handlePostListener(View view) {
        String title, description, days, months, years, people, amount;
        title = Objects.requireNonNull(binding.postTitle.getEditText()).getText().toString();
        description = Objects.requireNonNull(binding.postDescription.getEditText()).getText().toString();
        days = Objects.requireNonNull(binding.postDays.getEditText()).getText().toString();
        months = Objects.requireNonNull(binding.postMonths.getEditText()).getText().toString();
        years = Objects.requireNonNull(binding.postYears.getEditText()).getText().toString();
        amount = Objects.requireNonNull(binding.postAmount.getEditText()).getText().toString();
        people = Objects.requireNonNull(binding.postPeopleRequirement.getEditText()).getText().toString();

        if (TextUtils.isEmpty(title)) {
            showToast(getString(R.string.new_post_title_error));
        } else if (TextUtils.isEmpty(days)) {
            showToast(getString(R.string.new_post_days_error));
        } else if (TextUtils.isEmpty(months)) {
            showToast(getString(R.string.new_post_months_error));
        } else if (TextUtils.isEmpty(years)) {
            showToast(getString(R.string.new_post_years_error));
        } else if (TextUtils.isEmpty(amount)) {
            showToast(getString(R.string.new_post_amount_error));
        } else if (TextUtils.isEmpty(people)) {
            showToast(getString(R.string.new_post_people_error));
        } else {
            String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            createNewPost(new PostInfo(title, userID, days, months, years, people, amount),
                    new PostDetailInfo(description, userID));
        }
    }

    private void createNewPost(PostInfo postInfo, PostDetailInfo postDetailInfo) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        result.observe(this, successful -> {
            if (successful) {
                showToast(getString(R.string.new_post_post_successful));
                binding.progressBar.setVisibility(View.INVISIBLE);
                ((NewPostReset)requireActivity()).resetNewPostFragment();
            } else {
                showToast(getString(R.string.new_post_post_failure));
            }
        });

        binding.progressBar.setVisibility(View.VISIBLE);
        DBOperations.addPost(postInfo, postDetailInfo, result);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}