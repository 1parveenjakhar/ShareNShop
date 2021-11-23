package com.puteffort.sharenshop;

import static com.puteffort.sharenshop.utils.DBOperations.USER_PROFILE;
import static com.puteffort.sharenshop.utils.UtilFunctions.SERVER_URL;
import static com.puteffort.sharenshop.utils.UtilFunctions.SUCCESS_CODE;
import static com.puteffort.sharenshop.utils.UtilFunctions.client;
import static com.puteffort.sharenshop.utils.UtilFunctions.getRequest;
import static com.puteffort.sharenshop.utils.UtilFunctions.gson;
import static com.puteffort.sharenshop.utils.UtilFunctions.isEmailValid;
import static com.puteffort.sharenshop.utils.UtilFunctions.showToast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.databinding.ActivityLoginBinding;
import com.puteffort.sharenshop.models.UserProfile;
import com.puteffort.sharenshop.utils.UtilFunctions;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mSignInClient;
    private final int SIGN_IN_USING_EMAIL = 0;
    private final int SIGN_IN_USING_GOOGLE = 1;
    //private String TAG = this.getClass().getSimpleName();
    private final String TAG = "loginTrack";

    private final String IS_LINKING = "IS_LINKING";

    private TextInputLayout editEmailAddress;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);Thread.setDefaultUncaughtExceptionHandler((t, e) -> UtilFunctions.showToast(this, "Exception: " + e.getMessage()));
        if (setOrientation()) return;

        setTheme();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        editEmailAddress = Objects.requireNonNull(binding.emailAddress);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.i(TAG,"User already logged-in. Starting Dashboard...");
            handleSuccessfulAuthentication();
        } else {
            Log.i(TAG,"User NOT logged-in. Starting Authentication...");
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mSignInClient = GoogleSignIn.getClient(this, gso);

            db = FirebaseFirestore.getInstance();
            handler = new Handler(Looper.getMainLooper());

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
            Log.i(TAG,"Sign-in button clicked!");
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
                            .addOnSuccessListener(this, authResult -> handleSuccessfulAuthentication(SIGN_IN_USING_GOOGLE))
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
        Log.i(TAG,"Handling successful authentication...");

        // Checking if current user exists in DB
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection(USER_PROFILE).document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.i(TAG,"User already added. Not adding new one!");
                    handleSuccessfulAuthentication();
                } else {
                    addNewUserToFireStore(signInMethod);
                }
            });
    }

    private void addNewUserToFireStore(int signInMethod) {
        String email = currentUser.getEmail();
        String name = currentUser.getDisplayName();
        String Uid = currentUser.getUid();

        currentUser.getIdToken(false)
                .addOnSuccessListener(tokenResult -> {
                    UserProfile userProfile = new UserProfile(name, email, "", Uid);

                    client.newCall(getRequest(gson.toJson(userProfile), SERVER_URL + "createNewUser")).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            handler.post(() -> showToast(LoginActivity.this, "Failed to login !"));
                        }
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            if (response.code() != SUCCESS_CODE) {
                                handler.post(() -> showToast(LoginActivity.this, "Failed to login !"));
                                return;
                            }
                            handler.post(() -> syncLoginMethods(signInMethod));
                        }
                    });
                });
    }

    private void syncLoginMethods(int signInMethod) {
        //Check if auth providers are already linked
        MutableLiveData<Boolean> authLinkedLiveData = new MutableLiveData<>();
        getAuthLinkedStatus(currentUser, authLinkedLiveData);

        authLinkedLiveData.observe(this, isAuthLinked -> {
            // If live data has not been given a value
            if (isAuthLinked == null) return;

            if (isAuthLinked) {
                Log.i(TAG,"Auth already linked!");
                Toast.makeText(this,"isAuthLinked = true!", Toast.LENGTH_LONG).show();
                handleSuccessfulAuthentication();
                return;
            }

            //Both auth providers are not linked
            Log.i(TAG,"Auth NOT linked!");
            if (signInMethod == SIGN_IN_USING_EMAIL) {
                //ask for google sign-in
            } else if (signInMethod == SIGN_IN_USING_GOOGLE) {
                //ask for email sign-in
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                final Boolean LINKING = true;
                intent.putExtra(IS_LINKING,LINKING);
                Log.i(TAG,"Logged using Google. Asking for Email & Password to sync....");
                startActivity(intent);
                finish();
            }
        });


    }

    private void getAuthLinkedStatus(FirebaseUser currentUser, MutableLiveData<Boolean> authLinked) {
        //Fetching data from the fireStore
        db.collection(USER_PROFILE).document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    UserProfile userProfile = document.toObject(UserProfile.class);

                    // DB operations are async functions, hence using live data to notify observer
                    authLinked.setValue(userProfile != null && userProfile.getAuthLinked());
                }
            }
        });
    }

    private void handleSuccessfulAuthentication() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void handleFailedAuthentication(Exception exception) {
        showToast(this, exception.getMessage());
    }
}