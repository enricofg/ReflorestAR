package com.example.reflorestar.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;
import com.example.reflorestar.ui.projects.ProjectsFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AccountLoginFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_login, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.login_container);

        Button buttonLogin = root.findViewById(R.id.buttonLoginCredentials);
        Button backButton = root.findViewById(R.id.backButtonLogin);

        buttonLogin.setOnClickListener(v -> {
            accessProfile(fragmentContainer);

        });

        backButton.setOnClickListener(v -> {
            returnToAccountHome(fragmentContainer);
        });

        return root;
    }

    private void returnToAccountHome(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.login_container, new AccountHomeFragment()).addToBackStack( "account_login" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void accessProfile(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.login_container, new ProfileFragment()).addToBackStack( "account_login" ).commit();
        fragmentContainer.removeAllViews();
    }
}