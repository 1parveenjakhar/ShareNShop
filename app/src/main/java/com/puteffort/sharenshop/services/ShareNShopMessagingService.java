package com.puteffort.sharenshop.services;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.puteffort.sharenshop.models.UserDeviceToken;
import com.puteffort.sharenshop.utils.DBOperations;

import java.util.Objects;

public class ShareNShopMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseFirestore.getInstance().collection(DBOperations.TOKEN)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .set(new UserDeviceToken(token));
    }
}
