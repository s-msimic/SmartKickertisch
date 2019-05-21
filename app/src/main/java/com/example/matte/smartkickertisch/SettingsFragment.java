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
import android.widget.EditText;
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
    TextView errorMessageTextView;
    EditText newNickname;
    EditText newPassword;
    EditText newEMail;
    CircleImageView currentProfilePicture;
    Button saveChangesButton;

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
        saveChangesButton = view.findViewById(R.id.saveChangesButton);
        newNickname = view.findViewById(R.id.nicknameEditText);
        newPassword = view.findViewById(R.id.passwordEditText);
        newEMail = view.findViewById(R.id.emailEditText);
        errorMessageTextView = view.findViewById(R.id.errorSettingsTextView);
        currentNickname.setText(mAuth.getCurrentUser().getDisplayName());
        currentEMailAddress.setText(mAuth.getCurrentUser().getEmail());
        Log.i(TAG, "onCreateView: photoUrl = " + mAuth.getCurrentUser().getPhotoUrl());
        if (mAuth.getCurrentUser().getPhotoUrl() != null) {
            Picasso.get().load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.profile_picture_preview).into(currentProfilePicture);
        }
//        saveChangesButton.setOnClickListener(saveChangesListener);
        return view;
    }

    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "onDataChange: key = " + dataSnapshot.getKey() + " / value = " + dataSnapshot.getValue());
            Log.i(TAG, "onDataChange: user name = " + mAuth.getCurrentUser().getDisplayName());
            Log.i(TAG, "onDataChange: user mail = " + mAuth.getCurrentUser().getEmail());
            Log.i(TAG, "onDataChange: user photo = " + mAuth.getCurrentUser().getPhotoUrl());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

//    View.OnClickListener saveChangesListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String nickname = newNickname.getText().toString();
//            String eMail = newEMail.getText().toString();
//            String password = newPassword.getText().toString();
//            StringBuilder errorMessage = new StringBuilder();
//
//            if (nickname.contains(" "))
//                errorMessage.append("Your Nickname shouldn't contain any spaces!\n");
//
//            if (nickname.length() > 15)
//                errorMessage.append("Your Nickname can't be longer than 15 characters!\n");
//
//            if (!eMail.contains("@") | !eMail.contains(" ") )
//                errorMessage.append("Type in a valid e-mail address\n");
//
//            errorMessageTextView.setText(errorMessage);
//            if (!errorMessage.toString().equals("")) {
//                return;
//            }
//
//            if (!nickname.equals("")) {
//                Log.i(TAG, "onClick: new nickname = " + nickname);
////                database.getReference("user/").child(mAuth.getCurrentUser().getUid()).child("nickName").setValue(nickname);
//
//            }
//
//            if (!eMail.equals("")) {
//                Log.i(TAG, "onClick: new mail = " + eMail);
////                mAuth.getCurrentUser().updateEmail(eMail)
//
//            }
//
//            if (!password.equals("")) {
//                Log.i(TAG, "onClick: new password = " + password);
//            }
//        }
//    };

}