package com.amwallace.basicblogger.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.amwallace.basicblogger.Data.BlogRecyclerAdapter;
import com.amwallace.basicblogger.Model.BlogPost;
import com.amwallace.basicblogger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostFeedActivity extends AppCompatActivity {
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private List<BlogPost> blogPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_feed);

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

        //instantiate blog post list
        blogPosts = new ArrayList<>();
        //setup recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    //onStart method, setup everything before visible
    @Override
    protected void onStart() {
        super.onStart();
        blogPosts.clear();
        //get blog posts
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //retrieve items from DB and put into bloglist
                BlogPost blogPost = dataSnapshot.getValue(BlogPost.class);
                blogPosts.add(blogPost);
                //reverse blogposts list order to have newest post first
                Collections.reverse(blogPosts);
                //add post to recyclerview
                blogRecyclerAdapter = new BlogRecyclerAdapter(
                        PostFeedActivity.this,blogPosts);
                recyclerView.setAdapter(blogRecyclerAdapter);
                blogRecyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //menu option selected method
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //add button selected
            case R.id.action_add:
                //if user is logged in & authorized
                if(mUser != null && mAuth != null){
                    //go to add post activity
                    startActivity(
                            new Intent(PostFeedActivity.this, AddPostActivity.class));
                    finish();
                }
                break;

            //signout button selected
            case R.id.action_signOut:
                if(mUser != null && mAuth != null) {
                    //sign out and return to login/main activity
                    mAuth.signOut();
                    startActivity(
                            new Intent(PostFeedActivity.this,MainActivity.class));
                    finish();
                }
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
