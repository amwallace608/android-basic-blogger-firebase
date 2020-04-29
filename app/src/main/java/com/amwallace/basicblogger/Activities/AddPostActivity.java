package com.amwallace.basicblogger.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amwallace.basicblogger.Model.BlogPost;
import com.amwallace.basicblogger.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    private EditText titleEdt, descriptionEdt;
    private ImageButton postImg;
    private Button submitBtn;

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;

    private ProgressBar postProgress;
    private Uri mImageUri;
    private static final int GALLERY_CODE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        //get Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();
        //Get currently logged in user
        mUser = mAuth.getCurrentUser();
        //Set up database
        mDatabase = FirebaseDatabase.getInstance();
        //get/create "Blog" database reference
        mDatabaseReference = mDatabase.getReference().child("Blog");
        //keep database synced
        mDatabaseReference.keepSynced(true);
        //get storage instance reference
        mStorage = FirebaseStorage.getInstance().getReference();

        //Setup indeterminate progress bar, set invisible
        postProgress = (ProgressBar) findViewById(R.id.postProgressBar);
        postProgress.setVisibility(View.INVISIBLE);

        //setup UI elements (edit texts, imagebutton, submit button)
        postImg = (ImageButton) findViewById(R.id.addImageBtn);
        titleEdt = (EditText) findViewById(R.id.addPostTitleEdt);
        descriptionEdt = (EditText) findViewById(R.id.addDescriptionEdt);
        submitBtn = (Button) findViewById(R.id.addPostBtn);

        //image button onclick listener
        postImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create gallery intent to get image content
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        //submit post button onclick listener
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    //override onActivityResult method to get image from gallery intent


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if requestCode and result code are ok, get image
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            //set image Uri to image retrieved from gallery
            mImageUri = data.getData();
            postImg.setImageURI(mImageUri);

        }

    }

    //post to database method
    private void startPosting() {
        //set progress 'bar' visible
        postProgress.setVisibility(View.VISIBLE);
        //get text for title and desc. + trim
        final String titleVal = titleEdt.getText().toString().trim();
        final String descVal = descriptionEdt.getText().toString().trim();

        //check if title, description, and image have input
        if(!TextUtils.isEmpty(titleVal) && !TextUtils.isEmpty(descVal) && mImageUri != null){
            //mImageUri.getLastPathSegment() = /image/myphoto.jpg
            final StorageReference filepath = mStorage
                    .child("Blog_Images")
                    .child(mImageUri.getLastPathSegment());
            //put file on storage
            UploadTask uploadTask = filepath.putFile(mImageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    //continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        //get download url is successfull and complete
                        Uri downloadUrl = task.getResult();
                        //make new hashmap for blog post data
                        Map<String, String> postData = new HashMap<>();
                        postData.put("title", titleVal);
                        postData.put("description",descVal);
                        postData.put("image",downloadUrl.toString());
                        postData.put("timestamp",String.valueOf(System.currentTimeMillis()));
                        postData.put("userId",mUser.getUid());

                        //write post to database, w/ unique instance id
                        DatabaseReference newPost = mDatabaseReference.push();
                        newPost.setValue(postData);

                    }
                }
            });


            Toast.makeText(AddPostActivity.this,
                    "Post Uploaded Successfully", Toast.LENGTH_SHORT).show();
            postProgress.setVisibility(View.INVISIBLE);
            //go back to post feed
            startActivity(new Intent(
                    AddPostActivity.this,PostFeedActivity.class));
            finish();

        } else {
            //no input in one or both fields, notify user, hide progress bar
            postProgress.setVisibility(View.INVISIBLE);
            Toast.makeText(AddPostActivity.this,
                    "Enter a title and description",Toast.LENGTH_SHORT).show();
        }
    }
}
