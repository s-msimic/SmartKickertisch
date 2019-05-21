package com.example.matte.smartkickertisch;


import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    Button logoutButton;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    private static final String TAG = "SettingsFragment";
    TextView currentNickname;
    TextView currentEMailAddress;
    CircleImageView currentProfilePicture;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        Log.i(TAG, "onCreate: key = " + database.getReference("users").getKey());
        Log.i(TAG, "onCreate: child = " + database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("nickName"));
        Log.i(TAG, "onCreate: keyChild = " + database.getReference("users").child(mAuth.getCurrentUser().getUid()).child("nickName").getKey());
        database.getReference("users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(vel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
//        cardView = view.findViewById(R.id.leaderboardCardView);
//        cardView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.cardview_round_corners_stroke));
        logoutButton = view.findViewById(R.id.logoutSettingsButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent i = new Intent(getActivity(), WelcomeActivity.class);
                startActivity(i);
            }
        });

        currentNickname = view.findViewById(R.id.currentNickSettingTextView);
        currentEMailAddress = view.findViewById(R.id.currentMailSettingsTextView);
        currentProfilePicture = view.findViewById(R.id.currentPictureSettingsImageView);
        currentNickname.setText(mAuth.getCurrentUser().getDisplayName());
        currentEMailAddress.setText(mAuth.getCurrentUser().getEmail());
        Log.i(TAG, "onCreateView: photoUrl = " + mAuth.getCurrentUser().getPhotoUrl());
        if (mAuth.getCurrentUser().getPhotoUrl() != null) {
            Picasso.get().load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.profile_picture_preview).into(currentProfilePicture);
        }
        return view;
    }

    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "onDataChange: key = " + dataSnapshot.getKey() + " / value = " + dataSnapshot.getValue());
            Log.i(TAG, "onDataChange: nick = " + dataSnapshot.child("nickName"));
            Log.i(TAG, "onDataChange: user name = " + mAuth.getCurrentUser().getDisplayName());
            Log.i(TAG, "onDataChange: user mail = " + mAuth.getCurrentUser().getEmail());
            Log.i(TAG, "onDataChange: user photo = " + mAuth.getCurrentUser().getPhotoUrl());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

}