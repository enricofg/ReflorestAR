package com.example.reflorestar.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;
import com.example.reflorestar.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

public class AccountHomeFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private boolean authenticated = false;
    private ConstraintLayout fragmentContainer;
    private LinearLayout buttonsContainer;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_home, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        fragmentContainer = root.findViewById(R.id.account_home_container);
        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

        //shared preferences control
        sharedPreferences = root.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String authUser = sharedPreferences.getString("username", null);

        buttonsContainer = root.findViewById(R.id.homeButtonsContainer);

        if (authUser != null) {
            // User is signed in
            users.child(authUser).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                //user found
                                User user = dataSnapshot.getValue(User.class);
                                accessProfile(fragmentContainer, user);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            throw databaseError.toException();
                        }
                    });
        } else{
            buttonsContainer.setVisibility(View.VISIBLE);
        }

        //view buttons
        Button buttonLogin = root.findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = root.findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> {
            loginPage(fragmentContainer);
        });

        buttonCreateAccount.setOnClickListener(v -> {
            createAccount(fragmentContainer);
        });

        return root;
    }

    private void accessProfile(ConstraintLayout fragmentContainer, User user) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new ProfileFragment(user)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void loginPage(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new AccountLoginFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void createAccount(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new AccountCreateFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }
}