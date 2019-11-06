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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements SettingsReauthenticationDialog.ReauthenticateListener {
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
    CircleImageView currentProfilePictureCircleImageView;
    Button saveChangesButton;
    Button deleteAccountButton;
    AtomicInteger tasksRunning = new AtomicInteger(0);
    ConstraintLayout constraintLayout;
    private Uri profilePictureUri;
    private Bitmap profilePictureBitmap;
    private ProgressBar progressBar;
    private SettingsFragment settingsFragment;

    public SettingsFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        settingsFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        logoutButton = view.findViewById(R.id.logoutSettingsButton);
        logoutButton.setOnClickListener(logoutListener);
        deleteAccountButton = view.findViewById(R.id.deleteAccountSettingsButton);
        deleteAccountButton.setOnClickListener(deleteAccountListener);

        currentNickname = view.findViewById(R.id.currentNickSettingTextView);
        currentEMailAddress = view.findViewById(R.id.currentMailSettingsTextView);
        currentProfilePictureCircleImageView = view.findViewById(R.id.currentPictureSettingsImageView);
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
        currentProfilePictureCircleImageView.setOnClickListener(addPictureOnClickListener);

        Log.i(TAG, "onCreateView: photoUrl = " + mAuth.getCurrentUser().getPhotoUrl());
        if (mAuth.getCurrentUser().getPhotoUrl() != null) {
            Picasso.get().load(mAuth.getCurrentUser().getPhotoUrl())
                    .placeholder(R.drawable.profile_picture_preview).into(currentProfilePictureCircleImageView);
        }
        newNickname.addTextChangedListener(saveChangesTextWatcher);
        newPassword.addTextChangedListener(saveChangesTextWatcher);
        newEMail.addTextChangedListener(saveChangesTextWatcher);
        return view;
    }

    View.OnClickListener addPictureOnClickListener = v -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Log.d(TAG, "addImage: read storage permission = " + Manifest.permission.READ_EXTERNAL_STORAGE);
                pickImage();
            }
        } else
            pickImage();
    };


    View.OnClickListener hideKeyboardListener = v -> {
        Log.d(TAG, "hideKeyboardListener-138: focus= " + getActivity().getCurrentFocus());
        if (v.getId() == R.id.settingsConstraintLayout) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            Log.d(TAG, "hideKeyboardListener : " + getActivity().getSystemService(INPUT_METHOD_SERVICE));
            if (getActivity().getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull
                        (getActivity().getCurrentFocus()).getWindowToken(), 0);
            }
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

    TextWatcher saveChangesTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "onTextChanged: charseq = " + s + " start = " + start);
            String nickname = newNickname.getText().toString().trim();
            String email = newEMail.getText().toString().trim();
            String password = newPassword.getText().toString().trim();

            if (!nickname.isEmpty() || !email.isEmpty() || !password.isEmpty()) {
                Log.d(TAG, "onTextChanged: nick = " + nickname + " email = " + email + " password = " + password);
                saveChangesButton.setEnabled(true);
                saveChangesButton.setAlpha(1);
                saveChangesButton.setOnClickListener(saveChangesListener);
            } else {
                saveChangesButton.setEnabled(false);
                saveChangesButton.setAlpha(0.7f);
                saveChangesButton.setOnClickListener(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    View.OnClickListener saveChangesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progressBar.setVisibility(View.VISIBLE);
            newNickname.setText(newNickname.getText().toString().trim());
            StringBuilder errorMessage = new StringBuilder();
            final String oldNickname = currentNickname.getText().toString();
            final String nickname = newNickname.getText().toString();
            final String eMail = newEMail.getText().toString();
            final String password = newPassword.getText().toString();

            if (nickname.contains(" ")) {
                errorMessage.append("Your Nickname can't contain any spaces!\n");
            }
            if (nickname.length() > 15) {
                errorMessage.append("Your Nickname can't be longer than 15 characters!\n");
            }
            if (eMail.length() > 29) {
                errorMessage.append("Your E-Mail can't be longer than 30 characters");
            }
            if (mAuth.getCurrentUser() == null) {
                errorMessage.append("You have to log in again!");
                errorMessageTextView.setText(errorMessage);
                return;
            }

            // TODO: 30.10.2019 do this with cloud functions, once profile changes change database
            if (!nickname.equals("") && !nickname.equals(currentNickname.getText().toString()) && errorMessage.toString().equals("")) {
                tasksRunning.addAndGet(1);
                Log.d(TAG, "onClick: editText nickname = " + newNickname.getText());
                Log.d(TAG, "onClick: nickname " + nickname);

                UserProfileChangeRequest updateNickname = new UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build();
                mAuth.getCurrentUser().updateProfile(updateNickname)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "saveChanges-onClick-onSuccess: new nickname = " + mAuth.getCurrentUser().getDisplayName());
                            database.getReference("users/").child(mAuth.getCurrentUser().getUid()).child("nickName").setValue(nickname)
                                    .addOnSuccessListener(command -> Toast.makeText(getContext(), "Nickname updated!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Couldn't update nickname.\nPlease check your internet connection!", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "saveChangesListener-updateNickname-onClick-238: couldn't update database= " + e.getMessage());
                                    })
                                    .addOnCompleteListener(command -> {
                                        currentNickname.setText(nickname);
                                        newNickname.setText("");
                                        if (tasksRunning.decrementAndGet() == 0)
                                            progressBar.setVisibility(View.GONE);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "saveChanges-onClick-onFailure: unvalid nickname = ", e);
                            errorMessage.append(e.getMessage());
                            errorMessageTextView.setText(errorMessage);
                            newNickname.setText("");
                            if (tasksRunning.decrementAndGet() == 0)
                                progressBar.setVisibility(View.GONE);
                        });
            }

            if (!eMail.equals("") &&
                    !eMail.equals(mAuth.getCurrentUser().getEmail()) && eMail.length() < 30) {
                tasksRunning.addAndGet(1);
                if (!password.equals("")) {
                    tasksRunning.addAndGet(1);

                    SettingsReauthenticationDialog reauthenticationDialog = SettingsReauthenticationDialog.newInstance("changeMailAndPass", eMail, password);
                    createDialog(reauthenticationDialog, "changeMailAndPass");
                } else {
                    SettingsReauthenticationDialog reauthenticationDialog = SettingsReauthenticationDialog.newInstance("changeEMail", eMail);
                    createDialog(reauthenticationDialog, "changeEMail");
                }
            } else if (!password.equals("")) {
                tasksRunning.addAndGet(1);
                SettingsReauthenticationDialog reauthenticationDialog = SettingsReauthenticationDialog.newInstance("changePassword", password);
                createDialog(reauthenticationDialog, "changePassword");
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

            if (tasksRunning.get() == 0) {
                progressBar.setVisibility(View.GONE);
            }

            errorMessageTextView.setText(errorMessage);
        }
    };

    private void createDialog(SettingsReauthenticationDialog dialog, String tag) {
        dialog.setTargetFragment(settingsFragment, 0);
        dialog.show(requireActivity().getSupportFragmentManager(), tag);
    }

    public void changeEMail(String eMail, String tag, String password) {
        mAuth.getCurrentUser().updateEmail(eMail)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "onSuccess: new mail = " + eMail);
                    currentEMailAddress.setText(eMail);
                    if (tag.equals("changeMailAndPass")) {
                        changePassword(password);
                    } else {
                        Toast.makeText(getContext(), "E-Mail Address updated!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "changeMail-onSuccess: new mail = " + mAuth.getCurrentUser().getEmail());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: unvalid eMail ", e);
                    errorMessageTextView.append(e.getMessage() + "\n");
                })
                .addOnCompleteListener(voidTask -> {
                    if (tasksRunning.decrementAndGet() == 0) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "changeEMail-onComplete-340: tasksRunning= " + tasksRunning.get());
                    newEMail.setText("");
                });
    }

    public void changePassword(String password) {
        Log.d(TAG, "changePassword-347: UID= " + mAuth.getCurrentUser().getUid());
        mAuth.getCurrentUser().updatePassword(password)
                .addOnSuccessListener((Void) -> {
                    Log.d(TAG, "changePassword-onSuccess: password updated");
                    Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "changePassword-onFailure: ", e);
                    errorMessageTextView.append(e.getMessage() + "\n");
                })
                .addOnCompleteListener(voidTask -> {
                    if (tasksRunning.decrementAndGet() == 0)
                        progressBar.setVisibility(View.GONE);
                    newPassword.setText("");
                });
    }

    View.OnClickListener deleteAccountListener = v -> {
        SettingsReauthenticationDialog reauthenticationDialog = new SettingsReauthenticationDialog();
        createDialog(reauthenticationDialog, "deleteAccount");
    };

    public void deleteUser() {
        assert mAuth.getCurrentUser() != null;
        Log.d(TAG, "deleteUser-UID-364: " + mAuth.getCurrentUser().getUid());
        String uid = mAuth.getCurrentUser().getUid();
        mAuth.getCurrentUser().delete()
                .addOnSuccessListener(deleteTask -> {
                    database.getReference("users").child(uid).child("data").removeValue()
                            .addOnFailureListener(e -> Log.d(TAG, "deleteUser-onComplete-onFailure-database-374: " + e.getMessage()));
                    database.getReference("users").child(uid).child("finishedGames").removeValue();
                    database.getReference("users").child(uid).child("nickName").setValue("[deleted]");

                    mStorageRef.child("users").child(uid).delete()
                            .addOnCompleteListener(command -> {
                                progressBar.setVisibility(View.GONE);
                                Intent toWelcome = new Intent(getActivity(), WelcomeActivity.class);
                                startActivity(toWelcome);
                            })
                            .addOnFailureListener(e -> Log.d(TAG, "deleteUser-onComplete-onFailure-storage-200: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "deleteUser-onFailure-497: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public void reauthenticationSuccessful(boolean successful, String tag, String... data) {
        Log.d(TAG, "reauthenticationSuccessful-423: successful= " + successful + "-" + Arrays.toString(data));
        if (successful) {
            switch (tag) {
                case "deleteAccount":
                    deleteUser();
                    break;
                case "changeEMail":
                    changeEMail(data[0], tag, null);
                    break;
                case "changePassword":
                    changePassword(data[1]);
                    break;
                case "changeMailAndPass":
                    changeEMail(data[0], tag, data[1]);
                    break;
            }
        } else {
            Toast.makeText(getContext(), "Reauthentication unsuccessful! Wrong password!", Toast.LENGTH_SHORT).show();
            newPassword.setText("");
            newEMail.setText("");
            progressBar.setVisibility(View.GONE);
        }
    }

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
                currentProfilePictureCircleImageView.setImageBitmap(profilePictureBitmap);
                Log.i("WIDTH:", currentProfilePictureCircleImageView.getWidth() + "");
                Log.i("HEIGHT:", currentProfilePictureCircleImageView.getHeight() + "");
                saveChangesButton.setAlpha(1);
                saveChangesButton.setEnabled(true);
                saveChangesButton.setOnClickListener(saveChangesListener);
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