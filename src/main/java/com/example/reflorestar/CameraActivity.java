package com.example.reflorestar;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
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
    //private GestureDetector gestureDetector;
    private Button closeButton;
    private Button pineTreeButton;
    private Button elmTreeButton;
    private Button thirdButton;

    /*private static final int SWIPE_DISTANCE_THRESHOLD = 125;
    private static final int SWIPE_VELOCITY_THRESHOLD = 75;*/

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
        thirdButton = findViewById(R.id.buttonThree);

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

        /*View viewCamera = findViewById(R.id.modelOptions);
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        viewCamera.setOnTouchListener(touchListener);*/
    }

    private void setUpElmTreeModel() {
        ModelRenderable.builder().setSource(this, R.raw.pinus_sylvestris)
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

    /*View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);

        }
    };

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            float middle = pineTreeButton.getX();
            float left = elmTreeButton.getX();
            float right = thirdButton.getX();

            float distanceX = event2.getX() - event1.getX();
            float distanceY = event2.getY() - event1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) >
                    SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                // change picture to
                if (distanceX > 0) {
                    // start left increment
                    ObjectAnimator animX1 = ObjectAnimator.ofFloat(pineTreeButton, "x", left);
                    ObjectAnimator animX2 = ObjectAnimator.ofFloat(elmTreeButton, "x", right);
                    ObjectAnimator animX3 = ObjectAnimator.ofFloat(thirdButton, "x", middle);
                    AnimatorSet animSetXY = new AnimatorSet();
                    animSetXY.playTogether(animX1, animX2, animX3);
                    animSetXY.start();
                    //Toast.makeText(CameraActivity.this, "Right", Toast.LENGTH_SHORT).show();

                }
                else {  // the left
                    // start right increment
                    //Toast.makeText(CameraActivity.this, "Left", Toast.LENGTH_SHORT).show();
                }
            }

            return true;
        }
    }*/
}
