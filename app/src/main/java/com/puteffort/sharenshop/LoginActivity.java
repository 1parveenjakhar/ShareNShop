package com.puteffort.sharenshop;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.puteffort.sharenshop.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mSignInClient = GoogleSignIn.getClient(this, gso);
        addButtonActions();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }
    }

    private void addButtonActions() {
        TextInputLayout emailAddress = binding.emailAddress;
        Objects.requireNonNull(emailAddress.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                emailAddress.setError(s.toString().trim().isEmpty()
                        ? getString(R.string.empty_email_error) : null);
            }
        });


        binding.signInButton.setOnClickListener(view -> {
            String email = Objects.requireNonNull(binding.emailAddress.getEditText()).getText().toString();
            if (email.trim().isEmpty()) {
                emailAddress.setError(getString(R.string.empty_email_error));
                emailAddress.requestFocus();
                return;
            }
            // TODO("Authenticate user with provided email and password")
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
                    } catch (ApiException e) {
                        Log.w("GoogleSignIn", "Google sign in failed :(");
                    }
                }
        );
        binding.googleSignUpButton.setOnClickListener(view -> {
            // Launching Google Select Account Intent
            // Next Phase in 'launcher' lambda
            Intent signInIntent = mSignInClient.getSignInIntent();
            launcher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener(this, authResult -> {
                // Called on successful firebase authentication
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            })
            .addOnFailureListener(this, e ->
                Toast.makeText(this, "Authentication failed :(", Toast.LENGTH_SHORT).show());
    }
}