package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.PostStatus;
import com.puteffort.sharenshop.models.UserActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryFragmentViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final String userID;

    private final HashMap<String, PostInfo> idToPostMapping = new HashMap<>();
    private final Set<String> createdIds = new HashSet<>();
    private final Set<String> wishListedIds = new HashSet<>();
    private final Set<String> involvedIds = new HashSet<>();

    private final List<Set<String>> idArray = new ArrayList<>();
    private final Set<Integer> chipNumbers = new HashSet<>();

    private final List<PostInfo> posts = new ArrayList<>();
    private final MutableLiveData<List<PostInfo>> postsLiveData = new MutableLiveData<>();

    private final MutableLiveData<Integer> modifyIndex, deleteIndex;

    private final Handler handler;

    public HistoryFragmentViewModel() {
        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getUid();

        idArray.add(createdIds); // Idx 0
        idArray.add(wishListedIds); // Idx 1
        idArray.add(involvedIds);// Idx 2
        chipNumbers.add(1); // Default selected chip

        modifyIndex = new MutableLiveData<>();
        deleteIndex = new MutableLiveData<>();

        handler = new Handler(Looper.getMainLooper());

        loadData();
    }

    public void loadData() {
        postsLiveData.setValue(null);

        db.collection(USER_ACTIVITY).document(userID).get()
                .addOnSuccessListener(docSnap -> {
                    UserActivity userActivity = docSnap.toObject(UserActivity.class);
                    if (userActivity != null) {
                        createdIds.clear();
                        createdIds.addAll(userActivity.getPostsCreated());

                        wishListedIds.clear();
                        wishListedIds.addAll(userActivity.getPostsWishListed());

                        involvedIds.clear();
                        for (PostStatus postStatus: userActivity.getPostsInvolved())
                            involvedIds.add(postStatus.getPostID());

                        idToPostMapping.clear();

                        fetchPosts();
                    }
                });
    }

    private void fetchPosts() {
        Set<String> allPosts = new HashSet<>(createdIds);
        allPosts.addAll(wishListedIds);
        allPosts.addAll(involvedIds);

        if (allPosts.isEmpty()) {
            setUpPosts();
            return;
        }
        db.collection(POST_INFO).whereIn("id", new ArrayList<>(allPosts)).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                   for (QueryDocumentSnapshot docSnap: queryDocumentSnapshots) {
                       PostInfo postInfo = docSnap.toObject(PostInfo.class);
                       idToPostMapping.put(postInfo.getId(), postInfo);
                   }
                   setUpPosts();
                });
    }

    private void setUpPosts() {
        posts.clear();

        AsyncTask.execute(() -> {
            Set<String> tmpIDs = new HashSet<>();
            for (int chipNum: chipNumbers)
                tmpIDs.addAll(idArray.get(chipNum));
            for (String id: tmpIDs)
                posts.add(idToPostMapping.get(id));
            handler.post(() -> postsLiveData.setValue(posts));
        });
    }

    @SuppressLint("NonConstantResourceId")
    public synchronized void changeData(int chipNum, boolean isChecked) {
        postsLiveData.setValue(null);
        if (isChecked) {
            chipNumbers.add(chipNum);
        } else {
            chipNumbers.remove(chipNum);
        }
        if (chipNumbers.size() > 0)
            setUpPosts();
    }

    public void removeFavourite(int position, ProgressBar favProgress) {
        favProgress.setVisibility(View.VISIBLE);
        String postID = posts.get(position).getId();

        db.collection(USER_ACTIVITY).document(userID)
                .update(Collections.singletonMap("postsWishListed", FieldValue.arrayRemove(postID)))
                .addOnSuccessListener(unused -> {
                    favProgress.setVisibility(View.INVISIBLE);
                    wishListedIds.remove(postID);

                    // if current post is also in other selected criteria
                    if ((chipNumbers.contains(0) || chipNumbers.contains(2)) && (createdIds.contains(postID) || involvedIds.contains(postID))) {
                        modifyIndex.setValue(position);
                        modifyIndex.setValue(null);
                        return;
                    }
                    // Else need to delete from posts
                    posts.remove(position);
                    deleteIndex.setValue(position);
                    deleteIndex.setValue(null);
                })
                .addOnFailureListener(error -> favProgress.setVisibility(View.INVISIBLE));
    }

    public LiveData<List<PostInfo>> getPosts() {
        return postsLiveData;
    }

    public Set<Integer> getChipNumbers() {
        return chipNumbers;
    }

    public Set<String> getWishListedIds() {
        return wishListedIds;
    }

    public LiveData<Integer> getModifyIndex() {
        return modifyIndex;
    }
    public LiveData<Integer> getDeleteIndex() {
        return deleteIndex;
    }
}
