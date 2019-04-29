package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {

    boolean isWiggling = false;
    boolean deleteMode = false;
    String lobbyPath;
    DatabaseReference ref;
    private FirebaseAuth mAuth;

    PlayerButtonTag topLeftButton;
    PlayerButtonTag topRightButton;
    PlayerButtonTag bottomLeftButton;
    PlayerButtonTag bottomRightButton;
    List<PlayerButtonTag> players = new ArrayList<>();
    Button deleteButton;
    private static final String TAG = "LobbyActivity";

    @Override
    public void onDestroy() {
        ref.child("lobby").child(lobbyPath).removeValue();
        super.onDestroy();
    }

    @Override
    public void onStop(){
        ref.child("lobby").child(lobbyPath).removeValue();
        finish();
        super.onStop();
    }

    public void onClickReturn(View view){

        ref.child("lobby").child(lobbyPath).removeValue();
        Intent i = new Intent(LobbyActivity.this, LeaderboardActivity.class);
        startActivity(i);
    }

    public void onClickStartGame(View view){

        Intent i = new Intent(LobbyActivity.this, ResultActivity.class);
        i.putExtra("lobbyPath", lobbyPath);
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
        if(deleteMode)
            ref.child("lobby").child(getLobbyID()).child("tr").child("o").removeValue();
    }

    public void onClickPlayerR2(View view){
        if(deleteMode)
            ref.child("lobby").child(getLobbyID()).child("tr").child("d").removeValue();
    }

    public void onClickPlayerB1(View view){
        if(deleteMode)
            ref.child("lobby").child(getLobbyID()).child("tb").child("d").removeValue();
    }

    public void onClickPlayerB2(View view){
        if(deleteMode)
            ref.child("lobby").child(getLobbyID()).child("tb").child("o").removeValue();
    }

    public String myUID(){
        return mAuth.getCurrentUser().getUid();
    }


    public void playerArrangement(){



        for (PlayerButtonTag player: players) {
            if(player.playerNickName == null){
                player.setAlpha(0);
                player.setEnabled(false);
            }
            else{
                player.setAlpha(1);
                player.setEnabled(true);
                player.setText(player.playerNickName);
                if(player.isHost == true){
                    player.setText(player.playerNickName + " Host");
                }
            }

        }

    }

    public void checkForPlayerChanges(){
        ref.child("lobby").child(getLobbyID()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                playerNameQuery(dataSnapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                playerNameQuery(dataSnapshot);


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

                    playerNameQuery(dataSnapshot.child(getLobbyID()));

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getKey() == getLobbyID() ){

                    playerNameQuery(dataSnapshot.child(getLobbyID()));


                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ref.child("lobby").child(getLobbyID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                playerNameQuery(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    ValueEventListener topLeftEventListener;
    ValueEventListener topRightEventListener;
    ValueEventListener bottomLeftEventListener;
    ValueEventListener bottomRightEventListener;

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
                    }
                    else{
                        topLeftButton.playerUID = null;
                        topLeftButton.playerNickName = null;
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
                    }
                    else{
                        topRightButton.playerUID = null;
                        topRightButton.playerNickName = null;
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
    }

    public String getLobbyID(){
        return this.lobbyPath.split("/")[0];
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        ref = FirebaseDatabase.getInstance().getReference();

        this.lobbyPath = getIntent().getExtras().getString("lobbyPath");
        checkForPlayerChanges();

        topLeftButton = findViewById(R.id.buttonR1);
        topRightButton = findViewById(R.id.buttonR2);
        bottomLeftButton = findViewById(R.id.buttonB1);
        bottomRightButton = findViewById(R.id.buttonB2);

        players.add(topLeftButton);
        players.add(topRightButton);
        players.add(bottomLeftButton);
        players.add(bottomRightButton);

        deleteButton = findViewById(R.id.buttonDelete);

    }
}
