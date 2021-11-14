package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.PostStatus;
import com.puteffort.sharenshop.models.UserActivity;

import java.util.ArrayList;
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

    private final List<Set<String>> filterArray = new ArrayList<>();
    private final Set<Integer> filterNumbers = new HashSet<>();

    private final List<PostInfo> posts = new ArrayList<>();
    private final MutableLiveData<List<PostInfo>> postsLiveData = new MutableLiveData<>();

    private final Handler handler;

    public HistoryFragmentViewModel() {
        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getUid();

        filterArray.add(createdIds); // Idx 0
        filterArray.add(wishListedIds); // Idx 1
        filterArray.add(involvedIds);// Idx 2
        filterNumbers.add(1); // Default selected chip

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
        for (String postID: allPosts) {
            db.collection(POST_INFO).document(postID).get()
                    .addOnSuccessListener(postSnap -> {
                       PostInfo postInfo = postSnap.toObject(PostInfo.class);
                       if (postInfo != null) {
                           idToPostMapping.put(postID, postInfo);
                           if (idToPostMapping.size() == allPosts.size()) {
                               setUpPosts();
                           }
                       }
                    });
        }
    }

    private void setUpPosts() {
        posts.clear();

        AsyncTask.execute(() -> {
            Set<String> tmpIDs = new HashSet<>();
            for (int chipNum: filterNumbers)
                tmpIDs.addAll(filterArray.get(chipNum));
            for (String id: tmpIDs)
                posts.add(idToPostMapping.get(id));
            handler.post(() -> postsLiveData.setValue(posts));
        });
    }

    @SuppressLint("NonConstantResourceId")
    public synchronized void changeData(int chipNum, boolean isChecked) {
        postsLiveData.setValue(null);
        if (isChecked) {
            filterNumbers.add(chipNum);
        } else {
            filterNumbers.remove(chipNum);
        }
        setUpPosts();
    }

    public LiveData<List<PostInfo>> getPosts() {
        return postsLiveData;
    }

}
