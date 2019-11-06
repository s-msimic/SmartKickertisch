package com.example.matte.smartkickertisch;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchHistoryFragment extends Fragment {
    FirebaseAuth mAuth;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    MatchHistoryRecyclerViewAdapter recyclerViewAdapter;
    private static final String TAG = "MatchHistoryFragment";

    public MatchHistoryFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: called");
        mAuth = FirebaseAuth.getInstance();
        Query games = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                // TODO: 31.10.2019 check if here orderByValue works/is better!
                .orderByChild("finishedGames");

        games.addListenerForSingleValueEvent(lastGamesListener);
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

            HashMap<Integer, String> gamesMap = new HashMap<>();
            int position = (int) dataSnapshot.child("finishedGames").getChildrenCount() - 1;
            Log.i(TAG, "onDataChange: Key = " + dataSnapshot.getKey());
            Log.i(TAG, "onDataChange: Value = " + dataSnapshot.getValue());

            for (DataSnapshot game : dataSnapshot.child("finishedGames").getChildren()) {
                Log.i(TAG, "onDataChange: gameKey = " + game.getKey());
                Log.i(TAG, "onDataChange: gameValue = " + game.getValue());
                gamesMap.put(position--, game.getKey());
            }
            Log.i(TAG, "onDataChange: gamesMap = " + gamesMap.toString());
            recyclerViewAdapter = new MatchHistoryRecyclerViewAdapter(gamesMap);
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerViewAdapter.setOnItemClickListener(position1 -> {
                Bundle args = new Bundle();
                try {
                    args.putInt("blueScore", recyclerViewAdapter.dataMap.get(position1).getBlueTeamScore());
                    args.putInt("redScore", recyclerViewAdapter.dataMap.get(position1).getRedTeamScore());
                    args.putLong("date", recyclerViewAdapter.dataMap.get(position1).getGameDate());
                    if (recyclerViewAdapter.dataMap.get(position1).getBlueTeamDefenseID() != null) {
                        args.putString("blueDefense", recyclerViewAdapter.dataMap.get(position1).getBlueTeamDefenseID());
                    }
                    if (recyclerViewAdapter.dataMap.get(position1).getBlueTeamOffenseID() != null) {
                        args.putString("blueOffense", recyclerViewAdapter.dataMap.get(position1).getBlueTeamOffenseID());
                    }
                    if (recyclerViewAdapter.dataMap.get(position1).getRedTeamDefenseID() != null) {
                        args.putString("redDefense", recyclerViewAdapter.dataMap.get(position1).getRedTeamDefenseID());
                    }
                    if (recyclerViewAdapter.dataMap.get(position1).getRedTeamOffenseID() != null) {
                        args.putString("redOffense", recyclerViewAdapter.dataMap.get(position1).getRedTeamOffenseID());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "onItemClick: " + recyclerViewAdapter.dataMap.get(position1).getBlueTeamScore() + ":" + recyclerViewAdapter.dataMap.get(position1).getRedTeamScore());
                MatchHistoryFragment.this.setArguments(args);
                MatchHistoryGameDialog dialog = new MatchHistoryGameDialog();
                dialog.setArguments(args);
                dialog.show(requireActivity().getSupportFragmentManager(), position1 + "");
            });

            Log.i(TAG, "onDataChange: finished");
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

}