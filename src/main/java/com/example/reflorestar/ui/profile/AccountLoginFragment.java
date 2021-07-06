package com.example.reflorestar.ui.profile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

public class AccountLoginFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;
    private TextInputLayout usernameInput, passwordInput;
    private TextView passwordWarning;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_account_login, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.login_container);

        Button buttonLogin = root.findViewById(R.id.buttonLoginCredentials);
        Button backButton = root.findViewById(R.id.backButtonLogin);

        //login controls
        usernameInput = root.findViewById(R.id.loginInputUsername);
        passwordInput = root.findViewById(R.id.loginInputPassword);
        Editable usernameText = usernameInput.getEditText().getText();
        Editable passwordText = passwordInput.getEditText().getText();
        passwordWarning = root.findViewById(R.id.passwordWarningLogin);

        buttonLogin.setOnClickListener(v -> {
            //accessProfile(fragmentContainer);
            if(!usernameText.toString().isEmpty() && !passwordText.toString().isEmpty()){
                users.child(usernameText.toString()).addListenerForSingleValueEvent( //users.orderByChild("email").equalTo(emailText.toString().toLowerCase())
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    //user found
                                    try {
                                        User user = dataSnapshot.getValue(User.class);
                                        String hashedPassword = new User().hashPassword(passwordText.toString());
                                        if(hashedPassword.equals(user.getPassword())){
                                            accessProfile(fragmentContainer, user);
                                        } else{
                                            showInputWarning(passwordWarning, getString(R.string.incorrect_password));
                                        }
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                        //no such algorithm
                                    }

                                } else {
                                    showMessage(getString(R.string.not_found), getString(R.string.not_found_warning));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                throw databaseError.toException();
                            }
                        });
            } else{
                showMessage(getString(R.string.empty_fields), getString(R.string.empty_fields_warning));
            }
        });

        backButton.setOnClickListener(v -> {
            returnToAccountHome(fragmentContainer);
        });

        passwordInput.setEndIconOnClickListener(view -> {
            passwordText.clear();
            passwordWarning.setVisibility(View.GONE);
        });

        return root;
    }

    private void returnToAccountHome(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.login_container, new AccountHomeFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack( "account_login" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void accessProfile(ConstraintLayout fragmentContainer, User user) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.login_container, new ProfileFragment(user)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack( "account_login" ).commit();
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
}