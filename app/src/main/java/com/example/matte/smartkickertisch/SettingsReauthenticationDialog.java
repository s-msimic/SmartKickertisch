package com.example.matte.smartkickertisch;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SettingsReauthenticationDialog extends AppCompatDialogFragment {

    private static final String TAG = "SettingsReauthenticatio";
    TextView titleTextView;
    EditText passwordEditText;
    Button cancelButton;
    Button confirmButton;
    ReauthenticateListener buttonIsPressedListener;
    FirebaseAuth mAuth;
    String eMail = "";
    String password = "";
    ProgressBar progressBar;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_reauthenticate_dialog, null);
        titleTextView = view.findViewById(R.id.settingsReauthenticationTextView);
        passwordEditText = view.findViewById(R.id.settingsReauthenticationDialogPasswordEditText);
        cancelButton = view.findViewById(R.id.settingsReauthenticationDialogCancelButton);
        confirmButton = view.findViewById(R.id.settingsReauthenticationDialogConfirmButton);
        mAuth = FirebaseAuth.getInstance();

        assert getTag() != null;
        switch (getTag()) {
            case "deleteAccount":
                titleTextView.append("delete your account, please confirm your password!");
                break;
            case "changeEMail":
                titleTextView.append("change your E-Mail address, please confirm your password!");
                break;
            case "changePassword":
                titleTextView.append("change your password, please confirm your old password!");
                break;
            case "changeMailAndPass":
                titleTextView.append("change your E-Mail address and your password, please confirm your old password");
                break;
        }

        cancelButton.setOnClickListener(v -> getDialog().cancel());

        confirmButton.setOnClickListener(v -> {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            if (getActivity().getCurrentFocus() != null || confirmButton.getWindowToken() != null) {
                Log.d(TAG, "onCreateDialog-onClick-48: remove keyboard: " + getActivity() + "\n" + getTargetFragment());
                inputMethodManager.hideSoftInputFromWindow(confirmButton.getWindowToken(), 0);
            }
            String reauthPassword;
            if (passwordEditText.getText().toString().equals("")) {
                reauthPassword = "-";
            } else {
                reauthPassword = passwordEditText.getText().toString();
            }
            if (mAuth.getCurrentUser() != null) {
                assert mAuth.getCurrentUser().getEmail() != null;
                mAuth.getCurrentUser().reauthenticate(EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), reauthPassword))
                        .addOnCompleteListener(task -> {
                            buttonIsPressedListener.reauthenticationSuccessful(task.isSuccessful(), getTag(), eMail, password);
                            Log.d(TAG, "onCreateDialog-onComplete-79: success= " + task.isSuccessful());
                            getDialog().dismiss();
                        });
                progressBar.setVisibility(View.VISIBLE);
                getDialog().hide();
            }
        });
        builder.setView(view);
        return builder.create();
    }

    public static SettingsReauthenticationDialog newInstance(String tag, String... data) {
        Bundle args = new Bundle();

        SettingsReauthenticationDialog fragment = new SettingsReauthenticationDialog();
        switch (tag) {
            case "changeEMail":
                args.putString("eMail", data[0]);
                fragment.eMail = data[0];
                break;
            case "changePassword":
                args.putString("newPassword", data[0]);
                fragment.password = data[0];
                break;
            case "changeMailAndPass":
                args.putString("eMail", data[0]);
                args.putString("newPassword", data[1]);
                fragment.eMail = data[0];
                fragment.password = data[1];
        }
        fragment.setArguments(args);
        return fragment;
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
        if (getDialog() == null)
            Log.d(TAG, "onStart-135: ");
        getDialog().setOnCancelListener(dialog1 -> {
            Log.d(TAG, "onCreateDialog-97: " + getTargetFragment());
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            buttonIsPressedListener = (ReauthenticateListener) getTargetFragment();
            progressBar = getTargetFragment().getView().findViewById(R.id.settingsProgressBar);
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach-76: " + e.getMessage());
        }
    }

    public interface ReauthenticateListener {
        void reauthenticationSuccessful(boolean successful, String tag, String... data);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(TAG, "onCancel-158: called!");
    }
}