package com.example.reflorestar.ui.profile;

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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;

public class AccountHomeFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private boolean authenticated = false;
    private ConstraintLayout fragmentContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_home, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        fragmentContainer = root.findViewById(R.id.account_home_container);

        //view buttons
        Button buttonLogin = root.findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = root.findViewById(R.id.buttonCreateAccount);

        buttonLogin.setOnClickListener(v -> {
            loginPage(fragmentContainer);
        });

        buttonCreateAccount.setOnClickListener(v -> {
            createAccount(fragmentContainer);
        });

        /*if (user != null) {
            // User is signed in
            accessProfile(fragmentContainer);
        } else {
            // No user is signed in

        }*/

        return root;
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