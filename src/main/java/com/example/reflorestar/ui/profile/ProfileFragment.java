package com.example.reflorestar.ui.profile;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reflorestar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        fragmentContainer = root.findViewById(R.id.profile_container);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference user = mDatabase.child("matiasarielol");

        TextView paramName = root.findViewById(R.id.txtName);
        TextView paramEmail = root.findViewById(R.id.txtEmail);
        ImageView paramUserImage = root.findViewById(R.id.imageUser);
        Button buttonLogout = root.findViewById(R.id.buttonLogout);

        user.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.e("dataSnapshot", String.valueOf(dataSnapshot.getValue()));
                        if(dataSnapshot.getValue()!=null){
                            HashMap<String, Object> userProfile = (HashMap<String, Object>) dataSnapshot.getValue();
                            Log.e("dataSnapshot", userProfile.toString());

                            HashMap<String, Object> user = (HashMap<String, Object>) dataSnapshot.getValue();
                            paramName.setText(user.get("full_name").toString());
                            paramEmail.setText(user.get("email").toString());
                            Picasso.get().load(user.get("photo").toString()).error(R.drawable.ic_user).into(paramUserImage);
                        }
                        //Log.e("user:", user.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        buttonLogout.setOnClickListener(v -> {
            logout(fragmentContainer);
        });

        return root;
    }

    private void logout(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.profile_container, new AccountHomeFragment()).addToBackStack( "profile_page" ).commit();
        fragmentContainer.removeAllViews();
    }
}