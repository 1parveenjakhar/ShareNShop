package com.puteffort.sharenshop.fragments;

import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;
import static com.puteffort.sharenshop.utils.DBOperations.getUniqueID;
import static com.puteffort.sharenshop.utils.UITasks.showToast;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentNewPostBinding;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.PostStatus;

import java.util.Collections;
import java.util.Objects;

// Does not require a ViewModel
// A user can bear that much data loss
public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

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
            showToast(requireContext(), getString(R.string.new_post_title_error));
        } else if (TextUtils.isEmpty(days)) {
            showToast(requireContext(), getString(R.string.new_post_days_error));
        } else if (TextUtils.isEmpty(months)) {
            showToast(requireContext(), getString(R.string.new_post_months_error));
        } else if (TextUtils.isEmpty(years)) {
            showToast(requireContext(), getString(R.string.new_post_years_error));
        } else if (TextUtils.isEmpty(amount)) {
            showToast(requireContext(), getString(R.string.new_post_amount_error));
        } else if (TextUtils.isEmpty(people)) {
            showToast(requireContext(), getString(R.string.new_post_people_error));
        } else {
            String userID = Objects.requireNonNull(FirebaseAuth.getInstance()).getUid();
            createNewPost(new PostInfo(title, userID, days, months, years, people, amount),
                    new PostDetailInfo(description, userID), userID);
        }
    }

    private void createNewPost(PostInfo postInfo, PostDetailInfo postDetailInfo, String userID) {
        binding.progressBar.setVisibility(View.VISIBLE);

        if (db == null)
            db = FirebaseFirestore.getInstance();
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }

        String id = getUniqueID(POST_INFO);
        postInfo.setId(id);
        postInfo.setLastActivity(System.currentTimeMillis());
        postDetailInfo.setId(id);

        // Adding PostInfo
        db.collection(POST_INFO).document(id).set(postInfo)
                .addOnSuccessListener(post -> {
                    // Adding PostDetailInfo
                    db.collection(POST_DETAIL_INFO).document(id)
                            .set(postDetailInfo)
                            .addOnSuccessListener(postDetail -> {
                                // Adding post to users db
                                db.collection(USER_ACTIVITY).document(userID)
                                        .update(Collections.singletonMap("postsCreated", FieldValue.arrayUnion(id)));
                                db.collection(USER_ACTIVITY).document(userID)
                                        .update(Collections.singletonMap("postsInvolved", FieldValue.arrayUnion(new PostStatus(id))))
                                        .addOnSuccessListener(unused -> onPostSuccess())
                                        .addOnFailureListener(unused -> onPostFailure());
                            })
                            .addOnFailureListener(exception -> onPostFailure());
                    })
                .addOnFailureListener(exception -> onPostFailure());
    }

    private void onPostSuccess() {
        showToast(requireContext(), getString(R.string.new_post_post_successful));
        ((MainActivity)requireActivity()).changeFragment(R.id.homeMenuItem);
    }

    private void onPostFailure() {
        binding.progressBar.setVisibility(View.INVISIBLE);
        showToast(requireContext(), getString(R.string.new_post_post_failure));
    }
}