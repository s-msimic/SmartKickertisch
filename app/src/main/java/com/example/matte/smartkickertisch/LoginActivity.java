package com.example.matte.smartkickertisch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnKeyListener, View.OnClickListener{
    DatabaseReference ref;
    private FirebaseAuth mAuth;
    private EditText eMailEditText;
    private EditText passwordEditText;
    private ConstraintLayout loginConstraintLayout;
    private ProgressBar progressBar;

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            onClickLogin(v);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.loginBackgroundLayout) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void onClickLogin(View view){
        //if checks
        if(eMailEditText.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please enter your e-mail address", Toast.LENGTH_SHORT).show();
            return;
        }
        if(passwordEditText.getText().toString().isEmpty() ){
            Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        signIn();

    }

    private void signIn(){
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(eMailEditText.getText().toString(), passwordEditText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            //no errors
                            Log.i("NUTZERID",mAuth.getCurrentUser().getUid());

                            // go to next window
                            Intent i = new Intent(LoginActivity.this, MenuFolderActivity.class);
                            startActivity(i);

                        }
                        else{
                            //error handling
                            try {
                                throw task.getException();
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                Toast.makeText(LoginActivity.this,"Your inputs are false", Toast.LENGTH_SHORT).show();

                            }catch (FirebaseAuthInvalidUserException e){
                                Toast.makeText(LoginActivity.this,"There is no such user", Toast.LENGTH_SHORT).show();

                            }catch(FirebaseAuthEmailException e){
                                Toast.makeText(LoginActivity.this,"This eMail does not exist", Toast.LENGTH_SHORT).show();

                            }
                            catch (Exception e){
                                Toast.makeText(LoginActivity.this,"False inputs", Toast.LENGTH_SHORT).show();

                            }
                        }

                    }
                });
    }

    public void onClickRegistrierenSwap(View view){
        Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eMailEditText = findViewById(R.id.editTextE_Mail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginConstraintLayout = findViewById(R.id.loginBackgroundLayout);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.ref = database.getReference();

        passwordEditText.setOnKeyListener(this);
        loginConstraintLayout.setOnClickListener(this);
        progressBar = findViewById(R.id.loadingProgressBar);
    }
}
