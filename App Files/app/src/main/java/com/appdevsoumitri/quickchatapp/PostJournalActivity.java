package com.appdevsoumitri.quickchatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import utility.JournalAPI;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int GALLERY_CODE=69;
    private Uri imageUri;

    private Button btnSubmitPost;
    private ProgressBar progressBar;
    private ImageView ivPostImage, ivCameraButton;
    private EditText etPostTitle, etPostThought;
    private TextView currUserTextView;

    private String currID;
    private String currUser;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();

    // referring to the storage of firebase for media upload
    private StorageReference storageReference;
    private CollectionReference collectionReference=db.collection("Journal");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        /**
        Bundle bundle=getIntent().getExtras();

        if(bundle!=null) {
            String username=bundle.getString("Username");
            String userID=bundle.getString("userID");
        } */

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("New Post");

        storageReference= FirebaseStorage.getInstance().getReference();

        firebaseAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.post_progressBar);
        etPostTitle=findViewById(R.id.etPostTitle);
        etPostThought=findViewById(R.id.etPostThought);
        currUserTextView=findViewById(R.id.tvPostUsername);

        ivPostImage=findViewById(R.id.post_imageView);
        btnSubmitPost=findViewById(R.id.btnSubmitPost);
        btnSubmitPost.setOnClickListener(this);
        ivCameraButton=findViewById(R.id.ivCameraButton);
        ivCameraButton.setOnClickListener(this);

        progressBar.setVisibility(View.INVISIBLE);
        ivCameraButton.animate().alpha(0.5f);

        if(JournalAPI.getInstance()!=null) {
            currID=JournalAPI.getInstance().getUserID();
            currUser=JournalAPI.getInstance().getUsername();

            currUserTextView.setText(currUser);
        }

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user=firebaseAuth.getCurrentUser();
                if(user!=null)
                {

                }
                else {

                }
            }
        };

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnSubmitPost:
                // update journal
                if(!isNetworkAvailable()) {
                    Toast.makeText(getApplicationContext(),"Network Unavailable :(", Toast.LENGTH_SHORT).show();
                    Log.d("Network Availability","false");

                } else {
                    saveJournal();
                }
                break;

            case R.id.ivCameraButton:
                // get image from gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;
        }
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

    private void saveJournal() {
        final String title=etPostTitle.getText().toString().trim();
        final String thought=etPostThought.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        if(imageUri==null) {
            ivPostImage.setImageResource(R.drawable.wallpaper);
            imageUri=Uri.parse("android.resource://com.appdevsoumitri.quickchatapp/"+R.drawable.wallpaper);
            ivPostImage.setImageURI(null);
            ivPostImage.setImageURI(imageUri);
        }

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri!=null)
        {
            // ivPostImage.animate().alpha(0f);

            // creating a directory and path for storing images (MOST IMPORTANT THING !)
            final StorageReference filepath=storageReference
                        .child("journal_images")
                        .child("my_image_"+ Timestamp.now().getSeconds());

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                // to get image url as it is easier to fetch
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl=uri.toString();
                                    // create a Journal Object
                                    JournalModel journal=new JournalModel();
                                    journal.setTitle(title);
                                    journal.setThought(thought);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserID(currID);
                                    journal.setUsername(currUser);

                                    // invoke the collectionReference and save a Journal instance
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d("status:","ultimate success!");
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(PostJournalActivity.this, "Some problem occurred !", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });


        } else {
                progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE & resultCode==RESULT_OK) {
           if(data!=null) {
               imageUri = data.getData();
                ivPostImage.setImageURI(imageUri);
           }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        user=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
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
                startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                break;
            case R.id.action_add:
                // add new post
                if(user!=null && firebaseAuth!=null) {
                    finish();
                    startActivity(getIntent());
                }
                break;

            case R.id.action_signout:
                // sign out the user
                if(user!=null && firebaseAuth!=null) {
                    firebaseAuth.signOut();
                    Toast.makeText(this, "Successfully Logged out !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PostJournalActivity.this, com.appdevsoumitri.quickchatapp.MainActivity.class));
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
