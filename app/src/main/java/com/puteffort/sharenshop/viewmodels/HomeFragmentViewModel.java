package com.puteffort.sharenshop.viewmodels;

import androidx.appcompat.widget.SearchView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.utils.DBOperations;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentViewModel extends ViewModel implements SearchView.OnQueryTextListener {
    private final MutableLiveData<List<PostInfo>> postsInfoLiveData;
    private final List<PostInfo> originalPosts;
    private final List<PostInfo> postsInfo;

    public HomeFragmentViewModel() {
        postsInfoLiveData = new MutableLiveData<>();
        originalPosts = new ArrayList<>();
        postsInfo = new ArrayList<>();
        getPostsInfoFromFirestore();
    }

    public LiveData<List<PostInfo>> getPostsInfoLiveData() {
        return postsInfoLiveData;
    }

    private void getPostsInfoFromFirestore() {
        DBOperations.getDB().collection(DBOperations.POST_INFO).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        originalPosts.addAll(queryDocumentSnapshots.toObjects(PostInfo.class));
                        postsInfo.addAll(originalPosts);
                        postsInfoLiveData.setValue(postsInfo);
                    }
                });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        postsInfo.clear();
        String query = newText.trim().toLowerCase();
        if (query.isEmpty()) {
            postsInfo.addAll(originalPosts);
        } else {
            for (PostInfo post : originalPosts) {
                if (post.getTitle().toLowerCase().contains(query))
                    postsInfo.add(post);
            }
        }
        postsInfoLiveData.setValue(postsInfo);
        return true;
    }
}
