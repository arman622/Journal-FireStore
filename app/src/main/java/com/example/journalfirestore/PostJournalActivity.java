package com.example.journalfirestore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.journalfirestore.Model.Journal;
import com.example.journalfirestore.Util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostJournalActivity";

    private static final int GALLARY_CODE = 1;
    private TextView usernameTextView, dateTextView;
    private ImageView imageView, addPhotoBtn;
    private EditText titleEditText, thoughtEditText;
    private ProgressBar progressBar;
    private Button saveBtn;

    private String currentUserId;
    private String currentUserName;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private final CollectionReference collectionReference = db.collection("Journal");
    private Uri imageUri;

    private Bundle extra = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        firebaseAuth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.post_imageview);
        usernameTextView = findViewById(R.id.post_username_textview);
        dateTextView = findViewById(R.id.post_date_textview);
        titleEditText = findViewById(R.id.post_title_edittext);
        thoughtEditText = findViewById(R.id.post_thought_edittext);
        progressBar = findViewById(R.id.post_progressbar);
        addPhotoBtn = findViewById(R.id.camera_imageview_btn);
        addPhotoBtn.setOnClickListener(this);
        saveBtn = findViewById(R.id.post_save_btn);
        saveBtn.setOnClickListener(this);

        if (JournalApi.getInstance() != null) {
            currentUserName = JournalApi.getInstance().getUsername();
            currentUserId = JournalApi.getInstance().getUserId();
            usernameTextView.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };

        extra = getIntent().getExtras();

        if(extra != null){
            Log.d(TAG, "onCreate: " + "extra is not null");
            titleEditText.setText(extra.getString("title"));
            thoughtEditText.setText(extra.getString("thought"));

            Picasso.get()
                    .load(extra.getString("imageurl"))
                    .placeholder(android.R.drawable.stat_sys_download)
                    .error(android.R.drawable.stat_notify_error)
                    .into(imageView);

            imageUri = Uri.parse(extra.getString("imageurl"));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_imageview_btn:
                //get Image from phone gallary
                Intent gallaryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, GALLARY_CODE);
//                addPhotoBtn.setVisibility(View.INVISIBLE);
                break;
            case R.id.post_save_btn:
                //save journal data to firestore database
                saveJournalData();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                //show image
                imageView.setImageURI(imageUri);
            }
        }
    }

    private void saveJournalData() {
        String title = titleEditText.getText().toString().trim();
        String thought = thoughtEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null && extra == null) {
            progressBar.setVisibility(View.VISIBLE);
            addImageDataToFirestoreAndStorage(title, thought);
        } else if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null && extra != null){
            progressBar.setVisibility(View.VISIBLE);
            updateDataToFirestore(title, thought);
        } else {
            Toast.makeText(PostJournalActivity.this, "Empty Fields", Toast.LENGTH_SHORT);
        }
    }

    private void updateDataToFirestore(String title, String thought) {

        collectionReference.whereEqualTo("title", extra.getString("title"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document: task.getResult()){
                        String documentId = document.getId();

                        Map<String, Object> data = new HashMap<>();
                        data.put("title", title);
                        data.put("thought", thought);
                        data.put("timestamp", new Timestamp(new Date()));
                        data.put("userName", extra.getString("username"));
                        data.put("userId", extra.getString("userid"));
                        data.put("imageUrl", imageUri.toString());

                        collectionReference.document(documentId)
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onSuccess: " + "Update Successful");
                                        Intent intent = new Intent(PostJournalActivity.this, JournalListActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }). addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: " + e.getMessage());
                            }
                        });
                    }
                }
            }
        });

    }

    private void addImageDataToFirestoreAndStorage(String title, String thought) {
        //TODO: upload image from phone to Firebase Storage
        //.../journal_images/our_image.jpeg
        StorageReference filepath = storageReference
                .child("journal_images")
                .child("my_image_" + Timestamp.now().getSeconds()); //my_image_4802374

        Log.d(TAG, "saveJournalData: " + filepath.toString());

        filepath.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "upload: " + "Image Successful");
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                //TODO: Create Journal Object - Model
                                Journal journal = new Journal();
                                journal.setTitle(title);
                                journal.setThought(thought);
                                journal.setImageUrl(imageUrl);
                                journal.setTimestamp(new Timestamp(new Date()));
                                journal.setUserName(currentUserName);
                                journal.setUserId(currentUserId);

                                //TODO: invoke out collectionReference
                                collectionReference.add(journal)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                Intent intent = new Intent(PostJournalActivity.this, JournalListActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onFailure: " + e.getMessage());
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
