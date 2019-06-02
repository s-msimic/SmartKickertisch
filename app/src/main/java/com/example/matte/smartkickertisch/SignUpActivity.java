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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    StorageReference mStorageRef;
    private Button signUpButton;
    private EditText nicknameEditText;
    private EditText eMailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private TextView errorTextEditText;
    private de.hdodenhof.circleimageview.CircleImageView imageView;
    private Uri profilePictureUri;
    private Bitmap profilePictureBitmap;
    private ProgressBar signUpProgressBar;
    private static final String TAG = "SignUpActivity";

    public TextWatcher signUpTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String nickname = nicknameEditText.getText().toString().trim();
            String email = eMailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String repeatPassword = repeatPasswordEditText.getText().toString().trim();

            if (!nickname.isEmpty() && !email.isEmpty() && !password.isEmpty() && !repeatPassword.isEmpty()) {
                signUpButton.setEnabled(true);
                signUpButton.setAlpha(1);
            } else {
                signUpButton.setEnabled(false);
                signUpButton.setAlpha(0.7f);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * Creates account with Nickname, E-Mail and Password. If an error occurs it will be shown in a TextView.
     * If there are no input errors the account is created in createAccount().
     * @param view button to create account
     */
    public void createAccountClicked(View view) {
        String nicknameString = nicknameEditText.getText().toString();
        String eMailString = eMailEditText.getText().toString();
        String passwordString = passwordEditText.getText().toString();
        String repeatPasswordString = repeatPasswordEditText.getText().toString();
        final StringBuilder errorMessage = new StringBuilder();

        if (nicknameString.equals(""))
            errorMessage.append("Type in your nickname!\n");
        if (eMailString.equals("") || !eMailString.contains("@"))
            errorMessage.append("Type in a valid e-mail address\n");
        if (passwordString.equals(""))
            errorMessage.append("Type in your password!\n");
        if (repeatPasswordString.equals("") || !passwordString.equals(repeatPasswordString))
            errorMessage.append("Repeat your password correctly!\n");
        if (nicknameString.length() > 15)
            errorMessage.append("Your Nickname can't be longer than 15 characters!\n");
        if (nicknameString.contains(" "))
            errorMessage.append("Your Nickname shouldn't contain any spaces!");

        errorTextEditText.setText(errorMessage.toString());

        if (errorTextEditText.getText().toString().equals("")) {
            signUpProgressBar.setVisibility(View.VISIBLE);
            createAccount();
        }
    }

    /**
     * Creates a new entry in Firebase for "Authentication" and "Database" with a unique user id. If there exists a
     * picture it will be added under "Storage".
     */
    private void createAccount() {
        mAuth.createUserWithEmailAndPassword(eMailEditText.getText().toString(), passwordEditText.getText().toString()).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("nickName").setValue(nicknameEditText.getText().toString());
                Log.i(TAG, "onComplete: successful UID = " + mAuth.getCurrentUser().getUid());
                if (profilePictureUri != null) {
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
                                                    .setDisplayName(nicknameEditText.getText().toString())
                                                    .setPhotoUri(uri)
                                                    .build();

                                            mAuth.getCurrentUser().updateProfile(profileUpdate)
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Log.i(TAG, "onComplete: name = " + mAuth.getCurrentUser().getDisplayName());
                                                            Log.i(TAG, "onComplete: mail = " + mAuth.getCurrentUser().getEmail());
                                                            Log.i(TAG, "onComplete: photoURL = " + mAuth.getCurrentUser().getPhotoUrl());
                                                        }
                                                    });
                                        });
                                Intent i = new Intent(SignUpActivity.this, LeaderboardActivity.class);
                                signUpProgressBar.setVisibility(View.GONE);
                                startActivity(i);
                            });

                    // if there is no picture selected don't upload anything
                } else {
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nicknameEditText.getText().toString())
                            .build();

                    mAuth.getCurrentUser().updateProfile(profileUpdate)
                            .addOnCompleteListener(task12 -> {
                                if (task12.isSuccessful()) {
                                    Log.i(TAG, "onComplete: name = " + mAuth.getCurrentUser().getDisplayName());
                                    Log.i(TAG, "onComplete: mail = " + mAuth.getCurrentUser().getEmail());
                                    Log.i(TAG, "onComplete: photoURL = " + mAuth.getCurrentUser().getPhotoUrl());
                                }
                            });

                    Intent i = new Intent(SignUpActivity.this, LeaderboardActivity.class);
                    signUpProgressBar.setVisibility(View.GONE);
                    startActivity(i);
                }

            } else {
                //error handling
                try {
                    throw task.getException();
                } catch (FirebaseAuthWeakPasswordException e) {
                    errorTextEditText.setText(getString(R.string.sign_up_error_password_short));
                } catch (FirebaseAuthUserCollisionException e) {
                    errorTextEditText.setText(getString(R.string.sign_up_error_email_taken));
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    errorTextEditText.setText(e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "onComplete: sign up error = " + e.getMessage() ,e);
                } finally {
                    signUpProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * For users with android version 6.0 (Marshmallow, API 23) or greater permission has to be granted to open gallery
     * if permission is granted (or device doesn't support API 23) pickImage() is called.
     * @param view round image view
     */
    public void addImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            // if the user already gave the permission to use the gallery
            else {
                pickImage();
            }
        }
        // if the users device doesn't support request permission
        else
            pickImage();
    }

    /**
     * Image can be selected as profile picture.
     */
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    /**
     * If permission is granted to read gallery pickImage() is called.
     * @param requestCode identifier to assign which activity's result is received (here from gallery)
     * @param permissions all requested permissions (here READ_EXTERNAL_STORAGE)
     * @param grantResults is the permission granted (granted == 0, denied == -1)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
        }
    }

    /**
     * If picture is selected this is called.
     * @param requestCode identifier to assign which activity's result is received (here from gallery)
     * @param resultCode result message (OK == -1, CANCELED == -1)
     * @param data selected profile picture
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null)
            return;
        Uri selectedImage = data.getData();

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            try {
                profilePictureUri = selectedImage;
                profilePictureBitmap = scaleDown(getCorrectlyOrientedImage(this, profilePictureUri),512,true);
                imageView.setImageBitmap(profilePictureBitmap);
                Log.i("WIDTH:", imageView.getWidth() + "");
                Log.i("HEIGHT:", imageView.getHeight() + "");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Scales the images
     * @param realImage image which is supposed to be scaled
     * @param maxImageSize maximum width or height (maxImageSize x height or height x maxImageSize)
     * @param filter is the image supposed to be filtered
     * @return new Image with new size
     */
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

    /**
     * For pictures made with own camera orientation could change. Those picture's orientation has to be corrected.
     * @param context current context
     * @param photoUri picture uri
     * @return correctly orientated picture as Bitmap
     * @throws IOException inputStream can throw exception
     */
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

    /**
     * Gets the orientation of the picture.
     * @param context current context
     * @param photoUri picture uri
     * @return int of the orientation in degrees (either 0, 90, 180, 270)
     */
    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    /**
     * Exits keyboard if app background is clicked while using the keyboard.
     * @param v clicked element
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loginBackgroundLayout) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
                inputMethodManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        myRef = database.getReference();
        signUpButton = findViewById(R.id.signUpButton);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        eMailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);
        errorTextEditText = findViewById(R.id.errorTextView);
        ConstraintLayout signUpForegroundConstraintLayout = findViewById(R.id.signUpForegroundConstraintLayout);
        imageView = findViewById(R.id.profilePictureImageView);
        signUpProgressBar = findViewById(R.id.signUpLoadingProgressBar);
        nicknameEditText.addTextChangedListener(signUpTextWatcher);
        eMailEditText.addTextChangedListener(signUpTextWatcher);
        passwordEditText.addTextChangedListener(signUpTextWatcher);
        repeatPasswordEditText.addTextChangedListener(signUpTextWatcher);
        signUpForegroundConstraintLayout.setOnClickListener(this);

    }
}

