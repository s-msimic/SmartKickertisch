package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.Objects;

public class LeaderboardActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private static final String TAG = "LeaderboardActivity";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView spinnerTextView;
    private TextView scoreTextView;
    private String lobbyPathForRecentGameCheck;
    private String fullLobyPath;
    private String playerR1;
    private String playerR2;
    private String playerB3;
    private String playerB4;
    private String autoID;


    public void checkForRecentGame(){
//        getSharedPreferences("MyPreferences", 0).edit().clear().apply();
        Log.i(TAG, "checkForRecentGame: run into checkForRecentGame");
        SharedPreferences preferences = this.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.apply();
        if(getSharedPreferences("MyPreferences", 0).contains("var1")) {
//            if(getSharedPreferences("MyPreferences", 0) == null)
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(adapter);
        spinnerTextView = findViewById(R.id.spinnerTextView);
        scoreTextView = findViewById(R.id.leaderboardScoreTextView);

        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"ELO", "Wins", "Games"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);

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
        spinnerTextView.setText("Ranking sorted by: " + parent.getItemAtPosition(position));

//        switch (position) {
//            case 1:
//                scoreTextView.setText("ELO Rating: 2000");
//                break;
//            case 2:
//                scoreTextView.setText("Games Won: 50");
//                break;
//            case 3:
//                scoreTextView.setText("Games played: 100");
//                break;
//        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Nothing selected", Toast.LENGTH_LONG).show();
    }
}

