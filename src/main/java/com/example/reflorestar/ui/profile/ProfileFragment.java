package com.example.reflorestar.ui.profile;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.reflorestar.R;
import com.example.reflorestar.classes.User;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;
    public User user;
    private SharedPreferences sharedPreferences;

    public ProfileFragment(User user){
        this.user = user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        fragmentContainer = root.findViewById(R.id.profile_container);

        //shared preferences control
        sharedPreferences = root.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        TextView paramName = root.findViewById(R.id.txtName);
        TextView paramEmail = root.findViewById(R.id.txtEmail);
        TextView paramUserType = root.findViewById(R.id.paramUserType);
        ImageView paramUserImage = root.findViewById(R.id.imageUser);
        Button buttonLogout = root.findViewById(R.id.buttonLogout);
        Button configButton = root.findViewById(R.id.configButton);

        paramName.setText(user.name);
        paramEmail.setText(user.email);
        paramUserType.setText(user.type);

        configButton.setOnClickListener(
                v -> {
                    PopupMenu popup = new PopupMenu(root.getContext(), v);
                    popup.setOnMenuItemClickListener(this::settingsMenuClick);
                    popup.inflate(R.menu.profile_settings_menu);
                    popup.show();
                });

        buttonLogout.setOnClickListener(v -> {
            logout(fragmentContainer);
        });

        return root;
    }

    private void logout(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.profile_container, new AccountHomeFragment()).addToBackStack("profile_page").commit();
        fragmentContainer.removeAllViews();
        sharedPreferences.edit().remove("username").apply();
        Log.e("Logged user: ", sharedPreferences.getString("username", "null"));
    }

    /** Menu button to launch feature specific settings. */
    protected boolean settingsMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.profile_upload_image) {
            //launchUploadImageDialog();
            return true;
        } else if (item.getItemId() == R.id.profile_about) {
            //launchAboutDialog();
            return true;
        }
        return false;
    }
}