package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import com.puteffort.sharenshop.models.UserStatus;
import com.puteffort.sharenshop.utils.DBOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragmentViewModel extends ViewModel implements SearchView.OnQueryTextListener {
    // For filtering the list
    private final MutableLiveData<List<PostInfo>> postsLiveData;
    private final List<PostInfo> originalPosts;
    private final List<PostInfo> posts;

    private final MutableLiveData<Boolean> userDetailsChanged;
    private final Set<String> wishListedPosts;
    private final Map<String, String> postsStatus;

    private final FirebaseFirestore db;
    private final String userID;
    private final MutableLiveData<Integer> toastMessage = new MutableLiveData<>();

    private final MutableLiveData<Boolean> dataUpdating = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> dataChanged, dataAdded, dataRemoved;

    private final Map<String, String> statusMap;

    public HomeFragmentViewModel() {
        originalPosts = new ArrayList<>();
        posts = new ArrayList<>();
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

        fetchPosts();
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

    public void changeUserDetails(UserActivity userActivity) {
        if (userActivity == null) return;
        wishListedPosts.clear();
        wishListedPosts.addAll(userActivity.getPostsWishListed());
        postsStatus.clear();
        for (PostStatus postStatus: userActivity.getPostsInvolved())
            postsStatus.put(postStatus.getPostID(), postStatus.getStatus());

        userDetailsChanged.setValue(true);
    }

    public void changeStatus(int position, String status) {
        PostInfo post = posts.get(position);
        String newStatus = statusMap.get(status);
        List<Task<Void>> taskList = new ArrayList<>();

        if (status.equals("Interested ?")) {
            // Adding this user to userInterested in Post
            taskList.add(db.collection(POST_DETAIL_INFO).document(post.getId())
                    .update(Collections.singletonMap("usersInterested", FieldValue.arrayUnion(userID))));
        } else {
            // Deleting old status from post
            taskList.add(db.collection(POST_DETAIL_INFO).document(post.getId())
                    .update(Collections.singletonMap("usersAdded", FieldValue.arrayRemove(new UserStatus(userID, status)))));
            // Deleting old status from user
            taskList.add(db.collection(USER_ACTIVITY).document(userID)
                    .update(Collections.singletonMap("postsInvolved", FieldValue.arrayRemove(new PostStatus(post.getId(), status)))));

            // Adding new status to post
            taskList.add(db.collection(POST_DETAIL_INFO).document(post.getId())
                    .update(Collections.singletonMap("usersAdded", FieldValue.arrayUnion(new UserStatus(userID, newStatus)))));
        }

        // Adding new status to user
        taskList.add(db.collection(USER_ACTIVITY).document(userID)
                .update(Collections.singletonMap("postsInvolved", FieldValue.arrayUnion(new PostStatus(post.getId(), newStatus)))));

        Tasks.whenAllSuccess(taskList)
            .addOnSuccessListener(objects -> {
                postsStatus.put(post.getId(), newStatus);
                dataChanged.setValue(position);
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

    public LiveData<Boolean> areUserDetailsChanged() {
        return userDetailsChanged;
    }

    public LiveData<Boolean> isDataUpdating() {
        return dataUpdating;
    }

    public LiveData<Integer> getToastMessage() {
        return toastMessage;
    }

    public Set<String> getWishListedPosts() {
        return wishListedPosts;
    }

    public Map<String, String> getPostsStatus() {
        return postsStatus;
    }
}
