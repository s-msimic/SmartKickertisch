package com.example.matte.smartkickertisch;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Game {
    private String gameID;
    private long gameDate;
    private MatchHistoryViewHolder matchHistoryViewHolder;
    private int blueTeamScore;
    private int redTeamScore;
    private String blueTeamDefenseID;
    private String blueTeamOffenseID;
    private String redTeamDefenseID;
    private String redTeamOffenseID;
    private String thisPlayerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private boolean gameWon;
    private static final String TAG = "GameClass";

    public Game(String gameID, MatchHistoryViewHolder viewHolder) {
        this.gameID = gameID;
        this.matchHistoryViewHolder = viewHolder;
        Query myGame = FirebaseDatabase.getInstance().getReference("games").child(gameID);
        myGame.addListenerForSingleValueEvent(dataListener);
    }

    ValueEventListener dataListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "dataListener: Key = " + dataSnapshot.getKey());
            Log.i(TAG, "dataListener: Value = " + dataSnapshot.getValue());

            gameDate = dataSnapshot.child("data").child("date").getValue(Long.class);

            if (dataSnapshot.child("teamBlue").child("score").exists()) {
                blueTeamScore = dataSnapshot.child("teamBlue").child("score").getValue(int.class);
                redTeamScore = dataSnapshot.child("teamRed").child("score").getValue(int.class);
            }
            if (dataSnapshot.child("teamRed").child("player1").exists()) {
                Log.d(TAG, "dataListener: player 1 = " + dataSnapshot.child("teamRed").child("player1").getValue());
                redTeamOffenseID = dataSnapshot.child("teamRed").child("player1").getValue(String.class);
            }

            if (dataSnapshot.child("teamRed").child("player2").exists()) {
                Log.d(TAG, "dataListener: player 2 = " + dataSnapshot.child("teamRed").child("player2").getValue());
                redTeamDefenseID = dataSnapshot.child("teamRed").child("player2").getValue(String.class);
            }

            if (dataSnapshot.child("teamBlue").child("player3").exists())
                blueTeamDefenseID = dataSnapshot.child("teamBlue").child("player3").getValue(String.class);

            if (dataSnapshot.child("teamBlue").child("player4").exists())
                blueTeamOffenseID = dataSnapshot.child("teamBlue").child("player4").getValue(String.class);

            matchHistoryViewHolder.dateTextView.setText(TimeAgo.getTimeAgo(gameDate));
            matchHistoryViewHolder.scoreTextView.setText(blueTeamScore + ":");
            matchHistoryViewHolder.scoreTextView.setText(matchHistoryViewHolder.scoreTextView.getText().toString() + redTeamScore);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public String getGameID() {
        return gameID;
    }

    public long getGameDate() {
        return gameDate;
    }

    public MatchHistoryViewHolder getMatchHistoryViewHolder() {
        return matchHistoryViewHolder;
    }

    public int getBlueTeamScore() {
        return blueTeamScore;
    }

    public int getRedTeamScore() {
        return redTeamScore;
    }

    public String getBlueTeamDefenseID() {
        return blueTeamDefenseID;
    }

    public String getBlueTeamOffenseID() {
        return blueTeamOffenseID;
    }

    public String getRedTeamDefenseID() {
        return redTeamDefenseID;
    }

    public String getRedTeamOffenseID() {
        return redTeamOffenseID;
    }

    public String getThisPlayerID() {
        return thisPlayerID;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    @NonNull
    @Override
    public String toString() {
        return "Game{" +
                "gameID='" + gameID + '\'' +
                ", gameDate=" + gameDate +
                ", blueTeamScore=" + blueTeamScore +
                ", redTeamScore=" + redTeamScore +
                ", blueTeamDefenseID='" + blueTeamDefenseID + '\'' +
                ", blueTeamOffenseID='" + blueTeamOffenseID + '\'' +
                ", redTeamDefenseID='" + redTeamDefenseID + '\'' +
                ", redTeamOffenseID='" + redTeamOffenseID + '\'' +
                '}';
    }
}
