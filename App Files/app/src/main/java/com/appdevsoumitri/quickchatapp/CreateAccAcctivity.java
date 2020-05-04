package com.appdevsoumitri.quickchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Objects;

import utility.JournalAPI;

public class CreateAccAcctivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    // Firestore connection
    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    private CollectionReference collectionReference=db.collection("Users");

    private Button btnCreateAcc;
    private EditText etUsername,etPassword,etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);

        ActionBar actionBar=getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("Create Account");


        firebaseAuth=FirebaseAuth.getInstance();


        btnCreateAcc=findViewById(R.id.btnCreateAcc_new);
        progressBar=findViewById(R.id.create_acc_progress);
        etEmail=findViewById(R.id.email_acc);
        etPassword=findViewById(R.id.password_acc);
        etUsername=findViewById(R.id.username_acc);

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser=firebaseAuth.getCurrentUser();
                if(currentUser!=null)
                {
                    // user already logged in

                }
                else {
                    // no user yet
                }
            }
        };
        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=etEmail.getText().toString().trim();
                String username=etUsername.getText().toString().trim();
                String password=etPassword.getText().toString().trim();

                if(!TextUtils.isEmpty(email)
                        && !TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(username))
                {
                    createUserEmailAccount(email,password,username);
                } else {
                    Toast.makeText(CreateAccAcctivity.this, "Empty Fields not allowed !", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void createUserEmailAccount(String email, String password, final String username)
    {
        if(!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(username))
        {
            progressBar.setVisibility(View.VISIBLE);

            // create a new account
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                // we take user to the AddJournalActivity
                                currentUser=firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                final String currID=currentUser.getUid();

                                // create user map for user collection in firebase
                                HashMap<String,String> userObj=new HashMap<>();
                                userObj.put("userID",currID);
                                userObj.put("Username",username);

                                // save to our FireStore DB
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                Log.d("Status:","Successfully saved !");
                                                                // allow this user to go to the AddJournalActivity
                                                                if(Objects.requireNonNull(task.getResult()).exists()) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    String name=task.getResult()
                                                                            .getString("Username");

                                                                    JournalAPI journalAPI = JournalAPI.getInstance();
                                                                    journalAPI.setUserID(currID);
                                                                    journalAPI.setUsername(name);

                                                                    Toast.makeText(CreateAccAcctivity.this, "successfully created account !", Toast.LENGTH_SHORT).show();

                                                                    Intent intent=new Intent(CreateAccAcctivity.this, PostJournalActivity.class);
                                                                    intent.putExtra("Username",name);
                                                                    intent.putExtra("userID",currID);
                                                                    startActivity(intent);
                                                                    finish();

                                                                } else {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });



                            } else {
                                // unsuccessful
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(CreateAccAcctivity.this, "An error occurred !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(CreateAccAcctivity.this, "Account already exists!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else
        {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
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

            case R.id.action_add:

            case R.id.action_signout:
                // sign out the user
                // add new post
                Toast.makeText(this, "You must create account first!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_devSite:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/soumitri2001")));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
