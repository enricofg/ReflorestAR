package com.example.reflorestar.ui.projects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.reflorestar.R;

public class ProjectsItemFragment extends Fragment {

    private Button backButton;
    private String paramProjectName, paramDescription, paramAvailability, paramStatus, paramOwnerName, paramOwnerUsername, paramOwnerEmail, paramPhoto;
    private ConstraintLayout fragmentContainer;
    private View root;

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
        root = inflater.inflate(R.layout.fragment_projects_item, container, false);

        fragmentContainer = root.findViewById(R.id.project_item_container);

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

        if(!this.paramPhoto.isEmpty()){
            ownerPhoto.setImageBitmap(getImageFromBase64EncodedString(this.paramPhoto));
        }

        backButton = (Button) root.findViewById(R.id.backButtonProj);
        backButton.setOnClickListener(v -> {
            returnToProjects(fragmentContainer);
        });

        return root;
    }

    public Bitmap getImageFromBase64EncodedString(String encodedBase64String) {
        byte[] decodedString = Base64.decode(encodedBase64String, Base64.DEFAULT);
        Bitmap decodedImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        return decodedImg;
    }

    private void returnToProjects(ConstraintLayout fragmentContainer) {
        if(getActivity()!=null){
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.project_item_container, new ProjectsFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("project_item").commit();
            //fragmentContainer.setVisibility(View.INVISIBLE);
            fragmentContainer.removeAllViews();
        }
    }
}

