package com.puteffort.sharenshop.viewmodels;

import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.utils.DBOperations;

import java.util.ArrayList;
import java.util.List;

public class MyProfileFragmentViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<UserProfile> userLiveData;
    private UserProfile user;
    private boolean isOwner = false;

    public final String SUCCESS = "Success";

    public MyProfileFragmentViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userLiveData = new MutableLiveData<>();
    }

    public void setUser(UserProfile userProfile) {
        user = userProfile;
        userLiveData.setValue(user);
    }

    public void setUser(String userID) {
        db.collection(USER_PROFILE).document(userID).get()
                .addOnSuccessListener(docSnap -> {
                    UserProfile user = docSnap.toObject(UserProfile.class);
                    if (user != null) {
                        this.user = user;
                        userLiveData.setValue(user);
                    }
                });
    }

    // Fetching current user
    public void setUser() {
        isOwner = true;
        DBOperations.getUserProfile().observeForever(user -> {
            if (user != null) {
                this.user = user;
                userLiveData.setValue(user);
            }
        });
    }

    public synchronized LiveData<String> updateDetails(String name, String email, String currentPass, String newPass) {
        MutableLiveData<String> message = new MutableLiveData<>();

        if (auth.getCurrentUser() == null) {
            message.setValue("Unable to fetch user info !");
        } else {
            try {
                auth.signInWithEmailAndPassword(email, currentPass)
                        .addOnSuccessListener(authResult -> {
                            UserProfile newUser = new UserProfile();
                            newUser.setValues(user);
                            List<Task<Void>> taskList = new ArrayList<>();

                            if (!name.equals(user.getName())) newUser.setName(name);

                            // Update email if not same as previous, and then user details
                            if (!email.equals(user.getEmail())) {
                                newUser.setEmail(email);
                                taskList.add(auth.getCurrentUser().updateEmail(email)
                                        .addOnSuccessListener(unused -> {
                                            if (!newUser.isContentSame(user))
                                                db.collection(USER_PROFILE).document(user.getId()).set(newUser)
                                                        .addOnFailureListener(error ->
                                                                message.setValue("Unable to update user details!"));
                                        })
                                        .addOnFailureListener(error -> message.setValue("Problem in updating email!")));
                            } else if (!newUser.isContentSame(user)) {
                                // if email same, then check for user details only
                                taskList.add(db.collection(USER_PROFILE).document(user.getId()).set(newUser)
                                        .addOnFailureListener(error ->
                                                message.setValue("Unable to update user details!")));
                            }

                            // check for password change need
                            if (!newPass.trim().isEmpty() && !newPass.equals(currentPass) && auth.getCurrentUser() != null)
                                taskList.add(auth.getCurrentUser().updatePassword(newPass)
                                        .addOnFailureListener(error ->
                                                message.setValue("Unable to update password!")));

                            Tasks.whenAllSuccess(taskList)
                                    .addOnSuccessListener(unused -> message.setValue(SUCCESS))
                                    .addOnFailureListener(error -> message.setValue("Unexpected Error :("));

                        })
                        .addOnFailureListener(error -> message.setValue("Authentication Error!"));
            } catch (Exception e) {
                Log.d("update", "Error while updating user details = " + e.getMessage());
                message.setValue("Unexpected Error :(");
            }
        }

        return message;
    }

    public boolean isOwner() {
        return isOwner;
    }
    public LiveData<UserProfile> getUser() {
        return userLiveData;
    }
}
