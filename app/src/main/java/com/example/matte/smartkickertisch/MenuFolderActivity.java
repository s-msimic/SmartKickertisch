package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MenuFolderActivity extends Activity {

    private FirebaseAuth mAuth;

    public void onClickLogOut(View view){
        mAuth.signOut();
        Intent i = new Intent(MenuFolderActivity.this,WelcomeActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_folder);
    }
}
