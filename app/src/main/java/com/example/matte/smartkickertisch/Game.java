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
    private boolean gameWon = false;
    private static final String TAG = "GameClass";

    public Game(String gameID, MatchHistoryViewHolder viewHolder) {
        this.gameID = gameID;
        this.matchHistoryViewHolder = viewHolder;
        matchHistoryViewHolder.bluePlayer1TextView.setText("");
        matchHistoryViewHolder.bluePlayer2TextView.setText("");
        matchHistoryViewHolder.redPlayer1TextView.setText("");
        matchHistoryViewHolder.redPlayer2TextView.setText("");
        matchHistoryViewHolder.dateTextView.setText("");
        matchHistoryViewHolder.scoreTextView.setText("");
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
                FirebaseDatabase.getInstance().getReference("users").child(redTeamOffenseID).child("nickName").addListenerForSingleValueEvent(redPlayer1Listener);

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
                FirebaseDatabase.getInstance().getReference("users").child(redTeamDefenseID).child("nickName").addListenerForSingleValueEvent(redPlayer2Listener);
                if (redTeamDefenseID.equals(thisPlayerID)) {
                    Log.d(TAG, "onDataChange: redDefense = " + redTeamDefenseID.equals(thisPlayerID));
                    if (redTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (dataSnapshot.child("teamBlue").child("player1").exists()) {
                Log.d(TAG, "dataListener: blue player 1 = " + dataSnapshot.child("teamBlue").child("player1").getValue());
                blueTeamOffenseID = dataSnapshot.child("teamBlue").child("player1").getValue(String.class);
                FirebaseDatabase.getInstance().getReference("users").child(blueTeamOffenseID).child("nickName").addListenerForSingleValueEvent(bluePlayer1Listener);
                if (blueTeamOffenseID.equals(thisPlayerID)) {
                    if (blueTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (dataSnapshot.child("teamBlue").child("player2").exists()) {
                Log.d(TAG, "dataListener: blue player 2 = " + dataSnapshot.child("teamBlue").child("player2").getValue());
                blueTeamDefenseID = dataSnapshot.child("teamBlue").child("player2").getValue(String.class);
                FirebaseDatabase.getInstance().getReference("users").child(blueTeamDefenseID).child("nickName").addListenerForSingleValueEvent(bluePlayer2Listener);
                if (blueTeamDefenseID.equals(thisPlayerID)) {
                    if (blueTeamScore == 10) {
                        gameWon = true;
                    }
                }
            }

            if (gameWon) {
                matchHistoryViewHolder.winMarker.setBackgroundResource(R.color.colorMatchHistoryWin);
            } else {
                matchHistoryViewHolder.winMarker.setBackgroundResource(R.color.colorMatchHistoryLoss);
            }

            if (blueTeamScore == 10 ||  redTeamScore == 10)
                matchHistoryViewHolder.gameSateTextView.setText("FINAL");

            matchHistoryViewHolder.dateTextView.setText(TimeAgo.getTimeAgo(gameDate));
            matchHistoryViewHolder.scoreTextView.setText(blueTeamScore + ":");
            matchHistoryViewHolder.scoreTextView.setText(matchHistoryViewHolder.scoreTextView.getText().toString() + redTeamScore);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener redPlayer1Listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "redPlayer1Listener: key = " + dataSnapshot.getKey() + " ; value = " + dataSnapshot.getValue());
            matchHistoryViewHolder.redPlayer1TextView.setText(dataSnapshot.getValue(String.class));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener redPlayer2Listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "redPlayer2Listener: key = " + dataSnapshot.getKey() + " ; value = " + dataSnapshot.getValue());
            matchHistoryViewHolder.redPlayer2TextView.setText(dataSnapshot.getValue(String.class));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    ValueEventListener bluePlayer1Listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "bluePlayer1Listener: pos=" + matchHistoryViewHolder.getAdapterPosition() + " /key = " + dataSnapshot.getKey() + " ; value = " + dataSnapshot.getValue());
            matchHistoryViewHolder.bluePlayer2TextView.setText(dataSnapshot.getValue(String.class));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    ValueEventListener bluePlayer2Listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "bluePlayer2Listener: pos=" + matchHistoryViewHolder.getAdapterPosition() + " /key = " + dataSnapshot.getKey() + " ; value = " + dataSnapshot.getValue());
            matchHistoryViewHolder.bluePlayer1TextView.setText(dataSnapshot.getValue(String.class));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    public long getGameDate() {
        return gameDate;
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
