package com.example.reflorestar.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reflorestar.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragment extends Fragment {

    private DatabaseReference mDatabase, areas;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        @Override
        public void onMapReady(GoogleMap googleMap) {
            List<PatternItem> pattern = Arrays.asList(
                    new Dot(), new Gap(20), new Dash(30), new Gap(20));
            mDatabase = FirebaseDatabase.getInstance().getReference();
            areas = mDatabase.child("areas");

            areas.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                    HashMap<String, Object> areasList = (HashMap<String, Object>) dataSnapshot.getValue();
                    //ArrayList<HashMap<String, Object>> areasList = (ArrayList<HashMap<String, Object>>) dataSnapshot.getValue();

                    if(areasList!=null){
                        for (Map.Entry<String,Object> entry:areasList.entrySet()
                             ) {
                            //instantiate lat lng list
                            ArrayList<LatLng> latLngs = new ArrayList<>();

                            //Get area Map (Java class)
                            Map area = (Map) entry.getValue();

                            //get area's vertices
                            ArrayList<HashMap<String,Object>> vertices = (ArrayList<HashMap<String,Object>>) area.get("vertexs");
                            for (HashMap<String,Object> vertex:vertices
                                 ) {
                                latLngs.add(new LatLng((double) vertex.get("latitude"),(double) vertex.get("longitude")));
                            }

                            //Instantiates a new Polygon object and adds points to define a rectangle
                            PolygonOptions polygonOptions = new PolygonOptions();
                            for (int i = 0; i < latLngs.size(); i++) {
                                polygonOptions.add(latLngs.get(i));
                            }
                            polygonOptions.add(latLngs.get(0)); //close the polygon with initial coordinate

                            // Get back the mutable Polygon
                            Polygon polygon = googleMap.addPolygon(polygonOptions);
                            polygon.setStrokeColor(0xaaFF7913); //0xaa88B04B
                            polygon.setFillColor(0x44FF7913); //0x4488B04B
                            polygon.setStrokeWidth(5);
                            polygon.setStrokePattern(pattern);

                            googleMap.addMarker(new MarkerOptions().position(latLngs.get(0)).title((String) area.get("full_nome")));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 10));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}