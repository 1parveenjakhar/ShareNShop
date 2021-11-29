package com.puteffort.sharenshop.utils;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import com.puteffort.sharenshop.models.UserActivity;
import com.puteffort.sharenshop.models.UserProfile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DBOperations {
    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    public final static String POST_INFO = "PostInfo";
    public final static String POST_DETAIL_INFO = "PostDetailInfo";
    public final static String USER_PROFILE = "UserProfile";
    public final static String USER_ACTIVITY = "UserActivity";
    public final static String COMMENT = "Comment";
    public final static String TOKEN = "Token";

    public final static String INTERESTED = "Interested ?";
    public final static String REQUESTED = "Requested !";
    public final static String FINAL_CONFIRMATION = "Final Confirmation ?";
    public final static String ACCEPTED = "Accepted !";
    public final static String ADDED = "Added";
    public final static String OWNER = "Owner";

    private static ListenerRegistration profileListener = null, activityListener = null;

    public final static Map<String, String> statusMap;
    private static String token = null;

    static {
        statusMap = new HashMap<>();
        statusMap.put(INTERESTED, REQUESTED);
        statusMap.put(FINAL_CONFIRMATION, ACCEPTED);
    }

    private static final MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
    private static final MutableLiveData<UserActivity> userActivityLiveData = new MutableLiveData<>();

    public static void getUserDetails() {
        if (profileListener != null) profileListener.remove();
        profileListener = db.collection(USER_PROFILE).document(Objects.requireNonNull(auth.getUid()))
                .addSnapshotListener((docSnap, error) -> {
                    if (error == null && docSnap != null) {
                        UserProfile userProfile = docSnap.toObject(UserProfile.class);
                        userProfileLiveData.setValue(userProfile);
                        if (userProfile != null)
                            setUpToken(userProfile.getId());
                    }
                });

        if (activityListener != null) activityListener.remove();
        activityListener = db.collection(USER_ACTIVITY).document(Objects.requireNonNull(auth.getUid()))
                .addSnapshotListener((docSnap, error) -> {
                    if (error == null && docSnap != null) {
                        UserActivity userActivity = docSnap.toObject(UserActivity.class);
                        userActivityLiveData.setValue(userActivity);
                    }
                });
    }

    private static void setUpToken(String userID) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                if (token != null) {
                    DBOperations.token = token;
                    db.collection(DBOperations.TOKEN).document(userID)
                            .update(Collections.singletonMap("tokens", FieldValue.arrayUnion(token)));
                }
            }
        });
    }

    public static void removeToken(String userID) {
        if (token != null) {
            db.collection(DBOperations.TOKEN).document(userID)
                    .update(Collections.singletonMap("tokens", FieldValue.arrayRemove(token)));
        }
    }

    public static LiveData<UserProfile> getUserProfile() {
        return userProfileLiveData;
    }

    public static LiveData<UserActivity> getUserActivity() {
        return userActivityLiveData;
    }

    public static String getUniqueID(String collectionName) {
        return db.collection(collectionName).document().getId();
    }
}
