package com.example.reflorestar.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
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
import com.example.reflorestar.classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

public class AccountCreateFragment extends Fragment {

    private View root;
    private ConstraintLayout fragmentContainer;
    private TextInputLayout emailInput, passwordInput, confirmPasswordInput, usernameInput, nameInput;
    private TextView emailWarning, passwordWarning, usernameWarning, nameWarning;
    private BottomNavigationView navBar;
    private ViewGroup container;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.container = container;
        navBar = getActivity().findViewById(R.id.nav_view);
        toggleNavBar();
        root = inflater.inflate(R.layout.fragment_account_create, container, false);
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.create_account_container);

        //shared preferences control
        sharedPreferences = root.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

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
        nameWarning = root.findViewById(R.id.nameWarning);

        buttonConfirmAccount.setOnClickListener(v -> {
            //validations:
            if (!usernameText.toString().isEmpty() && !emailText.toString().isEmpty() && !nameText.toString().isEmpty() && !passwordText.toString().isEmpty() && !confirmText.toString().isEmpty()) {

                validateUsername(usernameText);
                validateName(nameText);
                validateEmail(emailText);
                validatePassword(passwordText, confirmText);

                if (validateUsername(usernameText) && validateName(nameText) && validateEmail(emailText) && validatePassword(passwordText, confirmText)) {
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
                                                                accessProfile(fragmentContainer, newUser);
                                                                sharedPreferences.edit().putString("username", newUser.username).apply();
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

        setListeners(usernameText, emailText, nameText, passwordText, confirmText);

        return root;
    }

    private void toggleNavBar() {
        Transition transition = new Fade();
        transition.setDuration(350);
        transition.addTarget(navBar);
        TransitionManager.beginDelayedTransition(container, transition);
        navBar.setVisibility(navBar.getVisibility()==View.GONE ? View.VISIBLE : View.GONE);
    }

    private void returnToAccountHome(ConstraintLayout fragmentContainer) {
        if(getActivity()!=null){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.create_account_container, new AccountHomeFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("create_account").commit();
            fragmentContainer.removeAllViews();
            toggleNavBar();
        }
    }

    private void accessProfile(ConstraintLayout fragmentContainer, User user) {
        if(getActivity()!=null){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.create_account_container, new ProfileFragment(user)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("create_account").commit();
            fragmentContainer.removeAllViews();
            toggleNavBar();
        }
    }

    private void showMessage(String message, String warning) {
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

    public boolean validateName(Editable usernameText) {
        if(usernameText.length()>=50){
            showInputWarning(nameWarning, getString(R.string.username_error2));
            return false;
        }
        if (!usernameText.toString().matches("^[a-zA-Z-]{3,}$")) {
            showInputWarning(nameWarning, getString(R.string.name_error));
            return false;
        }
        nameWarning.setVisibility(View.GONE);
        return true;
    }

    public boolean validateUsername(Editable usernameText) {
        if(usernameText.length()>=20){
            showInputWarning(usernameWarning, getString(R.string.username_error2));
            return false;
        }
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

    private void setListeners(Editable usernameText, Editable emailText, Editable nameText, Editable passwordText, Editable confirmText) {
        usernameInput.setEndIconOnClickListener(view -> {
            usernameText.clear();
            usernameWarning.setVisibility(View.GONE);
        });

        emailInput.setEndIconOnClickListener(view -> {
            emailText.clear();
            emailWarning.setVisibility(View.GONE);
        });

        nameInput.setEndIconOnClickListener(view -> {
            nameText.clear();
            nameWarning.setVisibility(View.GONE);
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