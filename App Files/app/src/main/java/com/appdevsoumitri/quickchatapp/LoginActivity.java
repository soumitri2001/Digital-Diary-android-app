package com.appdevsoumitri.quickchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.storage.StorageReference;

import utility.JournalAPI;

public class LoginActivity extends AppCompatActivity
{
    private Button btnLogin, btnCreateAcc;
    private EditText etPassword, etEmail;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    // referring to the storage of firebase for media upload
    private CollectionReference collectionReference=db.collection("Users");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setElevation(10.0f);
        actionBar.setTitle("Login");

        progressBar=findViewById(R.id.login_progress);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth=FirebaseAuth.getInstance();

        etPassword=findViewById(R.id.password);
        etEmail=findViewById(R.id.email);

        btnLogin=findViewById(R.id.btnLogin);
        btnCreateAcc=findViewById(R.id.btnCreateAcc);

        btnCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,com.appdevsoumitri.quickchatapp.CreateAccAcctivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginUserPasswordUser(etEmail.getText().toString().trim(),
                        etPassword.getText().toString().trim());
            }
        });
    }

    private void loginUserPasswordUser(String email, String pass) {

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {

            // username+="@quickchatdomain.com";

            firebaseAuth.signInWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            user=firebaseAuth.getCurrentUser();
                            assert user!=null;
                            if(!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Invalid credentials, try again !", Toast.LENGTH_LONG).show();
                            } else {
                                final String currID = user.getUid();

                                collectionReference
                                        .whereEqualTo("userID", currID)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                                @Nullable FirebaseFirestoreException e) {

                                                if (e != null) {
                                                    e.printStackTrace();
                                                }
                                                assert queryDocumentSnapshots != null;
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Toast.makeText(LoginActivity.this, "Logged in !", Toast.LENGTH_SHORT).show();
                                                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                        JournalAPI journalAPI = JournalAPI.getInstance();
                                                        journalAPI.setUsername(snapshot.getString("Username"));
                                                        journalAPI.setUserID(snapshot.getString("userID"));

                                                        // go to the JournalListActivity
                                                        startActivity(new Intent(LoginActivity.this, JournalListActivity.class));
                                                        finish();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "Authentication error, make sure you have entered correct details :(", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, "Please enter all fields !", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
