package com.appdevsoumitri.quickchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.*;

import ui.JournalRecyclerAdapter;
import utility.JournalAPI;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    // referring to the storage of firebase for media upload
    private CollectionReference collectionReference=db.collection("Journal");

    private ArrayList<JournalModel> journalModels=new ArrayList<>();
    private HashSet<JournalModel> journalSet=new HashSet<>();
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;
    private TextView noJournalEntry;

    public JournalListActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);
        journalModels.clear(); journalSet.clear();

        ActionBar actionBar=getSupportActionBar();
        actionBar.setElevation(20f);
        actionBar.setTitle("Recent Posts");

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        journalModels = new ArrayList<>();
        noJournalEntry=findViewById(R.id.list_no_thoughts);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        collectionReference.whereEqualTo("userID",JournalAPI.getInstance()
                .getUserID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()) {

                            for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots) {
                                JournalModel journal=snapshot.toObject(JournalModel.class);
                                /*journalModels.add(journal);*/
                                journalSet.add(journal);
                            }
                            // call recyclerView
                            for(JournalModel journal:journalSet) {
                                journalModels.add(journal);
                            }
                            Collections.sort(journalModels, new Comparator<JournalModel>() {
                                @Override
                                public int compare(JournalModel journalModel, JournalModel t1) {
                                    return t1.getTimeAdded().compareTo(journalModel.getTimeAdded());
                                }
                            });
                            journalSet.clear();
                            journalRecyclerAdapter=new JournalRecyclerAdapter(JournalListActivity.this,
                                    journalModels);
                            recyclerView.setAdapter(journalRecyclerAdapter);
                            journalRecyclerAdapter.notifyDataSetChanged();

                            Log.d("Status","adapter set up!");

                        } else {
                                noJournalEntry.setVisibility(View.VISIBLE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    public void shareFunction(String title, String thought)
    {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = "Title: "+title+"\nPost: "+thought+"\n";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Blog");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth!=null)
        {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_recentPosts:
                startActivity(getIntent());
                finish();
                break;
            case R.id.action_add:
                // add new post
                if(user!=null && firebaseAuth!=null) {
                    startActivity(new Intent(JournalListActivity.this, com.appdevsoumitri.quickchatapp.PostJournalActivity.class));
                    finish();
                }
                break;

            case R.id.action_signout:
                // sign out the user
                if(user!=null && firebaseAuth!=null) {
                    firebaseAuth.signOut();
                    Toast.makeText(this, "Successfully Logged out !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(JournalListActivity.this, com.appdevsoumitri.quickchatapp.MainActivity.class));
                    finish();
                }
                break;
            case R.id.action_devSite:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/soumitri2001")));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
