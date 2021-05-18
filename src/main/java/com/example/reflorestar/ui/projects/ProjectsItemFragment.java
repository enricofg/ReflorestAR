package com.example.reflorestar.ui.projects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.reflorestar.R;
import com.example.reflorestar.ui.catalog.CatalogFragment;

public class ProjectsItemFragment extends Fragment {

    private Button backButton;
    private String paramProjectName, paramDescription, paramAvailability, paramStatus, paramOwnerName, paramOwnerEmail;
    private int paramProjectId;
    private ConstraintLayout fragmentContainer;
    private View root;

    public ProjectsItemFragment(String projectName, String description, String availability, String status, String projectId, String ownerName, String ownerEmail) {
        paramProjectName = projectName;
        paramDescription = description;
        paramAvailability = availability;
        paramStatus = status;
        paramProjectId = Integer.parseInt(projectId);
        paramOwnerName = ownerName;
        paramOwnerEmail = ownerEmail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_projects_item, container, false);

        fragmentContainer = root.findViewById(R.id.fragment_container);

        TextView projectName = root.findViewById(R.id.projectName);
        TextView description = root.findViewById(R.id.projectDescription);
        TextView availability = root.findViewById(R.id.paramAvailability);
        TextView status = root.findViewById(R.id.paramStatus);
        TextView ownerName = root.findViewById(R.id.paramProjectOwner);
        TextView ownerEmail = root.findViewById(R.id.paramOwnerEmail);

        projectName.setText(this.paramProjectName);
        description.setText(this.paramDescription);
        availability.setText(this.paramAvailability);
        status.setText(this.paramStatus);
        ownerName.setText(this.paramOwnerName);
        ownerEmail.setText(this.paramOwnerEmail);

        backButton = (Button) root.findViewById(R.id.backButtonProj);
        backButton.setOnClickListener(v -> {
            returnToProjects(fragmentContainer);
        });

        return root;
    }

    private void returnToProjects(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, new ProjectsFragment()).addToBackStack( "project_item" ).commit();
        //fragmentContainer.setVisibility(View.INVISIBLE);
        fragmentContainer.removeAllViews();
    }
}

