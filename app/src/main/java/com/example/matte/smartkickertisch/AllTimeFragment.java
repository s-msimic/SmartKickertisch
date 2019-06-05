package com.example.matte.smartkickertisch;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.RBTreeSortedMap;

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.WeakHashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllTimeFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private PieChart allTimeWinLossPieChart;
    private TextView headlineTextView;
    private List<PieEntry> data = new ArrayList<>();
    private PieDataSet pieDataSet;
    private PieData pieData;
    private static final String TAG = "AllTimeFragment";

    public AllTimeFragment() {
    }

    ValueEventListener allTimeGetDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            int played = dataSnapshot.child("playedGames").getValue(int.class);
            int won = dataSnapshot.child("winCounter").getValue(int.class);
            Log.d(TAG, "allTimeGetDataListener: played = " + played + " - won = " + won + " - lost = " + (played - won));
            data.clear();
            data.add(new PieEntry((played - won), (played - won) + " Losses"));
            data.add(new PieEntry(won, won + " Wins"));
            allTimeWinLossPieChart.setVisibility(View.VISIBLE);
            pieDataSet = new PieDataSet(data, "");
            pieDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
            pieDataSet.setValueTextSize(14);
            pieDataSet.setColors(new int[] {R.color.colorMatchHistoryLoss, R.color.colorMatchHistoryWin}, getContext());
            allTimeWinLossPieChart.setUsePercentValues(true);
            allTimeWinLossPieChart.setRotationEnabled(false);
            allTimeWinLossPieChart.setHoleRadius(0);
            allTimeWinLossPieChart.setTransparentCircleRadius(0);
            allTimeWinLossPieChart.animateY(1200, Easing.EaseInOutCirc);
            pieData = new PieData(pieDataSet);
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
        headlineTextView = view.findViewById(R.id.allTimeHeadlineTextView);
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
