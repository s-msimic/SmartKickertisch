package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    public void onTouchRegister(View view){
        Intent i = new Intent(WelcomeActivity.this, SignUpActivity.class);
        startActivity(i);
    }

    public void onClickLogin(View view){

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            Intent i = new Intent(WelcomeActivity.this, MenuFolderActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }
}
