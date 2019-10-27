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

public class ResultActivity extends AppCompatActivity {
    DatabaseReference ref;
    FirebaseDatabase database;
    private EditText editTextResultRed;
    private EditText editTextResultBlue;
    private String lobbyPath;
    private String autoID;
    private String fullLobbyPath;
    private int editTextNumberResultRed;
    private int editTextNumberResultBlue;
    private String teamRedPlayerOne;
    private String teamRedPlayerTwo;
    private String teamBluePlayerTwo;
    private String teamBluePlayerOne;
    private String [] lobbyArray = new String[3];
    private static final String TAG = "ResultActivity";
    private long unixTime;


    public void onClickDiscardGame(View view) {
        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
        Intent toLeaderboard = new Intent(ResultActivity.this, LeaderboardActivity.class);
        startActivity(toLeaderboard);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please enter a result before leaving",
                Toast.LENGTH_SHORT).show();
    }


    public void onClickCommitResult(View view){

        if(editTextResultRed.getText().toString().isEmpty() ||
                editTextResultBlue.getText().toString().isEmpty()) {
            Toast.makeText(ResultActivity.this, "Make sure to edit results", Toast.LENGTH_SHORT).show();
            return;
        }

        String redTeamScoreString = editTextResultRed.getText().toString();
        editTextNumberResultRed = Integer.parseInt(redTeamScoreString);
        String blueTeamScoreString = editTextResultBlue.getText().toString();
        editTextNumberResultBlue = Integer.parseInt(blueTeamScoreString);

        if(editTextNumberResultBlue > 10 || editTextNumberResultBlue < 0 ||
                editTextNumberResultRed > 10 || editTextNumberResultRed < 0 ||
                editTextNumberResultRed == 10 && editTextNumberResultBlue == 10 ||
                editTextNumberResultBlue != 10 && editTextNumberResultRed != 10) {
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
                    Log.d(TAG, "onComplete: "+ databaseError);
                }
            });
            if (editTextNumberResultRed == 10) {
                database.getReference("users").child(teamRedPlayerOne).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
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
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: " + databaseError);
                    }
                });

                // check if this was the best win
                editBestWin(teamRedPlayerOne, true);
            } else {
                editWorstLoss(teamRedPlayerOne, true);
            }
            ref.child("users").child(teamRedPlayerOne).child("finishedGames").child(autoID).setValue(unixTime);
        }
        if(teamRedPlayerTwo != null) {
            database.getReference("users").child(teamRedPlayerTwo).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
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
                    Log.d(TAG, "onComplete: "+ databaseError);
                }
            });

            if (editTextNumberResultRed == 10) {
                database.getReference("users").child(teamRedPlayerTwo).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
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
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: " + databaseError);
                    }
                });

                editBestWin(teamRedPlayerTwo, true);
            } else {
                editWorstLoss(teamRedPlayerTwo, true);
            }
            ref.child("users").child(teamRedPlayerTwo).child("finishedGames").child(autoID).setValue(unixTime);
            Log.d(TAG, "onClickCommitResult: uid = " + teamRedPlayerTwo);

        }
        if(teamBluePlayerTwo != null) {
            database.getReference("users").child(teamBluePlayerTwo).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
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
                    Log.d(TAG, "onComplete: "+databaseError);
                }
            });
            if (editTextNumberResultBlue == 10) {
                database.getReference("users").child(teamBluePlayerTwo).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
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
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: " + databaseError);
                    }
                });
                editBestWin(teamBluePlayerTwo, false);
            } else {
                editWorstLoss(teamBluePlayerTwo, false);
            }
            ref.child("users").child(teamBluePlayerTwo).child("finishedGames").child(autoID).setValue(unixTime);
        }
        if(teamBluePlayerOne != null) {
            database.getReference("users").child(teamBluePlayerOne).child("data").child("playedGames").runTransaction(new Transaction.Handler() {
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
                    Log.d(TAG, "onComplete: " + databaseError);
                }
            });
            ref.child("users").child(teamBluePlayerOne).child("finishedGames").child(autoID).setValue(unixTime);

            if (editTextNumberResultBlue == 10) {
                database.getReference("users").child(teamBluePlayerOne).child("data").child("winCounter").runTransaction(new Transaction.Handler() {
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
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onComplete: " + databaseError);
                    }
                });
                editBestWin(teamBluePlayerOne, false);
            } else {
                editWorstLoss(teamBluePlayerOne, false);
            }

        }
        
        //ref.child("lobby").child(lobbyPath).removeValue();
        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
        Log.i(TAG, "onClickCommitResult: " + getSharedPreferences("MyPreferences", 0).getAll());
        Intent i = new Intent(ResultActivity.this, LeaderboardActivity.class);
        startActivity(i);
    }

    private void editBestWin(String playerID, boolean isRedTeam) {
        database.getReference("users").child(playerID).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("bestWin")) {
                    if (editTextNumberResultBlue <= Integer.valueOf(
                            dataSnapshot.child("bestWin").child("score").getValue(String.class).split(":")[1]) && isRedTeam) {
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("score")
                                .setValue(editTextNumberResultRed + ":" + editTextNumberResultBlue);
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("gameID")
                                .setValue(autoID);
                    } else if (editTextNumberResultRed <= Integer.valueOf(
                            dataSnapshot.child("bestWin").child("score").getValue(String.class).split(":")[1]) && !isRedTeam){
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("score")
                                .setValue(editTextNumberResultBlue + ":" + editTextNumberResultRed);
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("gameID")
                                .setValue(autoID);
                    }
                } else {
                    if (isRedTeam) {
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("score")
                                .setValue(editTextNumberResultRed + ":" + editTextNumberResultBlue);
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("gameID")
                                .setValue(autoID);
                    } else {
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("score")
                                .setValue(editTextNumberResultBlue + ":" + editTextNumberResultRed);
                        database.getReference("users").child(playerID).child("data").child("bestWin").child("gameID")
                                .setValue(autoID);
                    }
                }

                Log.i(TAG, "editBestWin: " + dataSnapshot.toString());
                Log.i(TAG, "editBestWin: autoID:" + autoID);
                Log.i(TAG, "editBestWin: lobbyPath:" + lobbyPath);
                Log.i(TAG, "editBestWin: fullLobbyPath:" + fullLobbyPath);
                Log.i(TAG, "editBestWin: lobbyArray:" + lobbyArray.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void editWorstLoss(String playerID, boolean isRedTeam) {
        Log.i(TAG, "editWorstLoss: worstLoss");
        database.getReference("users").child(playerID).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("worstLoss")) {
                    // player is team red AND new result worse or equal to current result
                    if (isRedTeam && editTextNumberResultRed <= Integer.valueOf(
                            dataSnapshot.child("worstLoss").child("score").getValue(String.class).split(":")[0])) {
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("score")
                                .setValue(editTextNumberResultRed + ":" + editTextNumberResultBlue);
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("gameID")
                                .setValue(autoID);
                    }
                    // player is team blue AND new result is worse or equal to current result
                    else if (!isRedTeam && editTextNumberResultBlue <= Integer.valueOf(
                            dataSnapshot.child("worstLoss").child("score").getValue(String.class).split(":")[0])){
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("score")
                                .setValue(editTextNumberResultBlue + ":" + editTextNumberResultRed);
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("gameID")
                                .setValue(autoID);
                    }
                } else {
                    // player is team red AND no worst game (first loss)
                    if (isRedTeam) {
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("score")
                                .setValue(editTextNumberResultRed + ":" + editTextNumberResultBlue);
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("gameID")
                                .setValue(autoID);
                    }
                    // player is team blue AND no worst game (first loss)
                    else {
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("score")
                                .setValue(editTextNumberResultBlue + ":" + editTextNumberResultRed);
                        database.getReference("users").child(playerID).child("data").child("worstLoss").child("gameID")
                                .setValue(autoID);
                        Log.d(TAG, "onDataChange() called with: dataSnapshot = [" + dataSnapshot + "]");

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void commitPreferences(){
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("var1", lobbyPath);
        editor.putString("var2", fullLobbyPath);
        editor.putString("varPlayerR1", teamRedPlayerOne);
        editor.putString("varPlayerR2", teamRedPlayerTwo);
        editor.putString("varPlayerB3", teamBluePlayerTwo);
        editor.putString("varPlayerB4", teamBluePlayerOne);
        editor.putString("autoID", autoID);
        editor.apply();
        Log.i(TAG, "commitPreferences: var1= " + getSharedPreferences("MyPreferences", 0).getString("var1", null));
        Log.i(TAG, "commitPreferences: var2= " + getSharedPreferences("MyPreferences", 0).getString("var2", null));
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
        teamBluePlayerTwo = (String)this.getIntent().getExtras().get("teamBluePlayerTwo");
        teamBluePlayerOne = (String)this.getIntent().getExtras().get("teamBluePlayerOne");

        Log.i(TAG, "onCreate: " + lobbyPath);
        ref = FirebaseDatabase.getInstance().getReference();
        fullLobbyPath = lobbyPath;
        lobbyArray = lobbyPath.split("/");
        lobbyPath = lobbyArray[0];
        commitPreferences();
    }
}
