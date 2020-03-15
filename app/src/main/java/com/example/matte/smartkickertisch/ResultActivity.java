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
import java.util.HashMap;
import java.util.Map;
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
        Log.i(TAG, "onClickCommitResult: " + gameID);

        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> playerRedMap = new HashMap<>();
        Map<String, Object> playerBlueMap = new HashMap<>();
        dataMap.put("date", gameDate);
        playerRedMap.put("player1", teamRedPlayerOne);
        playerRedMap.put("player2", teamRedPlayerTwo);
        playerRedMap.put("score", redTeamScore);
        playerBlueMap.put("player1", teamBluePlayerOne);
        playerBlueMap.put("player2", teamBluePlayerTwo);
        playerBlueMap.put("score", blueTeamScore);
        valueMap.put(("data"), dataMap);
        valueMap.put("teamRed", playerRedMap);
        valueMap.put("teamBlue", playerBlueMap);

//        ref.child("games").child(gameID).updateChildren(valueMap);
        ref.child("games").child(gameID).setValue(valueMap)
                .addOnCompleteListener(command -> {
                    if (tasksRunning.decrementAndGet() == 0) {
                        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
                        Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
                        Log.d(TAG, "onClickCommitResult-114: startActivity");
                        startActivity(i);
                    }
                });
        updateStatistics(teamRedPlayerOne, true);
        updateStatistics(teamRedPlayerTwo, true);
        updateStatistics(teamBluePlayerOne, false);
        updateStatistics(teamBluePlayerTwo, false);
    }

    public void updateStatistics(String playerID, boolean isRedTeam) {
        Log.d(TAG, "updateStatistics-119: playerID= " + playerID);
        if (playerID != null) {
            ref.child("users").child(playerID).child("finishedGames").child(gameID).setValue(gameDate)
                    .addOnCompleteListener(command -> {
                        if (tasksRunning.decrementAndGet() == 0) {
                            getSharedPreferences("MyPreferences", 0).edit().clear().apply();
                            Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
                            Log.d(TAG, "onClickCommitResult-114: startActivity");
                            startActivity(i);
                        }
                    });
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