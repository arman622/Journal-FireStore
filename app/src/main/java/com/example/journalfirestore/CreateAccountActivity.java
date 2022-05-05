package com.example.journalfirestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.journalfirestore.Util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    public static final String TAG = "CreateAccountActivity";
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection(database)
    private FirebaseFirestore database = FirebaseFirestore.getInstance();

    //Database Reference
    private CollectionReference collectionReference = database.collection("Users");


    private ProgressBar progressBar;
    private Button createAccountBtn;
    private EditText usernameEditText, passwordEditText;
    private AutoCompleteTextView emailAutoComplete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.create_account_progress_bar);
        usernameEditText = findViewById(R.id.create_acc_username_edittext);
        emailAutoComplete = findViewById(R.id.create_acc_email_autocomplete);
        passwordEditText = findViewById(R.id.create_acc_password_edittext);
        createAccountBtn = findViewById(R.id.create_account_btn);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null){
                    //user is already logged in...

                } else {
                    //No user login in the app
                }
            }
        };

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailAutoComplete.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
                    createAccount(email,password, username);
                } else {
                    Toast.makeText(CreateAccountActivity.this,"Empty Fields Not Allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void createAccount(String email, String password, String username) {
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //When Sign In is successful
                    if(task.isSuccessful()){
                        //we take user to AddJournalActivity
                        currentUser = firebaseAuth.getCurrentUser();
                        assert currentUser != null;
                        String currentUserId = currentUser.getUid();

                        //Create a user Map so we can create a user data in the User collection
                        Map<String, Object>  userObject = new HashMap<>();
                        userObject.put(JournalApi.KEY_USER_ID, currentUserId);
                        userObject.put(JournalApi.KEY_USERNAME, username);

                        //save to firestore database
                        //Adds to Document
                        collectionReference.add(userObject)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                documentReference.get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.getResult().exists()){
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    String name = task.getResult().getString("username");

                                                    JournalApi journalApi = JournalApi.getInstance();

                                                    Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                    journalApi.setUserId(currentUserId);
                                                    journalApi.setUsername(name);
                                                    startActivity(intent);
                                                } else {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });
                    } else {
                        //Sign In Failed
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + e.toString());
                }
            });
        } else {

        }
    }
}
