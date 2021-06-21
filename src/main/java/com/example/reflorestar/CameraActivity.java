package com.example.reflorestar;

import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private LinearLayout containerOptions;
    private SeekBar sliderQuant;
    private Button closeButton, configButton, pinusPinasterButton, pineTree2Button;

    //tree variables
    private AnchorNode currentAnchorNode, lastAnchorNode;
    private TextView tvDistance;
    private Anchor currentAnchor = null;
    private Anchor lastAnchor = null;
    private ArrayList<AnchorNode> anchorList;
    private float globalTreeHeight;

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

        //distance information and controls
        tvDistance = findViewById(R.id.tvDistance);
        tvDistance.setVisibility(View.INVISIBLE);
        globalTreeHeight = 1.3f;

        //node list controls
        anchorList = new ArrayList();

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

        //slider changes
        sliderQuant.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                globalTreeHeight = 1.3f + progress*0.01f;
                //Log.e("Progress Value:", String.valueOf(globalTreeHeight));

                for(AnchorNode a : anchorList){
                    Vector3 newScale = new Vector3(a.getLocalScale().x, globalTreeHeight, a.getLocalScale().z);
                    a.setLocalScale(newScale);
                }
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

/*            if (lastAnchor == null) {
                lastAnchor = anchor;
                lastAnchorNode = anchorNode;
            } else {
                lastAnchor = currentAnchor;
                lastAnchorNode = currentAnchorNode;
            }*/
            currentAnchor = anchor;
            //currentAnchorNode = anchorNode;

            /*if (currentAnchorNode != null && lastAnchorNode != null && currentAnchorNode != lastAnchorNode) {
                float dist = getDistanceBetweenVectorsInMeters(currentAnchor, lastAnchor);
                if (dist < 0.8) {
                    Toast.makeText(this, "Distance between trees is too small. Place tree at a farther distance.", Toast.LENGTH_LONG).show();
                    currentAnchor = lastAnchor;
                    currentAnchorNode = lastAnchorNode;
                    return;
                }
                //addLineBetweenHits(currentAnchorNode, lastAnchorNode);
            }*/
            if(!anchorList.isEmpty()){
                for (AnchorNode a : anchorList) {
                    float dist = getDistanceBetweenVectorsInMeters(currentAnchor, a.getAnchor());
                    if (dist < 0.8) {
                        Toast.makeText(this, "Distance between trees is too small. Place tree at a farther distance.", Toast.LENGTH_LONG).show();
/*                        currentAnchor = lastAnchor;
                        currentAnchorNode = lastAnchorNode;*/
                        return;
                    }
                }

                //addLineBetweenHits(currentAnchorNode, lastAnchorNode);
            }

            createModel(anchorNode);
            anchorList.add(anchorNode);

            //clearAnchor();
        });
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getTranslationController().setEnabled(false);
        node.getScaleController().setEnabled(false);
        node.setLocalScale(new Vector3(1.3f, globalTreeHeight, 1.3f));
        //node.getRotationController().setEnabled(false);
        //node.setLocalRotation(); //random Quaternion

        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();

        //remove node
        /*node.setOnTapListener((HitTestResult hitTestResult, MotionEvent motionEvent) ->
        {

        });*/
        
        node.setOnTouchListener(new TouchTimer() {
            @Override
            protected void onTouchEnded(long tapTimeInMillis, HitTestResult hitTestResult, MotionEvent motionEvent) {
                if(tapTimeInMillis >= 1500){
                    removeNode(hitTestResult, motionEvent);
                }
            }
        });
    }

    private void removeNode(HitTestResult hitTestResult, MotionEvent motionEvent) {
        arFragment.onPeekTouch(hitTestResult, motionEvent);

        if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
            return;
        }

        if (hitTestResult.getNode() != null) {
            Node hitNode = hitTestResult.getNode();
            arFragment.getArSceneView().getScene().removeChild(hitNode);
            AnchorNode hitNodeAnchor = (AnchorNode) hitNode.getParent();
            if (hitNodeAnchor != null) {
                anchorList.remove(((AnchorNode) hitNode.getParent()));
                ((AnchorNode) hitNode.getParent()).getAnchor().detach();
            }
            hitNode.setParent(null);
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        //distance between nodes debug
        /*if (currentAnchorNode != null && lastAnchorNode != null && currentAnchorNode != lastAnchorNode) {
            tvDistance.setVisibility(View.VISIBLE);
            tvDistance.setText("Distance between current node and last node: " + getDistanceBetweenVectorsInMeters(currentAnchor, lastAnchor) + " metres");
        }*/
    }

    private float getDistanceBetweenVectorsInMeters(Anchor currentAnchor, Anchor comparingAnchor) {
        Pose objectPose1 = currentAnchor.getPose();
        Pose objectPose2 = comparingAnchor.getPose();

        float distanceX = objectPose1.tx() - objectPose2.tx();
        float distanceY = objectPose1.ty() - objectPose2.ty();
        float distanceZ = objectPose1.tz() - objectPose2.tz();

        //straight line distance in meters
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
    }

    private void addLineBetweenHits(AnchorNode currentAnchorNode, AnchorNode lastAnchorNode) {
        if (lastAnchorNode != null) {
            currentAnchorNode.setParent(arFragment.getArSceneView().getScene());
            Vector3 point1, point2;
            point1 = lastAnchorNode.getWorldPosition();
            point2 = currentAnchorNode.getWorldPosition();

    /*
        First, find the vector extending between the two points and define a look rotation
        in terms of this Vector.
    */
            final Vector3 difference = Vector3.subtract(point1, point2);
            final Vector3 directionFromTopToBottom = difference.normalized();
            final Quaternion rotationFromAToB =
                    Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
            MaterialFactory.makeOpaqueWithColor(getApplicationContext(), new Color(android.graphics.Color.parseColor("#476A30")))
                    .thenAccept(
                            material -> {
                            /* Then, create a rectangular prism, using ShapeFactory.makeCube() and use the difference vector
                                   to extend to the necessary length.  */
                                ModelRenderable model = ShapeFactory.makeCube(
                                        new Vector3(.01f, .01f, difference.length()),
                                        Vector3.zero(), material);
                            /* Last, set the world rotation of the node to the rotation calculated earlier and set the world position to
                                   the midpoint between the given points . */
                                Node node = new Node();
                                node.setParent(currentAnchorNode);
                                node.setRenderable(model);
                                node.setWorldPosition(Vector3.add(point1, point2).scaled(.5f));
                                node.setWorldRotation(rotationFromAToB);
                            }
                    );
        }
    }

    public abstract class TouchTimer implements Node.OnTouchListener{
        private long touchStart = 0l;
        private long touchEnd = 0l;

        @Override
        public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.touchStart = System.currentTimeMillis();
                    return true;

                case MotionEvent.ACTION_UP:
                    this.touchEnd = System.currentTimeMillis();
                    long touchTime = this.touchEnd - this.touchStart;
                    onTouchEnded(touchTime, hitTestResult, motionEvent);
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    return true;

                default:
                    return false;
            }
        }

        protected abstract void onTouchEnded(long touchTimeInMillis, HitTestResult hitTestResult, MotionEvent motionEvent);
    }
}
