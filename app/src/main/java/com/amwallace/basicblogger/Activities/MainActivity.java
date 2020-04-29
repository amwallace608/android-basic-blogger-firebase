package com.amwallace.basicblogger.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amwallace.basicblogger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText emailEdt, passwordEdt;
    private Button signInBtn, createAccountBtn;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mUser;
    //Firebase Database


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //Buttons
        signInBtn = (Button) findViewById(R.id.signInBtn);
        createAccountBtn = (Button) findViewById(R.id.createAccountBtn);
        //Edit text fields
        emailEdt = (EditText) findViewById(R.id.emailEdt);
        passwordEdt = (EditText) findViewById(R.id.passwordEdt);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //get user if signed in
                mUser = firebaseAuth.getCurrentUser();
                if(mUser != null){
                    Toast.makeText(MainActivity.this,
                            "Signed in as " + mUser.getEmail(), Toast.LENGTH_SHORT).show();
                    //go to post feed activity when signed in
                    startActivity(
                            new Intent(MainActivity.this,PostFeedActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Not signed in", Toast.LENGTH_SHORT).show();
                }
            }
        };
        //sign in button click listener
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if email and password fields have input, try to authenticate/log in
                if(!TextUtils.isEmpty(emailEdt.getText().toString())
                        && !TextUtils.isEmpty(passwordEdt.getText().toString())){
                    //get email and password 
                    String email = emailEdt.getText().toString();
                    String password = passwordEdt.getText().toString();
                    //login user method
                    loginUser(email, password);

                } else {
                    Toast.makeText(MainActivity.this,
                            "Please enter your email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //create account click listener
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go to create account activity
                startActivity(new Intent(
                        MainActivity.this,CreateAccountActivity.class));
                finish();
            }
        });

    }
//sign in existing user with email and password
    private void loginUser(String email, String password) {
        //attempt to log in
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //login success
                    Toast.makeText(MainActivity.this, "Sign in success", Toast.LENGTH_SHORT).show();
                    //go to post board/feed activity
                    startActivity(new Intent(MainActivity.this, PostFeedActivity.class));
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Toolbar/Menu methods
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //sign out button selected
        if(item.getItemId() == R.id.action_signOut){
            //sign out with firebase
            mAuth.signOut();
        } else {    //add button selected

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //wire up auth on activity start
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //remove AuthStateListener on stop of app if it is not null
    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
