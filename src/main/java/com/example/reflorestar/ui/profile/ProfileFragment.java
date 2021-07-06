package com.example.reflorestar.ui.profile;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reflorestar.R;
import com.example.reflorestar.classes.User;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;
    public User user;

    public ProfileFragment(User user){
        this.user = user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        fragmentContainer = root.findViewById(R.id.profile_container);

        TextView paramName = root.findViewById(R.id.txtName);
        TextView paramEmail = root.findViewById(R.id.txtEmail);
        ImageView paramUserImage = root.findViewById(R.id.imageUser);
        Button buttonLogout = root.findViewById(R.id.buttonLogout);

        paramName.setText(user.name);
        paramEmail.setText(user.email);

        buttonLogout.setOnClickListener(v -> {
            logout(fragmentContainer);
        });

        return root;
    }

    private void logout(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.profile_container, new AccountHomeFragment()).addToBackStack("profile_page").commit();
        fragmentContainer.removeAllViews();
    }
}