package com.example.reflorestar.ui.catalog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class CatalogFragment extends Fragment {

    private CatalogViewModel catalogViewModel;
    private TextInputLayout searchText;
    private Button searchButton;
    private ListView listView;
    private ListItemAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        catalogViewModel =
                new ViewModelProvider(this).get(CatalogViewModel.class);
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference trees = mDatabase.child("trees");

        trees.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of trees in datasnapshot
                        getTrees(dataSnapshot, root);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        searchText = root.findViewById(R.id.catalogSearch);
        AtomicReference<Editable> editText = new AtomicReference<>(searchText.getEditText().getText());

        searchButton = root.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> {
            if(!editText.toString().matches("")){
                editText.set(searchText.getEditText().getText());
                Toast.makeText(root.getContext(), editText.get(), Toast.LENGTH_SHORT).show();
            }
        });

        /*searchText.setEndIconOnClickListener(view -> {
            searchText.getEditText().setText("");
        });*/

        return root;
    }

    private void getTrees(DataSnapshot dataSnapshot, View root) {
        listView = root.findViewById(R.id.resultList);

        ArrayList<String> trees = (ArrayList<String>) dataSnapshot.getValue();

        final ArrayList<HashMap<String,Object>> catalogResult = new ArrayList<>();
        for (Object tree : trees) {
            catalogResult.add((HashMap<String, Object>) tree);
            //Log.e("class:", tree.getClass().toString());
        }

        adapter = new ListItemAdapter(root.getContext(),catalogResult);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String commonName = catalogResult.get(position).get("common_name").toString();
            String minHeight = catalogResult.get(position).get("min_height").toString();
            String maxHeight = catalogResult.get(position).get("max_height").toString();
            String minDist = catalogResult.get(position).get("space_between").toString();
            String tree_id = catalogResult.get(position).get("id").toString();
            String imageUrl = catalogResult.get(position).get("photo").toString();

            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.catalog_container, new CatalogItemFragment(commonName, minHeight, maxHeight, minDist, tree_id, imageUrl)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
        });
    }

    class ListItemAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String,Object>> data;

        public ListItemAdapter(Context context, ArrayList<HashMap<String,Object>> data){
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount(){
            return data.size();
        }

        @Override
        public Object getItem(int position){
            return data.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder;
            if(convertView == null){
                holder =new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder.imgIcon = convertView.findViewById(R.id.imgIcon);
                holder.txtCommonName = convertView.findViewById(R.id.txtCommonName);
                holder.txtLatinName = convertView.findViewById(R.id.txtLatinName);
                //holder.txtDescription = convertView.findViewById(R.id.txtDescription);
                holder.txtMinHeight = convertView.findViewById(R.id.txtMinHeight);
                holder.txtSpaceBetween = convertView.findViewById(R.id.txtMinDistance);
                holder.txtMaxHeight = convertView.findViewById(R.id.txtMaxHeight);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            //holder.imgIcon.setImageResource((Integer) data.get(position).get("Image"));
            holder.txtCommonName.setText(data.get(position).get("common_name").toString());
            holder.txtLatinName.setText(data.get(position).get("latin_name").toString());
            //holder.txtDescription.setText(data.get(position).get("Descriptions").toString());
            holder.txtMinHeight.setText(data.get(position).get("min_height").toString()+"m");
            holder.txtSpaceBetween.setText(data.get(position).get("max_height").toString()+"m");
            holder.txtMaxHeight.setText(data.get(position).get("space_between").toString()+"m");
            Picasso.get().load(data.get(position).get("photo").toString()).into(holder.imgIcon);


            return convertView;
        }

        class ViewHolder{
            ImageView imgIcon;
            TextView txtCommonName;
            TextView txtLatinName;
            //TextView txtDescription;
            TextView txtMinHeight;
            TextView txtMaxHeight;
            TextView txtSpaceBetween;
        }
    }
}