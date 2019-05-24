package com.example.matte.smartkickertisch;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchHistoryFragment extends Fragment {

    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter recyclerViewAdapter;
    private static final String TAG = "MatchHistoryFragment";

    public MatchHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long l = System.currentTimeMillis();
        Log.i(TAG, "onCreate: called");
        mAuth = FirebaseAuth.getInstance();
        Query games = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .orderByChild("finishedGames");

        games.addValueEventListener(lastGamesListener);
        Log.i(TAG, "onCreate: finished");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView: called");
        View view = inflater.inflate(R.layout.fragment_match_history, container, false);
        recyclerView = view.findViewById(R.id.matchHistoryRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    ValueEventListener lastGamesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "onDataChange: Key = " + dataSnapshot.getKey());
            Log.i(TAG, "onDataChange: Value = " + dataSnapshot.getValue());

            for (DataSnapshot game : dataSnapshot.child("finishedGames").getChildren()) {
                Log.i(TAG, "onDataChange: gameKey = " + game.getKey());
                Log.i(TAG, "onDataChange: gameValue = " + game.getValue());
            }
            Log.i(TAG, "onDataChange: finished");
            Log.i(TAG, "onDataChange: timeAgoString1 = " + DateUtils.getRelativeTimeSpanString(1551687968L * 1000, 1558688968 , DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            Log.i(TAG, "onDataChange: timeAgoString2 = " + DateUtils.getRelativeTimeSpanString(1558340968L * 1000, 1558688968 , DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            Log.i(TAG, "onDataChange: timeAgoString3 = " + DateUtils.getRelativeTimeSpanString(1558310400L * 1000, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

}
