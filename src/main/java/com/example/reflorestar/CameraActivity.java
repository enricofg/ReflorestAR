package com.example.reflorestar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class CameraActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    //private GestureDetector gestureDetector;
    private Button closeButton, configButton;
    private LinearLayout containerOptions;
    private SeekBar sliderQuant;
    private Button pinusPinasterButton;
    private Button pineTree2Button;
    private Button thirdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //hide support action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View cameraContainer = findViewById(R.id.layoutCamera);
        closeButton = findViewById(R.id.closeButton);
        configButton = findViewById(R.id.configButtonCam);
        sliderQuant = findViewById(R.id.seekBarTreeQuantity);
        containerOptions = findViewById(R.id.containerCamOptions);
        containerOptions.setVisibility(View.GONE);


        pinusPinasterButton = findViewById(R.id.buttonPinusPinaster);
        pineTree2Button = findViewById(R.id.buttonTree2);


        closeButton.setOnClickListener(view -> {
            finish();
        });

        configButton.setOnClickListener(view -> {
            Transition transition = new Fade();
            transition.setDuration(200);
            transition.addTarget(containerOptions);

            TransitionManager.beginDelayedTransition((ViewGroup) cameraContainer, transition);
            containerOptions.setVisibility(containerOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        int maxValue=sliderQuant.getMax(); // get maximum value of the Seek bar
        Log.e("Max Value:", String.valueOf(maxValue));

        sliderQuant.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seekBarValue= sliderQuant.getProgress();
                Log.e("Progress Value:", String.valueOf(seekBarValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pinusPinasterButton.setOnClickListener(view -> {
            setUpPinusPinasterModel();
        });
        pineTree2Button.setOnClickListener(view -> {
            setUpPineTree2Model();
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setUpPinusPinasterModel();
        setUpPlane();
    }

    private void setUpPinusPinasterModel() {
        ModelRenderable.builder().setSource(this, R.raw.pinuspinaster)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, "Model can't be loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPineTree2Model() {
        ModelRenderable.builder().setSource(this, R.raw.pinetree)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, "Model can't be loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPlane() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            createModel(anchorNode);
        });
    }

    private void createModel(AnchorNode anchorNode){
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();
    }
}
