package com.puteffort.sharenshop;

import static com.puteffort.sharenshop.utils.UtilFunctions.isEmailValid;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.*;
import com.puteffort.sharenshop.databinding.ActivityLoginBinding;
import com.puteffort.sharenshop.models.UserActivity;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.utils.Constants;
import com.puteffort.sharenshop.utils.DBOperations;
import com.puteffort.sharenshop.utils.Messenger;
import com.puteffort.sharenshop.utils.UtilFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mSignInClient;
    private final int SIGN_IN_USING_EMAIL = 0;
    private final int SIGN_IN_USING_GOOGLE = 1;
    //private String TAG = this.getClass().getSimpleName();
    private final String TAG = "loginTrack";

    private final String IS_LINKING = "IS_LINKING";

    //Cloud fireStore constants
    private final String USER_PROFILE = "UserProfile"; //collection type
    // field

    private TextInputLayout editEmailAddress;
    private FirebaseUser currentUser;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (setOrientation()) return;

        this.activity = this;

        setTheme();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        editEmailAddress = Objects.requireNonNull(binding.emailAddress);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.i(TAG, "User already logged-in. Starting Dashboard...");
            handleSuccessfulAuthentication();
        } else {
            Log.i(TAG, "User NOT logged-in. Starting Authentication...");
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mSignInClient = GoogleSignIn.getClient(this, gso);
            addListeners();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setOrientation();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private boolean setOrientation() {
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
        }
        return false;
    }

    private void setTheme() {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        // Theme change
        int themeVal = sharedPrefs.getInt(getString(R.string.shared_pref_theme), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (themeVal != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(themeVal);
        }
    }

    private void addListeners() {
        //Sign-in button
        binding.signInButton.setOnClickListener(view -> {
            Log.i(TAG, "Sign-in button clicked!");
            //Read email & password
            String emailId = Objects.requireNonNull(editEmailAddress.getEditText()).getText().toString();
            String password = Objects.requireNonNull(binding.password.getEditText()).getText().toString();

            //If email format is invalid
            if (!isEmailValid(emailId)) {
                editEmailAddress.setError(getString(R.string.empty_email_error));
                editEmailAddress.requestFocus();
                return;
            }

            //Email format is valid - Auth using firebase
            authUsingEmailPassword(emailId, password);
        });


        binding.emailSignUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });

        //Google-button
        binding.googleSignUpButton.setOnClickListener(view -> googleAuthLauncher.launch(mSignInClient.getSignInIntent()));

        binding.forgotPasswordButton.setOnClickListener(v -> {
            EditText editTextEmail = Objects.requireNonNull(binding.emailAddress).getEditText();
            String emailAddress = editTextEmail.getText().toString();

            if (!UtilFunctions.isEmailValid(emailAddress)) {
                editTextEmail.setError("Invalid email!");
            } else {
                editTextEmail.setError(null);

                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                binding.forgotPasswordButton.setText(R.string.reset_link_mailed);
                                binding.forgotPasswordButton.setClickable(false);
                                binding.forgotPasswordButton.setAlpha(0.5F);
                                binding.emailAddress.setError(null);
                                Log.d(TAG, "Password reset email sent!");
                            } else {
                                String msg = task.getException().getMessage();
                                binding.emailAddress.setError(msg);
                            }
                        });
            }
        });
    }

    private final ActivityResultLauncher<Intent> googleAuthLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);

                    // Authenticating Google user with firebase
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    mAuth.signInWithCredential(credential)
                            .addOnSuccessListener(this, authResult -> {
                                createMessengerUser();
                                handleSuccessfulAuthentication(SIGN_IN_USING_GOOGLE);
                            })
                            .addOnFailureListener(this, this::handleFailedAuthentication);
                } catch (ApiException apiException) {
                    handleFailedAuthentication(apiException);
                }
            }
    );

    private void authUsingEmailPassword(String email, String password) {
        //Authenticate given email and password on signIn button click
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // If email is not verified
                            if (!user.isEmailVerified()) {
                                editEmailAddress.setError(getString(R.string.email_not_verified));

                                Log.i(TAG, "Email is not verified. Sending verification email...");
                                user.sendEmailVerification();
                                return;
                            }
                            //Email is verified log-in;
                            Log.i(TAG, "Email is verified. Proceeding...");
                            handleSuccessfulAuthentication(SIGN_IN_USING_EMAIL);
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(TAG, "signInWithEmail:failure", task.getException());
                        showToast(this, Objects.requireNonNull(task.getException()).getMessage());
                    }
                })
                .addOnFailureListener(e -> showToast(this, e.getMessage()));
    }

    private void handleSuccessfulAuthentication(int signInMethod) {
        Log.i(TAG, "Handling successful authentication...");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Messenger.login(currentUser.getUid(),this);
        // Checking if current user exists in DB
        DocumentReference docRef = db.collection(USER_PROFILE).document(currentUser.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.i(TAG, "User already added. Not adding new one!");
                handleSuccessfulAuthentication();
            } else {
                Tasks.whenAllSuccess(addNewUserToFireStore(db, docRef))
                        .addOnSuccessListener(results -> {
                            // Sync Login methods only if new user added successfully
                            // Else can give error
                            syncLoginMethods(signInMethod);
                        });
            }
        });
    }

    private void createMessengerUser() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        User user = new User();
        user.setUid(firebaseUser.getUid());
        user.setName(firebaseUser.getDisplayName());

        CometChat.createUser(user, Constants.authKey, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d("createUser", user.toString());
            }

            @Override
            public void onError(CometChatException e) {
                Log.e("createUser", e.getMessage());
            }
        });
    }

    private List<Task<Void>> addNewUserToFireStore(FirebaseFirestore db, DocumentReference docRef) {
        String email = currentUser.getEmail();
        String name = currentUser.getDisplayName();
        String Uid = currentUser.getUid();
        UserProfile userProfile = new UserProfile(name, email, "", Uid);

        List<Task<Void>> tasks = new ArrayList<>();
        tasks.add(docRef.set(userProfile));
        tasks.add(
                // Adding UserActivity for new user
                db.collection(DBOperations.USER_ACTIVITY).document(Uid).set(new UserActivity(Uid))
                        .addOnSuccessListener(unused -> Log.i(TAG, "New User info added in profile...")));
        return tasks;
    }

    private void syncLoginMethods(int signInMethod) {
        //Check if auth providers are already linked
        MutableLiveData<Boolean> authLinkedLiveData = new MutableLiveData<>();
        getAuthLinkedStatus(currentUser, authLinkedLiveData);

        authLinkedLiveData.observe(this, isAuthLinked -> {
            // If live data has not been given a value
            if (isAuthLinked == null) return;

            if (isAuthLinked) {
                Log.i(TAG, "Auth already linked!");
                Toast.makeText(this, "isAuthLinked = true!", Toast.LENGTH_LONG).show();
                handleSuccessfulAuthentication();
                return;
            }

            //Both auth providers are not linked
            Log.i(TAG, "Auth NOT linked!");
            if (signInMethod == SIGN_IN_USING_EMAIL) {
                //ask for google sign-in
            } else if (signInMethod == SIGN_IN_USING_GOOGLE) {
                //ask for email sign-in
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                final Boolean LINKING = true;
                intent.putExtra(IS_LINKING, LINKING);
                Log.i(TAG, "Logged using Google. Asking for Email & Password to sync....");

                startActivity(intent);
                finish();
            }
        });


    }

    private void getAuthLinkedStatus(FirebaseUser currentUser, MutableLiveData<Boolean> authLinked) {
        //Fetching data from the fireStore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userProfileRef = db.collection(USER_PROFILE).document(currentUser.getUid());

        userProfileRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    UserProfile userProfile = document.toObject(UserProfile.class);

                    // DB operations are async functions, hence using live data to notify observer
                    authLinked.setValue(userProfile != null && userProfile.getAuthLinked());
                }
            }
        });
    }

    private void handleSuccessfulAuthentication() {

        //Initiate messenger
        Messenger.init(this);
        Messenger.login(mAuth.getCurrentUser().getUid(),this);
        //startMessenger(this);

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void startMessenger(LoginActivity loginActivity) {
        startActivity(new Intent(this, CometChatUI.class));
    }

    private void handleFailedAuthentication(Exception exception) {
        showToast(this, exception.getMessage());
    }

}