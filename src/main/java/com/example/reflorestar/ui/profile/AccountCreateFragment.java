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

public class AccountCreateFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_create, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        fragmentContainer = root.findViewById(R.id.create_account_container);

        Button buttonConfirmAccount = root.findViewById(R.id.buttonConfirmAccount);
        Button backButton = root.findViewById(R.id.backButtonCreateAcc);

        buttonConfirmAccount.setOnClickListener(v -> {
            accessProfile(fragmentContainer);
        });

        backButton.setOnClickListener(v -> {
            returnToAccountHome(fragmentContainer);
        });

        return root;
    }

    private void accessProfile(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.create_account_container, new ProfileFragment()).addToBackStack( "account_login" ).commit();
        fragmentContainer.removeAllViews();
    }

    private void returnToAccountHome(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.create_account_container, new AccountHomeFragment()).addToBackStack( "account_login" ).commit();
        fragmentContainer.removeAllViews();
    }
}