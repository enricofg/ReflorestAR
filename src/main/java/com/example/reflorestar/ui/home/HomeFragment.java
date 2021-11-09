package com.example.reflorestar.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.reflorestar.CameraActivity;
import com.example.reflorestar.R;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {

    private Button cameraButton;
    private ScrollView scrollViewHome;
    private TextView textViewScrollMessage;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        scrollViewHome = root.findViewById(R.id.scrollViewHome);
        textViewScrollMessage = root.findViewById(R.id.textViewScrollMessage);
        cameraButton = root.findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                Toast.makeText(root.getContext(), getString(R.string.camera_message), Toast.LENGTH_SHORT).show();
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            } else{
                openCameraActivity();
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View child = (View) scrollViewHome.getChildAt(0);
        if (child != null) {
            int childHeight = (child).getHeight();
            boolean scrollable = scrollViewHome.getHeight() < childHeight + scrollViewHome.getPaddingTop() + scrollViewHome.getPaddingBottom();

            if(!scrollable){
                textViewScrollMessage.setVisibility(View.GONE);
            }
        }
    }

    private void openCameraActivity() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivity(intent);
    }
}