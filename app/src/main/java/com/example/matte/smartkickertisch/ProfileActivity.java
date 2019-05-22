package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SpaceNavigationView menuBottomNavigationView = findViewById(R.id.statisticsBottomNavigationView);
        menuBottomNavigationView.initWithSaveInstanceState(savedInstanceState);
        menuBottomNavigationView.addSpaceItem(new SpaceItem("RANKING", R.drawable.ic_leaderboard_icon));
        menuBottomNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_profile_icon));
        menuBottomNavigationView.changeCurrentItem(1);

        menuBottomNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {

            @Override
            public void onCentreButtonClick() {
                IntentIntegrator integrator = new IntentIntegrator(ProfileActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                Intent i = new Intent(ProfileActivity.this, LeaderboardActivity.class);
                startActivity(i);
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }
        });

        FragmentPagerItems pages = new FragmentPagerItems(this);
        pages.add(FragmentPagerItem.of(getString(R.string.all_time_stats), AllTimeFragment.class));
        pages.add(FragmentPagerItem.of(getString(R.string.match_history), MatchHistoryFragment.class));
        pages.add(FragmentPagerItem.of(getString(R.string.settings), SettingsFragment.class));

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), pages);

        ViewPager viewPager = findViewById(R.id.statisticsViewPager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                if (v != 0) {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        SmartTabLayout viewPagerTab = findViewById(R.id.statisticsSmartTabLayout);
        viewPagerTab.setViewPager(viewPager);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scan", Toast.LENGTH_LONG).show();
            } else {
                if (result.getContents().matches("(sk[0-9]+)\\/((tb)|(tr))\\/((o)|(d))")) {
                    // go to new window from here after scan was successful

                    DatabaseReference ref;
                    ref = FirebaseDatabase.getInstance().getReference();


                    ref.child("lobby").child(result.getContents()).setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    Intent i = new Intent(ProfileActivity.this, LobbyActivity.class);
                    i.putExtra("lobbyPath", result.getContents());
                    startActivity(i);
                } else {
                    // QR Code is none of HAW - Landshut
                    Toast.makeText(this, "Scanned QR Code is not viable", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
