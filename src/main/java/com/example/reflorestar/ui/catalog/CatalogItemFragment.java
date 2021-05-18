package com.example.reflorestar.ui.catalog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reflorestar.R;

import java.io.InputStream;

public class CatalogItemFragment extends Fragment {

    private Button backButton;
    private String paramCommonName, paramMinHeight, paramMaxHeight, paramMinDistance, paramImageUrl;
    private int paramTreeId;
    private ConstraintLayout fragmentContainer;
    private View root;

    public CatalogItemFragment(String commonName, String minHeight, String maxHeight, String minDist, String treeId, String imageUrl) {
        paramCommonName = commonName;
        paramMinHeight = minHeight;
        paramMaxHeight = maxHeight;
        paramMinDistance = minDist;
        paramTreeId = Integer.parseInt(treeId);
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

        fragmentContainer = root.findViewById(R.id.fragment_container);

        new DownloadImageFromInternet((ImageView) root.findViewById(R.id.imageView)).execute(this.paramImageUrl);

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
        fm.beginTransaction().replace(R.id.fragment_container, new CatalogFragment()).addToBackStack( "catalog_item" ).commit();
        //fragmentContainer.setVisibility(View.INVISIBLE);
        fragmentContainer.removeAllViews();
    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView=imageView;
            //Toast.makeText(root.getContext(),"Downloading image...",Toast.LENGTH_SHORT).show();
        }
        protected Bitmap doInBackground(String... urls) {
            String imageURL=urls[0];
            Bitmap bimage=null;
            try {
                InputStream in=new java.net.URL(imageURL).openStream();
                bimage= BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Toast.makeText(root.getContext(),"Error downloading image.",Toast.LENGTH_SHORT).show();
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }
}

