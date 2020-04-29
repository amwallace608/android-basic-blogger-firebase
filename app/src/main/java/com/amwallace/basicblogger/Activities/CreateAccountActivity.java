package com.amwallace.basicblogger.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amwallace.basicblogger.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class CreateAccountActivity extends AppCompatActivity {
    private EditText firstNameEdt, lastNameEdt, emailEdt, passwordEdt;
    private Button createSubmitBtn;
    private ImageButton createUserImg;
    private FirebaseAuth mAuth;
    private FirebaseAuth mUser;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private StorageReference mStorage;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private String userImgUrl = "none";
    private final static int GALLERY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //Firebase DB setup
        mDatabase = FirebaseDatabase.getInstance();
        //Users table/child
        mDatabaseReference = mDatabase.getReference().child("Users");
        //Firebase Auth setup
        mAuth = FirebaseAuth.getInstance();
        //Firebase Storage setup
        mStorage = FirebaseStorage.getInstance().getReference();

        //UI setup - progressbar, editTexts, Buttons
        mProgressBar = (ProgressBar) findViewById(R.id.createProgressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        firstNameEdt = (EditText) findViewById(R.id.createFirstNameEdt);
        lastNameEdt = (EditText) findViewById(R.id.createLastNameEdt);
        emailEdt = (EditText) findViewById(R.id.createAccountEmailEdt);
        passwordEdt = (EditText) findViewById(R.id.createPasswordEdt);

        createUserImg = (ImageButton) findViewById(R.id.createUserImg);
        createSubmitBtn = (Button) findViewById(R.id.createSubmitBtn);

        //set user image/profile picture
        createUserImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fetch image
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

        //create account
        createSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });


    }
    //On activity result - when finished choosing user image from gallery


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if requestCode and result code are ok, get image
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            //allow user to crop profile picture
            //use the selected image in cropping activity
            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }

        //result from crop image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                //set imagebutton to display cropped image
                createUserImg.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void createNewAccount() {
        //set progressbar visible
        mProgressBar.setVisibility(View.VISIBLE);
        final String name = firstNameEdt.getText().toString().trim();
        final String lastname = lastNameEdt.getText().toString().trim();
        final String email = emailEdt.getText().toString().trim();
        String password = passwordEdt.getText().toString().trim();
        //check if fields are empty
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(lastname)
                && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            //fields not empty, create user account
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    if(authResult != null){
                        //new storage directory for user images/profile pictures
                        final StorageReference filepath = mStorage
                                .child("User_Images")
                                .child(mImageUri.getLastPathSegment());
                        //put file in Firebase storage
                        UploadTask uploadTask = filepath.putFile(mImageUri);
                        Task<Uri> urlTask = uploadTask.continueWithTask(
                                new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task)
                                    throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                //continue with the task to get the download URL
                                return filepath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    //get download url is successful and complete
                                    Uri downloadUrl = task.getResult();
                                    userImgUrl = downloadUrl.toString();
                                    Log.d("User Image URL:",userImgUrl);

                                    String userid = mAuth.getUid();
                                    //create user child for userId
                                    DatabaseReference currentUserDb = mDatabaseReference.child(userid);
                                    currentUserDb.child("firstname").setValue(name);
                                    currentUserDb.child("lastname").setValue(lastname);
                                    currentUserDb.child("image").setValue(downloadUrl.toString());

                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(CreateAccountActivity.this,
                                            "Account Created", Toast.LENGTH_SHORT).show();
                                    //go to post feed activity
                                    Intent intent = new Intent(
                                            CreateAccountActivity.this,PostFeedActivity.class);
                                    //flag to allow this activity to be on top of the stack of the activites
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    //finish();
                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    //account creation failed
                    mProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(CreateAccountActivity.this,
                            "Account Creation Failed", Toast.LENGTH_SHORT).show();
                    //send back to main activity
                    startActivity(new Intent(
                            CreateAccountActivity.this,MainActivity.class));
                    finish();
                }
            });
        } else {
            //set progressbar invisible again
            mProgressBar.setVisibility(View.INVISIBLE);
            //instruct to fill all forms
            Toast.makeText(CreateAccountActivity.this,
                    "Enter all required information", Toast.LENGTH_SHORT).show();
        }

    }
}
