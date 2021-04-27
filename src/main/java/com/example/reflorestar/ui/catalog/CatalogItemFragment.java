package com.example.reflorestar.ui.catalog;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.reflorestar.R;

public class CatalogItemFragment extends Fragment {

    private Button backButton;
    private String mParam1;
    private String mParam2;

    public CatalogItemFragment(String param1, String param2) {
        mParam1 = param1;
        mParam2 = param2;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog_item, container, false);

        TextView textParam1 = root.findViewById(R.id.textParam1);
        TextView textParam2 = root.findViewById(R.id.textParam2);

        textParam1.setText(this.mParam1);
        textParam2.setText(this.mParam2);

        backButton = (Button) root.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_container, new CatalogFragment()).addToBackStack( "catalog_item" ).commit();
            backButton.setVisibility(View.INVISIBLE);
        });

        return root;
    }

}