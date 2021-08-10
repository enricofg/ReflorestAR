package com.example.reflorestar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.reflorestar.classes.Tree;
import com.google.ar.core.Anchor;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;

public class CameraActivity extends AppCompatActivity {

    private static final float R_EARTH = 6378;
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private LinearLayout containerOptions;
    private SeekBar sliderQuant;
    private Button closeButton, saveButton, configButton, pinusPinasterButton, pineTree2Button;
    private SharedPreferences sharedPreferences;
    private DatabaseReference users, project;

    //tree variables
    private Anchor currentAnchor = null;
    private ArrayList<AnchorNode> anchorList;
    private float globalTreeHeight;
    private static Random rand;
    private String treeType;
    private ArrayList<Tree> trees = new ArrayList<>();

    //ARCore-Location scene
    private LocationScene locationScene;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //hide support action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //location services
        checkLocationStatus();

        //DB and user prep
        sharedPreferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        users = FirebaseDatabase.getInstance().getReference("users");
        project = FirebaseDatabase.getInstance().getReference("projects").child("P2").child("trees");

        //camera view containers
        View cameraContainer = findViewById(R.id.layoutCamera);
        containerOptions = findViewById(R.id.containerCamOptions);
        containerOptions.setVisibility(View.GONE);

        //quantity slider
        sliderQuant = findViewById(R.id.seekBarTreeQuantity);

        //buttons
        closeButton = findViewById(R.id.closeButton);
        saveButton = findViewById(R.id.buttonSaveProject);
        configButton = findViewById(R.id.configButtonCam);
        pinusPinasterButton = findViewById(R.id.buttonPinusPinaster);
        pineTree2Button = findViewById(R.id.buttonTree2);

        //tree controls
        globalTreeHeight = 1.3f;
        rand = new Random();

        //node list controls
        anchorList = new ArrayList();

        //close camera
        closeButton.setOnClickListener(view -> finish());

        //save project
        saveButton.setOnClickListener(view -> {
            int c = 0;
            for (Tree tree:trees
                 ) {
                project.child(String.valueOf(c)).setValue(tree);
                c++;
            }
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
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
                globalTreeHeight = 1.3f + progress * 0.01f;
                //Log.e("Progress Value:", String.valueOf(globalTreeHeight));

                for (AnchorNode a : anchorList) {
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
            setUpPineTreeModel();
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        setUpPinusPinasterModel();
        setUpPlane();
    }

    private void setUpPinusPinasterModel() {
        treeType="pinusPinaster";
        ModelRenderable.builder().setSource(this, R.raw.pinuspinaster)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, getString(R.string.model_error), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPineTreeModel() {
        treeType="pineTree";
        ModelRenderable.builder().setSource(this, R.raw.pinetree)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, getString(R.string.model_error), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPlane() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (locationScene == null) {
                locationScene = new LocationScene(this, this, arFragment.getArSceneView());
            }

            // Creating Anchor
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            currentAnchor = anchor;

            if (!anchorList.isEmpty()) {
                for (AnchorNode a : anchorList) {
                    float dist = getDistanceBetweenAnchors(currentAnchor, a.getAnchor());
                    if (dist < 0.8) {
                        Toast.makeText(this, getString(R.string.min_distance_warning), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                //addLineBetweenHits(currentAnchorNode, lastAnchorNode);
            }

            createModel(anchorNode);
            anchorList.add(anchorNode);

            if (location != null) {
                LocationMarker layoutLocationMarker = new LocationMarker(
                        calculateLocation(anchor, anchorNode).get(0),
                        calculateLocation(anchor, anchorNode).get(1),
                        anchorNode
                );

                Tree tree = new Tree(
                        calculateLocation(anchor, anchorNode).get(0),
                        calculateLocation(anchor, anchorNode).get(1),
                        globalTreeHeight,
                        treeType
                );

                trees.add(tree);

                Log.e("Object world position: ", "Location -> longitude: "+layoutLocationMarker.longitude+" \n->latitude: "+layoutLocationMarker.latitude);
            } else{
                checkLocationStatus();
            }
        });
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getTranslationController().setEnabled(false);
        node.getScaleController().setEnabled(false);
        node.setLocalScale(new Vector3(1.3f, globalTreeHeight, 1.3f));
        node.getRotationController().setEnabled(false);
        node.setLocalRotation(new Quaternion(0, rand.nextFloat(), 0, 1));

        node.setParent(anchorNode);
        node.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        //node.select();

        node.setOnTouchListener(new TouchTimer() {
            @Override
            protected void onTouchEnded(long tapTimeInMillis, HitTestResult hitTestResult, MotionEvent motionEvent) {
                if (tapTimeInMillis >= 1500) {
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
            trees.remove(trees.size()-1);
        }
    }

    private ArrayList<Float> calculateLocation(Anchor anchor, AnchorNode anchorNode){
        Pose objectPose = anchor.getPose();
        Pose cameraPose = arFragment.getArSceneView().getArFrame().getCamera().getPose();

        float distanceX = objectPose.tx() - cameraPose.tx();
        float distanceY = objectPose.ty() - cameraPose.ty();


        float newLatitude  = (float) (location.getLatitude()  + (distanceY / R_EARTH) * (180 / Math.PI));
        float newLongitude  = (float) (location.getLongitude() + (distanceX / R_EARTH) * (180 / Math.PI) / Math.cos(location.getLatitude() * Math.PI/180));

        ArrayList<Float> objectLocation = new ArrayList<>();
        objectLocation.add(newLatitude);
        objectLocation.add(newLongitude);

        return objectLocation;
    }

    private float getDistanceBetweenAnchors(Anchor currentAnchor, Anchor comparingAnchor) {
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

    public abstract class TouchTimer implements Node.OnTouchListener {
        private long startTimer = 0l;
        private long endTimer = 0l;

        @Override
        public boolean onTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.startTimer = System.currentTimeMillis();
                    return true;

                case MotionEvent.ACTION_UP:
                    this.endTimer = System.currentTimeMillis();
                    long touchTime = this.endTimer - this.startTimer;
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

    //Get location
    public void checkLocationStatus() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
