package com.example.reflorestar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class CameraActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private Button closeButton;
    private Button pineTreeButton;
    private Button elmTreeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //hide support action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        closeButton = findViewById(R.id.closeButton);
        pineTreeButton = findViewById(R.id.buttonPineTree);
        elmTreeButton = findViewById(R.id.buttonElmTree);

        closeButton.setOnClickListener(view -> {
                finish();
        });

        pineTreeButton.setOnClickListener(view -> {
            setUpPineTreeModel();
        });

        elmTreeButton.setOnClickListener(view -> {
            setUpElmTreeModel();
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setUpElmTreeModel();
        setUpPlane();
    }

    private void setUpElmTreeModel() {
        ModelRenderable.builder().setSource(this, R.raw.elmtree)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, "Model can't be loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPineTreeModel() {
        ModelRenderable.builder().setSource(this, R.raw.pinetree)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, "Model can't be loaded", Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPlane() {
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                createModel(anchorNode);
            }
        });
    }

    private void createModel(AnchorNode anchorNode){
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        node.select();
    }
}
