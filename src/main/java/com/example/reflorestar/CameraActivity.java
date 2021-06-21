package com.example.reflorestar;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class CameraActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private LinearLayout containerOptions;
    private SeekBar sliderQuant;
    private Button closeButton, configButton, pinusPinasterButton, pineTree2Button;

    //distance variables
    private AnchorNode currentAnchorNode, lastAnchorNode;
    private TextView tvDistance;
    private Anchor currentAnchor = null;
    private Anchor lastAnchor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //hide support action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //camera view containers
        View cameraContainer = findViewById(R.id.layoutCamera);
        containerOptions = findViewById(R.id.containerCamOptions);
        containerOptions.setVisibility(View.GONE);

        //quantity slider
        sliderQuant = findViewById(R.id.seekBarTreeQuantity);

        //buttons
        closeButton = findViewById(R.id.closeButton);
        configButton = findViewById(R.id.configButtonCam);
        pinusPinasterButton = findViewById(R.id.buttonPinusPinaster);
        pineTree2Button = findViewById(R.id.buttonTree2);

        //distance information
        tvDistance = findViewById(R.id.tvDistance);
        tvDistance.setVisibility(View.INVISIBLE);

        //close camera
        closeButton.setOnClickListener(view -> {
            finish();
        });

        //open config menu
        configButton.setOnClickListener(view -> {
            Transition transition = new Fade();
            transition.setDuration(200);
            transition.addTarget(containerOptions);

            TransitionManager.beginDelayedTransition((ViewGroup) cameraContainer, transition);
            containerOptions.setVisibility(containerOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        //slider options
        int maxValue = sliderQuant.getMax(); // get maximum value of the Seek bar
        Log.e("Max Value:", String.valueOf(maxValue));

        sliderQuant.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seekBarValue = sliderQuant.getProgress();
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
            // Creating Anchor
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            if (lastAnchor == null) {
                lastAnchor = anchor;
                lastAnchorNode = anchorNode;
            } else {
                lastAnchor = currentAnchor;
                lastAnchorNode = currentAnchorNode;
            }
            currentAnchor = anchor;
            currentAnchorNode = anchorNode;

            if(currentAnchorNode != null && lastAnchorNode != null && currentAnchorNode!=lastAnchorNode) {
                float dist = getDistanceBetweenVectorsInMeters(currentAnchor, lastAnchor);
                if(dist<0.8){
                    Toast.makeText(this, "Distance between trees is too small. Place tree at a farther distance.", Toast.LENGTH_SHORT).show();
                    currentAnchor = lastAnchor;
                    currentAnchorNode = lastAnchorNode;
                } else{
                    createModel(anchorNode);
                }
            } else{
                createModel(anchorNode);
            }

            //clearAnchor();

            /*Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
            createModel(anchorNode);*/
        });
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        //arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();

        /*TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();*/
    }

    private void clearAnchor() {
        currentAnchor = null;

        if (currentAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(currentAnchorNode);
            currentAnchorNode.getAnchor().detach();
            currentAnchorNode.setParent(null);
            currentAnchorNode = null;
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        if (currentAnchorNode != null && lastAnchorNode != null && currentAnchorNode != lastAnchorNode) {
            tvDistance.setVisibility(View.VISIBLE);
            tvDistance.setText("Distance between current node and last node: " + getDistanceBetweenVectorsInMeters(currentAnchor, lastAnchor) + " metres");

                                /*float[] distance_vector = currentAnchor.getPose().inverse()
                    .compose(cameraPose).getTranslation();
            float totalDistanceSquared = 0;
            for (int i = 0; i < 3; ++i)
                totalDistanceSquared += distance_vector[i] * distance_vector[i];*/
        }
    }

    private float getDistanceBetweenVectorsInMeters(Anchor currentAnchor, Anchor lastAnchor) {
        Pose objectPose1 = currentAnchor.getPose();
        Pose objectPose2 = lastAnchor.getPose();

        float distanceX = objectPose1.tx() - objectPose2.tx();
        float distanceY = objectPose1.ty() - objectPose2.ty();
        float distanceZ = objectPose1.tz() - objectPose2.tz();

        //straight line distance in meters
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
    }
}
