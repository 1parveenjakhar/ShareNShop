package com.puteffort.sharenshop.utils;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.models.UserActivity;
import com.puteffort.sharenshop.models.UserProfile;

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

    public final static Map<String, String> statusMap;

    static {
        statusMap = new HashMap<>();
        statusMap.put("Interested ?", "Added");
        statusMap.put("Final Confirmation ?", "Accepted !");
    }

    public static MutableLiveData<UserProfile> userProfileLiveData = new MutableLiveData<>();
    public static MutableLiveData<UserActivity> userActivityLiveData = new MutableLiveData<>();

    public static void getUserDetails() {
        db.collection(USER_PROFILE).document(Objects.requireNonNull(auth.getUid())).get()
                .addOnSuccessListener(docSnap -> {
                    UserProfile userProfile = docSnap.toObject(UserProfile.class);
                    userProfileLiveData.setValue(userProfile);
                });

        db.collection(USER_ACTIVITY).document(Objects.requireNonNull(auth.getUid())).get()
                .addOnSuccessListener(docSnap -> {
                    UserActivity userActivity = docSnap.toObject(UserActivity.class);
                    userActivityLiveData.setValue(userActivity);
                });
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
