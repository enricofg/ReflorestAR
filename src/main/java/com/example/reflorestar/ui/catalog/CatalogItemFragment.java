package com.example.reflorestar.ui.catalog;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reflorestar.R;
import com.squareup.picasso.Picasso;

public class CatalogItemFragment extends Fragment {

    private Button backButton;
    private String paramCommonName, paramMinHeight, paramMaxHeight, paramMinDistance, paramImageUrl;
    //private int paramTreeId;
    private ConstraintLayout fragmentContainer;
    private View root;

    public CatalogItemFragment(String commonName, String minHeight, String maxHeight, String minDist, String imageUrl) {
        paramCommonName = commonName;
        paramMinHeight = minHeight;
        paramMaxHeight = maxHeight;
        paramMinDistance = minDist;
        //paramTreeId = Integer.parseInt(treeId);
        paramImageUrl = imageUrl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_catalog_item, container, false);

        fragmentContainer = root.findViewById(R.id.project_item_container);
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

    private void returnToCatalog(ConstraintLayout fragmentContainer) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.project_item_container, new CatalogFragment()).addToBackStack( "catalog_item" ).commit();
        //fragmentContainer.setVisibility(View.INVISIBLE);
        fragmentContainer.removeAllViews();
    }
}

