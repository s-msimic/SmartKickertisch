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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
    private ConstraintLayout signUpForegroundConstraintLayout;
    private de.hdodenhof.circleimageview.CircleImageView imageView;
    private Uri profilePictureUri;
    private static int MAX_IMAGE_DIMENSION = 5000;
    private Bitmap profilePictureBitmap;
    private ProgressBar signUpProgressBar;


//    check if scaling bitmap is needed, since round imageView already does that
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signUpForegroundConstraintLayout) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void createAccountClicked(View view) {
        String nicknameString = nicknameEditText.getText().toString();
        String eMailString = eMailEditText.getText().toString();
        String passwordString = passwordEditText.getText().toString();
        String repeatPasswordString = repeatPasswordEditText.getText().toString();
        StringBuilder errorMessage = new StringBuilder();

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

        errorTextEditText.setText(errorMessage.toString());

        if (errorTextEditText.getText().toString().equals("")) {
            signUpProgressBar.setVisibility(View.VISIBLE);
            createAccount();
        }
    }

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
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                Intent i = new Intent(SignUpActivity.this, MenuFolderActivity.class);
                                startActivity(i);
                                signUpProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
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
                        //error label set text(please enter longer 6 char)
                        errorTextEditText.setText("Your password must be atleast 6 characters long!");
                    } catch (FirebaseAuthUserCollisionException e) {
                        //mTxtEmail.setError(getString(R.string.error_user_exists));
                        //mTxtEmail.requestFocus();
                        errorTextEditText.setText("This e-mail address is already taken!");
                    } catch (Exception e) {
                        //Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
    }

    public void addImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                pickImage();
            }
        }
        else
            pickImage();
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }
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
        signUpForegroundConstraintLayout = findViewById(R.id.signUpForegroundConstraintLayout);
        imageView = findViewById(R.id.profilePictureImageView);
        signUpProgressBar = findViewById(R.id.signUpLoadingProgressBar);

        signUpForegroundConstraintLayout.setOnClickListener(this);
    }
}

