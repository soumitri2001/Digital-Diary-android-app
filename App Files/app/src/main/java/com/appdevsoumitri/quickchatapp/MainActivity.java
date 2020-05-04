package com.appdevsoumitri.quickchatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import utility.JournalAPI;

public class MainActivity extends AppCompatActivity {

    private Button btnStart;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActionBar actionBar=getSupportActionBar();
//        actionBar.setTitle("Dashboard");

        firebaseAuth=FirebaseAuth.getInstance();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser=firebaseAuth.getCurrentUser();
                for(int i=1;i<=Math.pow(10,5);i++)
                {
                    // dummy loop; delay;
                }
                if(currentUser!=null) {
                    currentUser=firebaseAuth.getCurrentUser();
                    assert currentUser != null;
                    final String currID=currentUser.getUid();
                    collectionReference
                            .whereEqualTo("userID",currID)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                    if(e!=null) {
                                        e.printStackTrace();
                                        return;
                                    }
                                    String name;
                                    assert queryDocumentSnapshots != null;
                                    if(!queryDocumentSnapshots.isEmpty()) {
                                        Toast.makeText(MainActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                                        for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots) {
                                            JournalAPI journalAPI=JournalAPI.getInstance();
                                            journalAPI.setUserID(snapshot.getString("userID"));
                                            journalAPI.setUsername(snapshot.getString("Username"));
                                            startActivity(new Intent(MainActivity.this, com.appdevsoumitri.quickchatapp.JournalListActivity.class));
                                            finish();
                                        }
                                    }
                                }
                            });
                } else {
                    Log.d("Status","new user, has to login/signup");
                }
            }
        };

        btnStart=findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(),"Network Unavailable :(", Toast.LENGTH_SHORT).show();
                    Log.d("Network Availability","false");
                } else {
                    startActivity(new Intent(MainActivity.this, com.appdevsoumitri.quickchatapp.LoginActivity.class));
                    // finish();
                }
            }
        });
    }
    private boolean isNetworkAvailable() {
        boolean isAvailable=false;
        ConnectivityManager manager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo network=manager.getActiveNetworkInfo();
        if(network!=null && network.isConnected()) isAvailable=true;
        Log.d("Network ",Boolean.toString(isAvailable));
        return isAvailable;
    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth!=null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    /*@Override
    protected void onStop() {
        super.onStop();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_recentPosts:

            case R.id.action_add:
                // add new post

            case R.id.action_signout:
                // sign out the user
                Toast.makeText(this, "You must login first !", Toast.LENGTH_SHORT).show();

                break;

            case R.id.action_devSite:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/soumitri2001")));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
