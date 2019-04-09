package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

public class GamesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        SpaceNavigationView menuBottomNavigationView = findViewById(R.id.gamesBottomNavigationView);
        menuBottomNavigationView.initWithSaveInstanceState(savedInstanceState);
        menuBottomNavigationView.addSpaceItem(new SpaceItem("GAMES", R.drawable.ic_menu_games_icon));
        menuBottomNavigationView.addSpaceItem(new SpaceItem("STATS", R.drawable.ic_menu_stats_icon));
        menuBottomNavigationView.changeCurrentItem(1);

        menuBottomNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {

            @Override
            public void onCentreButtonClick() {

            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                Intent i = new Intent(GamesActivity.this, MenuFolderActivity.class);
                startActivity(i);

            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }
        });
    }
}
