package com.puteffort.sharenshop.viewmodels;

import static android.view.View.GONE;
import static com.puteffort.sharenshop.utils.DBOperations.ACCEPTED;
import static com.puteffort.sharenshop.utils.DBOperations.ADDED;
import static com.puteffort.sharenshop.utils.DBOperations.COMMENT;
import static com.puteffort.sharenshop.utils.DBOperations.FINAL_CONFIRMATION;
import static com.puteffort.sharenshop.utils.DBOperations.INTERESTED;
import static com.puteffort.sharenshop.utils.DBOperations.POST_DETAIL_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.POST_INFO;
import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;
import static com.puteffort.sharenshop.utils.DBOperations.getUserProfile;
import static com.puteffort.sharenshop.utils.UtilFunctions.SERVER_URL;
import static com.puteffort.sharenshop.utils.UtilFunctions.SUCCESS_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.client;
import static com.puteffort.sharenshop.utils.UtilFunctions.getRequest;
import static com.puteffort.sharenshop.utils.UtilFunctions.gson;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.GroupMember;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.puteffort.sharenshop.R;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PostFragmentViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private PostInfo postInfo;
    private PostDetailInfo postDetailInfo;

    private final MutableLiveData<Drawable> ownerImage = new MutableLiveData<>();
    private final MutableLiveData<String> ownerImageURL = new MutableLiveData<>();

    private final InterestedRecyclerView interestedRecyclerView;
    private final CommentRecyclerView commentRecyclerView;
    private final AddedRecyclerView addedRecyclerView;

    private final MutableLiveData<PostInfo> postInfoLiveData;

    private List<AddedUser> usersAdded;
    private final MutableLiveData<List<AddedUser>> usersAddedLiveData = new MutableLiveData<>();
    private List<UserProfile> usersInterested;
    private final MutableLiveData<List<UserProfile>> usersInterestedLiveData = new MutableLiveData<>();
    private List<RecyclerViewComment> comments;
    private final MutableLiveData<List<RecyclerViewComment>> commentsLiveData = new MutableLiveData<>();
    private Map<String, String> statusMap;

    private boolean isUserPostOwner;
    private final Handler handler;
    private int previousTab = 1;

    private final ReentrantLock ADDED_LOCK = new ReentrantLock();
    private final ReentrantLock COMMENT_LOCK = new ReentrantLock();
    private final ReentrantLock INTERESTED_LOCK = new ReentrantLock();

    public PostFragmentViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        postInfoLiveData = new MutableLiveData<>(postInfo);

        interestedRecyclerView = new InterestedRecyclerView();
        commentRecyclerView = new CommentRecyclerView();
        addedRecyclerView = new AddedRecyclerView();

        usersAdded = new ArrayList<>();
        usersInterested = new ArrayList<>();
        comments = new ArrayList<>();
        statusMap = new HashMap<>();

        handler = new Handler(Looper.getMainLooper());
    }

    public void setPostInfo(PostInfo postInfo, Drawable ownerImage) {
        this.postInfo = postInfo;
        this.ownerImage.setValue(ownerImage);
        postInfoLiveData.setValue(postInfo);
        this.isUserPostOwner = postInfo.getOwnerID().equals(auth.getUid());
        setPostInfo(postInfo.getId());
    }

    public void setPostInfo(String postID) {
        loadPostInfo(postID);
        loadPostDetailInfo(postID);
    }

    public void loadPostInfo(String postID) {
        db.collection(POST_INFO).document(postID)
                .addSnapshotListener((docSnap, error) -> {
                    if (error == null && docSnap != null) {
                        this.postInfo = docSnap.toObject(PostInfo.class);
                        postInfoLiveData.setValue(postInfo);
                        this.isUserPostOwner = postInfo.getOwnerID().equals(auth.getUid());

                        db.collection(USER_PROFILE).document(postInfo.getOwnerID()).get()
                                .addOnSuccessListener(userSnap -> {
                                    String imageURL = userSnap.getString("imageURL");
                                    ownerImageURL.setValue(imageURL);
                                });
                    }
                });
    }

    public void loadPostDetailInfo(String postID) {
        if (postID == null) {
            if (postInfo == null) return;
            postID = postInfo.getId();
        }

        db.collection(POST_DETAIL_INFO).document(postID)
                .addSnapshotListener((docSnap, error) -> {
                    if (error == null && docSnap != null) {
                        ADDED_LOCK.lock();
                        COMMENT_LOCK.lock();
                        INTERESTED_LOCK.lock();

                        PostDetailInfo postDetailInfo = docSnap.toObject(PostDetailInfo.class);
                        if (postDetailInfo != null) {
                            this.postDetailInfo = postDetailInfo;
                            fetchUsersAdded(postDetailInfo.getUsersAdded());
                            fetchUsersInterested(postDetailInfo.getUsersInterested());
                            fetchComments(postDetailInfo.getComments());
                        }
                    }
                });
    }

    private void fetchUsersAdded(List<UserStatus> users) {
        List<AddedUser> tmpUsers = new ArrayList<>();
        if (users.isEmpty()) {
            handler.post(() -> usersAddedLiveData.setValue(tmpUsers));
            usersAdded = tmpUsers;
            ADDED_LOCK.unlock();
            return;
        }

        List<String> ids = new ArrayList<>();
        statusMap = new HashMap<>();
        for (UserStatus user: users) {
            ids.add(user.getUserID());
            statusMap.put(user.getUserID(), user.getStatus());
        }
        db.collection(USER_PROFILE).whereIn("id", ids).get()
                .addOnSuccessListener(docSnaps -> {
                    for (QueryDocumentSnapshot docSnap: docSnaps) {
                        UserProfile profile = docSnap.toObject(UserProfile.class);
                        tmpUsers.add(new AddedUser(profile, statusMap.get(profile.getId())));
                    }
                    usersAdded = tmpUsers;
                    handler.post(() -> usersAddedLiveData.setValue(tmpUsers));
                    ADDED_LOCK.unlock();
                })
                .addOnFailureListener(error -> ADDED_LOCK.unlock());
    }

    private void fetchUsersInterested(List<String> users) {
        List<UserProfile> tmpUsers = new ArrayList<>();
        if (users.isEmpty()) {
            handler.post(() -> usersInterestedLiveData.setValue(tmpUsers));
            usersInterested = tmpUsers;
            INTERESTED_LOCK.unlock();
            return;
        }

        db.collection(USER_PROFILE).whereIn("id", users).get()
                .addOnSuccessListener(docSnaps -> {
                    for (QueryDocumentSnapshot docSnap: docSnaps) {
                        tmpUsers.add(docSnap.toObject(UserProfile.class));
                    }
                    handler.post(() -> usersInterestedLiveData.setValue(tmpUsers));
                    usersInterested = tmpUsers;
                    INTERESTED_LOCK.unlock();
                })
                .addOnFailureListener(error -> INTERESTED_LOCK.unlock());
    }

    private void fetchComments(List<String> commentIDs) {
        List<RecyclerViewComment> tmpComments = new ArrayList<>();
        if (commentIDs.isEmpty()) {
            handler.post(() -> commentsLiveData.setValue(tmpComments));
            comments = tmpComments;
            COMMENT_LOCK.unlock();
            return;
        }

        db.collection(COMMENT).whereIn("id", commentIDs).orderBy("postedTime", Query.Direction.ASCENDING)
            .get().addOnSuccessListener(commentSnaps -> {
                List<String> userIDs = new ArrayList<>();
                Map<String, UserProfile> userMapping = new HashMap<>();

                for (QueryDocumentSnapshot commentSnap: commentSnaps) {
                    Comment comment = commentSnap.toObject(Comment.class);
                    tmpComments.add(new RecyclerViewComment(comment));
                    userIDs.add(comment.getUserID());
                }

                // Even though this check is not necessary, but still
                if (!userIDs.isEmpty()) {
                    db.collection(USER_PROFILE).whereIn("id", userIDs).get()
                            .addOnSuccessListener(userSnaps -> {
                                for (QueryDocumentSnapshot userSnap: userSnaps) {
                                    UserProfile user = userSnap.toObject(UserProfile.class);
                                    userMapping.put(user.getId(), user);
                                }

                                for (RecyclerViewComment comment: tmpComments) {
                                    comment.setUserProfile(userMapping.get(comment.getComment().getUserID()));
                                }

                                handler.post(() -> commentsLiveData.setValue(tmpComments));
                                comments = tmpComments;
                                COMMENT_LOCK.unlock();
                            }).addOnFailureListener(error -> COMMENT_LOCK.unlock());
                } else {
                    comments = tmpComments;
                    COMMENT_LOCK.unlock();
                }
        }).addOnFailureListener(error -> COMMENT_LOCK.unlock());

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
            if (position >= usersInterested.size()) {
                return;
            }
            String userID = usersInterested.get(position).getId();
            String json = new JSONObject()
                    .put("postID", postInfo.getId())
                    .put("userID", userID)
                    .put("postTitle", postInfo.getTitle())
                    .put("requirement", postInfo.getPeopleRequired())
                    .toString();
            progressBar.setVisibility(View.VISIBLE);
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
                        addUserToGroupChat(postInfo, userID);
                        progressBar.setVisibility(View.INVISIBLE);
                    });
                }
            });
        } catch (JSONException e) {
            interestedUserChangeFailure(progressBar);
        }
    }

    private void addUserToGroupChat(PostInfo postInfo, String userId) {
        String GUID = postInfo.getId();

        List<GroupMember> members = new ArrayList<>();
        GroupMember groupMember = new GroupMember(userId, CometChatConstants.SCOPE_PARTICIPANT);
        members.add(groupMember);

        CometChat.addMembersToGroup(GUID, members, null, new CometChat.CallbackListener<HashMap<String, String>>(){
            @Override
            public void onSuccess(HashMap<String, String> successMap) {
                Log.d("AddUser","User added: " + groupMember.getName());
            }

            @Override
            public void onError(CometChatException e) {
                Log.e("AddUser","User add failed: " + e.getMessage().toString());
            }
        });
    }

    public void removeUser(int position, ProgressBar progressBar) {
        try {
            String json = new JSONObject()
                    .put("postID", postInfo.getId())
                    .put("userID", usersInterested.get(position).getId())
                    .put("postTitle", postInfo.getTitle())
                    .toString();
            progressBar.setVisibility(View.VISIBLE);
            client.newCall(getRequest(json, SERVER_URL + "rejectUser")).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    handler.post(() -> interestedUserChangeFailure(progressBar));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != SUCCESS_CODE) {
                        handler.post(() -> interestedUserChangeFailure(progressBar));
                    }
                    handler.post(() -> progressBar.setVisibility(View.INVISIBLE));
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

    // Functions related to AddedRecyclerView
    public void askForFinalConfirmation(ProgressBar progressBar, Button button) {
        String originalText = button.getText().toString();
        if (originalText.trim().isEmpty()) return;

        System.out.println("Button text = " + originalText);

        try {
            String json = null, requestLink = null;
            if (originalText.equals(getApplication().getString(R.string.ask_for_final_confirmation))) {
                // Owner want to ask for final confirmation

                List<String> addedIDs = new ArrayList<>();
                for (AddedUser user : usersAdded) {
                    if (user.getStatus().equals(ADDED)) // sending notifications to added users only
                        addedIDs.add(user.getProfile().getId());
                }
                json = new JSONObject()
                        .put("post", gson.toJson(postInfo))
                        .put("users", gson.toJson(addedIDs))
                        .toString();
                requestLink = "askForFinalConfirmation";
            } else if (originalText.equals(getApplication().getString(R.string.want_to_accept))) {
                json = new JSONObject()
                        .put("post", gson.toJson(postInfo))
                        .put("oldStatus", FINAL_CONFIRMATION)
                        .put("newStatus", ACCEPTED)
                        .put("userID", auth.getUid())
                        .put("userName", Objects.requireNonNull(getUserProfile().getValue()).getName())
                        .toString();
                requestLink = "changeStatus";
            }

            if (json == null) return;
            button.setText("");
            progressBar.setVisibility(View.VISIBLE);

            client.newCall(getRequest(json, SERVER_URL + requestLink))
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            onAskRequestCompletion(progressBar, button, originalText);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            onAskRequestCompletion(progressBar, button,
                                    response.code() == SUCCESS_CODE ? "In Progress .." : originalText);
                        }
                    });
        } catch (Exception e) {
            onAskRequestCompletion(progressBar, button, originalText);
        }
    }
    private void onAskRequestCompletion(ProgressBar progressBar, Button button, String text) {
        handler.post(() -> {
            progressBar.setVisibility(GONE);
            button.setText(text);
        });
    }

    public boolean areAllRequiredAdded() {
        if (postDetailInfo == null) return true; // This way we are not showing accept/reject buttons
        return postInfo.getPeopleRequired() == postDetailInfo.getUsersAdded().size();
    }

    public LiveData<List<UserProfile>> getUsersInterested() {
        return usersInterestedLiveData;
    }
    public LiveData<List<RecyclerViewComment>> getComments() {
        return commentsLiveData;
    }
    public LiveData<List<AddedUser>> getUsersAdded() {
        return usersAddedLiveData;
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

    public int getPreviousTab() {
        return previousTab;
    }
    public void setPreviousTab(int tab) {
        previousTab = tab;
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
        private final Comment comment;
        private UserProfile userProfile;

        public RecyclerViewComment(Comment comment) {
            this.comment = comment;
            UserProfile user = DBOperations.getUserProfile().getValue();
            if (user != null) {
                this.userProfile = user;
            }
        }

        @NonNull
        @Override
        public String toString() {
            return comment.getMessage() + "->" + userProfile.getName();
        }

        public Comment getComment() {
            return comment;
        }

        public UserProfile getUserProfile() {
            return userProfile;
        }

        public void setUserProfile(UserProfile userProfile) {
            this.userProfile = userProfile;
        }
    }

    public static class AddedUser {
        private final String status;
        private final UserProfile profile;

        public AddedUser(UserProfile profile, String status) {
            this.profile = profile;
            this.status = (status == null) ? INTERESTED : status;
        }

        public UserProfile getProfile() {
            return profile;
        }

        public String getStatus() {
            return status;
        }

        @NonNull
        @Override
        public String toString() {
            return profile.getName() + "->" + status;
        }
    }
}


