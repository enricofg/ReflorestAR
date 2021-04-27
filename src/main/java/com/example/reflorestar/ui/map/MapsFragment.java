package com.example.reflorestar.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
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

import java.util.Arrays;
import java.util.List;

public class MapsFragment extends Fragment {

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
/*        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng pinhal = new LatLng(39.76, -9.01);
            googleMap.addMarker(new MarkerOptions().position(pinhal).title("Pinhal do Rei"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(pinhal));
        }*/

        @Override
        public void onMapReady(GoogleMap googleMap) {
            List<PatternItem> pattern = Arrays.asList(
                    new Dot(), new Gap(20), new Dash(30), new Gap(20));

            // Instantiates a new Polygon object and adds points to define a rectangle
            PolygonOptions polygonOptions = new PolygonOptions()
                    .add(new LatLng(39.879511, -8.970974),
                            new LatLng(39.88052450522114, -8.956372369118228),
                            new LatLng(39.858522059150246, -8.944270241899925),
                            new LatLng(39.859378573770336, -8.92753325724833),
                            new LatLng(39.837830689435364, -8.910452949834646),
                            new LatLng(39.815246767611804, -8.903941097804099),
                            new LatLng(39.726014423962454, -8.966976446142134),
                            new LatLng(39.749561409042684, -9.03515951866806),
                            new LatLng(39.879511, -8.970974));

            // Get back the mutable Polygon
            Polygon polygon = googleMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(0xaaFF7913);
            polygon.setFillColor(0x44FF7913);
            polygon.setStrokeWidth(5);
            polygon.setStrokePattern(pattern);

            // Position the map's camera near Alice Springs in the center of Australia,
            // and set the zoom factor so most of Australia shows on the screen.
            LatLng pinhal = new LatLng(39.76, -9.01);
            googleMap.addMarker(new MarkerOptions().position(pinhal).title("Pinhal do Rei"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.76, -9.01), 10));
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