package com.example.reflorestar.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

public class AccountCreateFragment extends Fragment {

    private View root;
    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;
    private TextInputLayout emailInput, passwordInput, confirmPasswordInput, usernameInput, nameInput;
    private TextView emailWarning, passwordWarning, usernameWarning;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_account_create, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.create_account_container);

        Button buttonConfirmAccount = root.findViewById(R.id.buttonConfirmAccount);
        Button backButton = root.findViewById(R.id.backButtonCreateAcc);

        //input controls
        usernameInput = root.findViewById(R.id.createInputUsername);
        emailInput = root.findViewById(R.id.createInputEmail);
        nameInput = root.findViewById(R.id.createInputName);
        passwordInput = root.findViewById(R.id.createInputPassword);
        confirmPasswordInput = root.findViewById(R.id.createConfirmPassword);
        Editable usernameText = usernameInput.getEditText().getText();
        Editable emailText = emailInput.getEditText().getText();
        Editable nameText = nameInput.getEditText().getText();
        Editable passwordText = passwordInput.getEditText().getText();
        Editable confirmText = confirmPasswordInput.getEditText().getText();
        emailWarning = root.findViewById(R.id.emailWarning);
        passwordWarning = root.findViewById(R.id.passwordWarning);
        usernameWarning = root.findViewById(R.id.usernameWarning);

        buttonConfirmAccount.setOnClickListener(v -> {
            //validations:
            if (!usernameText.toString().isEmpty() && !emailText.toString().isEmpty() && !nameText.toString().isEmpty() && !passwordText.toString().isEmpty() && !confirmText.toString().isEmpty()) {
                validateUsername(usernameText);
                validateEmail(emailText);
                validatePassword(passwordText, confirmText);

                if (validateUsername(usernameText) && validateEmail(emailText) && validatePassword(passwordText, confirmText)) {
                    users.orderByChild("username").equalTo(usernameText.toString()).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        showMessage(getString(R.string.username_exists), getString(R.string.username_exists_warning));
                                    } else {
                                        users.orderByChild("email").equalTo(emailText.toString().toLowerCase()).addListenerForSingleValueEvent(
                                                new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            showMessage(getString(R.string.email_exists), getString(R.string.email_exists_warning));
                                                        } else {
                                                            //criar utilizador
                                                            try {
                                                                User newUser = new User(usernameText.toString(), emailText.toString().toLowerCase(), nameText.toString(), passwordText.toString());
                                                                DatabaseReference newRef = users.child(usernameText.toString());
                                                                newRef.setValue(newUser);
                                                                showMessage(getString(R.string.user_created), getString(R.string.success));
                                                                returnToAccountHome(fragmentContainer);
                                                            } catch (NoSuchAlgorithmException e) {
                                                                e.printStackTrace();
                                                                //no such algorithm
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        throw databaseError.toException();
                                                    }
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    throw databaseError.toException();
                                }
                            });
                }

            } else {
                showMessage(getString(R.string.empty_fields), getString(R.string.empty_fields_warning));
            }
        });

        backButton.setOnClickListener(v -> {
            returnToAccountHome(fragmentContainer);
        });

        setListeners(usernameText, emailText, passwordText, confirmText);

        return root;
    }

    private void returnToAccountHome(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.create_account_container, new AccountHomeFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("create_account").commit();
        fragmentContainer.removeAllViews();
    }

    public void showMessage(String message, String warning) {
        AlertDialog alertDialog = new AlertDialog.Builder(root.getContext()).create();
        alertDialog.setTitle(warning);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void showInputWarning(TextView warning, String message) {
        warning.setVisibility(View.VISIBLE);
        warning.setText(message);
    }

    public boolean validateUsername(Editable usernameText) {
        if (!usernameText.toString().matches("^[a-zA-Z0-9._-]{6,}$")) {
            showInputWarning(usernameWarning, getString(R.string.username_error));
            return false;
        }
        usernameWarning.setVisibility(View.GONE);
        return true;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean validateEmail(Editable emailText) {
        if (!isValidEmail(emailText.toString())) {
            showInputWarning(emailWarning, getString(R.string.invalid_email));
            return false;
        }
        emailWarning.setVisibility(View.GONE);
        return true;
    }

    public boolean validatePassword(Editable passwordText, Editable confirmText) {
        if (!passwordText.toString().equals(confirmText.toString())) {
            showInputWarning(passwordWarning, getString(R.string.password_error));
            return false;
        }
        passwordWarning.setVisibility(View.GONE);
        return true;
    }

    private void setListeners(Editable usernameText, Editable emailText, Editable passwordText, Editable confirmText) {
        usernameInput.setEndIconOnClickListener(view -> {
            usernameText.clear();
            usernameWarning.setVisibility(View.GONE);
        });

        emailInput.setEndIconOnClickListener(view -> {
            emailText.clear();
            emailWarning.setVisibility(View.GONE);
        });

        passwordInput.setEndIconOnClickListener(view -> {
            passwordText.clear();
            passwordWarning.setVisibility(View.GONE);
        });

        confirmPasswordInput.setEndIconOnClickListener(view -> {
            confirmText.clear();
            passwordWarning.setVisibility(View.GONE);
        });
    }
}