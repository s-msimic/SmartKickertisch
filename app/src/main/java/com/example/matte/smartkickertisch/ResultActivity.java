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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.Transaction.Result;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultActivity extends AppCompatActivity {
    DatabaseReference ref;
    FirebaseDatabase database;
    SharedPreferences preferences;
    private EditText editTextResultRed;
    private EditText editTextResultBlue;
    private String gameID;
    private int redTeamScore;
    private int blueTeamScore;
    private String teamRedPlayerOne;
    private String teamRedPlayerTwo;
    private String teamBluePlayerTwo;
    private String teamBluePlayerOne;
    private static final String TAG = "ResultActivity";
    private long gameDate;
    AtomicInteger tasksRunning = new AtomicInteger(5);
    // TODO: 02.11.2019 IMPORTANT: do statistics logic with cloud functions, and delete this code here

    /**
     * Deletes game from database and goes back to LeaderboardActivity.
     *
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
     *
     * @param view Commit button.
     */
    public void onClickCommitResult(View view) {
        if (editTextResultRed.getText().toString().isEmpty() ||
                editTextResultBlue.getText().toString().isEmpty()) {
            Toast.makeText(ResultActivity.this, "Make sure to edit results", Toast.LENGTH_SHORT).show();
            return;
        }

        redTeamScore = Integer.parseInt(editTextResultRed.getText().toString());
        blueTeamScore = Integer.parseInt(editTextResultBlue.getText().toString());

        if (blueTeamScore > 10 || blueTeamScore < 0 ||
                redTeamScore > 10 || redTeamScore < 0 ||
                redTeamScore == 10 && blueTeamScore == 10 ||
                blueTeamScore != 10 && redTeamScore != 10) {
            Toast.makeText(ResultActivity.this, "Only values between 0 to 10 - " +
                    "one team must win with 10 points", Toast.LENGTH_SHORT).show();
            return;
        }
        //put results in each players firebase data
        updateStatistics(teamRedPlayerOne, true);
        updateStatistics(teamRedPlayerTwo, true);
        updateStatistics(teamBluePlayerOne, false);
        updateStatistics(teamBluePlayerTwo, false);
        Log.i(TAG, "onClickCommitResult: " + gameID);
        ref.child("games").child(gameID).child("data").child("date").setValue(gameDate);
        ref.child("games").child(gameID).child("teamBlue").child("score").setValue(blueTeamScore);
        ref.child("games").child(gameID).child("teamRed").child("score").setValue(redTeamScore);

        String loosingTeamScore = redTeamScore == 10 ? String.valueOf(blueTeamScore) : String.valueOf(redTeamScore);
        Log.d(TAG, "onClickCommitResult-93: loosingTeamScore= " + loosingTeamScore);

        ref.child("stats").child("resultCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange-108: resultCountSnapshot= " + dataSnapshot);
                int resultCount = 1;
                if (dataSnapshot.hasChild(loosingTeamScore)) {
                    resultCount = dataSnapshot.child(loosingTeamScore).getValue(int.class);
                    resultCount += 1;
                    Log.d(TAG, "onClickCommitResult-onDataChange-101: resultCount= " + resultCount);
                }
                ref.child("stats").child("resultCount").child(loosingTeamScore).setValue(resultCount)
                        .addOnCompleteListener(command -> {
                            if (tasksRunning.decrementAndGet() == 0) {
                                getSharedPreferences("MyPreferences", 0).edit().clear().apply();
                                Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
                                Log.d(TAG, "onClickCommitResult-119: startActivity");
                                startActivity(i);
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void updateStatistics(String playerID, boolean isRedTeam) {
        Log.d(TAG, "updateStatistics-119: playerID= " + playerID);
        if (playerID != null) {
            ifExistentUpdatePlayerData(playerID, isRedTeam);
            updateResultCount(playerID, isRedTeam);
        } else {
            if (tasksRunning.decrementAndGet() == 0) {
                getSharedPreferences("MyPreferences", 0).edit().clear().apply();
                Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
                Log.d(TAG, "onClickCommitResult-114: startActivity");
                startActivity(i);
            }
        }
    }

    /**
     * If the player exists, its score will be set in the database, and all of his statistics will be updated.
     *
     * @param playerID  UserID for the player.
     * @param isRedTeam Identifier it the player belongs to team red or blue.
     */
    private void ifExistentUpdatePlayerData(String playerID, boolean isRedTeam) {
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
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
            }
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
                    Log.d(TAG, "ifExistent-doTransaction-180: valueWinCounter= " + value);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                }
            });

            // check if this was the best win
            editBestWin(playerID, isRedTeam);
        } else {
            editWorstLoss(playerID, isRedTeam);
        }
        ref.child("users").child(playerID).child("finishedGames").child(gameID).setValue(gameDate);
    }

    /**
     * Called when player has won the game. Checks if the current result is the best win of the player.
     * If that is the case the game is added as the new best win of the player into the database.
     *
     * @param playerID  UID String from firebase database of the current player.
     * @param isRedTeam True if player is in red team.
     */
    private void editBestWin(String playerID, boolean isRedTeam) {
        database.getReference("users").child(playerID).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int goalsScored = isRedTeam ? redTeamScore : blueTeamScore;
                int goalsConceded = isRedTeam ? blueTeamScore : redTeamScore;
                Log.d(TAG, "editBestWin-onDataChange-192: scored= " + goalsScored + "-conceded= " + goalsConceded);
                if (dataSnapshot.hasChild("bestWin")) {
                    // new result better or equal to current result
                    if (goalsConceded <= Integer.valueOf(dataSnapshot.child("bestWin")
                            .child("score").getValue(String.class).split(":")[1])) {
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("score").setValue(goalsScored + ":" + goalsConceded);
                        database.getReference("users").child(playerID).child("data")
                                .child("bestWin").child("gameID").setValue(gameID);
                    }
                } else {
                    // no best game (first win) yet
                    database.getReference("users").child(playerID).child("data")
                            .child("bestWin").child("score").setValue(goalsScored + ":" + goalsConceded);
                    database.getReference("users").child(playerID).child("data")
                            .child("bestWin").child("gameID").setValue(gameID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void editWorstLoss(String playerID, boolean isRedTeam) {
        database.getReference("users").child(playerID).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int goalsScored = isRedTeam ? redTeamScore : blueTeamScore;
                int goalsConceded = isRedTeam ? blueTeamScore : redTeamScore;
                Log.d(TAG, "editWorstLoss-onDataChange-239: scored= " + goalsScored + "-conceded= " + goalsConceded);

                if (dataSnapshot.hasChild("worstLoss")) {
                    // new result worse or equal to current result
                    if (goalsScored <= Integer.valueOf(dataSnapshot.child("worstLoss")
                            .child("score").getValue(String.class).split(":")[0])) {
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("score").setValue(goalsScored + ":" + goalsConceded);
                        database.getReference("users").child(playerID).child("data")
                                .child("worstLoss").child("gameID").setValue(gameID);
                    }
                } else {
                    // no worst game (first loss) yet
                    database.getReference("users").child(playerID).child("data")
                            .child("worstLoss").child("score").setValue(goalsScored + ":" + goalsConceded);
                    database.getReference("users").child(playerID).child("data")
                            .child("worstLoss").child("gameID").setValue(gameID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Updates how often this result occurred for the player.
     *
     * @param playerID  UID String from firebase database of the current player.
     * @param isRedTeam True if player is in red team.
     */
    private void updateResultCount(String playerID, boolean isRedTeam) {
        int goalsMade = isRedTeam ? redTeamScore : blueTeamScore;
        int goalsConceded = isRedTeam ? blueTeamScore : redTeamScore;
        String result = goalsMade + ":" + goalsConceded;
        ref.child("users").child(playerID).child("data").child("resultCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "updateResultCount-onDataChange-260: snapshot= " + dataSnapshot);
                int currentCount = 1;
                if (dataSnapshot.hasChild(result)) {
                    currentCount = dataSnapshot.child(result).getValue(int.class);
                    Log.d(TAG, "onDataChange-295: currentCount= " + currentCount);
                    currentCount += 1;
                }
                ref.child("users").child(playerID).child("data").child("resultCount").child(result)
                        .setValue(currentCount).addOnCompleteListener(command -> {
                    if (tasksRunning.decrementAndGet() == 0) {
                        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
                        Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
                        Log.d(TAG, "onClickCommitResult-114: startActivity");
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Save lobby into SharedPreferences, so that if the app is closed by the user he will automatically
     * go back to the ResultActivity to enter the results.
     */
    public void commitPreferences() {
        preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("gameID", gameID);
        editor.putLong("gameDate", gameDate);
        editor.putString("teamBluePlayerOne", teamRedPlayerOne);
        editor.putString("teamBluePlayerTwo", teamRedPlayerTwo);
        editor.putString("teamRedPlayerOne", teamBluePlayerTwo);
        editor.putString("teamRedPlayerTwo", teamBluePlayerOne);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ref = FirebaseDatabase.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        editTextResultBlue = findViewById(R.id.editTextResultBlue);
        editTextResultRed = findViewById(R.id.editTextResultRed);

        Bundle lobbyDataBundle = getIntent().getExtras();
        Log.d(TAG, "onCreate-270: " + getIntent().getExtras());
        if (lobbyDataBundle != null) {
            gameID = lobbyDataBundle.getString("gameID");
            Log.d(TAG, "onCreate-305: intent gameID= " + gameID);
            gameDate = lobbyDataBundle.getLong("gameDate");
            teamBluePlayerOne = lobbyDataBundle.getString("teamBluePlayerTwo", null);
            teamBluePlayerTwo = lobbyDataBundle.getString("teamBluePlayerTwo", null);
            teamRedPlayerOne = lobbyDataBundle.getString("teamRedPlayerOne", null);
            teamRedPlayerTwo = lobbyDataBundle.getString("teamRedPlayerTwo", null);
            commitPreferences();
        } else {
            SharedPreferences lobbyDataPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
            gameID = lobbyDataPreferences.getString("gameID", null);
            Log.d(TAG, "onCreate-316: preferences gameID= " + gameID);
            gameDate = lobbyDataPreferences.getLong("gameDate", 0);
            teamBluePlayerOne = lobbyDataPreferences.getString("teamBluePlayerOne", null);
            teamBluePlayerTwo = lobbyDataPreferences.getString("teamBluePlayerTwo", null);
            teamRedPlayerOne = lobbyDataPreferences.getString("teamRedPlayerOne", null);
            teamRedPlayerTwo = lobbyDataPreferences.getString("teamRedPlayerTwo", null);
        }
    }
}