package com.example.reflorestar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.reflorestar.classes.Project;
import com.example.reflorestar.classes.Tree;
import com.example.reflorestar.classes.User;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CameraActivity extends AppCompatActivity {

    private static final float R_EARTH = 6378;
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private LinearLayout containerOptions, optionTree1, optionTree2;
    private SeekBar sliderQuant;
    private Button closeButton, saveButton, configButton, pinusPinasterButton, pineTree2Button, deleteAllButton, undoButton;
    private NumberPicker treeQuantityPicker;
    private SharedPreferences sharedPreferences;
    private DatabaseReference users, projects;
    private String projectName = "", projectDescription = "";

    //tree variables
    private ArrayList<AnchorNode> anchorList;
    private float nextTreeHeight;
    private static Random rand;
    private String treeType;
    private ArrayList<Tree> trees = new ArrayList<>();
    private Pose lastPose;
    private Location location;
    private int lastNumber;
    private boolean undoFlag, loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //hide support action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //DB and user prep
        sharedPreferences = CameraActivity.this.getSharedPreferences("user", Context.MODE_PRIVATE);
        String authUser = sharedPreferences.getString("username", null);
        users = FirebaseDatabase.getInstance().getReference("users");
        projects = FirebaseDatabase.getInstance().getReference("projects");

        //camera view containers
        View cameraContainer = findViewById(R.id.layoutCamera);
        containerOptions = findViewById(R.id.containerCamOptions);
        containerOptions.setVisibility(View.GONE);

        //check if project is loading through set name
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            projectName = bundle.getString("projectName");
        }
        Log.e("projectName", projectName);

        //quantity slider
        sliderQuant = findViewById(R.id.seekBarTreeQuantity);

        //buttons
        closeButton = findViewById(R.id.closeButton);
        configButton = findViewById(R.id.configButtonCam);
        saveButton = findViewById(R.id.buttonSaveProject);
        deleteAllButton = findViewById(R.id.buttonDeleteAll);
        undoButton = findViewById(R.id.buttonUndo);
        pinusPinasterButton = findViewById(R.id.buttonPinusPinaster);
        pineTree2Button = findViewById(R.id.buttonTree2);
        optionTree1 = findViewById(R.id.optionTree1);
        optionTree2 = findViewById(R.id.optionTree2);

        //tree controls
        nextTreeHeight = 1.3f;
        rand = new Random();
        treeQuantityPicker = findViewById(R.id.treeQuantPicker);
        treeQuantityPicker.setMinValue(1);
        treeQuantityPicker.setMaxValue(100);
        treeQuantityPicker.setEnabled(true);

        //node list controls
        anchorList = new ArrayList();
        undoFlag = false;
        lastNumber = 0;
        loaded = false;

        //close camera
        closeButton.setOnClickListener(view -> finish());

        //save project
        saveButton.setOnClickListener(view -> {
            saveProjectDialog();
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
                nextTreeHeight = 1.3f + progress * 0.01f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pinusPinasterButton.setOnClickListener(view -> {
            optionTree1.setBackground(getDrawable(R.drawable.ic_buttongreenbg_greenstroke));
            optionTree2.setBackground(getDrawable(R.drawable.ic_buttonwhitebg_greenstroke));
            setUpPinusPinasterModel();
        });

        pineTree2Button.setOnClickListener(view -> {
            optionTree2.setBackground(getDrawable(R.drawable.ic_buttongreenbg_greenstroke));
            optionTree1.setBackground(getDrawable(R.drawable.ic_buttonwhitebg_greenstroke));
            setUpPineTreeModel();
        });

        undoButton.setOnClickListener(view -> {
            undoLastAction();
        });

        deleteAllButton.setOnClickListener(view -> {
            deleteAllTrees();
        });

        if (authUser == null) {
            showMessage(getString(R.string.login_message), getString(R.string.login_warning));
            saveButton.setVisibility(View.INVISIBLE);
        }

        //location services
        checkLocationStatus();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);


        //if project name is not empty, then this project should be loaded
        if (!projectName.isEmpty()) {
            arFragment.getArSceneView().getScene().addOnUpdateListener(this::loadProjectOnDetectedPlane);
        }

        //Start with Pinus Pinaster tree selected
        optionTree1.setBackground(getDrawable(R.drawable.ic_buttongreenbg_greenstroke));
        setUpPinusPinasterModel();
        setUpPlane();
    }

    private void setUpPinusPinasterModel() {
        treeType = "pinusPinaster";
        ModelRenderable.builder().setSource(this, R.raw.pinuspinaster)
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(throwable -> {
                    Toast.makeText(CameraActivity.this, getString(R.string.model_error), Toast.LENGTH_SHORT).show();
                    return null;
                });
    }

    private void setUpPineTreeModel() {
        treeType = "pineTree";
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
            int i = 0;
            Session session = arFragment.getArSceneView().getSession();

            /*//only detect horizontal planes
            Config config = arFragment.getArSceneView().getSession().getConfig();
            config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
            arFragment.getArSceneView().getSession().configure(config);*/

            do {
                Anchor anchor;
                if (i == 0) {
                    lastPose = hitResult.getHitPose();
                    anchor = session.createAnchor(lastPose);
                } else {
                    boolean distanceCheck = false;
                    Pose newPose;
                    do {
                        newPose = calculateRandomNewPose();
                        for (int j = 0; j < anchorList.size(); j++) {
                            if (getDistanceBetweenPoseAndAnchor(newPose, anchorList.get(j).getAnchor()) > 0.8) {
                                distanceCheck = true;
                            } else {
                                distanceCheck = false;
                            }
                        }
                    } while (!distanceCheck);

                    anchor = session.createAnchor(newPose);
                    lastPose = newPose;
                }

                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                if (!anchorList.isEmpty()) {
                    for (AnchorNode a : anchorList) {
                        if (getDistanceBetweenAnchors(anchor, a.getAnchor()) < 0.8) {
                            if (treeQuantityPicker.getValue() == 1) {
                                Toast.makeText(this, getString(R.string.min_distance_warning), Toast.LENGTH_LONG).show();
                            }
                            continue;
                        }
                    }
                }

                if (treeType == "pinusPinaster") {
                    setUpPinusPinasterModel();
                } else {
                    setUpPineTreeModel();
                }

                createModel(anchorNode);
                anchorList.add(anchorNode);

                if (location != null) {
                    Tree tree = new Tree(
                            calculateLocation(anchor).get(0),
                            calculateLocation(anchor).get(1),
                            nextTreeHeight,
                            treeType,
                            lastPose.tx(),
                            lastPose.ty(),
                            lastPose.tz(),
                            lastPose.qx(),
                            lastPose.qy(),
                            lastPose.qz(),
                            lastPose.qw()
                    );

                    trees.add(tree);
                } else {
                    checkLocationStatus();
                }

                i++;
            } while (i < treeQuantityPicker.getValue());
            lastNumber = treeQuantityPicker.getValue();
            undoFlag = true;
        });
    }

    @NotNull
    private Pose calculateRandomNewPose() {
        float randomX = 0, randomZ = 0;

        do {
            randomX = (float) (Math.random() * (1.0 + 1.0) - 1.0); //random between -1 and 1
        }
        while (randomX < 0.8 && randomX > -0.8);

        do {
            randomZ = (float) (Math.random() * (1.0 + 1.0) - 1.0); //random between -1 and 1
        }
        while (randomZ < 0.8 && randomZ > -0.8);

        Pose newPose = new Pose(new float[]{(float) (lastPose.tx() + randomX), lastPose.ty(), (float) (lastPose.tz() + randomZ)}, lastPose.getRotationQuaternion());
        return newPose;
    }

    private void createModel(AnchorNode anchorNode) {
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getTranslationController().setEnabled(false);
        node.getScaleController().setEnabled(false);
        node.setLocalScale(new Vector3(1.3f, nextTreeHeight, 1.3f));
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
            trees.remove(trees.size() - 1);
        }
    }

    private ArrayList<Float> calculateLocation(Anchor anchor) {
        Pose objectPose = anchor.getPose();
        Pose cameraPose = arFragment.getArSceneView().getArFrame().getCamera().getPose();

        float distanceX = objectPose.tx() - cameraPose.tx();
        float distanceY = objectPose.ty() - cameraPose.ty();


        float newLatitude = (float) (location.getLatitude() + (distanceY / R_EARTH) * (180 / Math.PI));
        float newLongitude = (float) (location.getLongitude() + (distanceX / R_EARTH) * (180 / Math.PI) / Math.cos(location.getLatitude() * Math.PI / 180));

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

    private float getDistanceBetweenPoseAndAnchor(Pose objectPose1, Anchor comparingAnchor) {
        Pose objectPose2 = comparingAnchor.getPose();

        float distanceX = objectPose1.tx() - objectPose2.tx();
        float distanceY = objectPose1.ty() - objectPose2.ty();
        float distanceZ = objectPose1.tz() - objectPose2.tz();

        //straight line distance in meters
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
    }

    private void undoLastAction() {
        if (undoFlag && anchorList.size() > 0 && trees.size() > 0) {
            //undo last action
            trees.subList(trees.size() - lastNumber, trees.size()).clear();

            for (int i = 1; i <= lastNumber; i++) {
                AnchorNode nodeToRemove = anchorList.get(anchorList.size() - i);
                arFragment.getArSceneView().getScene().removeChild(nodeToRemove);
                AnchorNode anchorNodeParent = (AnchorNode) nodeToRemove.getParent();
                if (anchorNodeParent != null) {
                    anchorList.remove(anchorNodeParent);
                    anchorNodeParent.getAnchor().detach();
                }
                nodeToRemove.setParent(null);
            }

            undoFlag = false;
            lastNumber = 0;
        }
    }

    private void deleteAllTrees() {
        trees.clear();
        for (AnchorNode anchorNode : anchorList
        ) {
            arFragment.getArSceneView().getScene().removeChild(anchorNode);
            AnchorNode anchorNodeParent = (AnchorNode) anchorNode.getParent();
            if (anchorNodeParent != null) {
                anchorList.remove(anchorNodeParent);
                anchorNodeParent.getAnchor().detach();
            }
            anchorNode.setParent(null);
        }
        undoFlag = false;
        lastNumber = 0;
    }

    //Get location
    public void checkLocationStatus() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.location_message), Toast.LENGTH_SHORT).show();
            //showMessage(getString(R.string.location_message), getString(R.string.access_warning));
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            //saveButton.setVisibility(View.INVISIBLE);
            return;
        } else {
            saveButton.setVisibility(View.VISIBLE);
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
    }

    private void saveProjectDialog() {
        if (!trees.isEmpty()) {
            if (projectName.isEmpty()) {
                Context context = CameraActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText inputProjectName = new EditText(context);
                inputProjectName.setHint(getString(R.string.insert_project_name));
                layout.addView(inputProjectName);

                final EditText inputProjectDescription = new EditText(context);
                inputProjectDescription.setHint(getString(R.string.insert_project_description));
                layout.addView(inputProjectDescription);
                builder.setView(layout);

                builder.setPositiveButton("OK", (dialog, which) -> {
                    projectName = inputProjectName.getText().toString();
                    projectDescription = inputProjectDescription.getText().toString();

                    if (!projectName.isEmpty()) {
                        projects.orderByChild("name").equalTo(projectName).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            showMessage(getString(R.string.project_exists), getString(R.string.project_exists_warning));
                                            projectName = "";
                                            projectDescription = "";
                                        } else {
                                            Project newProject = new Project(projectName, projectDescription, sharedPreferences.getString("username", null));
                                            DatabaseReference newRef = projects.child(projectName);
                                            newRef.setValue(newProject);
                                            users.child(sharedPreferences.getString("username", null)).child("projects").child(projectName).setValue("full");
                                            saveProjectTrees();
                                            projects.child(projectName).child("users").child(sharedPreferences.getString("username", null)).setValue(true);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                }
                        );
                    } else {
                        showMessage(getString(R.string.empty_fields), getString(R.string.empty_fields_warning));
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            } else {
                saveProjectTrees();
            }
        } else if (trees.isEmpty() && !projectName.isEmpty()) { //existing project
            saveProjectTrees();
        } else {
            Toast.makeText(this, getString(R.string.no_trees), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProjectTrees() {
        projects.child(projectName).child("trees_android").removeValue();
        int c = 0;
        for (Tree tree : trees
        ) {
            projects.child(projectName).child("trees_android").child(String.valueOf(c)).setValue(tree);
            c++;
        }
        Toast.makeText(this, getString(R.string.project) + " " + projectName + " " + getString(R.string.saved), Toast.LENGTH_LONG).show();
    }

    private void showMessage(String message, String warning) {
        AlertDialog alertDialog = new AlertDialog.Builder(CameraActivity.this).create();
        alertDialog.setTitle(warning);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void loadProjectOnDetectedPlane(FrameTime frameTime) {
        if(!loaded) {
            Frame frame = arFragment.getArSceneView().getArFrame();
            Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
            for (Plane plane :
                    planes) {
                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    loadProject(projectName);
                    loaded = true; //set loaded state to true
                    Toast.makeText(this, getString(R.string.project) + " " + projectName + " " + getString(R.string.loaded), Toast.LENGTH_LONG).show();
                    nextTreeHeight = 1.3f + sliderQuant.getProgress() * 0.01f; //reset tree heights to slider progress value
                    break;
                }
            }
        }
    }

    private void loadProject(String projectName) {
        projects.child(projectName).child("trees_android").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                ArrayList<Object> projectTreesObject = (ArrayList<Object>) dataSnapshot.getValue();

                if (projectTreesObject != null) {
                    for (Object projectTree : projectTreesObject) {
                        Tree tree = extractTreeFromString(projectTree.toString());
                        trees.add(tree);
                    }

                    Session session = arFragment.getArSceneView().getSession();
                    for (Tree tree :
                            trees) {
                        Pose pose = new Pose(new float[]{tree.tx, tree.ty, tree.tz}, new float[]{tree.qx, tree.qy, tree.qz, tree.qw});

                        Anchor anchor = session.createAnchor(pose);
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        //treeType=tree.type;
                        if (tree.type == "pinusPinaster") {
                            setUpPinusPinasterModel();
                        } else if(tree.type == "pineTree"){
                            setUpPineTreeModel();
                        }

                        nextTreeHeight=tree.height;
                        createModel(anchorNode);
                        anchorList.add(anchorNode);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private Tree extractTreeFromString(String treeToString) {
        //parameters to extract and indexes
        String txParam = "tx=", tyParam = "ty=", tzParam = "tz=", qxParam = "qx=", qyParam = "qy=", qzParam = "qz=", qwParam = "qw=", typeParam = "type=", heightParam = "height=", latitudeParam = "latitude=", longitudeParam = "longitude=", comma = ",";
        int txIndex = treeToString.indexOf(txParam);
        int tyIndex = treeToString.indexOf(tyParam);
        int qwIndex = treeToString.indexOf(qwParam);
        int tzIndex = treeToString.indexOf(tzParam);
        int latitudeIndex = treeToString.indexOf(latitudeParam);
        int qxIndex = treeToString.indexOf(qxParam);
        int qyIndex = treeToString.indexOf(qyParam);
        int qzIndex = treeToString.indexOf(qzParam);
        int typeIndex = treeToString.indexOf(typeParam);
        int heightIndex = treeToString.indexOf(heightParam);
        int longitudeIndex = treeToString.indexOf(longitudeParam);
        int commaIdx = treeToString.indexOf(comma);

        //extract methods
        float tx = Float.parseFloat(treeToString.substring(txIndex + txParam.length(), commaIdx)); //tx
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float ty = Float.parseFloat(treeToString.substring(tyIndex + tyParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float qw = Float.parseFloat(treeToString.substring(qwIndex + qwParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float tz = Float.parseFloat(treeToString.substring(tzIndex + tzParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float latitude = Float.parseFloat(treeToString.substring(latitudeIndex + latitudeParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float qx = Float.parseFloat(treeToString.substring(qxIndex + qxParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float qy = Float.parseFloat(treeToString.substring(qyIndex + qyParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float qz = Float.parseFloat(treeToString.substring(qzIndex + qzParam.length(), commaIdx));
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        String type = treeToString.substring(typeIndex + typeParam.length(), commaIdx);
        commaIdx = treeToString.indexOf(comma, commaIdx + 1);
        float height = Float.parseFloat(treeToString.substring(heightIndex + heightParam.length(), commaIdx));
        float longitude = Float.parseFloat(treeToString.substring(longitudeIndex + longitudeParam.length(), treeToString.length() - 1));

        Tree tree = new Tree(latitude, longitude, height, type, tx, ty, tz, qx, qy, qz, qw);

        return tree;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
}
