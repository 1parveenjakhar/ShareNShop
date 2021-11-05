package com.puteffort.sharenshop.viewmodels;

import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.puteffort.sharenshop.R;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentViewModel extends ViewModel implements SearchView.OnQueryTextListener {
    private final MutableLiveData<List<PostInfo>> postsLiveData;
    private final List<PostInfo> originalPosts;
    private final List<PostInfo> posts;
    private final FirebaseFirestore db;
    private final MutableLiveData<Integer> toastMessage = new MutableLiveData<>();

    private final MutableLiveData<Boolean> dataUpdating = new MutableLiveData<>(true);

    private final MutableLiveData<Integer> dataChanged, dataAdded, dataRemoved;

    public HomeFragmentViewModel() {
        originalPosts = new ArrayList<>();
        posts = new ArrayList<>();
        postsLiveData = new MutableLiveData<>(posts);

        dataAdded = new MutableLiveData<>();
        dataRemoved = new MutableLiveData<>();
        dataChanged = new MutableLiveData<>();

        db = FirebaseFirestore.getInstance();
        fetchPosts();
    }

    private void fetchPosts() {
        db.collection(DBOperations.POST_INFO).orderBy("lastActivity", Query.Direction.DESCENDING)
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

    public LiveData<List<PostInfo>> getPostsLiveData() {
        return postsLiveData;
    }

    public LiveData<Boolean> isDataUpdating() {
        return dataUpdating;
    }

    public LiveData<Integer> getToastMessage() {
        return toastMessage;
    }
}
