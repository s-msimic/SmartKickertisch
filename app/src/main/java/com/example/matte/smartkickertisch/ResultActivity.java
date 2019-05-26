package com.example.matte.smartkickertisch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.Transaction.Result;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.*;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {
    DatabaseReference ref;
    FirebaseDatabase database;
    private EditText editTextResultRed;
    private EditText editTextResultBlue;
    private String lobbyPath;
    private String autoID;
    private String fullLobyPath;
    private int editTextNumberResultRed;
    private int editTextNumberResultBlue;
    private String teamRedPlayerOne;
    private String teamRedPlayerTwo;
    private String teamBluePlayerThree;
    private String teamBluePlayerFour;
    private String [] lobbyArray = new String[3];
    public boolean hostActivity = true;
    private static final String TAG = "ResultActivity";
    private boolean onStopCalled = true;
    private long unixTime;


    public boolean getHostActivity(){
        return hostActivity;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please enter a result before leaving", Toast.LENGTH_SHORT).show();
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
        Log.i(TAG, "onClickCommitResult: " + autoID);
        ref.child("games").child(autoID).child("teamBlue").child("score").setValue(editTextNumberResultBlue);
        ref.child("games").child(autoID).child("teamRed").child("score").setValue(editTextNumberResultRed);
        if(teamRedPlayerOne != null) {
            database.getReference("users").child(teamRedPlayerOne).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Result doTransaction(@NonNull MutableData mutableData) {
                    Long value = mutableData.getValue(Long.class);
                    mutableData.setValue(value +1);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onComplete: "+ databaseError);
                }
            });
            if(editTextNumberResultRed == 10){
                database.getReference("users").child(teamRedPlayerOne).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Result doTransaction(@NonNull MutableData mutableData) {
                        Long value = mutableData.getValue(Long.class);
                        mutableData.setValue(value +1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: " + databaseError);
                    }
                });
            }
            ref.child("users").child(teamRedPlayerOne).child("finishedGames").child(autoID).setValue(unixTime);
        }
        if(teamRedPlayerTwo != null) {
            database.getReference("users").child(teamRedPlayerTwo).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Result doTransaction(@NonNull MutableData mutableData) {
                    Long value = mutableData.getValue(Long.class);
                    mutableData.setValue(value + 1);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onComplete: "+ databaseError);
                }
            });

            if(editTextNumberResultRed == 10){
                database.getReference("users").child(teamRedPlayerTwo).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Result doTransaction(@NonNull MutableData mutableData) {
                        Long value = mutableData.getValue(Long.class);
                        mutableData.setValue(value +1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: "+databaseError);
                    }
                });
            }
            ref.child("users").child(teamRedPlayerTwo).child("finishedGames").child(autoID).setValue(unixTime);
            Log.d(TAG, "onClickCommitResult: uid = " + teamRedPlayerTwo);

        }
        if(teamBluePlayerThree != null) {
            database.getReference("users").child(teamBluePlayerThree).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Result doTransaction(@NonNull MutableData mutableData) {
                    Long value = mutableData.getValue(Long.class);
                    mutableData.setValue(value +1);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onComplete: "+databaseError);
                }
            });
            if(editTextNumberResultBlue == 10){
                database.getReference("users").child(teamBluePlayerThree).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Result doTransaction(@NonNull MutableData mutableData) {
                        Long value = mutableData.getValue(Long.class);
                        mutableData.setValue(value +1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: "+databaseError);
                    }
                });
            }
            ref.child("users").child(teamBluePlayerThree).child("finishedGames").child(autoID).setValue(unixTime);
        }
        if(teamBluePlayerFour != null) {
            database.getReference("users").child(teamBluePlayerFour).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Result doTransaction(@NonNull MutableData mutableData) {
                    Long value = mutableData.getValue(Long.class);
                    mutableData.setValue(value + 1);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onComplete: " + databaseError);
                }
            });
            ref.child("users").child(teamBluePlayerFour).child("finishedGames").child(autoID).setValue(unixTime);

            if(editTextNumberResultBlue == 10){
                database.getReference("users").child(teamBluePlayerFour).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Result doTransaction(@NonNull MutableData mutableData) {
                        Long value = mutableData.getValue(Long.class);
                        mutableData.setValue(value + 1);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: " + databaseError);
                    }
                });
            }

        }
        
        //ref.child("lobby").child(lobbyPath).removeValue();
        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
        Log.i(TAG, "onClickCommitResult: " + getSharedPreferences("MyPreferences", 0).getAll());
        Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
        onStopCalled = false;
        startActivity(i);
    }

    public void commitPreferences(){
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("var1", lobbyPath);
        editor.putString("var2", fullLobyPath);
        editor.putString("varPlayerR1", teamRedPlayerOne);
        editor.putString("varPlayerR2", teamRedPlayerTwo);
        editor.putString("varPlayerB3", teamBluePlayerThree);
        editor.putString("varPlayerB4", teamBluePlayerFour);
        editor.putString("autoID", autoID);
        editor.apply();
        Log.i(TAG, "onDestroy: " + getSharedPreferences("MyPreferences", 0).getString("var1", null));
        Log.i(TAG, "onDestroy: " + getSharedPreferences("MyPreferences", 0).getString("var2", null));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        unixTime = System.currentTimeMillis();
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: is called");
        setContentView(R.layout.activity_result);
        database = FirebaseDatabase.getInstance();
        editTextResultBlue = findViewById(R.id.editTextResultBlue);
        editTextResultRed = findViewById(R.id.editTextResultRed);
        lobbyPath = this.getIntent().getExtras().getString("lobbyPath");
        autoID = (String)this.getIntent().getExtras().get("autoID");
        teamRedPlayerOne = (String)this.getIntent().getExtras().get("teamRedPlayerOne");
        teamRedPlayerTwo = (String)this.getIntent().getExtras().get("teamRedPlayerTwo");
        teamBluePlayerThree = (String)this.getIntent().getExtras().get("teamBluePlayerThree");
        teamBluePlayerFour = (String)this.getIntent().getExtras().get("teamBluePlayerFour");


        Log.i(TAG, "onCreate: " + lobbyPath);
        ref = FirebaseDatabase.getInstance().getReference();
        fullLobyPath = lobbyPath;
        lobbyArray = lobbyPath.split("/");
        lobbyPath = lobbyArray[0];
        commitPreferences();
    }
}
