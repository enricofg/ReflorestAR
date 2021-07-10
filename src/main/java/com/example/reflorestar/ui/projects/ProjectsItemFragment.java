package com.example.reflorestar.ui.projects;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.reflorestar.R;
import com.example.reflorestar.classes.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectsItemFragment extends Fragment {

    private ListItemAdapter adapter;
    private Button backButton;
    private String paramProjectName, paramDescription, paramAvailability, paramStatus, paramOwnerName, paramOwnerUsername, paramOwnerEmail, paramPhoto, authUser;
    private TextView shareMessage, usernameWarning;
    private LinearLayout containerShareInput;
    private ConstraintLayout fragmentContainer;
    private ListView userlist;
    private DatabaseReference database, projects, users;
    private SharedPreferences sharedPreferences;
    private View root;
    private BottomNavigationView navBar;
    private ViewGroup container;

    public ProjectsItemFragment(String projectName, String description, String availability, String status, String ownerName, String username, String ownerEmail, String photo) {
        paramProjectName = projectName;
        paramDescription = description;
        paramAvailability = availability;
        paramStatus = status;
        paramOwnerName = ownerName;
        paramOwnerUsername = username;
        paramOwnerEmail = ownerEmail;
        paramPhoto = photo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;
        navBar = getActivity().findViewById(R.id.nav_view);
        toggleNavBar();
        root = inflater.inflate(R.layout.fragment_projects_item, container, false);
        fragmentContainer = root.findViewById(R.id.project_item_container);
        userlist = root.findViewById(R.id.userAccessList);

        sharedPreferences = root.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        database = FirebaseDatabase.getInstance().getReference();
        projects = database.child("projects");
        users = database.child("users");

        //share project controls
        shareMessage = root.findViewById(R.id.textViewShareMessage);
        containerShareInput = root.findViewById(R.id.containerShareInput);
        TextInputLayout usernameInput = root.findViewById(R.id.insertUsername);
        Editable usernameText = usernameInput.getEditText().getText();
        usernameWarning = root.findViewById(R.id.usernameWarning2);

        authUser = sharedPreferences.getString("username", null);
        if (authUser == null || !authUser.equals(paramOwnerUsername)) {
            shareMessage.setVisibility(View.GONE);
            containerShareInput.setVisibility(View.GONE);
        } else {
            Button shareButton = root.findViewById(R.id.buttonShare);
            shareButton.setOnClickListener(v -> {
                if (!usernameText.toString().isEmpty() && !usernameText.toString().equals(authUser)) {
                    users.child(usernameText.toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User user = dataSnapshot.getValue(User.class);
                                ShareProjectDialog alert = new ShareProjectDialog();
                                alert.showDialog(getActivity(), user.name, user.username, user.email, user.photo);

                            } else {
                                usernameWarning.setVisibility(View.VISIBLE);
                                usernameWarning.setText(getString(R.string.not_found_username));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                } else{
                    usernameWarning.setVisibility(View.VISIBLE);
                    usernameWarning.setText(getString(R.string.share_error));
                }
            });
        }

        //populate project info page
        TextView projectName = root.findViewById(R.id.projectName);
        TextView description = root.findViewById(R.id.projectDescription);
        TextView availability = root.findViewById(R.id.paramAvailability);
        TextView status = root.findViewById(R.id.paramStatus);
        TextView ownerName = root.findViewById(R.id.paramProjectOwner);
        TextView ownerUsername = root.findViewById(R.id.paramOwnerUsername);
        TextView ownerEmail = root.findViewById(R.id.paramOwnerEmail);
        ImageView ownerPhoto = root.findViewById(R.id.thumbnailPicture);

        projectName.setText(this.paramProjectName);
        description.setText(this.paramDescription);
        availability.setText(this.paramAvailability);
        status.setText(this.paramStatus);
        ownerName.setText(this.paramOwnerName);
        ownerUsername.setText(this.paramOwnerUsername);
        ownerEmail.setText(this.paramOwnerEmail);

        if (!this.paramPhoto.isEmpty()) {
            ownerPhoto.setImageBitmap(getImageFromBase64EncodedString(this.paramPhoto));
        }

        //shared user list
        buildUserList();

        //listeners
        usernameInput.setEndIconOnClickListener(view -> {
            usernameText.clear();
            usernameWarning.setVisibility(View.GONE);
        });

        backButton = (Button) root.findViewById(R.id.backButtonProj);
        backButton.setOnClickListener(v -> {
            returnToProjects(fragmentContainer);
        });

        return root;
    }

    private void toggleNavBar() {
        Transition transition = new Fade();
        transition.setDuration(350);
        transition.addTarget(navBar);
        TransitionManager.beginDelayedTransition(container, transition);
        navBar.setVisibility(navBar.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    private void buildUserList() {
        projects.child(this.paramProjectName).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                HashMap<String, Object> projectUsers = (HashMap<String, Object>) dataSnapshot.getValue();
                if (projectUsers != null) {
                    ArrayList<HashMap<String, Object>> projectUserList = new ArrayList<>();
                    projectUsers.forEach((key, value) ->
                            users.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                    HashMap<String, Object> projectUser = (HashMap<String, Object>) dataSnapshot.getValue();
                                    if (projectUser != null) {
                                        Log.e("User: ", projectUser.toString());
                                        projectUserList.add(projectUser);
                                    }
                                    getProjectUserListView(projectUserList);
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            })
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getProjectUserListView(ArrayList<HashMap<String, Object>> projectUserList) {
        Log.e("Userlist: ", " length: " + projectUserList.size() + projectUserList.toString());
        adapter = new ProjectsItemFragment.ListItemAdapter(root.getContext(), projectUserList);
        userlist.setAdapter(adapter);
    }

    public Bitmap getImageFromBase64EncodedString(String encodedBase64String) {
        byte[] decodedString = Base64.decode(encodedBase64String, Base64.DEFAULT);
        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedImg;
    }

    private void returnToProjects(ConstraintLayout fragmentContainer) {
        if (getActivity() != null) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.project_item_container, new ProjectsFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("project_item").commit();
            //fragmentContainer.setVisibility(View.INVISIBLE);
            fragmentContainer.removeAllViews();
            toggleNavBar();
        }
    }

    class ListItemAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String, Object>> data;

        public ListItemAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ProjectsItemFragment.ListItemAdapter.ViewHolder holder;
            if (convertView == null) {
                holder = new ProjectsItemFragment.ListItemAdapter.ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.user_list_item, null);
                holder.userPicture = convertView.findViewById(R.id.thumbnailUserListPicture);
                holder.txtName = convertView.findViewById(R.id.paramUserListName);
                holder.txtUsername = convertView.findViewById(R.id.paramUserListUsername);
                holder.txtEmail = convertView.findViewById(R.id.paramUserListEmail);
                holder.removeButton = convertView.findViewById(R.id.buttonRemove);
                convertView.setTag(holder);
            } else {
                holder = (ProjectsItemFragment.ListItemAdapter.ViewHolder) convertView.getTag();
            }

            try {
                if (!data.get(position).get("photo").toString().isEmpty()) {
                    holder.userPicture.setImageBitmap(getImageFromBase64EncodedString(data.get(position).get("photo").toString()));
                }
                holder.txtName.setText(data.get(position).get("name").toString());
                holder.txtUsername.setText(data.get(position).get("username").toString());
                holder.txtEmail.setText(data.get(position).get("email").toString());
                if (authUser != null && authUser.equals(paramOwnerUsername) && !authUser.equals(data.get(position).get("username").toString())) {
                    holder.removeButton.setVisibility(View.VISIBLE);
                    holder.removeButton.setOnClickListener(v -> {
                        users.child(data.get(position).get("username").toString()).child("projects").child(paramProjectName).removeValue();
                        projects.child(paramProjectName).child("users").child(data.get(position).get("username").toString()).removeValue();
                        buildUserList();
                    });
                }
            } catch (Exception e) {
                Log.e("Error:", e.getMessage());
            }

            return convertView;
        }

        class ViewHolder {
            ImageView userPicture;
            TextView txtName;
            TextView txtUsername;
            TextView txtEmail;
            Button removeButton;
        }
    }

    public class ShareProjectDialog {

        public Button confirm, cancel;
        public ImageView paramUserPicture;
        public TextView paramUserName, paramUserUsername, paramUserEmail;
        public CheckBox readonly;

        public void showDialog(Activity activity, String name, String username, String email, String photo){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_share_project);

            readonly = dialog.findViewById(R.id.checkBoxReadOnly);
            paramUserPicture = dialog.findViewById(R.id.thumbnailConfirmDialogPicture);
            paramUserName = dialog.findViewById(R.id.paramConfirmDialogName);
            paramUserUsername = dialog.findViewById(R.id.paramConfirmDialogUsername);
            paramUserEmail = dialog.findViewById(R.id.paramConfirmDialogEmail);

            if(!photo.isEmpty()){
                paramUserPicture.setImageBitmap(getImageFromBase64EncodedString(photo));
            }

            paramUserName.setText(name);
            paramUserUsername.setText(username);
            paramUserEmail.setText(email);

            confirm = (Button) dialog.findViewById(R.id.buttonConfirm);
            cancel = (Button) dialog.findViewById(R.id.buttonCancel);

            confirm.setOnClickListener(v -> {
                if(!readonly.isChecked()){
                    users.child(username).child("projects").child(paramProjectName).setValue("full");
                } else{
                    users.child(username).child("projects").child(paramProjectName).setValue("readonly");
                }
                projects.child(paramProjectName).child("users").child(username).setValue("true");
                buildUserList();
                dialog.dismiss();
            });

            cancel.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();

        }
    }
}

