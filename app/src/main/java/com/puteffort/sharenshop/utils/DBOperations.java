package com.puteffort.sharenshop.utils;

import android.annotation.SuppressLint;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.models.PostDetailInfo;
import com.puteffort.sharenshop.models.PostInfo;

public class DBOperations {
    @SuppressLint("StaticFieldLeak")
    private static FirebaseFirestore db;
    public final static String POST_INFO = "PostInfo";
    public final static String POST_DETAIL_INFO = "PostDetailInfo";
    public final static String USER_PROFILE = "UserProfile";
    public final static String USER_ACTIVITY = "UserActivity";
    public final static String COMMENT = "Comment";

    public static void addPost(PostInfo postInfo, PostDetailInfo postDetailInfo, MutableLiveData<Boolean> result) {
        String id = getUniqueID(POST_INFO);
        postInfo.setId(id);
        postInfo.setLastActivity(System.currentTimeMillis());
        postDetailInfo.setId(id);
        getDB().collection(POST_INFO).document(id).set(postInfo)
                .addOnSuccessListener(docRef -> getDB().collection(POST_DETAIL_INFO).document(id).set(postDetailInfo)
                        .addOnSuccessListener(doc2Ref -> setResultValue(result, true))
                        .addOnFailureListener(exception -> setResultValue(result, false)))
                .addOnFailureListener(exception -> setResultValue(result, false));
    }

    private static FirebaseFirestore getDB() {
        if (db == null) db = FirebaseFirestore.getInstance();
        return db;
    }

    private static void setResultValue(MutableLiveData<Boolean> result, boolean value) {
        if (result != null)
            result.setValue(value);
    }

    private static String getUniqueID(String collectionName) {
        return getDB().collection(collectionName).document().getId();
    }
}
