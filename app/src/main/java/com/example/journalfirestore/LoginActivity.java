package com.example.journalfirestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.journalfirestore.Model.Journal;
import com.example.journalfirestore.Util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {


    public static final String TAG = "LoginActivity";

    private ProgressBar progressBar;
    private AutoCompleteTextView emailAutoComplete;
    private EditText passwordEditText;
    private Button loginBtn, createAccountBtn;

    private FirebaseAuth firebaseAuth;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress_bar);
        emailAutoComplete = findViewById(R.id.login_email_autocomplete);
        passwordEditText = findViewById(R.id.login_password_edittext);
        loginBtn = findViewById(R.id.login_btn);
        createAccountBtn = findViewById(R.id.login_create_account_btn);

        loginBtn.setOnClickListener(this);
        createAccountBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                String email = emailAutoComplete.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                loginToUser(email, password);
                break;
            case R.id.login_create_account_btn:
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void loginToUser(String email, String password) {

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //TODO: Get the userId from Firebase Auth
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String currentUserId = user.getUid();
                            collectionReference.whereEqualTo(JournalApi.KEY_USER_ID,currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if(error != null){
                                                return;
                                            }
                                            if (!value.isEmpty() && value != null){
                                                for(QueryDocumentSnapshot snapshot: value){
                                                    progressBar.setVisibility(View.GONE);
                                                    JournalApi journalAPi = JournalApi.getInstance();
                                                    String username = snapshot.getString(JournalApi.KEY_USERNAME);
                                                    String userId = snapshot.getString(JournalApi.KEY_USER_ID);
                                                    Log.d(TAG, "onEvent: " + "username: " + username + ", " + "UserId: " + userId);

                                                    //Go to ListActivity
                                                    Intent intent = new Intent(LoginActivity.this, JournalListActivity.class);
                                                    journalAPi.setUsername(username);
                                                    journalAPi.setUserId(userId);
                                                    startActivity(intent);
                                                }
                                            }
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_LONG).show();
        }
    }
}