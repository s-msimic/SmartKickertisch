package com.example.matte.smartkickertisch;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    Button logoutButton;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    StorageReference mStorageRef;
    private static final String TAG = "SettingsFragment";
    TextView currentNickname;
    TextView currentEMailAddress;
    TextView errorMessageTextView;
    EditText newNickname;
    EditText newPassword;
    EditText newEMail;
    CircleImageView currentProfilePictureCirecleImageView;
    Button saveChangesButton;
    private Uri profilePictureUri;
    private Bitmap profilePictureBitmap;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
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
        logoutButton.setOnClickListener(logoutListener);

        currentNickname = view.findViewById(R.id.currentNickSettingTextView);
        currentEMailAddress = view.findViewById(R.id.currentMailSettingsTextView);
        currentProfilePictureCirecleImageView = view.findViewById(R.id.currentPictureSettingsImageView);
        saveChangesButton = view.findViewById(R.id.saveChangesButton);
        newNickname = view.findViewById(R.id.changeNickSettingEditText);
        newPassword = view.findViewById(R.id.changePasswordSettingEditText);
        newEMail = view.findViewById(R.id.changeMailSettingsEditText);
        errorMessageTextView = view.findViewById(R.id.errorSettingsTextView);
        progressBar = view.findViewById(R.id.settingsProgressBar);
        constraintLayout = view.findViewById(R.id.settingsConstraintLayout);
        constraintLayout.setOnClickListener(hideKeyboardListener);
        currentNickname.setText(mAuth.getCurrentUser().getDisplayName());
        currentEMailAddress.setText(mAuth.getCurrentUser().getEmail());
        currentProfilePictureCirecleImageView.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    Log.d(TAG, "addImage: read storage permission = " + Manifest.permission.READ_EXTERNAL_STORAGE);
                    pickImage();
                }
            } else
                pickImage();
        });
        Log.i(TAG, "onCreateView: photoUrl = " + mAuth.getCurrentUser().getPhotoUrl());
        if (mAuth.getCurrentUser().getPhotoUrl() != null) {
            Picasso.get().load(mAuth.getCurrentUser().getPhotoUrl()).placeholder(R.drawable.profile_picture_preview).into(currentProfilePictureCirecleImageView);
        }
        saveChangesButton.setOnClickListener(saveChangesListener);
        return view;
    }

    View.OnClickListener hideKeyboardListener = v -> {
        if (v.getId() == R.id.settingsConstraintLayout) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            Log.d(TAG, "hideKeyboardListener : " + getActivity().getSystemService(INPUT_METHOD_SERVICE));
            if (getActivity().getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(), 0);
        }
    };

    View.OnClickListener logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mAuth.signOut();
            Intent i = new Intent(getActivity(), WelcomeActivity.class);
            startActivity(i);
        }
    };

    ValueEventListener vel = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "onDataChange: key = " + dataSnapshot.getKey() + " / value = " + dataSnapshot.getValue());
            Log.d(TAG, "onDataChange: wins = " + dataSnapshot.child("data").child("winCounter").getValue());
            Log.i(TAG, "onDataChange: user name = " + mAuth.getCurrentUser().getDisplayName());
            Log.i(TAG, "onDataChange: user mail = " + mAuth.getCurrentUser().getEmail());
            Log.i(TAG, "onDataChange: user photo = " + mAuth.getCurrentUser().getPhotoUrl());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    View.OnClickListener saveChangesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
            AtomicInteger tasksRunning = new AtomicInteger(0);
            Log.i(TAG, "onClick: nickname string = " + newNickname);
            StringBuilder errorMessage = new StringBuilder();
            final String nickname = newNickname.getText().toString();
            final String eMail = newEMail.getText().toString();
            final String password = newPassword.getText().toString();

            if (nickname.contains(" ")) {
                errorMessage.append("Your Nickname shouldn't contain any spaces!\n");
            }
            if (nickname.length() > 15) {
                errorMessage.append("Your Nickname can't be longer than 15 characters!\n");
            }

            if (!nickname.equals("") && !nickname.equals(currentNickname.getText().toString()) && errorMessage.toString().equals("")) {
                tasksRunning.addAndGet(1);
                Log.d(TAG, "onClick: editText nickname = " + newNickname.getText());
                Log.d(TAG, "onClick: nickname " + nickname);

                database.getReference("users/").child(mAuth.getCurrentUser().getUid()).child("nickName").setValue(nickname);
                currentNickname.setText(nickname);
                UserProfileChangeRequest updateNickname = new UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build();
                mAuth.getCurrentUser().updateProfile(updateNickname)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "onSuccess: new nickname = " + mAuth.getCurrentUser().getDisplayName());
                            Toast.makeText(getContext(), "Nickname updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "onFailure: unvalid nickname = ", e);
                            errorMessage.append(e.getMessage());
                            errorMessageTextView.setText(errorMessage);
                        })
                        .addOnCompleteListener(voidTask -> {
                            if (tasksRunning.decrementAndGet() == 0)
                                progressBar.setVisibility(View.GONE);
                            newNickname.setText("");
                        });
            }

            if (!eMail.equals("") && !eMail.equals(mAuth.getCurrentUser().getEmail())) {
                tasksRunning.addAndGet(1);
                Log.d(TAG, "onClick: email " + eMail);

                mAuth.getCurrentUser().updateEmail(eMail)
                        .addOnSuccessListener(aVoid -> {
                            Log.i(TAG, "onSuccess: new mail = " + eMail);
                            currentEMailAddress.setText(eMail);
                            Toast.makeText(getContext(), "E-Mail Address updated!", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "onClick: new mail = " + mAuth.getCurrentUser().getEmail());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "onFailure: unvalid eMail ", e);
                            errorMessage.append(e.getMessage()).append("\n");
                            errorMessageTextView.setText(errorMessage);
                        })
                        .addOnCompleteListener(voidTask -> {
                            if (tasksRunning.decrementAndGet() == 0) {
                                progressBar.setVisibility(View.GONE);
                                Log.d(TAG, "onClick: progressBar visibility = gone");
                                newEMail.setText("");
                            }
                        });
            }

            if (!password.equals("")) {
                tasksRunning.addAndGet(1);
                Log.i(TAG, "onClick: password " + password);

                mAuth.getCurrentUser().updatePassword(password)
                        .addOnSuccessListener((Void) -> {
                            Log.d(TAG, "onSuccess: password updated");
                            Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "onFailure: ", e);
                            errorMessage.append(e.getMessage()).append("\n");
                            errorMessageTextView.setText(errorMessage);
                        })
                        .addOnCompleteListener(voidTask -> {
                            if (tasksRunning.decrementAndGet() == 0)
                                progressBar.setVisibility(View.GONE);
                            newPassword.setText("");
                        });
            }
            if (profilePictureUri != null) {
                tasksRunning.addAndGet(1);
                // Get the data from an ImageView as bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                profilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mStorageRef.child("users/" + mAuth.getCurrentUser().getUid() + "/profileImage.jpg").putBytes(data);
                uploadTask
                        .addOnFailureListener(exception -> Log.d("Storage", exception.getMessage()))
                        .addOnSuccessListener(taskSnapshot -> {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            String picUid = "users/" + mAuth.getCurrentUser().getUid() + "/profileImage.jpg";
                            mStorageRef.child(picUid).getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        Log.i(TAG, "onSuccess: uri successfully retrieved = " + uri);
                                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                                .setPhotoUri(uri)
                                                .build();

                                        mAuth.getCurrentUser().updateProfile(profileUpdate)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(getContext(), "Profile Picture updated!", Toast.LENGTH_SHORT).show();
                                                        Log.i(TAG, "onComplete: name = " + mAuth.getCurrentUser().getDisplayName());
                                                        Log.i(TAG, "onComplete: mail = " + mAuth.getCurrentUser().getEmail());
                                                        Log.i(TAG, "onComplete: photoURL = " + mAuth.getCurrentUser().getPhotoUrl());
                                                    }
                                                });
                                    });
                        })
                        .addOnCompleteListener(voidTask -> {
                            if (tasksRunning.decrementAndGet() == 0)
                                progressBar.setVisibility(View.GONE);
                        });
            }
            if (tasksRunning.get() == 0)
                progressBar.setVisibility(View.GONE);
            errorMessageTextView.setText(errorMessage);
        }
    };

    // TODO: 22.05.2019 duplicate coder in SettingsFragment and in SignUpActivity
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;
        Uri selectedImage = data.getData();

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            try {
                profilePictureUri = selectedImage;
                profilePictureBitmap = scaleDown(getCorrectlyOrientedImage(getContext(), profilePictureUri), 512, true);
                currentProfilePictureCirecleImageView.setImageBitmap(profilePictureBitmap);
                Log.i("WIDTH:", currentProfilePictureCirecleImageView.getWidth() + "");
                Log.i("HEIGHT:", currentProfilePictureCirecleImageView.getHeight() + "");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        Log.i("newSize:", newBitmap.getByteCount() + "");
        Log.i("newWidth:", newBitmap.getWidth() + "");
        Log.i("newHeight:", newBitmap.getHeight() + "");
        return newBitmap;
    }

    public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        int MAX_IMAGE_DIMENSION = 5000;
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }


}