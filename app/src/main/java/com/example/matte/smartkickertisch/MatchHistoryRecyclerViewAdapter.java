package com.example.matte.smartkickertisch;

import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchHistoryRecyclerViewAdapter extends RecyclerView.Adapter<MatchHistoryViewHolder> {

    private static final String TAG = "MatchHistoryAdapter";
    HashMap<Integer, String> gameMap;
    HashMap<Integer, Game> dataMap;
    private OnItemClickListener mListener;


    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    MatchHistoryRecyclerViewAdapter(HashMap<Integer, String> gameMap) {
        this.gameMap = gameMap;
        dataMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MatchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.match_history_card_layout, viewGroup, false);
        MatchHistoryViewHolder viewHolder = new MatchHistoryViewHolder(view, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MatchHistoryViewHolder matchHistoryViewHolder, int i) {
        Log.i(TAG, "onBindViewHolder: game " + i + " = " + gameMap.get(i));
        dataMap.put(i, new Game(gameMap.get(i), matchHistoryViewHolder));
    }

    @Override
    public int getItemCount() {
        return gameMap.size();
    }
}

class MatchHistoryViewHolder extends RecyclerView.ViewHolder {

    CircleImageView resultCircleImageView;
    TextView scoreTextView;
    TextView dateTextView;
    MaterialCardView materialCardView;
    View winMarker;

    public MatchHistoryViewHolder(@NonNull View itemView, MatchHistoryRecyclerViewAdapter.OnItemClickListener itemClickListener) {
        super(itemView);
        resultCircleImageView = itemView.findViewById(R.id.matchHistoryCardViewResultCircleImageView);
        scoreTextView = itemView.findViewById(R.id.matchHistoryCardViewScoreTextView);
        dateTextView = itemView.findViewById(R.id.matchHistoryCardViewDateTextView);
        materialCardView = itemView.findViewById(R.id.matchHistoryCardView);
        winMarker = itemView.findViewById(R.id.matchHistoryCardViewWinMarkerView);

        itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position);
                }
            }
        });
    }
}
