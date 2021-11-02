package com.puteffort.sharenshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import com.puteffort.sharenshop.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);

        addListeners();
    }

    private void addListeners() {
        addInputFieldListeners();
        //Sign up with email button onClick handler
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Read input fields
                String userName = Objects.requireNonNull(binding.userName.getEditText().getText().toString());
                String emailId = Objects.requireNonNull(binding.signUpEmail.getEditText().getText().toString());
                String password = Objects.requireNonNull(binding.signUpPassword.getEditText().getText().toString());
                String confirmPassword = Objects.requireNonNull(binding.signUpConfirmPassword.getEditText().getText().toString());

                boolean areValidFields = validateFields(userName,emailId,password,confirmPassword);
                if(areValidFields == false){
                    return;
                }

                //User entered correct data - register the user now;
                FirebaseUser user = registerUser(userName, emailId, password);
                updateUI(user);
            }

            private boolean validateFields(String userName, String emailId, String password, String confirmPassword) {

                if(confirmPassword.compareTo(password)!=0){
                    binding.signUpConfirmPassword.setError("Passwords doesn't match!");
                    //Toast.makeText(SignUpActivity.this,confirmPassword,Toast.LENGTH_LONG).show();
                    return false;
                }else{
                    binding.signUpConfirmPassword.setError(null);
                }

                return binding.userName.getError() == null &&
                        binding.signUpEmail.getError() == null &&
                        binding.signUpPassword.getError() == null &&
                        binding.signUpConfirmPassword.getError() == null;
            }
        });
    }

    private void addInputFieldListeners() {
        //Validating username on the go!
        binding.userName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Left blank intentionally...
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Left blank intentionally...
            }

            @Override
            public void afterTextChanged(Editable s) {
                String userNameRegex = "[a-zA-Z\\s]+";
                if(s.toString().trim().matches(userNameRegex)==false || s.toString().trim().isEmpty()){
                    binding.userName.setError("Name should not be empty or containing numbers or special chars");
                }else{
                    binding.userName.setError(null);//may create issue
                }
            }
        });

        //Email on the go!
        binding.signUpEmail.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Left blank intentionally...
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Left blank intentionally...
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(LoginActivity.emailValidator(s.toString().trim())==false){
                    binding.signUpEmail.setError("Invalid email!");
                }else{
                    binding.signUpEmail.setError(null);
                }
            }
        });

        //Password
        binding.signUpPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Left blank intentionally...
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length()<6){
                    binding.signUpPassword.setError("Password should be at-least 6 characters long!");
                }else{
                    binding.signUpPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Left blank intentionally...
            }
        });
    }

    private FirebaseUser registerUser(String userName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");

                            //FirebaseUser user = mAuth.getCurrentUser();
                            //User-registered....updating firebase user-name
                            Toast.makeText(SignUpActivity.this, "Authentication successful!.",
                                    Toast.LENGTH_LONG).show();
                            updateUserName(userName);

                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        FirebaseUser user = mAuth.getCurrentUser();
        return user;
    }

    private void updateUserName(String userName) {
        FirebaseUser user = mAuth.getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
        //        .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User name updated.");
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        // TODO: 03-11-2021 ("Successful account creation toast not showing up?!"); 
        Toast.makeText(this,"Account created. Please login," + user.getDisplayName().toString(),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}

// TODO: 03-11-2021 ("Link G-button & Email Pass-login");