package com.example.reflorestar.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference user = mDatabase.child("1");

        TextView paramName = root.findViewById(R.id.txtName);
        TextView paramEmail = root.findViewById(R.id.txtEmail);
        ImageView paramUserImage = root.findViewById(R.id.imageUser);


        user.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of trees in datasnapshot
                        HashMap<String, Object> user = (HashMap<String, Object>) dataSnapshot.getValue();

                        paramName.setText(user.get("full_name").toString());
                        paramEmail.setText(user.get("email").toString());
                        Picasso.get().load(user.get("photo").toString()).into(paramUserImage);

                        Log.e("class:", user.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        /*final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        return root;
    }
}