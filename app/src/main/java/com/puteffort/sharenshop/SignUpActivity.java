package com.puteffort.sharenshop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.puteffort.sharenshop.databinding.ActivitySignUpBinding;
import com.puteffort.sharenshop.utils.UtilFunctions;

import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;

    private String IS_LINKING = "IS_LINKING";

    //Cloud Firestore
    private final String USER_PROFILE = "UserProfile"; //collection type
    private final String isAuthLinkedField = "isAuthLinked"; // field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);Thread.setDefaultUncaughtExceptionHandler((t, e) -> UtilFunctions.showToast(this, "Exception: " + e.getMessage()));
        if (setOrientation()) return;

        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        binding.progressBar.setVisibility(View.INVISIBLE);

        boolean listenerSetAlready = false;
        Toast.makeText(this,"Sign up activity called",Toast.LENGTH_LONG).show();

        Intent intent = getIntent();

        if(intent!=null && intent.hasExtra(IS_LINKING)){
            boolean is_linking = false;
            //Log.i(TAG,intent.getAction());
            is_linking = intent.getBooleanExtra(IS_LINKING, is_linking);
            if(is_linking){
                Log.i(TAG,"SignUp Acitivity being used for linking auths. Setup layout...");
                setUpLinking();
                listenerSetAlready = true; // polymorphic functioning of singUp button
            }
        }

        addListeners(listenerSetAlready);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setOrientation();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private boolean setOrientation() {
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
        }
        return false;
    }

    private void setUpLinking() {
        //Assuming we have already signed-in using Google button
        EditText email = Objects.requireNonNull(binding.signUpEmail.getEditText());
        EditText name = Objects.requireNonNull(binding.userName.getEditText());
        Button btn_signUp = Objects.requireNonNull(binding.signUpButton);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        email.setText(firebaseUser.getEmail().toString());
        email.setFocusable(false);

        name.setText(firebaseUser.getDisplayName().toString());
        name.setFocusable(false);

        btn_signUp.setText("ADD PASSWORD");
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editPassword = Objects.requireNonNull(binding.signUpPassword.getEditText());
                EditText editConfirmPassword = Objects.requireNonNull(binding.signUpConfirmPassword.getEditText());

                if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())==false){
                    editConfirmPassword.setError("Passwords doesn't match!");
                    return;
                }

                //Password do match, so proceed with the linkage
                String email = firebaseUser.getEmail().toString();
                String password = editConfirmPassword.getText().toString();
                String userName = Objects.requireNonNull(binding.userName.getEditText()).getText().toString();

                registerUser(userName,email,password);
                linkWithGoogleButton(email,password);
                Log.i(TAG,"User email & password, synced with G-button...");
            }
        });
    }

    private void linkWithGoogleButton(String email, String password) {

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        Log.i(TAG,"Initiating linking Email & Password with G-button....");
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            updateAuthLinkageFirestore(user);
                            updateUI(user);
                        } else {
                            Log.i(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateAuthLinkageFirestore(FirebaseUser currentUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userProfileRef = db.collection(USER_PROFILE).document(currentUser.getUid());

        userProfileRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Map<String, Object> data = document.getData(); //fetching data
                    data.put(isAuthLinkedField,true); // changing value
                    userProfileRef.update(data); // updating
                    Log.i(TAG,"Linkage successful. Updating on the firestore, authLinkedField");
                }
            }
        });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void addListeners(boolean listenerSetAlready) {
        addInputFieldListeners();

        if (listenerSetAlready == false) {
            //Sign up with email button onClick handler
            binding.signUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Read input fields
                    String userName = Objects.requireNonNull(binding.userName.getEditText()).getText().toString();
                    String emailId = Objects.requireNonNull(binding.signUpEmail.getEditText()).getText().toString();
                    String password = Objects.requireNonNull(binding.signUpPassword.getEditText()).getText().toString();
                    String confirmPassword = Objects.requireNonNull(binding.signUpConfirmPassword.getEditText()).getText().toString();

                    boolean areFieldsValid = validateFields(userName, emailId, password, confirmPassword);
                    if (areFieldsValid) {
                        //User entered correct data - register the user now;
                        binding.progressBar.setVisibility(View.VISIBLE);
                        registerUser(userName, emailId, password);
                    }
                }

                private boolean validateFields(String userName, String emailId, String password, String confirmPassword) {

                    if (confirmPassword.compareTo(password) != 0) {
                        binding.signUpConfirmPassword.setError("Passwords doesn't match!");
                        //Toast.makeText(SignUpActivity.this,confirmPassword,Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        binding.signUpConfirmPassword.setError(null);
                    }

                    return binding.userName.getError() == null &&
                            binding.signUpEmail.getError() == null &&
                            binding.signUpPassword.getError() == null &&
                            binding.signUpConfirmPassword.getError() == null;
                }
            });
        }
    }

    private void addInputFieldListeners() {
        //Validating username on the go!
        Objects.requireNonNull(binding.userName.getEditText()).addTextChangedListener(new TextWatcher() {
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
                if (!s.toString().trim().matches(userNameRegex) || s.toString().trim().isEmpty()) {
                    binding.userName.setError("Name should not be empty or containing numbers or special chars");
                } else {
                    binding.userName.setError(null);//may create issue
                }
            }
        });

        //Email on the go!
        Objects.requireNonNull(binding.signUpEmail.getEditText()).addTextChangedListener(new TextWatcher() {
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
                if (!UtilFunctions.isEmailValid(s.toString().trim())) {
                    binding.signUpEmail.setError("Invalid email!");
                } else {
                    binding.signUpEmail.setError(null);
                }
            }
        });

        //Password
        Objects.requireNonNull(binding.signUpPassword.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Left blank intentionally...
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length()<6) {
                    binding.signUpPassword.setError("Password should be at-least 6 characters long!");
                } else {
                    binding.signUpPassword.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Left blank intentionally...
            }
        });
    }

    private void registerUser(String userName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information


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
                });
    }

    private void updateUserName(String userName) {
        FirebaseUser user = mAuth.getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
        //        .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User name updated.");

                        // Its time to say good bye to this activity
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        finish();
                    }
                });
    }
}

// TODO: 03-11-2021 ("Link G-button & Email Pass-login");