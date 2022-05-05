package com.example.journalfirestore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.journalfirestore.Adapter.JournalListAdapter;
import com.example.journalfirestore.Model.Journal;
import com.example.journalfirestore.Util.JournalApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class JournalListActivity extends AppCompatActivity {

    public static final String TAG = "JournalListActivity";

    //Declaration: EX - int x, String myName
    //Initialization: Ex - int x = 7;, String myName = "Emi"
    private TextView noJournalEntry;

    private String currentUsername;
    private String currentUserId;

    private RecyclerView recyclerView;
    private JournalListAdapter adapter;
    private List<Journal> journalList;

    //Firebase Authorization
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //Firebase Firestore database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journal");

    //Firebase Storage
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        //Assignment Ex:
        //int x
        //x = 7
        //or
        //String myName = "Emi"
        //myName = "John"

        journalList = new ArrayList<>();
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (JournalApi.getInstance() != null){
            currentUsername = JournalApi.getInstance().getUsername();
            currentUserId = JournalApi.getInstance().getUserId();
            Log.d(TAG, "onCreate: " + currentUserId + ", " + currentUsername);
        }

        noJournalEntry = findViewById(R.id.no_journals_textview);
        recyclerView = findViewById(R.id.list_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getJournalList();
        Log.d(TAG, "onStart: " + journalList.size());
    }

    private void getJournalList() {
        journalList.clear();

        collectionReference.whereEqualTo(JournalApi.KEY_USER_ID, currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot journals: queryDocumentSnapshots){
                                Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal);
                            }
                            Log.d(TAG, "onEvent: "  + journalList.size());
                            //Invoke recycler
                            Collections.reverse(journalList);
                            adapter = new JournalListAdapter(JournalListActivity.this, journalList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            adapter.setOnItemClickListener(new JournalListAdapter.OnJournalClickListener() {
                                @Override
                                public void onJournalClick(Journal journal) {
                                    Intent intent = new Intent(JournalListActivity.this, PostJournalActivity.class);
                                    intent.putExtra("username", journal.getUserName());
                                    intent.putExtra("title", journal.getTitle());
                                    intent.putExtra("imageurl", journal.getImageUrl());
                                    intent.putExtra("thought", journal.getThought());
                                    intent.putExtra("userid", journal.getUserId());
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            noJournalEntry.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                        noJournalEntry.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add:
                //Take users to back to PostJournalActivity to add another post
                if(user != null && firebaseAuth != null){
                    Intent intent = new Intent(JournalListActivity.this, PostJournalActivity.class);
                    startActivity(intent);
                    finish();
                }

                break;
            case R.id.action_signout:
                //The user will be signed out back the LoginActivity
                if(user != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    Intent intent = new Intent(JournalListActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
