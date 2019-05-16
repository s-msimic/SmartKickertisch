package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LeaderboardActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static String WINS = "data/winCounter";
    public static String GAMES = "data/playedGames";
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private static final String TAG = "LeaderboardActivity";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView spinnerTextView;
    private ArrayList<String> nicknames = new ArrayList<>();
    private ArrayList<String> scores = new ArrayList<>();
    private int countBestPlayers = 7;
    private Spinner dropdown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
//        Query wins = FirebaseDatabase.getInstance().getReference("users")
//                .orderByChild(WINS)
//                .limitToLast(countBestPlayers);
//
//        wins.addListenerForSingleValueEvent(vel);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        spinnerTextView = findViewById(R.id.spinnerTextView);


        dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Wins", "Games"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(arrayAdapter);
        adapter = new RecyclerViewAdapter(nicknames, scores, countBestPlayers, dropdown.getSelectedItemPosition());
        dropdown.setOnItemSelectedListener(itemClickListener);


        final SpaceNavigationView menuBottomNavigationView = findViewById(R.id.menuBottomNavigationView);
        menuBottomNavigationView.initWithSaveInstanceState(savedInstanceState);
        menuBottomNavigationView.addSpaceItem(new SpaceItem("RANKING", R.drawable.ic_leaderboard_icon));
        menuBottomNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_menu_stats_icon));

        menuBottomNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {

            /**
             * function which opens the QR-Code scanner when the middle "football" button is pressed
             */
            @Override
            public void onCentreButtonClick() {
                IntentIntegrator integrator = new IntentIntegrator(LeaderboardActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }

            /**
             * function to perform a action (here to change the screen) if a element is clicked, which currently isn't active
             * @param itemIndex index of the element, item on the far left is '0' then ascending
             * @param itemName name of the clicked element
             */
            @Override
            public void onItemClick(int itemIndex, String itemName) {
                Intent i = new Intent(LeaderboardActivity.this, ProfileActivity.class);
                startActivity(i);
            }

            /**
             * fucntion to perform a action if current active element is clicked (here no action is done)
             * @param itemIndex index of the element, item on the far left is '0' then ascending
             * @param itemName name of the clicked element
             */
            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }


        });

//        myRef.child("users").child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                System.out.println(dataSnapshot.getChildrenCount());
//                if (dataSnapshot.getChildrenCount() == 0) {
//                    System.out.println("test " + Objects.requireNonNull(dataSnapshot.getValue()).toString());
//                    System.out.println();
//                    System.out.println("s= " + s);
//                }
//            }

//        });
    }

    AdapterView.OnItemSelectedListener itemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            switch (position){
                case 0:
                    Log.i(TAG, "onItemSelected OnItemSelectedListener: posi " + position);
                    Query wins = FirebaseDatabase.getInstance().getReference("users")
                            .orderByChild(WINS)
                            .limitToLast(countBestPlayers);
                    wins.addListenerForSingleValueEvent(vel);
                    spinnerTextView.setText("Sort by: WINS");
                    break;
                case 1:
                    Log.i(TAG, "onItemSelected OnItemSelectedListener: posi " + position);
                    Query games = FirebaseDatabase.getInstance().getReference("users")
                            .orderByChild(GAMES)
                            .limitToLast(countBestPlayers);
                    games.addListenerForSingleValueEvent(vel);
                    spinnerTextView.setText("Sort by: GAMES PLAYED");
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            nicknames.clear();
            scores.clear();
            Log.i(TAG, "onDataChange: snap: Key = " + dataSnapshot.getKey() + " Value = " + dataSnapshot.getValue());
            for (DataSnapshot snap : dataSnapshot.getChildren()) {
//                Log.i(TAG, "onDataChange: playerCount = " + snap.getChildrenCount());
//                Log.i(TAG, "onDataChange: playerCount = " + database.getReference("players").count);
                Log.i(TAG, "onDataChange: child: Key = " + snap.getKey() + " Value = " + snap.getValue());
                Log.i(TAG, "onDataChange: nickname: " + Objects.requireNonNull(snap.child("nickName").getValue()).toString());
                Log.i(TAG, "onDataChange: wins: " + Objects.requireNonNull(snap.child(WINS).getValue()).toString());
                Log.i(TAG, "onDataChange: games: " + Objects.requireNonNull(snap.child(GAMES).getValue()).toString());
                nicknames.add(Objects.requireNonNull(snap.child("nickName").getValue()).toString());
                if (dropdown.getSelectedItemPosition() == 0)
                    scores.add(Objects.requireNonNull(snap.child(WINS).getValue()).toString());
                else scores.add(Objects.requireNonNull(snap.child(GAMES).getValue()).toString());
                Log.i(TAG, "onDataChange: list " + nicknames.toString());
            }
            Collections.reverse(nicknames);
            Collections.reverse(scores);
            adapter = new RecyclerViewAdapter(nicknames, scores, countBestPlayers, dropdown.getSelectedItemPosition());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "You cancelled the scan", Toast.LENGTH_LONG).show();
            }
            else{
                if(result.getContents().matches("(sk[0-9]+)\\/((tb)|(tr))\\/((o)|(d))")) {
                    // go to new window from here after scan was successful

                    DatabaseReference ref;
                    ref = FirebaseDatabase.getInstance().getReference();

                    Log.i(TAG, "onActivityResult: UID = " + Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                    ref.child("lobby").child(result.getContents()).setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Log.i(TAG, "onActivityResult: Path = " + ref.child("lobby").child(result.getContents()).toString());

                    Intent i = new Intent(LeaderboardActivity.this, LobbyActivity.class);
                    i.putExtra("lobbyPath", result.getContents());
                    startActivity(i);
                }
                else{
                    // QR Code is none of HAW - Landshut
                    Toast.makeText(this, "Scanned QR Code is not viable", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        spinnerTextView.setText("Ranking sorted by: " + parent.getItemAtPosition(position));
//
//        Log.i(TAG, "onItemSelected: Override " + position);
//        switch (position){
//            case 0:
//                Query wins = FirebaseDatabase.getInstance().getReference("users")
//                        .orderByChild(WINS)
//                        .limitToLast(countBestPlayers);
//                wins.addListenerForSingleValueEvent(vel);
//                break;
//            case 1:
//                Query games = FirebaseDatabase.getInstance().getReference("users")
//                        .orderByChild(GAMES)
//                        .limitToLast(countBestPlayers);
//                games.addListenerForSingleValueEvent(vel);
//                break;
//        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Nothing selected", Toast.LENGTH_LONG).show();
    }
}

