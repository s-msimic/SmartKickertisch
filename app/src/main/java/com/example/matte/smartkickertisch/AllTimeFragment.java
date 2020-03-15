package com.example.matte.smartkickertisch;


import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllTimeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private PieChart allTimeWinLossPieChart;
    private List<PieEntry> data = new ArrayList<>();
    private TextView bestWinTextView;
    private TextView worstLossTextView;
    private static final String TAG = "AllTimeFragment";

    public AllTimeFragment() { }

    ValueEventListener allTimeGetDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.hasChild("playedGames") && dataSnapshot.hasChild("winCounter")) {
                int played = dataSnapshot.child("playedGames").getValue(int.class);
                int won = dataSnapshot.child("winCounter").getValue(int.class);
                Log.d(TAG, "allTimeGetDataListener: played = " + played + " - won = " + won + " - lost = " + (played - won));
                data.clear();
                data.add(new PieEntry((played - won), (played - won) + " Losses"));
                data.add(new PieEntry(won, won + " Wins"));
                allTimeWinLossPieChart.setVisibility(View.VISIBLE);
                PieDataSet pieDataSet = new PieDataSet(data, "");
                pieDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                pieDataSet.setValueTextSize(14);
                pieDataSet.setColors(new int[]{R.color.colorMatchHistoryLoss, R.color.colorMatchHistoryWin}, getContext());
                allTimeWinLossPieChart.setUsePercentValues(true);
                allTimeWinLossPieChart.setRotationEnabled(false);
                allTimeWinLossPieChart.setHoleRadius(0);
                allTimeWinLossPieChart.setTransparentCircleRadius(0);
                allTimeWinLossPieChart.animateY(1200, Easing.EaseInOutCirc);
                PieData pieData = new PieData(pieDataSet);
                pieData.setValueTextColor(getResources().getColor(R.color.colorWhite));
                Legend legend = allTimeWinLossPieChart.getLegend();
                allTimeWinLossPieChart.setEntryLabelTextSize(10);
                legend.setEnabled(false);
                legend.setTextColor(Color.WHITE);
                legend.setForm(Legend.LegendForm.CIRCLE);
                Description description = allTimeWinLossPieChart.getDescription();
                description.setEnabled(false);
                allTimeWinLossPieChart.setData(pieData);
                allTimeWinLossPieChart.invalidate();
                if (dataSnapshot.child("bestWin").child("score").exists()) {
                    bestWinTextView.append(dataSnapshot.child("bestWin").child("score").getValue(Long.class).toString());
                }
                if (dataSnapshot.child("worstLoss").child("score").exists()) {
                    worstLossTextView.append(dataSnapshot.child("worstLoss").child("score").getValue(Long.class) + ":10");
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_time, container, false);
        allTimeWinLossPieChart = view.findViewById(R.id.allTimeFragmentPieChart);
        TextView headlineTextView = view.findViewById(R.id.allTimeHeadlineTextView);
        bestWinTextView = view.findViewById(R.id.allTimeFragmentBestWinTextView);
        worstLossTextView = view.findViewById(R.id.allTimeFragmentWorstLossTextView);
        if (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null)
            headlineTextView.setText(auth.getCurrentUser().getDisplayName() + "'s Statistics");
        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "onCreateView: data can be retrieved");
            database.getReference("users").child(auth.getCurrentUser().getUid()).child("data").addListenerForSingleValueEvent(allTimeGetDataListener);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }
}