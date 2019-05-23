package com.example.matte.smartkickertisch;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchHistoryRecyclerViewAdapter extends RecyclerView.Adapter<MatchHistoryViewHolder> {

    private static final String TAG = "MatchHistoryAdapter";

    @NonNull
    @Override
    public MatchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.match_history_card_layout, viewGroup, false);
        MatchHistoryViewHolder viewHolder = new MatchHistoryViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchHistoryViewHolder matchHistoryViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

class MatchHistoryViewHolder extends RecyclerView.ViewHolder {

    CircleImageView resultCircleImageView;
    TextView scoreTextView;
    TextView dateTextView;

    public MatchHistoryViewHolder(@NonNull View itemView) {
        super(itemView);
        resultCircleImageView = itemView.findViewById(R.id.matchHistoryCardViewResultCircleImageView);
        scoreTextView = itemView.findViewById(R.id.matchHistoryCardViewScoreTextView);
        dateTextView = itemView.findViewById(R.id.matchHistoryCardViewDateTextView);
    }
}
