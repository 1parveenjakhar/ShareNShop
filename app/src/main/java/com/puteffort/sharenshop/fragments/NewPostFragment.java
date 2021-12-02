package com.puteffort.sharenshop.fragments;

import static android.view.View.GONE;
import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.getUniqueID;
import static com.puteffort.sharenshop.utils.UtilFunctions.SERVER_URL;
import static com.puteffort.sharenshop.utils.UtilFunctions.SUCCESS_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.client;
import static com.puteffort.sharenshop.utils.UtilFunctions.getRequest;
import static com.puteffort.sharenshop.utils.UtilFunctions.gson;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.MainActivity;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.databinding.FragmentNewPostBinding;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserStatus;
import com.puteffort.sharenshop.utils.Messenger;

import org.json.JSONObject;

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
    private final Handler handler = new Handler(Looper.getMainLooper());
    private PostInfo postInfo;

    public NewPostFragment() {
        // Required empty public constructor
    }

    public NewPostFragment(PostInfo postInfo) {
        this.postInfo = postInfo;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_post, container, false);

        if (postInfo != null) {
            binding.postButton.setText("Edit Post");
            setPostInfo();
            binding.postButton.setOnClickListener(this::editPost);
        } else {
            binding.postButton.setOnClickListener(this::handlePostListener);
        }
        binding.progressBar.setVisibility(GONE);

        return binding.getRoot();
    }

    private void setPostInfo() {
        Objects.requireNonNull(binding.postTitle.getEditText()).setText(postInfo.getTitle());
        Objects.requireNonNull(binding.postAmount.getEditText()).setText(String.valueOf(postInfo.getAmount()));
        Objects.requireNonNull(binding.postDescription.getEditText()).setText(postInfo.getDescription());
        Objects.requireNonNull(binding.postPeopleRequirement.getEditText()).setText(String.valueOf(postInfo.getPeopleRequired()));
        Objects.requireNonNull(binding.postDays.getEditText()).setText(String.valueOf(postInfo.getDays()));
        Objects.requireNonNull(binding.postMonths.getEditText()).setText(String.valueOf(postInfo.getMonths()));
        Objects.requireNonNull(binding.postYears.getEditText()).setText(String.valueOf(postInfo.getYears()));

        binding.postPeopleRequirement.getEditText().setEnabled(false);
        binding.includeMe.setVisibility(GONE);
    }

    private void editPost(View view) {
        String title, description, days, months, years, amount;
        title = Objects.requireNonNull(binding.postTitle.getEditText()).getText().toString().trim();
        description = Objects.requireNonNull(binding.postDescription.getEditText()).getText().toString().trim();
        days = Objects.requireNonNull(binding.postDays.getEditText()).getText().toString().trim();
        months = Objects.requireNonNull(binding.postMonths.getEditText()).getText().toString().trim();
        years = Objects.requireNonNull(binding.postYears.getEditText()).getText().toString().trim();
        amount = Objects.requireNonNull(binding.postAmount.getEditText()).getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            showToast(requireContext(), getString(R.string.new_post_title_error));
        } else if (!(isValidTime(days) || isValidTime(months) || isValidTime(years))) {
            showToast(requireContext(), getString(R.string.new_post_days_error));
        } else if (TextUtils.isEmpty(amount)) {
            showToast(requireContext(), getString(R.string.new_post_amount_error));
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            postInfo.setTitle(title);
            postInfo.setAmount(Integer.parseInt(amount));
            postInfo.setDays(days.isEmpty() ? 0 : Integer.parseInt(days));
            postInfo.setMonths(months.isEmpty() ? 0 : Integer.parseInt(months));
            postInfo.setYears(years.isEmpty() ? 0 : Integer.parseInt(years));
            postInfo.setDescription(description);

            if (db == null)
                db = FirebaseFirestore.getInstance();
            db.collection(POST_INFO).document(postInfo.getId()).set(postInfo)
                    .addOnSuccessListener(unused -> {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        requireActivity().getSupportFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(error -> {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        showToast(requireContext(), "Unable to update your post! Try Again!");
                    });
        }
    }

    private boolean isValidTime(String time) {
        time = time.trim();
        if (TextUtils.isEmpty(time)) return false;
        return Integer.parseInt(time) > 0;
    }

    private void handlePostListener(View view) {
        String title, description, days, months, years, people, amount;
        title = Objects.requireNonNull(binding.postTitle.getEditText()).getText().toString().trim();
        description = Objects.requireNonNull(binding.postDescription.getEditText()).getText().toString().trim();
        days = Objects.requireNonNull(binding.postDays.getEditText()).getText().toString().trim();
        months = Objects.requireNonNull(binding.postMonths.getEditText()).getText().toString().trim();
        years = Objects.requireNonNull(binding.postYears.getEditText()).getText().toString().trim();
        amount = Objects.requireNonNull(binding.postAmount.getEditText()).getText().toString().trim();
        people = Objects.requireNonNull(binding.postPeopleRequirement.getEditText()).getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            showToast(requireContext(), getString(R.string.new_post_title_error));
        } else if (!(isValidTime(days) || isValidTime(months) || isValidTime(years))) {
            showToast(requireContext(), getString(R.string.new_post_days_error));
        } else if (TextUtils.isEmpty(amount)) {
            showToast(requireContext(), getString(R.string.new_post_amount_error));
        } else if (TextUtils.isEmpty(people)) {
            showToast(requireContext(), getString(R.string.new_post_people_error));
        } else if (binding.includeMe.isChecked() && Integer.parseInt(people) < 2) {
            showToast(requireContext(), "Add someone else apart from you!");
        } else if (Integer.parseInt(people) < 1) {
            showToast(requireContext(), "People can't be ZERO!");
        } else {
            if(TextUtils.isEmpty(days))
                days = "0";
            if(TextUtils.isEmpty(months))
                months = "0";
            if(TextUtils.isEmpty(years))
                years = "0";
            String userID = Objects.requireNonNull(FirebaseAuth.getInstance()).getUid();
            createNewPost(new PostInfo(title, description, userID, days, months, years, people, amount),
                    binding.includeMe.isChecked());
        }
    }

    private void createNewPost(PostInfo postInfo, boolean isIncluded) {
        binding.progressBar.setVisibility(View.VISIBLE);

        if (db == null)
            db = FirebaseFirestore.getInstance();
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }

        try {
            String id = getUniqueID(POST_INFO);
            postInfo.setId(id);

            String json = new JSONObject()
                    .put("post", gson.toJson(postInfo))
                    .put("isIncluded", isIncluded)
                    .toString();

            client.newCall(getRequest(json, SERVER_URL + "createNewPost")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    handler.post(() -> onPostFailure());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        handler.post(() -> onPostFailure());
                        return;
                    }
                    handler.post(() -> onPostSuccess(postInfo));
                }
            });
        } catch (Exception e) {
            onPostFailure();
        }
    }

    private void onPostSuccess(PostInfo postInfo) {
        createGroup(postInfo);
        showToast(requireContext(), getString(R.string.new_post_post_successful));
        ((MainActivity) requireActivity()).changeFragment(R.id.homeMenuItem);
    }

    private void createGroup(PostInfo postInfo) {
        String GUID = postInfo.getId();
        String groupName = postInfo.getTitle();
        String groupType = CometChatConstants.GROUP_TYPE_PRIVATE;
        String password = "";

        Group new_group = new Group(GUID, groupName, groupType, password);
        new_group.setOwner(postInfo.getOwnerID());
        String description = (postInfo.getDescription() == null || postInfo.getDescription().isEmpty())
                ? "No description" : postInfo.getDescription();
        new_group.setDescription(description);
        new_group.setScope(CometChatConstants.SCOPE_PARTICIPANT);

        CometChat.createGroup(new_group, new CometChat.CallbackListener<Group>() {
            @Override
            public void onSuccess(Group group) {
                Log.d("GroupCreated", "Group created successfully: " + group.toString());
            }

            @Override
            public void onError(CometChatException e) {
                Log.d("GroupCreated", "Group creation failed with exception: " + e.getMessage());
            }
        });
    }

    private void onPostFailure() {
        binding.progressBar.setVisibility(View.INVISIBLE);
        showToast(requireContext(), getString(R.string.new_post_post_failure));
    }
}