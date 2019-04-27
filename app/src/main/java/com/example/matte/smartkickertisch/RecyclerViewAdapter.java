package com.example.matte.smartkickertisch;

import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> positions = new ArrayList<>();
    private ArrayList<CircleImageView> profilePictures = new ArrayList<>();
    private ArrayList<String> nicknames = new ArrayList<>();
    private ArrayList<String> scores = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.positionTextView.setText((i + 1) + ".");
        viewHolder.profilePictureImageView.setImageResource(R.drawable.profile_picture_preview);
        viewHolder.nicknameTextView.setText("Nickname");
        viewHolder.scoreTextView.setText("Games played: 100");
    }

    @Override
    public int getItemCount() {
        return 20;
    }
}

class ViewHolder extends RecyclerView.ViewHolder {

    TextView positionTextView;
    CircleImageView profilePictureImageView;
    TextView nicknameTextView;
    TextView scoreTextView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        positionTextView = itemView.findViewById(R.id.leaderboardRankingTextView);
        profilePictureImageView = itemView.findViewById(R.id.profilePictureImageViewLeaderboard);
        nicknameTextView = itemView.findViewById(R.id.leaderboardPlayerName);
        scoreTextView = itemView.findViewById(R.id.leaderboardScoreTextView);
    }
}
