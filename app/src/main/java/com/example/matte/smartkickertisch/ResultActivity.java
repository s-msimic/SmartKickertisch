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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.Transaction.Result;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class ResultActivity extends AppCompatActivity {
    DatabaseReference ref;
    FirebaseDatabase database;
    SharedPreferences preferences;
    private EditText editTextResultRed;
    private EditText editTextResultBlue;
    private String lobbyPath;
    private String gameID;
    private String fullLobbyPath;
    private int redTeamScore;
    private int blueTeamScore;
    private String teamRedPlayerOne;
    private String teamRedPlayerTwo;
    private String teamBluePlayerTwo;
    private String teamBluePlayerOne;
    private String [] lobbyArray = new String[3];
    private static final String TAG = "ResultActivity";
    private long unixTime;


    /**
     * Deletes game from database and goes back to LeaderboardActivity.
     * @param view Discard game button.
     */
    public void onClickDiscardGame(View view) {
        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
        ref.child("games").child(gameID).removeValue();
        Intent toLeaderboard = new Intent(ResultActivity.this, LeaderboardActivity.class);
        startActivity(toLeaderboard);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please enter a result before leaving",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Saves game into database and adds all statistics for all players in the current game.
     * @param view Commit button.
     */
    public void onClickCommitResult(View view){
        if(editTextResultRed.getText().toString().isEmpty() ||
                editTextResultBlue.getText().toString().isEmpty()) {
            Toast.makeText(ResultActivity.this, "Make sure to edit results", Toast.LENGTH_SHORT).show();
            return;
        }

        redTeamScore = Integer.parseInt(editTextResultRed.getText().toString());
        blueTeamScore = Integer.parseInt(editTextResultBlue.getText().toString());

        if(blueTeamScore > 10 || blueTeamScore < 0 ||
                redTeamScore > 10 || redTeamScore < 0 ||
                redTeamScore == 10 && blueTeamScore == 10 ||
                blueTeamScore != 10 && redTeamScore != 10) {
            Toast.makeText(ResultActivity.this, "Only values between 0 to 10 - " +
                    "one team must win with 10 points", Toast.LENGTH_SHORT).show();
            return;
        }
        //put results in each players firebase data
        Log.i(TAG, "onClickCommitResult: " + gameID);
        ref.child("games").child(gameID).child("teamBlue").child("score").setValue(blueTeamScore);
        ref.child("games").child(gameID).child("teamRed").child("score").setValue(redTeamScore);
        ifExistentUpdatePlayerData(teamRedPlayerOne, true);
        ifExistentUpdatePlayerData(teamRedPlayerTwo, true);
        ifExistentUpdatePlayerData(teamBluePlayerOne, false);
        ifExistentUpdatePlayerData(teamBluePlayerTwo, false);

        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
        Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
        startActivity(i);
    }

    /**
     * If the player exists, its score will be set in the database, and all of his statistics will be updated.
     * @param playerID UserID for the player.
     * @param isRedTeam Identifier it the player belongs to team red or blue.
     */
    private void ifExistentUpdatePlayerData(String playerID, boolean isRedTeam) {
        if(playerID != null) {
            database.getReference("users").child(playerID).child("data")
                    .child("playedGames").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Result doTransaction(@NonNull MutableData mutableData) {
                    Long value = 1L;
                    if (mutableData.getValue() != null) {
                        value = mutableData.getValue(Long.class);
                        mutableData.setValue(value + 1);
                    }
                    // for first played game value is null
                    else {
                        mutableData.setValue(value);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
            });

            int myTeamScore = isRedTeam ? redTeamScore : blueTeamScore;

            if (myTeamScore == 10) {
                database.getReference("users").child(playerID).child("data")
                        .child("winCounter").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Result doTransaction(@NonNull MutableData mutableData) {
                        Long value = 1L;
                        if (mutableData.getValue() != null) {
                            value = mutableData.getValue(Long.class);
                            mutableData.setValue(value + 1);
                        }
                        // for first win value is null
                        else {
                            mutableData.setValue(value);
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
                });

                // check if this was the best win
                editBestWin(playerID, isRedTeam);
            } else {
                editWorstLoss(playerID, isRedTeam);
            }
            ref.child("users").child(playerID).child("finishedGames").child(gameID).setValue(unixTime);
        }
    }

    /**
     * Called when player has won the game. Checks if the current result is the best win of the player.
     * If that is the case the game is added as the new best win of the player into the database.
     * @param playerID UID from firebase database of the current player.
     * @param isRedTeam True if player is in red team.
     */
    private void editBestWin(String playerID, boolean isRedTeam) {
        database.getReference("users").child(playerID).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("bestWin")) {
                    // player is team red AND new result better or equal to current result
                    if (isRedTeam && blueTeamScore <= Integer.valueOf(dataSnapshot.child("bestWin")
                            .child("score").getValue(String.class).split(":")[1])) {
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("score").setValue(redTeamScore + ":" + blueTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("gameID").setValue(gameID);
                    }
                    // player is team blue AND new result is better or equal to current result
                    else if (redTeamScore <= Integer.valueOf(dataSnapshot.child("bestWin")
                            .child("score").getValue(String.class).split(":")[1]) && !isRedTeam){
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("score").setValue(blueTeamScore + ":" + redTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("gameID").setValue(gameID);
                    }
                } else {
                    // player is team red AND no best game (first win)
                    if (isRedTeam) {
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("score").setValue(redTeamScore + ":" + blueTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("gameID").setValue(gameID);
                    }
                    // player is team blue AND no best game (first win)
                    else {
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("score").setValue(blueTeamScore + ":" + redTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("gameID").setValue(gameID);
                    }
                }
                Log.i(TAG, "editBestWin: lobbyArray:" + Arrays.toString(lobbyArray));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void editWorstLoss(String playerID, boolean isRedTeam) {
        database.getReference("users").child(playerID).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("worstLoss")) {
                    // player is team red AND new result worse or equal to current result
                    if (isRedTeam && redTeamScore <= Integer.valueOf(dataSnapshot.child("worstLoss")
                            .child("score").getValue(String.class).split(":")[0])) {
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("score").setValue(redTeamScore + ":" + blueTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("gameID").setValue(gameID);
                    }
                    // player is team blue AND new result is worse or equal to current result
                    else if (!isRedTeam && blueTeamScore <= Integer.valueOf(dataSnapshot.child("worstLoss")
                            .child("score").getValue(String.class).split(":")[0])){
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("score").setValue(blueTeamScore + ":" + redTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("gameID").setValue(gameID);
                    }
                } else {
                    // player is team red AND no worst game (first loss)
                    if (isRedTeam) {
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("score").setValue(redTeamScore + ":" + blueTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("gameID").setValue(gameID);
                    }
                    // player is team blue AND no worst game (first loss)
                    else {
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("score").setValue(blueTeamScore + ":" + redTeamScore);
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("gameID").setValue(gameID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /**
     * Save lobby into SharedPreferences, so that if the app is closed by the user he will automatically
     * go back to the ResultActivity to enter the results.
     */
    public void commitPreferences(){
        preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("var1", lobbyPath);
        editor.putString("var2", fullLobbyPath);
        editor.putString("varPlayerR1", teamRedPlayerOne);
        editor.putString("varPlayerR2", teamRedPlayerTwo);
        editor.putString("varPlayerB3", teamBluePlayerTwo);
        editor.putString("varPlayerB4", teamBluePlayerOne);
        editor.putString("gameID", gameID);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unixTime = System.currentTimeMillis();
        setContentView(R.layout.activity_result);
        ref = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        editTextResultBlue = findViewById(R.id.editTextResultBlue);
        editTextResultRed = findViewById(R.id.editTextResultRed);
        lobbyPath = this.getIntent().getExtras().getString("lobbyPath");
        gameID = (String)this.getIntent().getExtras().get("gameID");
        teamRedPlayerOne = (String)this.getIntent().getExtras().get("teamRedPlayerOne");
        teamRedPlayerTwo = (String)this.getIntent().getExtras().get("teamRedPlayerTwo");
        teamBluePlayerTwo = (String)this.getIntent().getExtras().get("teamBluePlayerTwo");
        teamBluePlayerOne = (String)this.getIntent().getExtras().get("teamBluePlayerOne");

        fullLobbyPath = lobbyPath;
        lobbyArray = lobbyPath.split("/");
        lobbyPath = lobbyArray[0];
        commitPreferences();
    }
}