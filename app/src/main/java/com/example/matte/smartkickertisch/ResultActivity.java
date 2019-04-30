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
    private String autoID;
    private int editTextNumberResultRed;
    private int editTextNumberResultBlue;
    private String teamRedPlayerOne;
    private String teamRedPlayerTwo;
    private String teamBluePlayerThree;
    private String teamBluePlayerFour;
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

        String value = editTextResultRed.getText().toString();
        editTextNumberResultRed = Integer.parseInt(value);
        String value_scnd = editTextResultBlue.getText().toString();
        editTextNumberResultBlue = Integer.parseInt(value_scnd);

        if(editTextNumberResultBlue > 10 || editTextNumberResultBlue < 0 || editTextNumberResultRed > 10 || editTextNumberResultRed < 0
            || editTextNumberResultRed == 10 && editTextNumberResultBlue == 10 || editTextNumberResultBlue != 10 && editTextNumberResultRed != 10){
            Toast.makeText(ResultActivity.this, "Only values between 0 to 10 - one team must win with 10 points", Toast.LENGTH_SHORT).show();
            return;
        }
        //put results in each players firebase data
        ref.child("games").child(autoID).child("teamBlue").child("score").setValue(editTextNumberResultBlue);
        ref.child("games").child(autoID).child("teamRed").child("score").setValue(editTextNumberResultRed);
        if(teamRedPlayerOne != null) {
            ref.child("users").child(teamRedPlayerOne).child("finishedGames").child(autoID).setValue(autoID);
        }
        if(teamRedPlayerTwo != null) {
            ref.child("users").child(teamRedPlayerTwo).child("finishedGames").child(autoID).setValue(autoID);
        }
        if(teamBluePlayerThree != null) {
            ref.child("users").child(teamBluePlayerThree).child("finishedGames").child(autoID).setValue(autoID);
        }
        if(teamBluePlayerFour != null) {
            ref.child("users").child(teamBluePlayerFour).child("finishedGames").child(autoID).setValue(autoID);
        }
        
        //ref.child("lobby").child(lobbyPath).removeValue();

        Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        editTextResultBlue = findViewById(R.id.editTextResultBlue);
        editTextResultRed = findViewById(R.id.editTextResultRed);
        lobbyPath = this.getIntent().getExtras().getString("lobbyPath");
        autoID = (String)this.getIntent().getExtras().get("autoID");
        teamRedPlayerOne = (String)this.getIntent().getExtras().get("teamRedPlayerOne");
        teamRedPlayerTwo = (String)this.getIntent().getExtras().get("teamRedPlayerTwo");
        teamBluePlayerThree = (String)this.getIntent().getExtras().get("teamBluePlayerThree");
        teamBluePlayerFour = (String)this.getIntent().getExtras().get("teamBluePlayerFour");

        ref = FirebaseDatabase.getInstance().getReference();
        lobbyArray = lobbyPath.split("/");
        lobbyPath = lobbyArray[0];

    }
}
