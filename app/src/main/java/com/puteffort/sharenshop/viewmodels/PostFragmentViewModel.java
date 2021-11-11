package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.COMMENT;
import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.fragments.AddedRecyclerView;
import com.puteffort.sharenshop.fragments.CommentRecyclerView;
import com.puteffort.sharenshop.fragments.InterestedRecyclerView;
import com.puteffort.sharenshop.models.Comment;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.models.UserStatus;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.UITasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostFragmentViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final PostInfo postInfo;

    private final InterestedRecyclerView interestedRecyclerView;
    private final CommentRecyclerView commentRecyclerView;
    private final AddedRecyclerView addedRecyclerView;

    private final MutableLiveData<Fragment> selectedTab;
    private int previousTabIndex = 1;

    private final MutableLiveData<String> postDescription = new MutableLiveData<>();
    private final MutableLiveData<PostInfo> postInfoLiveData = new MutableLiveData<>();

    private final List<UserProfile> usersAdded, usersInterested;
    private final List<RecyclerViewComment> comments;

    private final MutableLiveData<Integer> addedIndex, interestedIndex, commentIndex;

    public PostFragmentViewModel(PostInfo postInfo) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        this.postInfo = postInfo;

        interestedRecyclerView = new InterestedRecyclerView(this, postInfo.getOwnerID().equals(auth.getUid()));
        commentRecyclerView = new CommentRecyclerView(this);
        addedRecyclerView = new AddedRecyclerView(this);

        selectedTab = new MutableLiveData<>(commentRecyclerView);

        usersAdded = new ArrayList<>();
        usersInterested = new ArrayList<>();
        comments = new ArrayList<>();

        addedIndex = new MutableLiveData<>();
        interestedIndex = new MutableLiveData<>();
        commentIndex = new MutableLiveData<>();

        loadPostDetailInfo();
    }

    public void loadPostInfo() {
        db.collection(POST_INFO).document(postInfo.getId()).get()
                .addOnSuccessListener(docSnap -> postInfoLiveData.setValue(docSnap.toObject(PostInfo.class)));
    }

    public void loadPostDetailInfo() {
        db.collection(POST_DETAIL_INFO).document(postInfo.getId()).get()
                .addOnSuccessListener(docSnap -> {
                    PostDetailInfo postDetailInfo = docSnap.toObject(PostDetailInfo.class);
                    if (postDetailInfo != null) {
                        postDescription.setValue(postDetailInfo.getDescription());

                        fetchUsersAdded(postDetailInfo.getUsersAdded());
                        fetchUsersInterested(postDetailInfo.getUsersInterested());
                        fetchComments(postDetailInfo.getComments());
                    }
                });
    }

    private void fetchUsersAdded(List<UserStatus> users) {
        usersAdded.clear();
        addedIndex.setValue(-1);
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
        usersInterested.clear();
        interestedIndex.setValue(-1);
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
        comments.clear();
        commentIndex.setValue(-1);
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
                                                comments.add(recyclerViewComment);
                                                commentIndex.setValue(comments.size() - 1);
                                            }
                                        });
                                recyclerViewComment.setMessage(comment.getMessage());
                            }
                        }
                    });
        }
    }

    // Functions related to CommentRecyclerView
    public void addComment(Comment comment, AlertDialog alertDialog, Context context) {
        String commentID = DBOperations.getUniqueID(COMMENT);
        comment.setId(commentID);
        comment.setPostID(postInfo.getId());
        comment.setUserID(auth.getUid());

        db.collection(COMMENT).document(commentID).set(comment)
                .addOnSuccessListener(unused -> db.collection(POST_DETAIL_INFO).document(postInfo.getId())
                        .update(Collections.singletonMap("comments", FieldValue.arrayUnion(commentID)))
                        .addOnSuccessListener(none -> {
                            alertDialog.dismiss();
                            comments.add(new RecyclerViewComment(comment.getMessage()));
                            UITasks.showToast(context, "Commented successfully :)");
                        })
                        .addOnFailureListener(error -> UITasks.showToast(context, "Failed to comment :(")))
                .addOnFailureListener(unused -> UITasks.showToast(context, "Failed to comment :("));
    }

    public List<UserProfile> getUsersInterested() {
        return usersInterested;
    }
    public List<RecyclerViewComment> getComments() {
        return comments;
    }
    public List<UserProfile> getUsersAdded() {
        return usersAdded;
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
    public LiveData<PostInfo> getPostInfo() {
        return postInfoLiveData;
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
        public RecyclerViewComment() {}

        public RecyclerViewComment(String message) {
            this.message = message;
            UserProfile user = DBOperations.getUserProfile().getValue();
            if (user != null) {
                name = user.getName();
                imageURL = user.getImageURL();
            }
        }

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


