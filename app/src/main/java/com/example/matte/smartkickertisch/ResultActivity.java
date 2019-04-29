package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {
    DatabaseReference ref;
    private EditText editTextResultRed;
    private EditText editTextResultBlue;
    private String lobbyPath;
    private String [] lobbyArray = new String[3];

    @Override
    public void onDestroy() {
        ref.child("lobby").child(lobbyPath).removeValue();
        super.onDestroy();
    }


    public void onClickCommitResult(View view){

        if(editTextResultRed.getText().toString().isEmpty() || editTextResultBlue.getText().toString().isEmpty()){
            Toast.makeText(ResultActivity.this, "Make sure to edit results",Toast.LENGTH_SHORT).show();
            return;
        }
        //put results in each players firebase data

        ref.child("lobby").child(lobbyPath).removeValue();

        Intent i = new Intent(ResultActivity.this, MenuFolderActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        editTextResultBlue = findViewById(R.id.editTextResultBlue);
        editTextResultRed = findViewById(R.id.editTextResultRed);
        lobbyPath = this.getIntent().getExtras().getString("lobbyPath");

        ref = FirebaseDatabase.getInstance().getReference();
        lobbyArray = lobbyPath.split("/");
        lobbyPath = lobbyArray[0];
    }
}
