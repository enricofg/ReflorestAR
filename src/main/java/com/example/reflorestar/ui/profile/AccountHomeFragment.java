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
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.account_home_container);

        if(authenticated == false){
            Button buttonLogin = root.findViewById(R.id.buttonLogin);
            Button buttonCreateAccount = root.findViewById(R.id.buttonCreateAccount);

            buttonLogin.setOnClickListener(v -> {
                loginPage(fragmentContainer);
            });

            buttonCreateAccount.setOnClickListener(v -> {
                createAccount(fragmentContainer);
            });

            return root;
        } else{
            accessProfile(fragmentContainer);
        }

        return root;
    }

    private void loginPage(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new AccountLoginFragment()).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void createAccount(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new AccountCreateFragment()).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void accessProfile(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.account_home_container, new ProfileFragment()).addToBackStack( "account_home" ).commit();
        fragmentContainer.removeAllViews();
    }
}