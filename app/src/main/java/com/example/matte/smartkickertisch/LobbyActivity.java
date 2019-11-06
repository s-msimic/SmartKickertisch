package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LobbyActivity extends AppCompatActivity {

    boolean isWiggling = false;
    boolean deleteMode = false;
    String lobbyPath;
    String lobbyID;
    String myUID;
    DatabaseReference ref;

    ValueEventListener topLeftEventListener;
    ValueEventListener topRightEventListener;
    ValueEventListener bottomLeftEventListener;
    ValueEventListener bottomRightEventListener;
    ChildEventListener myOwnStatusEventListener;
    ChildEventListener myLobbyChildListener;
    ChildEventListener allLobbyChildListener;
    ValueEventListener myLobbyValueListener;

    ValueEventListener testListener;

    PlayerButtonTag topLeftButton;
    PlayerButtonTag topRightButton;
    PlayerButtonTag bottomLeftButton;
    PlayerButtonTag bottomRightButton;
    List<PlayerButtonTag> players = new ArrayList<>();
    Button deleteButton;
    Button buttonStartGame;
    AtomicBoolean buttonIsPressed;
    private static final String TAG = "LobbyActivity";


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: is called");
        if(!buttonIsPressed.get()){
            super.onDestroy();
        }
        else{
            ref.child("lobby").child(lobbyPath).removeValue();
            super.onDestroy();
        }

        ref.child("lobby").removeEventListener(allLobbyChildListener);
        ref.child("lobby").child(lobbyID).removeEventListener(myLobbyChildListener);
        ref.child("lobby").child(lobbyID).removeEventListener(myLobbyValueListener);
    }

    @Override
    public void onStop(){
        // TODO: 31.10.2019 check if onStop and onDestroy work properly
        Log.i(TAG, "onStop: is called");
        if(!buttonIsPressed.get()) {
            Log.i(TAG, "onStop: if true");
            ref.child("lobby").child(lobbyPath).removeValue();
            finish();
            super.onStop();
        }
        else{
            super.onStop();
            Log.i(TAG, "onStop: if not true");
        }
    }

    /**
     * Removes user ID from lobby in Firebase and directs the user to the 'LeaderboardActivity'
     * @param view Return button.
     */
    public void onClickReturn(View view){
        Log.i(TAG, "onClickReturn: return button was clicked");
        // TODO: 31.10.2019 check if it deletes lobby if one player leaves
        ref.child("lobby").child(lobbyPath).removeValue();
        Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
        startActivity(i);
    }

    public void onClickStartGame(View view){

        // checks if both teams have a player in the lobby
        if(topLeftButton.playerUID == null && topRightButton.playerUID == null ||
                bottomLeftButton.playerUID == null && bottomRightButton.playerUID == null){
            Toast.makeText(LobbyActivity.this, "At both teams must be atleast one player", Toast.LENGTH_SHORT).show();
            return;
        }
        buttonIsPressed.set(true);
        Log.d(TAG, "onClickStartGame: buttonIsPressed = " + buttonIsPressed);

        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> playerRedMap = new HashMap<>();
        Map<String, Object> playerBlueMap = new HashMap<>();

        // TODO: 26.10.2019 simplify this structure
        playerRedMap.put("player1", this.topLeftButton.playerUID);
        playerRedMap.put("player2", this.topRightButton.playerUID);
        playerRedMap.put("score", 0);
        playerBlueMap.put("player1", this.bottomRightButton.playerUID);
        playerBlueMap.put("player2", this.bottomLeftButton.playerUID);
        playerBlueMap.put("score", 0);
        valueMap.put("teamRed", playerRedMap);
        valueMap.put("teamBlue", playerBlueMap);

        String gameID = ref.child("games").push().getKey();
        ref.child("games").child(gameID).updateChildren(valueMap);
        final Intent i = new Intent(LobbyActivity.this, ResultActivity.class);
        i.putExtra("gameDate", System.currentTimeMillis());
        i.putExtra("gameID", gameID);
        i.putExtra("teamRedPlayerOne", this.topLeftButton.playerUID);
        i.putExtra("teamRedPlayerTwo", this.topRightButton.playerUID);
        i.putExtra("teamBluePlayerTwo", this.bottomLeftButton.playerUID);
        i.putExtra("teamBluePlayerOne", this.bottomRightButton.playerUID);
        ref.child("lobby").child(lobbyID).removeValue();
        if(topLeftEventListener != null)
            ref.removeEventListener(topLeftEventListener);
        if(topRightEventListener != null)
            ref.removeEventListener(topRightEventListener);
        if(bottomLeftEventListener != null)
            ref.removeEventListener(bottomLeftEventListener);
        if(bottomRightEventListener != null)
            ref.removeEventListener(bottomRightEventListener);
        if(myOwnStatusEventListener != null)
            ref.removeEventListener(myOwnStatusEventListener);
        Log.i(TAG, "onClickStartGame: " + i.toString());
        startActivity(i);
    }

    /**
     * Enables/disables delete mode, which allows the user to remove a player from the lobby.
     * Once the delete mode is activated, all players in the lobby are wiggling.
     * @param view 'Delete' button
     */
    public void onClickDelete(View view){
        if (!isWiggling) {
            deleteMode = !deleteMode;
            isWiggling = true;
            Animation wiggle = AnimationUtils.loadAnimation(this, R.anim.wiggle);

            for (PlayerButtonTag el : players) {
                el.startAnimation(wiggle);
            }

            deleteButton.setTextColor(getResources().getColor(R.color.colorSignUpBackgroundDark));
            deleteButton.setBackground(getDrawable(R.drawable.round_button_yellow));
        } else {
            deleteMode = !deleteMode;
            isWiggling = false;

            for (PlayerButtonTag el : players) {
                el.clearAnimation();
            }
            deleteButton.setTextColor(getResources().getColor(R.color.colorSignUpButtonText));
            deleteButton.setBackground(getDrawable(R.drawable.round_button));
        }
    }


    public void onClickPlayerR1(View view){
        if(deleteMode) {
            if(topLeftButton.playerUID.equals(myUID)) {
                ref.child("lobby").child(lobbyID).child("tr").child("o").removeValue();
                Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                Log.i(TAG, "onClickPlayerR1: leaderboard activity called line 185");
                startActivity(i);
            }
            ref.child("lobby").child(lobbyID).child("tr").child("o").removeValue();
        }
    }

    public void onClickPlayerR2(View view){
        if(deleteMode) {
            if(topRightButton.playerUID.equals(myUID)) {
                ref.child("lobby").child(lobbyID).child("tr").child("d").removeValue();
                Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                Log.i(TAG, "onClickPlayerR2: leaderboard activity called line 198");
                startActivity(i);
            }
            ref.child("lobby").child(lobbyID).child("tr").child("d").removeValue();
        }
    }

    public void onClickPlayerB1(View view){
        if(deleteMode) {
            if(bottomLeftButton.playerUID.equals(myUID)) {
                ref.child("lobby").child(lobbyID).child("tb").child("d").removeValue();
                Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                Log.i(TAG, "onClickPlayerB1: leaderboard activity called line 210");
                startActivity(i);
            }
            ref.child("lobby").child(lobbyID).child("tb").child("d").removeValue();
        }
    }

    public void onClickPlayerB2(View view){
        if(deleteMode) {
            if(bottomRightButton.playerUID.equals(myUID)) {
                ref.child("lobby").child(lobbyID).child("tb").child("o").removeValue();
                Intent i = new Intent(LobbyActivity.this,LeaderboardActivity.class);
                Log.i(TAG, "onClickPlayerB2: leaderboard activity called line 222");
                startActivity(i);
            }
            ref.child("lobby").child(lobbyID).child("tb").child("o").removeValue();
        }
    }

    /**
     * Set 'PlayerButtonTag' visibility depending if a player exists.
     * Set Red Offense (top left) player as Host if, team red has two active players.
     */
    public void playerArrangement(){

        for (PlayerButtonTag player: players) {
            if(player.playerNickName == null){
                player.setAlpha(0);
                player.setEnabled(false);
                if(player.isHost){
                    if(topLeftButton.isHost){
                        if(topRightButton.playerUID != null){
                            topRightButton.isHost = true;
                        }
                    }
                    player.isHost = false;
                }
            }
            else{
                player.setAlpha(1);
                player.setEnabled(true);
                player.setText(player.playerNickName);
                if(topLeftButton.playerUID != null){
                    topLeftButton.isHost = true;
                }
                if(player.isHost){
                    player.setText("Host\n");
                    player.append(player.playerNickName);
                }
                if(player.isHost && myUID.equals(player.playerUID)){
                    buttonStartGame.setVisibility(View.VISIBLE);
                }
                if(!player.isHost && myUID.equals(player.playerUID)){
                    buttonStartGame.setVisibility(View.GONE);
                }
            }

        }

    }

    public void checkForPlayerChanges(){
        myLobbyChildListener = ref.child("lobby").child(lobbyID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "checkForPlayerChanges-onChildAdded-280:");
                Log.d(TAG, "checkForPlayerChanges-onChildAdded() called with: dataSnapshot = [" + dataSnapshot + "], s = [" + s + "]");
                playerNameQuery(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "checkForPlayerChanges-onChildChanged-287: was called");
                playerNameQuery(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "checkForPlayerChanges-onChildRemoved-293: ");
                playerNameQuery(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        allLobbyChildListener = ref.child("lobby").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "checkForPlayerChanges-onChildAdded-325: snap: " + dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "checkForPlayerChanges-onChildChanged-330: snap: " + dataSnapshot);
                if (dataSnapshot.getKey().equals(lobbyID)) {
                    Log.i(TAG, "onChildChanged: was called");
                    playerNameQuery(dataSnapshot.child(lobbyID));
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "checkForPlayerChanges-onChildRemoved-339: snap: " + dataSnapshot);
                if(dataSnapshot.getKey().equals(lobbyID)){
                    Log.i(TAG, "onChildRemoved: was called");
                    playerNameQuery(dataSnapshot.child(lobbyID));
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildMoved: called");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: called");
            }
        });
        myLobbyValueListener = ref.child("lobby").child(lobbyID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "checkForPlayerChanges-onDataChange: called " + dataSnapshot);
                playerNameQuery(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: called");
            }
        });

        // TODO: 30.10.2019 get rid of all but one lobbyListeners
        testListener = ref.child("lobby").child(lobbyID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void playerNameQuery(final DataSnapshot snapshot) {

        if (topLeftEventListener != null) {
            ref.removeEventListener(topLeftEventListener);
        }

        if (snapshot.child("tr").child("o").exists()) {
            final String UID = snapshot.child("tr").child("o").getValue().toString();
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        topLeftButton.playerUID = UID;
                        topLeftButton.playerNickName = dataSnapshot.child("nickName").getValue().toString();
                        topLeftButton.isHost = true;
                        topRightButton.isHost = false;
                    } else {
                        Log.i(TAG, "onDataChange: went to else");
                        topLeftButton.playerUID = null;
                        topLeftButton.playerNickName = null;
                        topLeftButton.isHost = false;
                        if (!(topRightButton.playerUID == null)) {
                            Log.i(TAG, "onDataChange: topRightButtonPlayer is not null");
                            topRightButton.isHost = true;
                            if (myUID.equals(topRightButton.playerUID)) {
                                Log.i(TAG, "onDataChange: myUID is equal to topRightButtonPlayer UID");
                                buttonStartGame.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    playerArrangement();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            myOwnStatusEventListener = ref.child("lobby").child(lobbyPath.split("/")[0])
                    .child(lobbyPath.split("/")[1]).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    if(!buttonIsPressed.get() && !topLeftButton.isHost) {
                        if (dataSnapshot.getKey().equals(lobbyPath.split("/")[2])) {
                            Intent i;
                            i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                            Log.i(TAG, "onChildRemoved: startActivity pressed from on childRemoved line 440");
                            startActivity(i);
                        }
                    }

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            topLeftButton.playerUID = null;
            topLeftButton.playerNickName = null;
            playerArrangement();
        }

        if (topRightEventListener != null) {
            ref.removeEventListener(topRightEventListener);
        }

        if (snapshot.child("tr").child("d").exists()) {

            final String UID = snapshot.child("tr").child("d").getValue().toString();
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        topRightButton.playerUID = UID;
                        topRightButton.playerNickName = dataSnapshot.child("nickName").getValue(String.class);
                        if (!topLeftButton.isHost && topLeftButton.playerUID == null) {
                            topRightButton.isHost = true;
                            if (myUID.equals(topLeftButton.playerUID)) {
                                buttonStartGame.setVisibility(View.VISIBLE);
                            }
                        }
                        Log.i(TAG, "onDataChange: created myOwnStatusEventListener");
                        myOwnStatusEventListener = ref.child("lobby").child(lobbyPath.split("/")[0]).child(lobbyPath.split("/")[1]).addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                if(!buttonIsPressed.get() && !topRightButton.isHost) {
                                    Log.i(TAG, "onChildRemoved: buttonIsPressed false start activity (leaderboard)");
                                    if (dataSnapshot.getKey().equals(lobbyPath.split("/")[2])) {
                                        Intent i;
                                        i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                                        Log.i(TAG, "onChildRemoved: leaderboard activity called line 505");
                                        startActivity(i);
                                    }
                                }

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        topRightButton.playerUID = null;
                        topRightButton.playerNickName = null;
                        if (topRightButton.isHost) {
                            topRightButton.isHost = false;
                            if (!(topLeftButton.playerUID == null)) {
                                if (myUID.equals(topLeftButton.playerUID)) {
                                    buttonStartGame.setVisibility(View.GONE);
                                }
                            }

                        }
                    }
                    playerArrangement();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            topRightButton.playerUID = null;
            topRightButton.playerNickName = null;
            playerArrangement();
        }

        if (bottomLeftEventListener != null) {
            ref.removeEventListener(bottomLeftEventListener);
        }

        if (snapshot.child("tb").child("d").exists()) {

            final String UID = snapshot.child("tb").child("d").getValue().toString();
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        bottomLeftButton.playerUID = UID;
                        bottomLeftButton.playerNickName = dataSnapshot.child("nickName").getValue().toString();
                    } else {
                        bottomLeftButton.playerUID = null;
                        bottomLeftButton.playerNickName = null;
                    }
                    playerArrangement();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            myOwnStatusEventListener = ref.child("lobby").child(lobbyPath.split("/")[0]).child(lobbyPath.split("/")[1]).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    if(!buttonIsPressed.get()) {
                        if (dataSnapshot.getKey().equals(lobbyPath.split("/")[2])) {
                            Intent i;
                            i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                            Log.i(TAG, "onChildRemoved: leaderboard activity called line 594");
                            startActivity(i);
                        }
                    }

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            bottomLeftButton.playerUID = null;
            bottomLeftButton.playerNickName = null;
            playerArrangement();
        }

        if (bottomRightEventListener != null) {
            ref.removeEventListener(bottomRightEventListener);
        }

        if (snapshot.child("tb").child("o").exists()) {
            final String UID = snapshot.child("tb").child("o").getValue(String.class);
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        bottomRightButton.playerUID = UID;
                        bottomRightButton.playerNickName = dataSnapshot.child("nickName").getValue(String.class);
                    } else {
                        bottomRightButton.playerUID = null;
                        bottomRightButton.playerNickName = null;
                    }
                    playerArrangement();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            myOwnStatusEventListener = ref.child("lobby").child(lobbyPath.split("/")[0]).child(lobbyPath.split("/")[1]).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    if(!buttonIsPressed.get()) {
                        if (dataSnapshot.getKey().equals(lobbyPath.split("/")[2])) {
                            Intent i;
                            i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                            Log.i(TAG, "onChildRemoved: leaderboard activity called line 661");
                            startActivity(i);
                        }
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        } else {
            bottomRightButton.playerUID = null;
            bottomRightButton.playerNickName = null;
            playerArrangement();
        }
    }

    /**
     * If the player isn't the Host of the lobby, the 'Commit' button is invisible.
     */
    public void visibilityCommitButton(){
        // TODO: 31.10.2019 check if this is correct
        Log.i(TAG, "visibilityCommitButton: button = " + buttonStartGame.getVisibility());
        if(topLeftButton.playerUID != null) {
            if (!(myUID.equals(topLeftButton.playerUID)) && topLeftButton.isHost) {
                buttonStartGame.setVisibility(View.GONE);
            }
        }

        if(topRightButton.playerUID != null) {
            if (!(myUID.equals(topRightButton.playerUID)) && topRightButton.isHost) {
                buttonStartGame.setVisibility(View.GONE);
            }
        }

        // this might me unnecessary
//        if(topRightButton.playerUID == null && topLeftButton.playerUID == null){
//            buttonStartGame.setVisibility(View.GONE);
//        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        ref = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        getSharedPreferences("MyPreferences", 0).edit().clear().apply();

        buttonIsPressed = new AtomicBoolean(false);
        lobbyPath = getIntent().getExtras().getString("lobbyPath");
        lobbyID = lobbyPath.split("/")[0];
        myUID = mAuth.getCurrentUser().getUid();
        checkForPlayerChanges();

        topLeftButton = findViewById(R.id.buttonR1);
        topRightButton = findViewById(R.id.buttonR2);
        bottomLeftButton = findViewById(R.id.buttonB1);
        bottomRightButton = findViewById(R.id.buttonB2);
        buttonStartGame = findViewById(R.id.buttonStartGame);
        deleteButton = findViewById(R.id.buttonDelete);

        players.add(topLeftButton);
        players.add(topRightButton);
        players.add(bottomLeftButton);
        players.add(bottomRightButton);

        visibilityCommitButton();
    }
}