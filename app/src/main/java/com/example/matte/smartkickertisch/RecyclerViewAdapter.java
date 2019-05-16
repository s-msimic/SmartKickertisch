package com.example.matte.smartkickertisch;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.Collections;

public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> positions = new ArrayList<>();
    private ArrayList<CircleImageView> profilePictures = new ArrayList<>();
    private ArrayList<String> nicknames;
    private ArrayList<String> scores;
    private int countBestPlayers;
    private int spinnerPosition;

    RecyclerViewAdapter(ArrayList<String> nicknames, ArrayList<String> scores, int count, int spinnerPosition) {
        this.nicknames = nicknames;
        this.scores = scores;
        countBestPlayers = count;
        this.spinnerPosition = spinnerPosition;
    }

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
        Log.i(TAG, "onBindViewHolder: nickname = " + nicknames.toString());
        viewHolder.positionTextView.setText((i + 1) + ".");
        viewHolder.profilePictureImageView.setImageResource(R.drawable.profile_picture_preview);
        if (scores.size() == countBestPlayers) {
            viewHolder.nicknameTextView.setText("Nickname: " + nicknames.get(i));
            if (spinnerPosition == 0 )
                viewHolder.scoreTextView.setText("Wins: " + scores.get(i));
            else
                viewHolder.scoreTextView.setText("Games: " + scores.get(i));
        }
    }

    @Override
    public int getItemCount() {
        return countBestPlayers;
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
