package com.example.reflorestar.ui.profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.reflorestar.R;
import com.example.reflorestar.classes.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ConstraintLayout fragmentContainer;
    private View root;
    public User user;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<Intent> launchUploadImageDialog;
    private DatabaseReference users, projects;
    private TextView paramName, paramUsername, paramEmail, paramUserType, paramProjects, paramTrees;
    private ImageView paramUserImage;
    private int treeCount = 0;
    private String newName = "";

    public ProfileFragment(User user) {
        this.user = user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        fragmentContainer = root.findViewById(R.id.profile_container);
        users = FirebaseDatabase.getInstance().getReference("users");
        projects = FirebaseDatabase.getInstance().getReference("projects");

        //shared preferences control
        sharedPreferences = root.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        paramName = root.findViewById(R.id.txtName);
        paramUsername = root.findViewById(R.id.txtUsername);
        paramEmail = root.findViewById(R.id.txtEmail);
        paramUserType = root.findViewById(R.id.paramUserType);
        paramProjects = root.findViewById(R.id.paramProjects);
        paramTrees = root.findViewById(R.id.paramPlantedTrees);
        paramUserImage = root.findViewById(R.id.imageUser);

        Button buttonLogout = root.findViewById(R.id.buttonLogout);
        Button configButton = root.findViewById(R.id.configButton);

        paramName.setText(user.name);
        paramUsername.setText(user.username);
        paramEmail.setText(user.email);
        paramUserType.setText(user.type.substring(0, 1).toUpperCase() + user.type.substring(1).toLowerCase());

        if (!user.photo.isEmpty()) {
            paramUserImage.setImageBitmap(getImageFromBase64EncodedString(user.photo));
        }


        users.child(user.username).child("projects").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> userProjects = (HashMap<String, Object>) dataSnapshot.getValue();
                if (userProjects != null) {
                    paramProjects.setText(String.valueOf(userProjects.size()));
                    userProjects.forEach((key, value) ->
                            projects.child(key).child("trees").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                    ArrayList<Object> projectTrees = (ArrayList<Object>) dataSnapshot.getValue();
                                    if (projectTrees != null) {
                                        treeCount += projectTrees.size();
                                        /*for (Object projectTree : projectTrees) {
                                            Log.e("Project Tree: ", projectTree.toString());
                                        }*/
                                        paramTrees.setText(String.valueOf(treeCount));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            })
                    );
                } else {
                    paramProjects.setText("0");
                    paramTrees.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        prepareUploadImageDialog();

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
        if (getActivity() != null) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.profile_container, new AccountHomeFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("profile_page").commit();
            fragmentContainer.removeAllViews();
            sharedPreferences.edit().remove("username").apply();
            Log.e("Logged user: ", sharedPreferences.getString("username", "null"));
        }
    }

    protected boolean settingsMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.profile_upload_image) {
            launchUploadImageDialog();
            return true;
        } else if (item.getItemId() == R.id.profile_remove_image) {
            removeProfileImage();
            return true;
        } else if (item.getItemId() == R.id.profile_about) {
            launchEditNameDialog();
            return true;
        }
        return false;
    }

    private void launchEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
        builder.setTitle(getString(R.string.insert_name));

        final EditText input = new EditText(root.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            newName = input.getText().toString();
            paramName.setText(newName);
            users.child(user.username).child("name").setValue(newName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void prepareUploadImageDialog() {
        launchUploadImageDialog = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                            String encodedBase64String = getBase64EncodedStringFromImage(bitmap);
                            users.child(user.username).child("photo").setValue(encodedBase64String);
                            Log.i("base64 image: ", encodedBase64String);
                            paramUserImage.setImageBitmap(getImageFromBase64EncodedString(encodedBase64String));
                        } catch (IOException e) {
                            Log.i("ERROR", "Error uploading image: " + e);
                        }
                    }
                });
    }

    private void launchUploadImageDialog() {
        Intent selectImageIntent = new Intent(Intent.ACTION_PICK);
        selectImageIntent.setType("image/*");
        launchUploadImageDialog.launch(selectImageIntent);
    }

    public String getBase64EncodedStringFromImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteFormat = stream.toByteArray();
        String imgBase64 = Base64.encodeToString(byteFormat, Base64.NO_WRAP);

        return imgBase64;
    }

    public Bitmap getImageFromBase64EncodedString(String encodedBase64String) {
        byte[] decodedString = Base64.decode(encodedBase64String, Base64.DEFAULT);
        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedImg;
    }

    private void removeProfileImage() {
        users.child(user.username).child("photo").setValue("");
        paramUserImage.setImageResource(R.drawable.ic_user);
    }
}