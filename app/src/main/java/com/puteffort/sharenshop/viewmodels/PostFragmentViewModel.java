package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.COMMENT;
import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_ACTIVITY;
import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.fragments.AddedRecyclerView;
import com.puteffort.sharenshop.fragments.CommentRecyclerView;
import com.puteffort.sharenshop.fragments.InterestedRecyclerView;
import com.puteffort.sharenshop.models.Comment;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.PostStatus;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.models.UserStatus;
import com.puteffort.sharenshop.utils.DBOperations;

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

    private final MutableLiveData<String> postDescription = new MutableLiveData<>();
    private final MutableLiveData<PostInfo> postInfoLiveData;

    private final List<UserProfile> usersAdded, usersInterested;
    private final List<RecyclerViewComment> comments;

    private final MutableLiveData<Integer> addedIndex, interestedIndex, commentIndex, interestedRemoveIndex;

    private final boolean isUserPostOwner;

    public PostFragmentViewModel(PostInfo postInfo) {
        Log.d("a", "ViewModel created!");
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        this.postInfo = postInfo;
        postInfoLiveData = new MutableLiveData<>(postInfo);

        isUserPostOwner = postInfo.getOwnerID().equals(auth.getUid());

        interestedRecyclerView = new InterestedRecyclerView();
        commentRecyclerView = new CommentRecyclerView();
        addedRecyclerView = new AddedRecyclerView();

        usersAdded = new ArrayList<>();
        usersInterested = new ArrayList<>();
        comments = new ArrayList<>();

        addedIndex = new MutableLiveData<>();
        interestedIndex = new MutableLiveData<>();
        commentIndex = new MutableLiveData<>();
        interestedRemoveIndex = new MutableLiveData<>();

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

        comments.clear();
        usersAdded.clear();
        usersInterested.clear();
        // Notifying observers about data load
        commentIndex.setValue(null);
        addedIndex.setValue(null);
        interestedIndex.setValue(null);
    }

    private void fetchUsersAdded(List<UserStatus> users) {
        // -1 as a flag for empty list
        if (users.isEmpty()) addedIndex.setValue(-1);

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
        // -1 as a flag for empty list
        if (users.isEmpty()) interestedIndex.setValue(-1);

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
        // -1 as a flag for empty list
        if (commentID.isEmpty()) commentIndex.setValue(-1);

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
                            showToast(context, "Commented successfully :)");
                        })
                        .addOnFailureListener(error -> showToast(context, "Failed to comment :(")))
                .addOnFailureListener(unused -> showToast(context, "Failed to comment :("));
    }

    // Functions related to InterestedRecyclerView
    public void addUser(int position, ProgressBar progressBar) {
        String userID = usersInterested.get(position).getId();
        List<Task<Void>> tasks = new ArrayList<>();

        // Updating status to UserActivity
        tasks.add(db.collection(USER_ACTIVITY).document(userID)
                .update(Collections.singletonMap("postsInvolved", FieldValue.arrayRemove(new PostStatus(postInfo.getId(), "Requested !")))));
        tasks.add(db.collection(USER_ACTIVITY).document(userID)
                .update(Collections.singletonMap("postsInvolved", FieldValue.arrayUnion(new PostStatus(postInfo.getId())))));

        // Updating status to PostDetailInfo
        tasks.add(db.collection(POST_DETAIL_INFO).document(postInfo.getId())
                .update(Collections.singletonMap("usersInterested", FieldValue.arrayRemove(userID))));
        tasks.add(db.collection(POST_DETAIL_INFO).document(postInfo.getId())
                .update(Collections.singletonMap("usersAdded", FieldValue.arrayUnion(new UserStatus(userID)))));


        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            usersAdded.add(usersInterested.remove(position));
            addedIndex.setValue(usersAdded.size() - 1);
            interestedRemoveIndex.setValue(position);
            // Setting to null to avoid wrong delete request in InterestedRecyclerView on recreate
            interestedRemoveIndex.setValue(null);
        }).addOnFailureListener(unused -> interestedUserChangeFailure(progressBar));
    }

    public void removeUser(int position, ProgressBar progressBar) {
        String userID = usersInterested.get(position).getId();
        List<Task<Void>> tasks = new ArrayList<>();

        // Updating status to UserActivity
        tasks.add(db.collection(USER_ACTIVITY).document(userID)
        .update(Collections.singletonMap("postsInvolved", FieldValue.arrayRemove(new PostStatus(postInfo.getId(), "Requested !")))));

        // Updating status to PostDetailInfo
        tasks.add(db.collection(POST_DETAIL_INFO).document(postInfo.getId())
                .update(Collections.singletonMap("usersInterested", FieldValue.arrayRemove(userID))));

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            usersInterested.remove(position);
            interestedRemoveIndex.setValue(position);
            // Setting to null to avoid wrong delete request in InterestedRecyclerView on recreate
            interestedRemoveIndex.setValue(null);
        }).addOnFailureListener(unused -> interestedUserChangeFailure(progressBar));
    }

    private void interestedUserChangeFailure(ProgressBar progressBar) {
        showToast(interestedRecyclerView.getContext(), "Unable to process your request :(");
        progressBar.setVisibility(View.INVISIBLE);
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
    public LiveData<Integer> getInterestedRemoveIndex() {
        return interestedRemoveIndex;
    }
    public LiveData<Integer> getCommentIndex() {
        return commentIndex;
    }

    public Fragment getFragment(int position) {
        switch (position) {
            case 0: return interestedRecyclerView;
            case 1: return commentRecyclerView;
            case 2: return addedRecyclerView;
            default: return null;
        }
    }
    public String getFragmentTitle(int position) {
        switch (position) {
            case 0: return "People\nInterested";
            case 1: return "Comments";
            case 2: return "People\nAdded";
            default: return "";
        }
    }

    public boolean isUserPostOwner() {
        return isUserPostOwner;
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


