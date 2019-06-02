package com.example.matte.smartkickertisch;

import android.graphics.Color;
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
    private boolean gameWon = false;
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
            if (gameDate < 1000000000000L) {
                gameDate *= 1000;
            }

            if (dataSnapshot.child("teamBlue").child("score").exists()) {
                blueTeamScore = dataSnapshot.child("teamBlue").child("score").getValue(int.class);
                redTeamScore = dataSnapshot.child("teamRed").child("score").getValue(int.class);
            }
            if (dataSnapshot.child("teamRed").child("player1").exists()) {
                Log.d(TAG, "dataListener: player 1 = " + dataSnapshot.child("teamRed").child("player1").getValue());
                redTeamOffenseID = dataSnapshot.child("teamRed").child("player1").getValue(String.class);

                if (redTeamOffenseID.equals(thisPlayerID)) {
                    Log.d(TAG, "onDataChange: redOffense = " + redTeamOffenseID.equals(thisPlayerID));
                    if (redTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (dataSnapshot.child("teamRed").child("player2").exists()) {
                Log.d(TAG, "dataListener: player 2 = " + dataSnapshot.child("teamRed").child("player2").getValue());
                redTeamDefenseID = dataSnapshot.child("teamRed").child("player2").getValue(String.class);

                if (redTeamDefenseID.equals(thisPlayerID)) {
                    Log.d(TAG, "onDataChange: redDefense = " + redTeamDefenseID.equals(thisPlayerID));
                    if (redTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (dataSnapshot.child("teamBlue").child("player2").exists()) {
                blueTeamDefenseID = dataSnapshot.child("teamBlue").child("player2").getValue(String.class);

                if (blueTeamDefenseID.equals(thisPlayerID)) {
                    if (blueTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (dataSnapshot.child("teamBlue").child("player1").exists()) {
                blueTeamOffenseID = dataSnapshot.child("teamBlue").child("player1").getValue(String.class);

                if (blueTeamOffenseID.equals(thisPlayerID)) {
                    if (blueTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (gameWon) {
//                matchHistoryViewHolder.materialCardView.setStrokeColor(Color.parseColor("#008B00"));
//                matchHistoryViewHolder.resultCircleImageView.setBorderColor(Color.parseColor("#008B00"));
                matchHistoryViewHolder.winMarker.setBackgroundColor(Color.GREEN);
            } else {
//                matchHistoryViewHolder.materialCardView.setStrokeColor(Color.parseColor("#ff0000"));
//                matchHistoryViewHolder.resultCircleImageView.setBorderColor(Color.parseColor("#ff0000"));
                matchHistoryViewHolder.winMarker.setBackgroundColor(Color.RED);
            }

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
