package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.fragments.PostFragment;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserStatus;

import java.util.ArrayList;
import java.util.List;

public class PostFragmentViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final PostInfo postInfo;

    private final List<String> usersInterested, usersAdded, comments;
    private final MutableLiveData<List<String>> usersInterestedLiveData, usersAddedLiveData, commentsLiveData;

    public PostFragmentViewModel() {
        db = FirebaseFirestore.getInstance();
        this.postInfo = PostFragment.getPostInfo();

        this.usersInterested = new ArrayList<>();
        usersInterestedLiveData = new MutableLiveData<>();

        this.comments = new ArrayList<>();
        commentsLiveData = new MutableLiveData<>();

        this.usersAdded = new ArrayList<>();
        this.usersAddedLiveData = new MutableLiveData<>();

        loadPostDetailInfo();
    }

    private void loadPostDetailInfo() {
        db.collection(POST_DETAIL_INFO).document(postInfo.getId())
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        PostDetailInfo postDetailInfo = value.toObject(PostDetailInfo.class);
                        if (postDetailInfo != null) {
                            usersAdded.clear();
                            usersInterested.clear();
                            comments.clear();

                            for (UserStatus userStatus: postDetailInfo.getUsersAdded()) {
                                usersAdded.add(userStatus.getUserID());
                            }
                            usersInterested.addAll(postDetailInfo.getUsersInterested());
                            comments.addAll(postDetailInfo.getComments());

                            usersAddedLiveData.setValue(usersAdded);
                            usersInterestedLiveData.setValue(usersInterested);
                            commentsLiveData.setValue(comments);
                        }
                    }
                });
    }


    public LiveData<List<String>> getUsersInterested() {
        return usersInterestedLiveData;
    }
    public LiveData<List<String>> getComments() {
        return commentsLiveData;
    }
    public LiveData<List<String>> getUsersAdded() {
        return usersAddedLiveData;
    }
}
