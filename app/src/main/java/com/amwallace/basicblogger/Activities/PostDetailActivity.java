package com.amwallace.basicblogger.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.amwallace.basicblogger.Model.BlogPost;
import com.amwallace.basicblogger.Model.User;
import com.amwallace.basicblogger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class PostDetailActivity extends AppCompatActivity {
    private ImageView postImage, userImage;
    private TextView postTitle, postDescription, postDate, postAuthor;
    private BlogPost blogPost;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //Firebase setup
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        //get ref to database
        mDatabaseReference = mDatabase.getReference();

        //setup textviews & Imageviews
        postTitle = (TextView) findViewById(R.id.postDetailTitle);
        postDescription = (TextView) findViewById(R.id.postDetailTxt);
        postDate = (TextView) findViewById(R.id.postDetailTime);
        postAuthor = (TextView) findViewById(R.id.detailPostAuthorTxt);

        postImage = (ImageView) findViewById(R.id.postDetailImg);
        userImage = (ImageView) findViewById(R.id.detailAuthorImg);

        //get blogpost from intent extra
        blogPost = (BlogPost) getIntent().getSerializableExtra("blogpost");

        //display post data
        populatePostDetails(blogPost);


    }

    private void populatePostDetails(BlogPost blogPost) {
        //display post details
        postTitle.setText(blogPost.getTitle());
        postDescription.setText(blogPost.getDescription());
        //display post image
        Picasso.with(PostDetailActivity.this).load(blogPost.image).into(postImage);

        DateFormat dateFormat = DateFormat.getDateInstance();
        postDate.setText(dateFormat
                .format(new Date(Long.valueOf(blogPost.getTimestamp())).getTime()));

        Log.d("USER ID FROM BLOGPOST: ", blogPost.getUserId());
        //get author user info from database
        mDatabaseReference.child("Users").child(blogPost.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get user object from database
                        User author = dataSnapshot.getValue(User.class);
                        String fullName = author.getFirstname() + ' ' + author.getLastname();
                        //display author name
                        postAuthor.setText(fullName);
                        //display author profile picture
                        Picasso.with(PostDetailActivity.this)
                                .load(author.getImage()).into(userImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_back){
            //back button pressed, go to main activity
            startActivity(new Intent(
                    PostDetailActivity.this, PostFeedActivity.class));
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
