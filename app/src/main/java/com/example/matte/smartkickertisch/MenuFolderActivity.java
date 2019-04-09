package com.example.matte.smartkickertisch;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.Objects;

public class MenuFolderActivity extends Activity {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;

    public void onClickLogOut(View view){
        mAuth.signOut();
        Intent i = new Intent(MenuFolderActivity.this,WelcomeActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_folder);

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
                Toast.makeText(MenuFolderActivity.this, "This opens the QR-Code scanner", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(MenuFolderActivity.this, itemIndex + " this is a test " + itemName, Toast.LENGTH_SHORT).show();
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
}
