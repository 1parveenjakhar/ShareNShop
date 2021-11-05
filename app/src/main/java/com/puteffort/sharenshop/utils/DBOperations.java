package com.puteffort.sharenshop.utils;

import android.annotation.SuppressLint;

import com.google.firebase.firestore.FirebaseFirestore;

public class DBOperations {
    @SuppressLint("StaticFieldLeak")
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public final static String POST_INFO = "PostInfo";
    public final static String POST_DETAIL_INFO = "PostDetailInfo";
    public final static String USER_PROFILE = "UserProfile";
    public final static String USER_ACTIVITY = "UserActivity";
    public final static String COMMENT = "Comment";

    public static String getUniqueID(String collectionName) {
        return db.collection(collectionName).document().getId();
    }
}
