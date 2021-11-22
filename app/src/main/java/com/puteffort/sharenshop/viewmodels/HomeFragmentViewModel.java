package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;
import static com.puteffort.sharenshop.utils.UtilFunctions.SERVER_URL;
import static com.puteffort.sharenshop.utils.UtilFunctions.SUCCESS_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.client;
import static com.puteffort.sharenshop.utils.UtilFunctions.getRequest;
import static com.puteffort.sharenshop.utils.UtilFunctions.gson;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.PostStatus;
import com.puteffort.sharenshop.models.UserActivity;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.UtilFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragmentViewModel extends ViewModel implements SearchView.OnQueryTextListener {
    // For filtering the list
    private final MutableLiveData<List<PostInfo>> postsLiveData;
    private final List<PostInfo> originalPosts;
    private final List<PostInfo> posts, searchedPosts;

    private final MutableLiveData<Boolean> userDetailsChanged;
    private final Set<String> wishListedPosts;
    private final Map<String, String> postsStatus;

    private final FirebaseFirestore db;
    private final String userID;

    private final MutableLiveData<Boolean> dataUpdating = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> dataChanged, dataAdded, dataRemoved;

    private final Map<String, String> statusMap;
    private final Map<Integer, Integer> lastActivityTimeMap;
    private final Map<Integer, String> sortMap;
    private Set<Integer> lastActivityChips;
    private List<String> fromAndTos;
    private int sortSelected;

    private final Handler handler;

    public HomeFragmentViewModel() {
        originalPosts = new ArrayList<>();
        posts = new ArrayList<>();
        searchedPosts = new ArrayList<>();
        postsLiveData = new MutableLiveData<>(posts);

        userDetailsChanged = new MutableLiveData<>(false);
        wishListedPosts = new HashSet<>();
        postsStatus = new HashMap<>();

        dataAdded = new MutableLiveData<>();
        dataRemoved = new MutableLiveData<>();
        dataChanged = new MutableLiveData<>();

        statusMap = DBOperations.statusMap;

        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getUid();

        lastActivityTimeMap = new HashMap<>();
        lastActivityTimeMap.put(R.id.lessThan1Month, 0);
        lastActivityTimeMap.put(R.id.oneMonthTo6Months, 1);
        lastActivityTimeMap.put(R.id.sixMonthsTo1Year, 2);
        lastActivityTimeMap.put(R.id.greaterThan1Year, 3);
        lastActivityChips = UtilFunctions.getDefaultLastActivityChips();
        fromAndTos = Arrays.asList("0", "", "0", "");

        sortSelected = 2;
        sortMap = new HashMap<>();
        sortMap.put(0, "Amount");
        sortMap.put(1, "People Required");
        sortMap.put(2, "Last Activity");
        handler = new Handler(Looper.getMainLooper());

        fetchPosts();
    }

    private void fetchPosts() {
        new Thread(() -> db.collection(POST_INFO).orderBy("lastActivity", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        for (DocumentChange docChange: value.getDocumentChanges()) {
                            DocumentChange.Type type = docChange.getType();
                            PostInfo post = docChange.getDocument().toObject(PostInfo.class);
                            if (type == DocumentChange.Type.ADDED) {
                                originalPosts.add(post);
                                posts.add(post);
                                searchedPosts.add(post);

                                handler.post(() -> dataAdded.setValue(posts.size()-1));
                            } else if (type == DocumentChange.Type.MODIFIED) {
                                int index = originalPosts.indexOf(post);
                                originalPosts.set(index, post);

                                index = posts.indexOf(post);
                                posts.set(index, post);

                                index = searchedPosts.indexOf(post);
                                posts.set(index, post);

                                int finalIndex = index;
                                handler.post(() -> dataChanged.setValue(finalIndex));
                            } else if (type == DocumentChange.Type.REMOVED) {
                                originalPosts.remove(post);
                                searchedPosts.remove(post);

                                int index = posts.indexOf(post);
                                posts.remove(index);

                                handler.post(() -> dataRemoved.setValue(index));
                            }
                            handler.post(() -> dataUpdating.setValue(false));
                        }
                    } else {
                        // Database Error
                        handler.post(() -> dataUpdating.setValue(false));
                    }
                })).start();
    }

    public void changeUserDetails(UserActivity userActivity) {
        if (userActivity == null) return;
        wishListedPosts.clear();
        wishListedPosts.addAll(userActivity.getPostsWishListed());
        postsStatus.clear();
        for (PostStatus postStatus: userActivity.getPostsInvolved())
            postsStatus.put(postStatus.getPostID(), postStatus.getStatus());

        userDetailsChanged.setValue(true);
    }

    public void changeStatus(int position, Button postStatus, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        String status = postStatus.getText().toString();
        postStatus.setText("");

        PostInfo post = posts.get(position);
        String newStatus = statusMap.get(status);

        try {
            String json = new JSONObject()
                    .put("post", gson.toJson(post))
                    .put("oldStatus", status)
                    .put("newStatus", statusMap.get(status))
                    .put("userID", userID)
                    .toString();

            client.newCall(getRequest(json, SERVER_URL + "changeStatus")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onStatusChangeFailure(progressBar, postStatus, status);
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        onStatusChangeFailure(progressBar, postStatus, status);
                        return;
                    }
                    handler.post(() -> {
                        postsStatus.put(post.getId(), newStatus);
                        progressBar.setVisibility(View.GONE);
                        postStatus.setText(newStatus);
                        dataChanged.setValue(position);
                    });
                }
            });
        } catch (JSONException e) {
            onStatusChangeFailure(progressBar, postStatus, status);
        }
    }

    private void onStatusChangeFailure(ProgressBar progressBar, Button postStatus, String status) {
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);
            postStatus.setText(status);
        });
    }

    public void changePostFavourite(int position, ImageView favorite, boolean isFavourite, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        favorite.setVisibility(View.GONE);
        List<Task<Void>> taskList = new ArrayList<>();

        DocumentReference doc = db.collection(USER_ACTIVITY).document(userID);
        String postID = posts.get(position).getId();
        if (isFavourite) {
            taskList.add(doc.update(Collections.singletonMap("postsWishListed", FieldValue.arrayUnion(postID)))
                    .addOnSuccessListener(unused -> {
                        wishListedPosts.add(postID);
                        dataChanged.setValue(position);
                    }));
        } else {
            taskList.add(doc.update(Collections.singletonMap("postsWishListed", FieldValue.arrayRemove(postID)))
                    .addOnSuccessListener(unused -> {
                        wishListedPosts.remove(postID);
                        dataChanged.setValue(position);
                    }));
        }
        Tasks.whenAllSuccess(taskList).addOnSuccessListener(objects -> {
            progressBar.setVisibility(View.GONE);
            favorite.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public synchronized boolean onQueryTextChange(String newText) {
        dataUpdating.setValue(true);
        searchedPosts.clear();
        String query = newText.trim().toLowerCase();
        if (query.isEmpty()) {
            searchedPosts.addAll(originalPosts);
        } else {
            for (PostInfo post : originalPosts) {
                if (post.getTitle().toLowerCase().contains(query))
                    searchedPosts.add(post);
            }
        }
        filterPosts(lastActivityChips, fromAndTos);
        return true;
    }

    public LiveData<Integer> getDataChanged() {
        return dataChanged;
    }

    public LiveData<Integer> getDataAdded() {
        return dataAdded;
    }

    public LiveData<Integer> getDataRemoved() {
        return dataRemoved;
    }

    public LiveData<List<PostInfo>> getPosts() {
        return postsLiveData;
    }

    public Set<Integer> getLastActivityChips() { return lastActivityChips; }
    public List<String> getEditTexts() {
        return fromAndTos;
    }
    public int getCheckedSort() { return sortSelected; }

    public void filterPosts(Set<Integer> lastActivityChips, List<String> fromAndTos) {
        dataUpdating.setValue(true);

        AsyncTask.execute(() -> {
            this.lastActivityChips = lastActivityChips;
            this.fromAndTos = fromAndTos;
            Set<Integer> allowedCategories = new HashSet<>();
            for (int chip: lastActivityChips) allowedCategories.add(lastActivityTimeMap.get(chip));

            int amountFrom = fromAndTos.get(0).trim().isEmpty() ? 0 : Integer.parseInt(fromAndTos.get(0));
            int amountTo = fromAndTos.get(1).trim().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(fromAndTos.get(1));
            int peopleFrom = fromAndTos.get(2).trim().isEmpty() ? 0 : Integer.parseInt(fromAndTos.get(2));
            int peopleTo = fromAndTos.get(3).trim().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(fromAndTos.get(3));

            List<PostInfo> tmpList = new ArrayList<>(searchedPosts);
            posts.clear();

            for (PostInfo postInfo: tmpList) {
                if (amountFrom <= postInfo.getAmount() && postInfo.getAmount() <= amountTo &&
                        peopleFrom <= postInfo.getPeopleRequired() && postInfo.getPeopleRequired() <= peopleTo &&
                        allowedCategories.contains(getTimeCategory(postInfo.getLastActivity()))) {
                    posts.add(postInfo);
                }
            }
            sortPosts(Objects.requireNonNull(sortMap.get(sortSelected)));
        });
    }

    private int getTimeCategory(long lastActivity) {
        long timeDiff = System.currentTimeMillis() - lastActivity;
        int years = (int)(timeDiff / 31556952000L);
        int months = (int)(timeDiff / 2629746000L);

        int category;
        if (years >= 1) category = 3;
        else if (months >= 6) category = 2;
        else if (months >= 1) category = 1;
        else category = 0;
        return category;
    }

    public LiveData<Boolean> areUserDetailsChanged() {
        return userDetailsChanged;
    }
    public LiveData<Boolean> isDataUpdating() {
        return dataUpdating;
    }
    public Set<String> getWishListedPosts() {
        return wishListedPosts;
    }
    public Map<String, String> getPostsStatus() {
        return postsStatus;
    }

    public void sortPosts(String sortBy) {
        handler.post(() -> dataUpdating.setValue(true));

        switch (sortBy) {
            case "Amount":
                sortSelected = 0;
                Collections.sort(posts, (p1, p2) -> p1.getAmount() - p2.getAmount());
                break;
            case "People Required":
                sortSelected = 1;
                Collections.sort(posts, (p1, p2) -> p1.getPeopleRequired() - p2.getPeopleRequired());
                break;
            case "Last Activity":
                sortSelected = 2;
                Collections.sort(posts, (p1, p2) -> (int)(p1.getLastActivity() - p2.getLastActivity()));
                break;
            case "DEFAULTS":
                sortSelected = -1;
                break;
        }
        handler.post(() -> {
            dataUpdating.setValue(false);
            postsLiveData.setValue(posts);
        });
    }
}
