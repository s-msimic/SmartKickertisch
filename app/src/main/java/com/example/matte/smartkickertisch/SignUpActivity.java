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
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.Function;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    StorageReference mStorageRef;
    private EditText nicknameEditText;
    private EditText eMailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private TextView errorTextEditText;
    private de.hdodenhof.circleimageview.CircleImageView imageView;
    private Uri profilePictureUri;
    private Bitmap profilePictureBitmap;
    private ProgressBar signUpProgressBar;


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
        if (!eMailString.contains("@haw-landshut.de"))
            errorMessage.append("You need a \"@haw-landshut.de\" e-mail address!\n");
        if (passwordString.equals(""))
            errorMessage.append("Type in your password!\n");
        if (repeatPasswordString.equals("") || !passwordString.equals(repeatPasswordString))
            errorMessage.append("Repeat your password correctly!\n");
        if (nicknameString.length() > 15)
            errorMessage.append("Your Nickname can't be longer than 15 characters!");
        if (nicknameString.contains(" "))
            errorMessage.append("Your Nickname shouldn't contain any spaces!");

        errorTextEditText.setText(errorMessage.toString());

//        Query query = myRef.orderByChild("nickName").equalTo(nicknameString);
//        Log.d("query", query.toString());
//        System.out.println(query.toString());
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                System.out.println(s);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                System.out.println(s + " changed");
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

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
        mAuth.createUserWithEmailAndPassword(eMailEditText.getText().toString(), passwordEditText.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("nickName").setValue(nicknameEditText.getText().toString());
                    Log.i("SIGN_UP", mAuth.getCurrentUser().getUid() + mAuth.getCurrentUser().getUid());

                    if (profilePictureUri != null) {
                        // Get the data from an ImageView as bytes
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        profilePictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = mStorageRef.child("users/" + mAuth.getCurrentUser().getUid() + "/profileImage.jpg").putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d("Storage", exception.getMessage());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                Intent i = new Intent(SignUpActivity.this, MenuFolderActivity.class);
                                signUpProgressBar.setVisibility(View.INVISIBLE);
                                startActivity(i);
                            }
                        });
                        // if there is no picture selected don't upload anything
                    } else {
                        Intent i = new Intent(SignUpActivity.this, MenuFolderActivity.class);
                        startActivity(i);
                        signUpProgressBar.setVisibility(View.INVISIBLE);
                    }

//                    mStorageRef.child("users").child(mAuth.getCurrentUser().getUid()).child("profileImage").putFile(profilePictureUri)
//                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                @Override
//                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                    // Get a URL to the uploaded content
//                                    Intent i = new Intent(SignUpActivity.this, MenuFolderActivity.class);
//                                    startActivity(i);
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception exception) {
//                                    // Handle unsuccessful uploads
//                                    // ...
//                                }
//                            });
                } else {
                    //error handling
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        errorTextEditText.setText(getString(R.string.sign_up_error_password_short));
                    } catch (FirebaseAuthUserCollisionException e) {
                        //mTxtEmail.setError(getString(R.string.error_user_exists));
                        //mTxtEmail.requestFocus();
                        errorTextEditText.setText(getString(R.string.sign_up_error_email_taken));
                    } catch (Exception e) {
                        Log.e("Firebase", e.getMessage());
                    }
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
                profilePictureBitmap = scaleDown(getCorrectlyOrientedImage(this, profilePictureUri),1024,true);
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
        nicknameEditText = findViewById(R.id.nicknameEditText);
        eMailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText);
        errorTextEditText = findViewById(R.id.errorTextView);
        ConstraintLayout signUpForegroundConstraintLayout = findViewById(R.id.signUpForegroundConstraintLayout);
        imageView = findViewById(R.id.profilePictureImageView);
        signUpProgressBar = findViewById(R.id.signUpLoadingProgressBar);

        signUpForegroundConstraintLayout.setOnClickListener(this);

    }
}

