package com.example.reflorestar.ui.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.CameraActivity;
import com.example.reflorestar.R;

public class CameraFragment extends Fragment {

    private CameraViewModel cameraViewModel;
    private Button cameraButton;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cameraViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);
        root = inflater.inflate(R.layout.fragment_camera, container, false);

        cameraButton = root.findViewById(R.id.cameraButton);

        cameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(root.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                showMessage(getString(R.string.camera_message), getString(R.string.access_warning));
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            } else{
                openCameraActivity();
            }
        });

        return root;
    }

    private void openCameraActivity() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivity(intent);
    }

    public void showMessage(String message, String warning) {
        AlertDialog alertDialog = new AlertDialog.Builder(root.getContext()).create();
        alertDialog.setTitle(warning);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
}