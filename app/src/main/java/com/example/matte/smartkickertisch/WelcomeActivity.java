package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    /**
     * Opens the sign up screen.
     * @param view button to open the sign up screen
     */
    public void onClickSignUp(View view){
        Intent i = new Intent(WelcomeActivity.this, SignUpActivity.class);
        startActivity(i);
    }

    /**
     * Opens the log in scree.
     * @param view button to open the log in screen
     */
    public void onClickLogin(View view){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            Intent i = new Intent(WelcomeActivity.this, LeaderboardActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }
}
