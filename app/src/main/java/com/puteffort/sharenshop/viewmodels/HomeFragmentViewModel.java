package com.puteffort.sharenshop.viewmodels;

import static android.view.View.GONE;
import static com.puteffort.sharenshop.utils.DBOperations.INTERESTED;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.PostStatus;
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
    private final MutableLiveData<List<RecyclerViewPost>> postsLiveData;
    private final List<RecyclerViewPost> originalPosts;
    private final List<RecyclerViewPost> posts, searchedPosts;

    private final FirebaseFirestore db;
    private final String userID;

    private final MutableLiveData<Boolean> dataUpdating = new MutableLiveData<>(true);

    private final Map<String, String> statusMap;
    private final Map<Integer, Integer> lastActivityTimeMap;
    private final Map<Integer, String> sortMap;
    private Set<Integer> lastActivityChips;
    private List<String> fromAndTos;
    private int sortSelected;

    private final Handler handler;
    private String previousSearch = "";

    public HomeFragmentViewModel() {
        originalPosts = new ArrayList<>();
        posts = new ArrayList<>();
        searchedPosts = new ArrayList<>();
        postsLiveData = new MutableLiveData<>();

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

        observeUserDetails();
    }

    private void observeUserDetails() {
        DBOperations.getUserActivity().observeForever(userActivity -> {
            if (userActivity == null) return;

            AsyncTask.execute(() -> {
                Set<String> wishListedPosts = new HashSet<>(userActivity.getPostsWishListed());
                Map<String, String> postsStatus = new HashMap<>();
                for (PostStatus postStatus: userActivity.getPostsInvolved()) {
                    postsStatus.put(postStatus.getPostID(), postStatus.getStatus());
                }

                fetchPosts(wishListedPosts, postsStatus);
            });
        });
    }
    private void fetchPosts(Set<String> wishListedPosts, Map<String, String> postsStatus) {
        originalPosts.clear();
        db.collection(POST_INFO)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        for (DocumentChange docChange: value.getDocumentChanges()) {
                            DocumentChange.Type type = docChange.getType();
                            PostInfo postInfo = docChange.getDocument().toObject(PostInfo.class);
                            RecyclerViewPost post = new RecyclerViewPost(postInfo);

                            if (type == DocumentChange.Type.ADDED) {
                                post.setStatus(postsStatus.get(postInfo.getId()));
                                post.setFavourite(wishListedPosts.contains(postInfo.getId()));
                                originalPosts.add(post);
                            } else if (type == DocumentChange.Type.MODIFIED) {
                                originalPosts.set(originalPosts.indexOf(post), post);
                            } else if (type == DocumentChange.Type.REMOVED) {
                                originalPosts.remove(post);
                            }
                        }
                        onQueryTextChange(previousSearch);
                    } else {
                        // Database Error
                        handler.post(() -> dataUpdating.setValue(false));
                    }
                });
    }

    public void changeStatus(int position, Button statusButton, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        String status = statusButton.getText().toString();
        statusButton.setText("");

        PostInfo post = posts.get(position).getPostInfo();
        String newStatus = statusMap.get(status);

        try {
            String name = DBOperations.getUserProfile().getValue() == null
                    ? "User" : DBOperations.getUserProfile().getValue().getName();
            String json = new JSONObject()
                    .put("post", gson.toJson(post))
                    .put("oldStatus", status)
                    .put("newStatus", statusMap.get(status))
                    .put("userID", userID)
                    .put("userName", name)
                    .toString();

            client.newCall(getRequest(json, SERVER_URL + "changeStatus")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    onStatusChangeFailure(progressBar, statusButton, status);
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        onStatusChangeFailure(progressBar, statusButton, status);
                        return;
                    }
                    handler.post(() -> {
                        progressBar.setVisibility(GONE);
                        statusButton.setText(newStatus);
                        posts.get(position).setStatus(newStatus);
                    });
                }
            });
        } catch (JSONException e) {
            onStatusChangeFailure(progressBar, statusButton, status);
        }
    }
    private void onStatusChangeFailure(ProgressBar progressBar, Button postStatus, String status) {
        handler.post(() -> {
            progressBar.setVisibility(GONE);
            postStatus.setText(status);
        });
    }

    public void changePostFavourite(int position, ImageView favorite, boolean isFavourite, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        favorite.setVisibility(View.INVISIBLE);
        String postID = posts.get(position).getPostInfo().getId();

        db.collection(USER_ACTIVITY).document(userID)
                .update(Collections.singletonMap("postsWishListed",
                isFavourite ? FieldValue.arrayUnion(postID) : FieldValue.arrayRemove(postID)))
                .addOnSuccessListener(unused -> {
                    posts.get(position).setFavourite(isFavourite);
                    favorite.setImageResource(isFavourite ? R.drawable.filled_star_icon : R.drawable.unfilled_star_icon);
                    progressBar.setVisibility(GONE);
                    favorite.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(error -> onFavouriteChangeFailure(favorite, progressBar));
    }
    private void onFavouriteChangeFailure(ImageView icon, ProgressBar progressBar) {
        icon.setVisibility(View.VISIBLE);
        progressBar.setVisibility(GONE);
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
        previousSearch = query;

        if (query.isEmpty()) {
            searchedPosts.addAll(originalPosts);
        } else {
            for (RecyclerViewPost post : originalPosts) {
                if (post.getPostInfo().getTitle().toLowerCase().contains(query))
                    searchedPosts.add(post);
            }
        }
        filterPosts(lastActivityChips, fromAndTos);
        return true;
    }

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

            List<RecyclerViewPost> tmpList = new ArrayList<>(searchedPosts);
            posts.clear();

            for (RecyclerViewPost recyclerViewPost: tmpList) {
                PostInfo postInfo = recyclerViewPost.getPostInfo();
                if (amountFrom <= postInfo.getAmount() && postInfo.getAmount() <= amountTo &&
                        peopleFrom <= postInfo.getPeopleRequired() && postInfo.getPeopleRequired() <= peopleTo &&
                        allowedCategories.contains(getTimeCategory(postInfo.getLastActivity()))) {
                    posts.add(recyclerViewPost);
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

    public void sortPosts(String sortBy) {
        handler.post(() -> dataUpdating.setValue(true));

        switch (sortBy) {
            case "Amount":
                sortSelected = 0;
                Collections.sort(posts, (p1, p2) ->
                        p2.getPostInfo().getAmount() - p1.getPostInfo().getAmount());
                break;
            case "People Required":
                sortSelected = 1;
                Collections.sort(posts, (p1, p2) ->
                        p2.getPostInfo().getPeopleRequired() - p1.getPostInfo().getPeopleRequired());
                break;
            case "Last Activity":
                sortSelected = 2;
                Collections.sort(posts, (p1, p2) ->
                        (int)(p2.getPostInfo().getLastActivity() - p1.getPostInfo().getLastActivity()));
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


    public LiveData<Boolean> isDataUpdating() {
        return dataUpdating;
    }
    public LiveData<List<RecyclerViewPost>> getPosts() {
        return postsLiveData;
    }

    public Set<Integer> getLastActivityChips() { return lastActivityChips; }
    public List<String> getEditTexts() {
        return fromAndTos;
    }
    public int getCheckedSort() { return sortSelected; }

    public static class RecyclerViewPost {
        private final PostInfo postInfo;
        private boolean isFavourite;
        private String status;

        public RecyclerViewPost(PostInfo postInfo) {
            this.postInfo = postInfo;
            this.status = INTERESTED;
        }

        @NonNull
        @Override
        public String toString() {
            return postInfo.toString() + "->" + isFavourite;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RecyclerViewPost that = (RecyclerViewPost) o;

            return postInfo.equals(that.postInfo);
        }

        public PostInfo getPostInfo() {
            return postInfo;
        }

        public boolean isFavourite() {
            return isFavourite;
        }

        public void setFavourite(boolean favourite) {
            isFavourite = favourite;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            if (status == null) return;
            this.status = status;
        }
    }
}
