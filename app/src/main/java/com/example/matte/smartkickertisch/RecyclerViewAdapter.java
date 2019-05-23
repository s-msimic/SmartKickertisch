package com.example.matte.smartkickertisch;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;
import java.util.Objects;

public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";
    private int countBestPlayers;
    private int spinnerPosition;
    private HashMap<Integer, User> userList;

    RecyclerViewAdapter(HashMap<Integer, User> myUsers, int playerCount, int spinnerPos) {
        userList = myUsers;
        countBestPlayers = playerCount;
        spinnerPosition = spinnerPos;
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
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        if (userList.size() == countBestPlayers) {
            viewHolder.positionTextView.setText(userList.get(i + 1).getPosition() + ".");
            viewHolder.nicknameTextView.setText("Nickname: " + userList.get(i + 1).getNickname());
            if (spinnerPosition == 0) {
                viewHolder.scoreTextView.setText("Wins: " + userList.get(i + 1).getWins());
            } else if (spinnerPosition == 1){
                viewHolder.scoreTextView.setText("Games: " + userList.get(i + 1).getGames());
            }
            if (userList.get(i + 1).getProfilePicture() != null){
                Log.i(TAG, "onBindViewHolder: user nr." + (i + 1) + " " + Objects.requireNonNull(userList.get(i + 1)).getProfilePicture().toString());
                Picasso.get().load(userList.get(i + 1).getProfilePicture()).placeholder(R.drawable.profile_picture_preview).into(viewHolder.profilePictureImageView);
            }
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

    public ViewHolder(@NonNull final View itemView) {
        super(itemView);
        positionTextView = itemView.findViewById(R.id.leaderboardRankingTextView);
        profilePictureImageView = itemView.findViewById(R.id.profilePictureImageViewLeaderboard);
        nicknameTextView = itemView.findViewById(R.id.leaderboardPlayerName);
        scoreTextView = itemView.findViewById(R.id.leaderboardScoreTextView);
    }
}
