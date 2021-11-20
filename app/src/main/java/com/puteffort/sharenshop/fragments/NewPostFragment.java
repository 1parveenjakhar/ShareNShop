package com.puteffort.sharenshop.fragments;

import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.getUniqueID;
import static com.puteffort.sharenshop.utils.UtilFunctions.ERROR_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.SERVER_URL;
import static com.puteffort.sharenshop.utils.UtilFunctions.SUCCESS_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.client;
import static com.puteffort.sharenshop.utils.UtilFunctions.getRequest;
import static com.puteffort.sharenshop.utils.UtilFunctions.gson;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentNewPostBinding;
import com.puteffort.sharenshop.models.PostInfo;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

// Does not require a ViewModel
// A user can bear that much data loss
public class NewPostFragment extends Fragment {
    private FragmentNewPostBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseFunctions firebaseFunctions;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public NewPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_post, container, false);
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
            createNewPost(new PostInfo(title, description, userID, days, months, years, people, amount));
        }
    }

    private void createNewPost(PostInfo postInfo) {
        binding.progressBar.setVisibility(View.VISIBLE);

        if (db == null)
            db = FirebaseFirestore.getInstance();
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        if (firebaseFunctions == null) {
            firebaseFunctions = FirebaseFunctions.getInstance();
        }

        String id = getUniqueID(POST_INFO);
        postInfo.setId(id);
        postInfo.setLastActivity(System.currentTimeMillis());

        client.newCall(getRequest(gson.toJson(postInfo), SERVER_URL + "createNewPost")).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("a", "Exception while creating post = " + e.getMessage());
                handler.post(() -> onPostFailure());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.code() != SUCCESS_CODE) {
                    handler.post(() -> onPostFailure());
                    return;
                }
                Log.d("a", "Response is = " + response + ", body = " + Objects.requireNonNull(response.body()).toString());
                handler.post(() -> onPostSuccess());
            }
        });
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