package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.COMMENT;
import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.fragments.AddedRecyclerView;
import com.puteffort.sharenshop.fragments.CommentRecyclerView;
import com.puteffort.sharenshop.fragments.InterestedRecyclerView;
import com.puteffort.sharenshop.models.Comment;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.models.UserStatus;

import java.util.ArrayList;
import java.util.List;

public class PostFragmentViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final PostInfo postInfo;

    private final InterestedRecyclerView interestedRecyclerView;
    private final CommentRecyclerView commentRecyclerView;
    private final AddedRecyclerView addedRecyclerView;

    private final MutableLiveData<Fragment> selectedTab;
    private int previousTabIndex = 1;

    private final MutableLiveData<String> postDescription = new MutableLiveData<>();

    private final MutableLiveData<List<UserProfile>> usersAddedLiveData, usersInterestedLiveData;
    private final MutableLiveData<List<RecyclerViewComment>> commentsLiveData;

    private final MutableLiveData<Integer> addedIndex, interestedIndex, commentIndex;

    public PostFragmentViewModel(PostInfo postInfo) {
        db = FirebaseFirestore.getInstance();
        this.postInfo = postInfo;

        this.interestedRecyclerView = new InterestedRecyclerView(this, postInfo.getOwnerID());
        this.commentRecyclerView = new CommentRecyclerView(this);
        this.addedRecyclerView = new AddedRecyclerView(this);

        this.selectedTab = new MutableLiveData<>(commentRecyclerView);

        this.usersAddedLiveData = new MutableLiveData<>();
        this.usersInterestedLiveData = new MutableLiveData<>();
        this.commentsLiveData = new MutableLiveData<>();

        this.addedIndex = new MutableLiveData<>();
        this.interestedIndex = new MutableLiveData<>();
        this.commentIndex = new MutableLiveData<>();

        loadPostDetailInfo();
    }

    private void loadPostDetailInfo() {
        db.collection(POST_DETAIL_INFO).document(postInfo.getId())
                .addSnapshotListener((value, error) -> {
                    if (error == null && value != null) {
                        PostDetailInfo postDetailInfo = value.toObject(PostDetailInfo.class);
                        if (postDetailInfo != null) {
                            postDescription.setValue(postDetailInfo.getDescription());

                            fetchUsersAdded(postDetailInfo.getUsersAdded());
                            fetchUsersInterested(postDetailInfo.getUsersInterested());
                            fetchComments(postDetailInfo.getComments());
                        }
                    }
                });
    }

    private void fetchUsersAdded(List<UserStatus> users) {
        List<UserProfile> usersAdded = new ArrayList<>();
        usersAddedLiveData.setValue(usersAdded);
        for (UserStatus user: users) {
            db.collection(USER_PROFILE).document(user.getUserID()).get()
                    .addOnSuccessListener(docSnap -> {
                        if (docSnap != null) {
                            usersAdded.add(docSnap.toObject(UserProfile.class));
                            addedIndex.setValue(usersAdded.size() - 1);
                        }
                    });
        }
    }

    private void fetchUsersInterested(List<String> users) {
        List<UserProfile> usersInterested = new ArrayList<>();
        usersInterestedLiveData.setValue(usersInterested);
        for (String id: users) {
            db.collection(USER_PROFILE).document(id).get()
                    .addOnSuccessListener(docSnap -> {
                        if (docSnap != null) {
                            usersInterested.add(docSnap.toObject(UserProfile.class));
                            interestedIndex.setValue(usersInterested.size() - 1);
                        }
                    });
        }
    }

    private void fetchComments(List<String> commentID) {
        List<RecyclerViewComment> recyclerViewComments = new ArrayList<>();
        commentsLiveData.setValue(recyclerViewComments);
        for (String id : commentID) {
            db.collection(COMMENT).document(id).get()
                    .addOnSuccessListener(commentSnap -> {
                        if (commentSnap != null) {
                            RecyclerViewComment recyclerViewComment = new RecyclerViewComment();
                            Comment comment = commentSnap.toObject(Comment.class);
                            if (comment != null) {
                                db.collection(USER_PROFILE).document(comment.getUserID()).get()
                                        .addOnSuccessListener(docSnap -> {
                                            UserProfile user = docSnap.toObject(UserProfile.class);
                                            if (user != null) {
                                                recyclerViewComment.setName(user.getName());
                                                recyclerViewComment.setImageURL(user.getImageURL());
                                                recyclerViewComments.add(recyclerViewComment);
                                                commentIndex.setValue(recyclerViewComments.size() - 1);
                                            }
                                        });
                                recyclerViewComment.setMessage(comment.getMessage());
                            }
                        }
                    });
        }
    }


    public LiveData<List<UserProfile>> getUsersInterested() {
        return usersInterestedLiveData;
    }
    public LiveData<List<RecyclerViewComment>> getComments() {
        return commentsLiveData;
    }
    public LiveData<List<UserProfile>> getUsersAdded() {
        return usersAddedLiveData;
    }

    public LiveData<Fragment> getSelectedTab() {
        return selectedTab;
    }
    public int getPreviousTabIndex() {
        return previousTabIndex;
    }
    public void setSelectedTab(int tabIndex) {
        Fragment recyclerView;
        previousTabIndex = tabIndex;
        switch (tabIndex) {
            case 0:
                recyclerView = interestedRecyclerView;
                break;
            case 1:
                recyclerView = commentRecyclerView;
                break;
            case 2:
                recyclerView = addedRecyclerView;
                break;
            default:
                recyclerView = null;
        }
        if (recyclerView != null) {
            selectedTab.setValue(recyclerView);
        }
    }

    public LiveData<String> getPostDescription() {
        return postDescription;
    }

    public LiveData<Integer> getAddedIndex() {
        return addedIndex;
    }
    public LiveData<Integer> getInterestedIndex() {
        return interestedIndex;
    }
    public LiveData<Integer> getCommentIndex() {
        return commentIndex;
    }


    public static class RecyclerViewComment {
        private String name, message, imageURL;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }
    }
}


