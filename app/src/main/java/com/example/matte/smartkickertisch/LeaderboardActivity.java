package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.*;

public class LeaderboardActivity extends Activity {

    private static final String WINS = "data/winCounter";
    private static final String GAMES = "data/playedGames";
    private static final String TAG = "LeaderboardActivity";
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private TextView spinnerTextView;
    private Spinner dropdown;
    private int countBestPlayers = 15;
    private HashMap<Integer, User> userList = new HashMap<>();
    private ProgressBar progressBar;
    private Query bestPlayers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        progressBar = findViewById(R.id.leaderboardProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.leaderBoardRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        spinnerTextView = findViewById(R.id.spinnerTextView);
        dropdown = findViewById(R.id.spinner);
        SpaceNavigationView menuBottomNavigationView = findViewById(R.id.menuBottomNavigationView);
        String[] items = new String[]{"Wins", "Games"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(arrayAdapter);
        dropdown.setOnItemSelectedListener(itemClickListener);
        menuBottomNavigationView.initWithSaveInstanceState(savedInstanceState);
        menuBottomNavigationView.addSpaceItem(new SpaceItem("RANKING", R.drawable.ic_leaderboard_icon));
        menuBottomNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_profile_icon));

        menuBottomNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {

            /**
             * Function which opens the QR-Code scanner when the middle "football" button is pressed
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
            public void onItemReselected(int itemIndex, String itemName) { }
        });
    }

    @Override
    protected void onStart(){
        Log.d(TAG, "onStart-106");
        if (getSharedPreferences("MyPreferences", 0).contains("gameID")) {
            Log.i(TAG, "onStart: gameID= " + getSharedPreferences("MyPreferences", 0).getString("gameID", null));
            Intent i = new Intent(LeaderboardActivity.this, ResultActivity.class);
            startActivity(i);
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy-116: removed listener");
        bestPlayers.removeEventListener(vel);
        super.onDestroy();
    }

    AdapterView.OnItemSelectedListener itemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "onItemSelected OnItemSelectedListener: posi " + position);

            switch (position) {
                case 0:
                    bestPlayers = getLeaderboardPlayers(WINS);
                    spinnerTextView.setText(getString(R.string.sort_by_wins));
                    break;
                case 1:
                    bestPlayers = getLeaderboardPlayers(GAMES);
                    spinnerTextView.setText(getString(R.string.sort_by_games));
                    break;
            }
            bestPlayers.addValueEventListener(vel);
        }

        /**
         * Get first @countBestPlayers, which are best in selected category
         * @param sortingIdentifier specifies according to which criteria leaderboard is sorted
         * @return Query of best players
         */
        private Query getLeaderboardPlayers(String sortingIdentifier) {
            return FirebaseDatabase.getInstance().getReference("users")
                    .orderByChild(sortingIdentifier)
                    .limitToLast(countBestPlayers);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.i(TAG, "onNothingSelected: nothing selected");
        }
    };
    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d(TAG, "vel-onDataChange-203: userList: " + userList.size());
            userList.clear();
            int posi = countBestPlayers;
            String uid;

            // FIXME: 28.10.2019 deleted players might crash the app
            for (DataSnapshot snap : dataSnapshot.getChildren()) {
//                Log.i(TAG, "onDataChange: playerCount = " + snap.getChildrenCount());
//                Log.i(TAG, "onDataChange: playerCount = " + database.getReference("players").count);
                Log.i(TAG, "vel-onDataChange: child: Key = " + snap.getKey() + " Value = " + snap.getValue());
                uid = "users/" + snap.getKey() + "/profileImage.jpg";
                if (snap.child("nickName").getValue() != null && snap.child(WINS).getValue() != null && snap.child(GAMES).getValue() != null) {
                    Log.d(TAG, "vel-onDataChange: values are not null");
                    userList.put(posi, new User(snap.getKey(), snap.child("nickName").getValue().toString(),
                            posi--, snap.child(WINS).getValue().toString(), snap.child(GAMES).getValue().toString()));
                }
                // TODO: 31.10.2019 fix onComplete/onFailure
                storageRef.child(uid).getDownloadUrl()
                        .addOnFailureListener(e -> Log.e(TAG, "vel-onDataChange-onFailure: getDownloadUrl failed for user ", e))
                        .addOnSuccessListener(uri -> Log.i(TAG, "vel-onDataChange-onSuccess: uri successfully retrieved = " + uri.getPath()))
                        .addOnCompleteListener(task -> {
                            if (task.getException() == null) {
                                for (User el : userList.values()) {
                                    if (task.getResult().getPath().contains(el.getUid())) {
                                        el.setProfilePicture(task.getResult());
                                        Log.i(TAG, "vel-onDataChange-onComplete: added uri to user = " + el.toString());
                                    }
                                }
                            }

                            if (userList.size() == countBestPlayers) {
                                adapter = new RecyclerViewAdapter(userList, countBestPlayers, dropdown.getSelectedItemPosition());
                                recyclerView.setAdapter(adapter);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
            } else {
                if (result.getContents().matches("(sk[0-9]+)\\/((tb)|(tr))\\/((o)|(d))")) {
                    // go to new window from here after scan was successful

                    Log.i(TAG, "onActivityResult: result.getContents = " + result.getContents());
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    database.child("lobby").child(result.getContents()).setValue(mAuth.getCurrentUser().getUid())
                            .addOnSuccessListener(command -> {
                                Intent i = new Intent(LeaderboardActivity.this, LobbyActivity.class);
                                i.putExtra("lobbyPath", result.getContents());
                                startActivity(i);
                            })
                            .addOnFailureListener(command -> Log.d(TAG, "onActivityResult-onFailure-221: " + command.getMessage()));
                }
                else {
                    // QR Code is none of HAW-Landshut
                    Toast.makeText(this, "Scanned QR Code is not valid", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}