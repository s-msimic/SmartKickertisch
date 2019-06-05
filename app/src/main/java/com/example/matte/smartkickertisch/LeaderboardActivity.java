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

    public static String WINS = "data/winCounter";
    public static String GAMES = "data/playedGames";
    private static final String TAG = "LeaderboardActivity";
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView spinnerTextView;
    private Spinner dropdown;
    private int countBestPlayers = 15;
    private HashMap<Integer, User> userList = new HashMap<>();
    private ProgressBar progressBar;
    private SpaceNavigationView menuBottomNavigationView;
    private String lobbyPathForRecentGameCheck;
    private String fullLobyPath;
    private String playerR1;
    private String playerR2;
    private String playerB3;
    private String playerB4;
    private String autoID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        progressBar = findViewById(R.id.leaderboardProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.leaderBoardRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        spinnerTextView = findViewById(R.id.spinnerTextView);
        dropdown = findViewById(R.id.spinner);
        menuBottomNavigationView = findViewById(R.id.menuBottomNavigationView);
        String[] items = new String[]{"Wins", "Games"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(arrayAdapter);
        dropdown.setOnItemSelectedListener(itemClickListener);
        menuBottomNavigationView.initWithSaveInstanceState(savedInstanceState);
        menuBottomNavigationView.addSpaceItem(new SpaceItem("RANKING", R.drawable.ic_leaderboard_icon));
        menuBottomNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_profile_icon));

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
    }

    public void checkForRecentGame(){
        Log.i(TAG, "checkForRecentGame: run into checkForRecentGame");
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.apply();
        if(getSharedPreferences("MyPreferences", 0).contains("var1")) {
            Log.i(TAG, "checkForRecentGame: nullcheck true");
            Log.i(TAG, "checkForRecentGame: " + getSharedPreferences("MyPreferences", 0).getString("var1", null));
            if ((getSharedPreferences("MyPreferences", 0).getString("var1", null).contains("sk"))) {
                Log.i(TAG, "checkForRecentGame: ran into else checkForRecentGame");
                Log.i(TAG, "checkForRecentGame: " + getSharedPreferences("MyPreferences", 0).toString());
                Log.i(TAG, "checkForRecentGame: " + getSharedPreferences("MyPreferences", 0).getString("var1", "nothing there"));
                lobbyPathForRecentGameCheck = getSharedPreferences("MyPreferences", 0).getString("var1", null);
                playerR1 = getSharedPreferences("MyPreferences", 0).getString("varPlayerR1", null);
                playerR2 = getSharedPreferences("MyPreferences",0).getString("varPlayerR2", null);
                playerB3 = getSharedPreferences("MyPreferences",0).getString("varPlayerB3", null);
                playerB4 = getSharedPreferences("MyPreferences",0).getString("varPlayerB4", null);
                autoID = getSharedPreferences("MyPreferences", 0).getString("autoID", null);

            } else {
                lobbyPathForRecentGameCheck = null;

            }
        }

    }

    @Override
    protected void onStart(){

        checkForRecentGame();
        Log.i(TAG, "onCreate: "+ lobbyPathForRecentGameCheck);
        if(!(lobbyPathForRecentGameCheck == null)){
            fullLobyPath = getSharedPreferences("MyPreferences",0).getString("var2", null);
            Log.i(TAG, "onStart: " + fullLobyPath +" is the full lobby path");
            Intent i;
            i = new Intent(LeaderboardActivity.this, ResultActivity.class);
            i.putExtra("lobbyPath", fullLobyPath);
            i.putExtra("autoID", autoID);
            startActivity(i);

        }

        super.onStart();
    }

    AdapterView.OnItemSelectedListener itemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.i(TAG, "onItemSelected OnItemSelectedListener: posi " + position);

            switch (position) {
                case 0:
                    Query wins = FirebaseDatabase.getInstance().getReference("users")
                            .orderByChild(WINS)
                            .limitToLast(countBestPlayers);
                    wins.addValueEventListener(vel);
                    spinnerTextView.setText(getString(R.string.sort_by_wins));
                    break;
                case 1:
                    Query games = FirebaseDatabase.getInstance().getReference("users")
                            .orderByChild(GAMES)
                            .limitToLast(countBestPlayers);
                    games.addValueEventListener(vel);
                    spinnerTextView.setText(getString(R.string.sort_by_games));
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.i(TAG, "onNothingSelected: nothing selected");
        }
    };

    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userList.clear();
            int posi = countBestPlayers;
            String uid;

            for (DataSnapshot snap : dataSnapshot.getChildren()) {
//                Log.i(TAG, "onDataChange: playerCount = " + snap.getChildrenCount());
//                Log.i(TAG, "onDataChange: playerCount = " + database.getReference("players").count);
                Log.i(TAG, "onDataChange: child: Key = " + snap.getKey() + " Value = " + snap.getValue());
                uid = "users/" + snap.getKey() + "/profileImage.jpg";
                if (snap.child("nickName").getValue() != null && snap.child(WINS).getValue() != null && snap.child(GAMES).getValue() != null) {
                    Log.d(TAG, "onDataChange: values are not null");
                    userList.put(posi, new User(snap.getKey(), snap.child("nickName").getValue().toString(),
                            posi--, snap.child(WINS).getValue().toString(), snap.child(GAMES).getValue().toString()));
                }
                storageRef.child(uid).getDownloadUrl()
                        .addOnFailureListener(e -> Log.e(TAG, "onDataChange: getDownloadUrl failed for user ", e))
                        .addOnSuccessListener(uri -> Log.i(TAG, "onSuccess: uri successfully retrieved = " + uri.getPath()))
                        .addOnCompleteListener(task -> {
                            if (task.getException() == null) {

                                for (User el : userList.values()) {
                                    if (task.getResult().getPath().contains(el.getUid())) {
                                        el.setProfilePicture(task.getResult());
                                        Log.i(TAG, "onComplete: added uri to user = " + el.toString());
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
}