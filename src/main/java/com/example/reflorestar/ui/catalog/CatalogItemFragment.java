package com.example.reflorestar.ui.catalog;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reflorestar.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class CatalogItemFragment extends Fragment {

    private Button backButton;
    private String paramCommonName, paramMinHeight, paramMaxHeight, paramMinDistance, paramImageUrl;
    //private int paramTreeId;
    private ConstraintLayout fragmentContainer;
    private View root;
    private BottomNavigationView navBar;
    private ViewGroup container;

    public CatalogItemFragment(String commonName, String minHeight, String maxHeight, String minDist, String imageUrl) {
        paramCommonName = commonName;
        paramMinHeight = minHeight;
        paramMaxHeight = maxHeight;
        paramMinDistance = minDist;
        paramImageUrl = imageUrl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.container = container;
        navBar = getActivity().findViewById(R.id.nav_view);
        toggleNavBar();
        root = inflater.inflate(R.layout.fragment_catalog_item, container, false);

        fragmentContainer = root.findViewById(R.id.catalog_item_container);
        Picasso.get().load(this.paramImageUrl).into((ImageView) root.findViewById(R.id.imageView));

        TextView commonName = root.findViewById(R.id.commonName);
        TextView minHeight = root.findViewById(R.id.textParamMinHeight);
        TextView maxHeight = root.findViewById(R.id.textParamMaxHeight);
        TextView minDist = root.findViewById(R.id.textParamMinDist);

        commonName.setText(this.paramCommonName);
        minHeight.setText(this.paramMinHeight+"m");
        maxHeight.setText(this.paramMaxHeight+"m");
        minDist.setText(this.paramMinDistance+"m");

        backButton = (Button) root.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            returnToCatalog(fragmentContainer);
        });

        return root;
    }

    private void toggleNavBar() {
        Transition transition = new Fade();
        transition.setDuration(350);
        transition.addTarget(navBar);
        TransitionManager.beginDelayedTransition(container, transition);
        navBar.setVisibility(navBar.getVisibility()==View.GONE ? View.VISIBLE : View.GONE);
    }

    private void returnToCatalog(ConstraintLayout fragmentContainer) {
        if(getActivity()!=null) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.catalog_item_container, new CatalogFragment()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack("catalog_item").commit();
            //fragmentContainer.setVisibility(View.INVISIBLE);
            fragmentContainer.removeAllViews();
            toggleNavBar();
        }
    }
}

