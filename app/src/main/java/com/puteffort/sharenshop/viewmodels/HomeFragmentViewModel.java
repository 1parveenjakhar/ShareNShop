package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;

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
import com.puteffort.sharenshop.models.UserActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragmentViewModel extends ViewModel implements SearchView.OnQueryTextListener {
    private final MutableLiveData<List<PostInfo>> postsLiveData;
    private final MutableLiveData<Set<String>> wishListedPostsLiveData;
    private final Set<String> wishListedPosts;
    private final List<PostInfo> originalPosts;
    private final List<PostInfo> posts;
    private final FirebaseFirestore db;
    private final String userID;
    private final MutableLiveData<Integer> toastMessage = new MutableLiveData<>();

    private final MutableLiveData<Boolean> dataUpdating = new MutableLiveData<>(true);

    private final MutableLiveData<Integer> dataChanged, dataAdded, dataRemoved;

    public HomeFragmentViewModel() {
        originalPosts = new ArrayList<>();
        posts = new ArrayList<>();
        postsLiveData = new MutableLiveData<>(posts);

        wishListedPosts = new HashSet<>();
        wishListedPostsLiveData = new MutableLiveData<>(wishListedPosts);

        dataAdded = new MutableLiveData<>();
        dataRemoved = new MutableLiveData<>();
        dataChanged = new MutableLiveData<>();

        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getUid();

        fetchPosts();
        fetchWishListedPosts();
    }

    private void fetchPosts() {
        db.collection(POST_INFO).orderBy("lastActivity", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        for (DocumentChange docChange: value.getDocumentChanges()) {
                            DocumentChange.Type type = docChange.getType();
                            PostInfo post = docChange.getDocument().toObject(PostInfo.class);
                            if (type == DocumentChange.Type.ADDED) {
                                originalPosts.add(post);
                                posts.add(post);

                                dataAdded.setValue(posts.size()-1);
                            } else if (type == DocumentChange.Type.MODIFIED) {
                                int index = originalPosts.indexOf(post);
                                originalPosts.set(index, post);

                                index = posts.indexOf(post);
                                posts.set(index, post);

                                dataChanged.setValue(index);
                            } else if (type == DocumentChange.Type.REMOVED) {
                                originalPosts.remove(post);

                                int index = posts.indexOf(post);
                                posts.remove(index);

                                dataRemoved.setValue(index);
                            }
                            dataUpdating.setValue(false);
                        }
                    } else {
                        // Database Error
                        toastMessage.setValue(R.string.db_fetch_error);
                        dataUpdating.setValue(false);
                    }
                });
    }

    private void fetchWishListedPosts() {
        db.collection(USER_ACTIVITY).document(userID)
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        UserActivity userActivity = value.toObject(UserActivity.class);
                        if (userActivity != null) {
                            wishListedPosts.clear();
                            wishListedPosts.addAll(userActivity.getPostsWishListed());
                            wishListedPostsLiveData.setValue(wishListedPosts);
                        }
                    }
                });
    }

    public void changePostFavourite(int position, boolean isFavourite) {
        DocumentReference doc = db.collection(USER_ACTIVITY).document(userID);
        String postID = posts.get(position).getId();
        if (isFavourite) {
            doc.update(Collections.singletonMap("postsWishListed", FieldValue.arrayUnion(postID)))
                    .addOnSuccessListener(unused -> {
                        wishListedPosts.add(postID);
                        dataChanged.setValue(position);
                    });
        } else {
            doc.update(Collections.singletonMap("postsWishListed", FieldValue.arrayRemove(postID)))
                    .addOnSuccessListener(unused -> {
                        wishListedPosts.remove(postID);
                        dataChanged.setValue(position);
                    });
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        posts.clear();
        String query = newText.trim().toLowerCase();
        if (query.isEmpty()) {
            posts.addAll(originalPosts);
        } else {
            for (PostInfo post : originalPosts) {
                if (post.getTitle().toLowerCase().contains(query))
                    posts.add(post);
            }
        }
        postsLiveData.setValue(posts);
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

    public LiveData<Set<String>> getWishListedPosts() {
        return wishListedPostsLiveData;
    }

    public LiveData<Boolean> isDataUpdating() {
        return dataUpdating;
    }

    public LiveData<Integer> getToastMessage() {
        return toastMessage;
    }
}
