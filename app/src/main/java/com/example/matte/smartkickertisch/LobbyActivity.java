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

public class LobbyActivity extends AppCompatActivity {

    boolean isWiggling = false;
    boolean deleteMode = false;
    String lobbyPath;
    String deletePath;
    DatabaseReference ref;
    private FirebaseAuth mAuth;
    private boolean hostActivity = true;
    private boolean isStopped = false;

    PlayerButtonTag topLeftButton;
    PlayerButtonTag topRightButton;
    PlayerButtonTag bottomLeftButton;
    PlayerButtonTag bottomRightButton;
    List<PlayerButtonTag> players = new ArrayList<>();
    Button deleteButton;
    Button buttonStartGame;
    boolean buttonIsPressed = false;
    private static final String TAG = "LobbyActivity";
    private String automatedID;


    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: is called");
        if(buttonIsPressed == false){
            super.onDestroy();
            return;
        }
        else{
            ref.child("lobby").child(lobbyPath).removeValue();
        super.onDestroy();
        }

    }

    public void checkHostActivity(ResultActivity caller, boolean leftLobby){
        hostActivity = caller.getHostActivity();
    }

    @Override
    public void onStop(){
        Log.i(TAG, "onStop: is called");
        if(isStopped == false && buttonIsPressed == false) {
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

    public void onClickReturn(View view){
        Log.i(TAG, "onClickReturn: return button was clicked");
        ref.child("lobby").child(lobbyPath).removeValue();
        Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
        startActivity(i);
    }

    public void onClickStartGame(View view){

        if(topLeftButton.playerUID == null && topRightButton.playerUID == null || bottomLeftButton.playerUID == null && bottomRightButton.playerUID == null){
            Toast.makeText(LobbyActivity.this, "At both teams must be atleast one player", Toast.LENGTH_SHORT).show();
            return;
        }
        buttonIsPressed = true;



        Map<String, Object> valueMap = new HashMap<>();
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> playerRedMap = new HashMap<>();
        Map<String, Object> playerBlueMap = new HashMap<>();


        dataMap.put("date",System.currentTimeMillis()/1000L);
        valueMap.put("data", dataMap);
        playerRedMap.put("player1", this.topLeftButton.playerUID);
        playerRedMap.put("player2", this.topRightButton.playerUID);
        playerRedMap.put("score", 0);
        playerBlueMap.put("player3", this.bottomLeftButton.playerUID);
        playerBlueMap.put("player4", this.bottomRightButton.playerUID);
        playerBlueMap.put("score", 0);
        valueMap.put("teamRed", playerRedMap);
        valueMap.put("teamBlue", playerBlueMap);


        String autoID = ref.child("games").push().getKey();
        ref.child("games").child(autoID).updateChildren(valueMap);
//        ref.child("lobby").child(lobbyPath).removeValue();
        final Intent i = new Intent(LobbyActivity.this, ResultActivity.class);
        i.putExtra("lobbyPath", lobbyPath);
        i.putExtra("autoID", autoID);
        i.putExtra("teamRedPlayerOne", this.topLeftButton.playerUID);
        i.putExtra("teamRedPlayerTwo", this.topRightButton.playerUID);
        i.putExtra("teamBluePlayerThree", this.bottomLeftButton.playerUID);
        i.putExtra("teamBluePlayerFour", this.bottomRightButton.playerUID);
        Task t = ref.child("lobby").child(getLobbyID()).removeValue();
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
        Log.i(TAG, "onClickStartGame: " + i);
        Log.i(TAG, "onClickStartGame: players deleted");

        startActivity(i);
    }

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
            isWiggling =false;

            for (PlayerButtonTag el : players) {
                el.clearAnimation();
            }
            deleteButton.setTextColor(getResources().getColor(R.color.colorSignUpButtonText));
            deleteButton.setBackground(getDrawable(R.drawable.round_button));
        }
    }





    public void onClickPlayerR1(View view){
        if(deleteMode) {
            if(topLeftButton.playerUID.equals(myUID())) {
                ref.child("lobby").child(getLobbyID()).child("tr").child("o").removeValue();
                Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                startActivity(i);
            }
            ref.child("lobby").child(getLobbyID()).child("tr").child("o").removeValue();
        }

    }

    public void onClickPlayerR2(View view){
        if(deleteMode) {
            if(topRightButton.playerUID.equals(myUID())) {
                ref.child("lobby").child(getLobbyID()).child("tr").child("d").removeValue();
                Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                startActivity(i);
            }
            ref.child("lobby").child(getLobbyID()).child("tr").child("d").removeValue();
        }
    }

    public void onClickPlayerB1(View view){
        if(deleteMode) {
            if(bottomLeftButton.playerUID.equals(myUID())) {
                ref.child("lobby").child(getLobbyID()).child("tb").child("d").removeValue();
                Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                startActivity(i);
            }
            ref.child("lobby").child(getLobbyID()).child("tb").child("d").removeValue();
        }
    }

    public void onClickPlayerB2(View view){
        if(deleteMode) {
            if(bottomRightButton.playerUID.equals(myUID())) {
                ref.child("lobby").child(getLobbyID()).child("tb").child("o").removeValue();
                Intent i = new Intent(LobbyActivity.this,LeaderboardActivity.class);
                startActivity(i);
            }
            ref.child("lobby").child(getLobbyID()).child("tb").child("o").removeValue();
        }
    }

    public String myUID(){
        Log.i(TAG, "myUID: " + mAuth.getCurrentUser());
        return mAuth.getCurrentUser().getUid();
    }


    public void playerArrangement(){



        for (PlayerButtonTag player: players) {
            if(player.playerNickName == null){
                player.setAlpha(0);
                player.setEnabled(false);
                if(player.isHost == true){
                    if(topLeftButton.isHost == true){
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
                if(player.isHost == true){
                    player.setText(player.playerNickName + " Host");
                }
                if(player.isHost == true && myUID().equals(player.playerUID)){
                    buttonStartGame.setVisibility(View.VISIBLE);
                }
                if(player.isHost == false && myUID().equals(player.playerUID)){
                    buttonStartGame.setVisibility(View.GONE);
                }
            }

        }

    }

    public void checkForPlayerChanges(){
        ref.child("lobby").child(getLobbyID()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                playerNameQuery(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                playerNameQuery(dataSnapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                DatabaseReference dbRefr1 = ref.child("lobby").child("tr").child("o");
//                DatabaseReference dbRefr2 = ref.child("lobby").child("tr").child("d");
//                DatabaseReference dbRefb3 = ref.child("lobby").child("tb").child("d");
//                DatabaseReference dbRefb4 = ref.child("lobby").child("tb").child("o");
//                Log.i(TAG, "onChildRemoved: " + dbRefr1);
//
//                FirebaseDatabase newDatabase = FirebaseDatabase.getInstance();
//
//                Log.i(TAG, "onChildRemoved: " + dataSnapshot.child(getLobbyID()));
                  playerNameQuery(dataSnapshot);
//                Log.i(TAG, "onChildRemoved: child was removed " + ref.child("lobby").child("tr").child("o"));
//                if(dbRefr1.equals(ref.child("lobby").child("tr").child("o")) || dbRefr2.equals(ref.child("lobby").child("tr").child("d"))
//                || dbRefb3.equals(ref.child("lobby").child("tb").child("d")) || dbRefb4.equals(ref.child("lobby").child("tb").child("d"))) {
//                    Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
//                    startActivity(i);
//                }


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        ref.child("lobby").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.getKey() == getLobbyID() ){
                    Log.i(TAG, "onChildChanged: was called");
                    playerNameQuery(dataSnapshot.child(getLobbyID()));

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getKey() == getLobbyID() ){
                    Log.i(TAG, "onChildRemoved: was called");
                    playerNameQuery(dataSnapshot.child(getLobbyID()));


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

        ref.child("lobby").child(getLobbyID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: called " + dataSnapshot);
                playerNameQuery(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: called");
            }
        });


    }
    ValueEventListener topLeftEventListener;
    ValueEventListener topRightEventListener;
    ValueEventListener bottomLeftEventListener;
    ValueEventListener bottomRightEventListener;
    ValueEventListener myOwnStatusEventListener;

    public void playerNameQuery(final DataSnapshot snapshot){

        if(topLeftEventListener != null){
            ref.removeEventListener(topLeftEventListener);
        }

        if(snapshot.child("tr").child("o").exists()){

           final String UID = snapshot.child("tr").child("o").getValue().toString();
           topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        topLeftButton.playerUID = UID;
                        topLeftButton.playerNickName = dataSnapshot.child("nickName").getValue().toString();
                        topLeftButton.isHost = true;
                        topRightButton.isHost = false;
                        Log.i(TAG, "onDataChange: trOffense host: " + topRightButton.isHost);

                        // new code CHECK
//                        if(hostActivity == false){
//                            Intent i;
//                            i = new Intent(LobbyActivity.this, MatchHistoryFragment.class);
//                            startActivity(i);
//                        }
                    }
                    else{
                        Log.i(TAG, "onDataChange: went to else");
                        topLeftButton.playerUID = null;
                        topLeftButton.playerNickName = null;
                        topLeftButton.isHost = false;
                        if(!(topRightButton.playerUID == null)){
                            Log.i(TAG, "onDataChange: topRightButtonPlayer is not null");
                            topRightButton.isHost = true;
                            if(myUID().equals(topRightButton.playerUID)){
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
        }
        else{
            topLeftButton.playerUID = null;
            topLeftButton.playerNickName = null;
            playerArrangement();
        }

     // -----------------------------------------------------------------------------------------
        if(topRightEventListener != null){
            ref.removeEventListener(topRightEventListener);
        }

        if(snapshot.child("tr").child("d").exists()){

            final String UID = snapshot.child("tr").child("d").getValue().toString();
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        topRightButton.playerUID = UID;
                        topRightButton.playerNickName = dataSnapshot.child("nickName").getValue().toString();
                        if(topLeftButton.isHost == false) {
                            if((topLeftButton.playerUID == null)) {
                                topRightButton.isHost = true;
                                if (myUID().equals(topLeftButton.playerUID)) {
                                    buttonStartGame.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                    else{
                        topRightButton.playerUID = null;
                        topRightButton.playerNickName = null;
                        if(topRightButton.isHost == true){
                            topRightButton.isHost = false;
                            if(!(topLeftButton.playerUID == null)){
                                if(myUID().equals(topLeftButton.playerUID)){
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
        }
        else{
            topRightButton.playerUID = null;
            topRightButton.playerNickName = null;
            playerArrangement();
        }

        // -----------------------------------------------------------------------------------------
        if(bottomLeftEventListener != null){
            ref.removeEventListener(bottomLeftEventListener);
        }

        if(snapshot.child("tb").child("d").exists()){

            final String UID = snapshot.child("tb").child("d").getValue().toString();
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        bottomLeftButton.playerUID = UID;
                        bottomLeftButton.playerNickName = dataSnapshot.child("nickName").getValue().toString();
                    }
                    else{
                        bottomLeftButton.playerUID = null;
                        bottomLeftButton.playerNickName = null;
                    }
                    playerArrangement();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            bottomLeftButton.playerUID = null;
            bottomLeftButton.playerNickName = null;
            playerArrangement();
        }

        // -----------------------------------------------------------------------------------------
        if(bottomRightEventListener != null){
            ref.removeEventListener(bottomRightEventListener);
        }

        if(snapshot.child("tb").child("o").exists()){

            final String UID = snapshot.child("tb").child("o").getValue().toString();
            topLeftEventListener = ref.child("users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        bottomRightButton.playerUID = UID;
                        bottomRightButton.playerNickName = dataSnapshot.child("nickName").getValue().toString();
                    }
                    else{
                        bottomRightButton.playerUID = null;
                        bottomRightButton.playerNickName = null;
                    }
                    playerArrangement();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            bottomRightButton.playerUID = null;
            bottomRightButton.playerNickName = null;
            playerArrangement();
        }
 // ----------------------------------------------------------------------
        if(myOwnStatusEventListener != null){
            ref.removeEventListener(myOwnStatusEventListener);
        }
        if(snapshot.child(lobbyPath).exists()){
            final String UID = snapshot.child(lobbyPath).getValue().toString();
            myOwnStatusEventListener = ref.child(lobbyPath).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: key = " + dataSnapshot.getKey() + " value = " + dataSnapshot.getValue());
                    if(dataSnapshot.exists()){
                        Log.i(TAG, "onDataChange: snapshot exists");
                        return;
                    }
                    else{
                        Log.i(TAG, "onDataChange: snapshot doesn`t exist");
                        Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                        startActivity(i);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "onDataChange: snapshot doesn`t exist");
                    Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
                    startActivity(i);

                }
            });

        }
    }

    public String getLobbyID(){
        return this.lobbyPath.split("/")[0];
    }
//
//   @Override
//    protected void onResume() {
//        if(getSharedPreferences("MyPreferences", 0).contains("var1")){
//            topLeftButton.playerUID = getSharedPreferences("MyPreferences", 0).getString("varPlayerR1", null);
//            topRightButton.playerUID = getSharedPreferences("MyPreferences",0).getString("varPlayerR2", null);
//            bottomLeftButton.playerUID = getSharedPreferences("MyPreferences",0).getString("varPlayerB3", null);
//            bottomRightButton.playerUID = getSharedPreferences("MyPreferences",0).getString("varPlayerB4", null);
//            automatedID = getSharedPreferences("MyPreferences", 0).getString("autoID", null);
//            lobbyPath = getSharedPreferences("MyPreferences", 0).getString("var1", null);
//
//            Intent i = new Intent(LobbyActivity.this, ResultActivity.class);
//            i.putExtra("lobbyPath", lobbyPath);
//            i.putExtra("autoID", automatedID);
//            i.putExtra("teamRedPlayerOne", this.topLeftButton.playerUID);
//            i.putExtra("teamRedPlayerTwo", this.topRightButton.playerUID);
//            i.putExtra("teamBluePlayerThree", this.bottomLeftButton.playerUID);
//            i.putExtra("teamBluePlayerFour", this.bottomRightButton.playerUID);
//            Log.i(TAG, "onRestart: is called");
//            Log.i(TAG, "onRestart: start Activity should be called");
//            startActivity(i);
//
//        }
//        super.onResume();
//    }

    public void visibilityCommitButton(){
        Log.i(TAG, "visibilityCommitButton: button = " + buttonStartGame.getVisibility());
        if(topLeftButton.playerUID != null) {
            if (!(myUID().equals(topLeftButton.playerUID)) && topLeftButton.isHost == true) {
                buttonStartGame.setVisibility(View.GONE);
            }
        }
        if(topRightButton.playerUID != null) {
            if (!(myUID().equals(topRightButton.playerUID)) && topRightButton.isHost == true) {
                buttonStartGame.setVisibility(View.GONE);

            }
        }
        if(topRightButton.playerUID == null && topLeftButton.playerUID == null){
            buttonStartGame.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        ref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        this.lobbyPath = getIntent().getExtras().getString("lobbyPath");
        checkForPlayerChanges();

        topLeftButton = findViewById(R.id.buttonR1);
        topRightButton = findViewById(R.id.buttonR2);
        bottomLeftButton = findViewById(R.id.buttonB1);
        bottomRightButton = findViewById(R.id.buttonB2);
        buttonStartGame = findViewById(R.id.buttonStartGame);


        players.add(topLeftButton);
        players.add(topRightButton);
        players.add(bottomLeftButton);
        players.add(bottomRightButton);

        deleteButton = findViewById(R.id.buttonDelete);
        String lobbyPathSplit = lobbyPath;
        deletePath = lobbyPathSplit.split("/")[0];
        visibilityCommitButton();



    }
}
