package com.puteffort.sharenshop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.puteffort.sharenshop.utils.StaticData;
import com.puteffort.sharenshop.databinding.ActivityLoginBinding;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeAndLanguage();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            handleSuccessfulAuthentication();
        } else {
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mSignInClient = GoogleSignIn.getClient(this, gso);
            addListeners();
        }

    }

    private void setThemeAndLanguage() {
        SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        // Theme change
        int themeVal = sharedPrefs.getInt(getString(R.string.theme), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (themeVal != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(themeVal);
        }

        // Language change
        String language = sharedPrefs.getString(getString(R.string.language), "en");
        StaticData.changeLocale(language, this);
    }

    private void addListeners() {

        TextInputLayout editEmailAddress = Objects.requireNonNull(binding.emailAddress);
        //TextInputLayout editPassword = Objects.requireNonNull(binding.password);

        //Sign-in button
        binding.signInButton.setOnClickListener(view -> {
            //Read email & password
            String emailId = Objects.requireNonNull(binding.emailAddress.getEditText()).getText().toString();
            String password = Objects.requireNonNull(binding.password.getEditText()).getText().toString();

            //Validate emailId using regex
            boolean isValidEmail = emailValidator(emailId);

            //If email format is invalid
            if(isValidEmail == false){
                editEmailAddress.setError(getString(R.string.empty_email_error));
                editEmailAddress.requestFocus();
                return;
            }

            // TODO("Authenticate user with provided email and password")
            //Email format is valid - Auth using firebase
            authUsingEmailPassword(emailId, password);
        });


        binding.emailSignUpButton.setOnClickListener(view ->
                startActivity(new Intent(this, SignUpActivity.class)));


        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        // Authenticating Google user with firebase
                        // Next Phase in 'firebaseAuthWithGoogle' method
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException apiException) {
                        handleFailedAuthentication(apiException);
                    }
                }
        );

        //Google-button
        binding.googleSignUpButton.setOnClickListener(view -> {
            // Launching Google Select Account Intent
            // Next Phase in 'launcher' lambda
            Intent signInIntent = mSignInClient.getSignInIntent();
            Toast.makeText(this,"Login Successful", Toast.LENGTH_SHORT).show();
            launcher.launch(signInIntent);
        });

    }

    private void authUsingEmailPassword(String email, String password) {
        //Authenticate given email and password on signIn button click
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        //FirebaseAuth is a singleton class, you can get instance of firebase auth anywhere from the app.
        //Therefore, function parameter can be ignored

        if(user!=null){
            // TODO: 02-11-2021 ("Open Dashboard Activity")
            Toast.makeText(LoginActivity.this, "Authentication Successful. Welcome " +
                            user.getEmail() +"!"
                    ,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean emailValidator(String emailId) {
        //Validating email ID using Regex
        final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailId);
        return matcher.find();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener(this, authResult -> handleSuccessfulAuthentication())
            .addOnFailureListener(this, this::handleFailedAuthentication);
    }

    private void handleSuccessfulAuthentication() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void handleFailedAuthentication(Exception exception) {
        Toast.makeText(this, getString(R.string.failed_auth_error), Toast.LENGTH_SHORT).show();
    }
}