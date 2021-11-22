package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.COMMENT;
import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;
import static com.puteffort.sharenshop.utils.UtilFunctions.SERVER_URL;
import static com.puteffort.sharenshop.utils.UtilFunctions.SUCCESS_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.client;
import static com.puteffort.sharenshop.utils.UtilFunctions.getRequest;
import static com.puteffort.sharenshop.utils.UtilFunctions.gson;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.puteffort.sharenshop.fragments.AddedRecyclerView;
import com.puteffort.sharenshop.fragments.CommentRecyclerView;
import com.puteffort.sharenshop.fragments.InterestedRecyclerView;
import com.puteffort.sharenshop.models.Comment;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.models.UserStatus;
import com.puteffort.sharenshop.utils.DBOperations;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PostFragmentViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private PostInfo postInfo;
    private final MutableLiveData<Drawable> ownerImage = new MutableLiveData<>();
    private final MutableLiveData<String> ownerImageURL = new MutableLiveData<>();

    private final InterestedRecyclerView interestedRecyclerView;
    private final CommentRecyclerView commentRecyclerView;
    private final AddedRecyclerView addedRecyclerView;

    private final MutableLiveData<PostInfo> postInfoLiveData;

    private final List<UserProfile> usersAdded, usersInterested;
    private final List<RecyclerViewComment> comments;

    private final MutableLiveData<Integer> addedIndex, interestedIndex, commentIndex, interestedRemoveIndex;

    private boolean isUserPostOwner;
    private final Handler handler;

    public PostFragmentViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        postInfoLiveData = new MutableLiveData<>(postInfo);

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

        handler = new Handler(Looper.getMainLooper());
    }

    public void setPostInfo(PostInfo postInfo, Drawable ownerImage) {
        this.postInfo = postInfo;
        this.ownerImage.setValue(ownerImage);
        postInfoLiveData.setValue(postInfo);
        this.isUserPostOwner = postInfo.getOwnerID().equals(auth.getUid());
        loadPostDetailInfo(postInfo.getId());
    }

    public void setPostInfo(String postID) {
        loadPostInfo(postID);
        loadPostDetailInfo(postID);
    }

    public void loadPostInfo(String postID) {
        db.collection(POST_INFO).document(postID).get()
                .addOnSuccessListener(docSnap -> {
                    this.postInfo = docSnap.toObject(PostInfo.class);
                    postInfoLiveData.setValue(postInfo);
                    this.isUserPostOwner = postInfo.getOwnerID().equals(auth.getUid());

                    db.collection(USER_PROFILE).document(postInfo.getOwnerID()).get()
                            .addOnSuccessListener(userSnap -> {
                                String imageURL = userSnap.getString("imageURL");
                                ownerImageURL.setValue(imageURL);
                            });
                });
    }

    public void loadPostDetailInfo(String postID) {
        if (postID == null) {
            if (postInfo == null) return;
            postID = postInfo.getId();
        }

        comments.clear();
        usersAdded.clear();
        usersInterested.clear();
        // Notifying observers about data load
        commentIndex.setValue(null);
        addedIndex.setValue(null);
        interestedIndex.setValue(null);

        String finalPostID = postID;
        new Thread(() -> db.collection(POST_DETAIL_INFO).document(finalPostID).get()
                .addOnSuccessListener(docSnap -> {
                    PostDetailInfo postDetailInfo = docSnap.toObject(PostDetailInfo.class);
                    if (postDetailInfo != null) {
                        fetchUsersAdded(postDetailInfo.getUsersAdded());
                        fetchUsersInterested(postDetailInfo.getUsersInterested());
                        fetchComments(postDetailInfo.getComments());
                    }
                })).start();
    }

    private void fetchUsersAdded(List<UserStatus> users) {
        // -1 as a flag for empty list
        if (users.isEmpty()) {
            handler.post(() -> addedIndex.setValue(-1));
            return;
        }

        List<String> ids = new ArrayList<>();
        for (UserStatus user: users) ids.add(user.getUserID());
        db.collection(USER_PROFILE).whereIn("id", ids).get()
                .addOnSuccessListener(docSnaps -> {
                    for (QueryDocumentSnapshot docSnap: docSnaps) {
                        usersAdded.add(docSnap.toObject(UserProfile.class));
                        handler.post(() -> addedIndex.setValue(usersAdded.size() - 1));
                    }
                });
    }

    private void fetchUsersInterested(List<String> users) {
        // -1 as a flag for empty list
        if (users.isEmpty()) {
            handler.post(() -> interestedIndex.setValue(-1));
            return;
        }

        db.collection(USER_PROFILE).whereIn("id", users).get()
                .addOnSuccessListener(docSnaps -> {
                    for (QueryDocumentSnapshot docSnap: docSnaps) {
                        usersInterested.add(docSnap.toObject(UserProfile.class));
                        handler.post(() -> interestedIndex.setValue(usersInterested.size() - 1));
                    }
                });
    }

    private void fetchComments(List<String> commentIDs) {
        if (commentIDs.isEmpty()) {
            commentIndex.setValue(CommentRecyclerView.EMPTY_LIST);
            return;
        }

        db.collection(COMMENT).whereIn("id", commentIDs).orderBy("postedTime", Query.Direction.ASCENDING)
            .get().addOnSuccessListener(commentSnaps -> {
                for (QueryDocumentSnapshot commentSnap: commentSnaps) {
                    RecyclerViewComment recyclerViewComment = new RecyclerViewComment();
                    Comment comment = commentSnap.toObject(Comment.class);
                    db.collection(USER_PROFILE).document(comment.getUserID()).get()
                            .addOnSuccessListener(docSnap -> {
                                UserProfile user = docSnap.toObject(UserProfile.class);
                                if (user != null) {
                                    recyclerViewComment.setName(user.getName());
                                    recyclerViewComment.setImageURL(user.getImageURL());
                                    comments.add(recyclerViewComment);
                                    handler.post(() -> commentIndex.setValue(comments.size() - 1));
                                }
                            });
                    recyclerViewComment.setMessage(comment.getMessage());
                }
        });

    }

    // Functions related to CommentRecyclerView
    public void addComment(String message, AlertDialog alertDialog, Context context, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        String commentID = DBOperations.getUniqueID(COMMENT);
        Comment comment = new Comment(commentID, message, auth.getUid());

        try {
            String json = new JSONObject()
                    .put("postID", postInfo.getId())
                    .put("comment", gson.toJson(comment)) // JSON itself in form of a string
                    .toString();
            client.newCall(getRequest(json, SERVER_URL + "addComment")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    handler.post(() -> commentFailure(progressBar, context));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        handler.post(() -> commentFailure(progressBar, context));
                        return;
                    }
                    handler.post(() -> {
                        alertDialog.dismiss();
                        comments.add(new RecyclerViewComment(comment.getMessage()));
                        commentIndex.setValue(comments.size() - 1);
                        showToast(context, "Commented successfully :)");
                    });
                }
            });
        } catch (JSONException e) {
            commentFailure(progressBar, context);
        }
    }

    private void commentFailure(ProgressBar progressBar, Context context) {
        progressBar.setVisibility(View.INVISIBLE);
        showToast(context, "Failed to comment :(");
    }

    // Functions related to InterestedRecyclerView
    public void addUser(int position, ProgressBar progressBar) {
        try {
            String json = new JSONObject()
                    .put("postID", postInfo.getId())
                    .put("userID", usersInterested.get(position).getId())
                    .toString();
            client.newCall(getRequest(json, SERVER_URL + "acceptUser")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    handler.post(() -> interestedUserChangeFailure(progressBar));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        handler.post(() -> interestedUserChangeFailure(progressBar));
                        return;
                    }
                    handler.post(() -> {
                        usersAdded.add(usersInterested.remove(position));
                        addedIndex.setValue(usersAdded.size() - 1);
                        interestedRemoveIndex.setValue(position);
                        // Setting to null to avoid wrong delete request in InterestedRecyclerView on recreate
                        interestedRemoveIndex.setValue(null);
                    });
                }
            });
        } catch (JSONException e) {
            interestedUserChangeFailure(progressBar);
        }
    }

    public void removeUser(int position, ProgressBar progressBar) {
        try {
            String json = new JSONObject()
                    .put("postID", postInfo.getId())
                    .put("userID", usersInterested.get(position).getId())
                    .toString();
            client.newCall(getRequest(json, SERVER_URL + "rejectUser")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    handler.post(() -> interestedUserChangeFailure(progressBar));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        handler.post(() -> interestedUserChangeFailure(progressBar));
                        return;
                    }
                    handler.post(() -> {
                        usersInterested.remove(position);
                        interestedRemoveIndex.setValue(position);
                        // Setting to null to avoid wrong delete request in InterestedRecyclerView on recreate
                        interestedRemoveIndex.setValue(null);
                    });
                }
            });
        } catch (JSONException e) {
            interestedUserChangeFailure(progressBar);
        }
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

    public LiveData<PostInfo> getPostInfo() {
        return postInfoLiveData;
    }
    public LiveData<Drawable> getOwnerImage() {
        return ownerImage;
    }
    public LiveData<String> getOwnerImageURL() {
        return ownerImageURL;
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


