package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.firebase.database.*;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

public class MenuFolderActivity extends Activity {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    public void onClickLogOut(View view) {
        mAuth.signOut();
        Intent i = new Intent(MenuFolderActivity.this, WelcomeActivity.class);
        startActivity(i);
    }

    public void onClickScan(View view){
        Intent i = new Intent(MenuFolderActivity.this,setupGameActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_folder);

//        scn_btn = findViewById(R.id.scan_btn);
//        final Activity activity = this;
//        scn_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                IntentIntegrator integrator = new IntentIntegrator(activity);
//                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
//                integrator.setPrompt("just scan the kicker side you want to play");
//                integrator.setCameraId(0);
//                integrator.setBeepEnabled(false);
//                integrator.setBarcodeImageEnabled(false);
//                integrator.initiateScan();
//            }
//        });

        final SpaceNavigationView menuBottomNavigationView = findViewById(R.id.menuBottomNavigationView);
        menuBottomNavigationView.initWithSaveInstanceState(savedInstanceState);
        menuBottomNavigationView.addSpaceItem(new SpaceItem("GAMES", R.drawable.ic_menu_games_icon));
        menuBottomNavigationView.addSpaceItem(new SpaceItem("STATS", R.drawable.ic_menu_stats_icon));

        menuBottomNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {

            /**
             * function which opens the QR-Code scanner when the middle "football" button is pressed
             */
            @Override
            public void onCentreButtonClick() {
                IntentIntegrator integrator = new IntentIntegrator(MenuFolderActivity.this);
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

                Intent i = new Intent(MenuFolderActivity.this, GamesActivity.class);
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


                    ref.child("lobby").child(result.getContents()).setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());



//                    mAuth.getCurrentUser().getUid();

                    Intent i = new Intent(MenuFolderActivity.this, setupGameActivity.class);
                    i.putExtra("lobbyPath", result.getContents());
                    startActivity(i);
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
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

    };
}
