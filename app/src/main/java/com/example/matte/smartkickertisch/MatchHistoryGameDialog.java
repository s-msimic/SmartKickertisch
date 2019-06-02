package com.example.matte.smartkickertisch;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MatchHistoryGameDialog extends AppCompatDialogFragment {

    TextView blueTeamScore;
    TextView redTeamScore;
    TextView date;
    PlayerButtonTag blueDefense;
    PlayerButtonTag blueOffense;
    PlayerButtonTag redDefense;
    PlayerButtonTag redOffense;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final String TAG = "MatchHistoryGameDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.match_history_game_dialog_layout, null);
        view.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.match_history_dialog_background));
        builder.setView(view);

        blueTeamScore = view.findViewById(R.id.gameDialogBlueTeamScoreTextView);
        redTeamScore = view.findViewById(R.id.gameDialogRedTeamScoreTextView);
        date = view.findViewById(R.id.gameDialogDateTextView);
        blueOffense = view.findViewById(R.id.gameDialogBlueOffensePlayerButtonTag);
        blueDefense = view.findViewById(R.id.gameDialogBlueDefenseBlayerButtonTag);
        redDefense = view.findViewById(R.id.gameDialogRedDefensePlayerButtonTag);
        redOffense = view.findViewById(R.id.gameDialogRedOffensePlayerButtonTag);

        Log.d(TAG, "onCreateDialog: setArguments");
        if (getArguments().containsKey("blueDefense")) {
            Log.d(TAG, "onCreateDialog: ");
            database.getReference("users").child(getArguments().getString("blueDefense")).child("nickName")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            blueDefense.setText(dataSnapshot.getValue(String.class));
                            blueDefense.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        if (getArguments().containsKey("blueOffense")) {
            database.getReference("users").child(getArguments().getString("blueOffense")).child("nickName")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            blueOffense.setText(dataSnapshot.getValue(String.class));
                            blueOffense.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        if (getArguments().containsKey("redDefense")) {
            database.getReference("users").child(getArguments().getString("redDefense")).child("nickName")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            redDefense.setText(dataSnapshot.getValue(String.class));
                            redDefense.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        if (getArguments().containsKey("redOffense")) {
            database.getReference("users").child(getArguments().getString("redOffense")).child("nickName")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            redOffense.setText(dataSnapshot.getValue(String.class));
                            redOffense.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        blueTeamScore.setText(String.valueOf(getArguments().get("blueScore")));
        redTeamScore.setText(String.valueOf(getArguments().get("redScore")));
        SimpleDateFormat formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm", Locale.ENGLISH);
        String dateString = formatter.format(new Date((getArguments().getLong("date"))));
        date.setText(dateString);

        return builder.create();
    }

    @Override
    public Dialog getDialog() {
        return super.getDialog();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
