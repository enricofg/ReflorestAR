package com.example.reflorestar.ui.catalog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    private DatabaseReference mDatabase, treesDB;
    private TextView resultMessage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        catalogViewModel =
                new ViewModelProvider(this).get(CatalogViewModel.class);
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        resultMessage = root.findViewById(R.id.emptySearch);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        treesDB = mDatabase.child("trees");
        queryTrees(root);

        searchText = root.findViewById(R.id.catalogSearch);
        AtomicReference<Editable> editText = new AtomicReference<>(searchText.getEditText().getText());

        searchButton = root.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(view -> {
            if (!editText.toString().isEmpty()) {
                editText.set(searchText.getEditText().getText());
                //Toast.makeText(root.getContext(), editText.get().toString(), Toast.LENGTH_SHORT).show();
                //Log.e("Hello:", editText.get().toString());

                String searchQuery = editText.get().toString();

                //get query
                if (editText.toString().length() >= 1) {
                    searchQuery = editText.get().toString().substring(0, 1).toUpperCase() + editText.get().toString().substring(1).toLowerCase();
                }

                treesDB.orderByChild("common_name").startAt(searchQuery).endAt(searchQuery + "\uf8ff").addListenerForSingleValueEvent(
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
            } else {
                //get all
                treesDB = mDatabase.child("trees");
                queryTrees(root);
            }
        });

        searchText.setEndIconOnClickListener(view -> {
            searchText.getEditText().setText("");
            treesDB = mDatabase.child("trees");
            queryTrees(root);
        });

        return root;
    }

    private void queryTrees(View root) {
        treesDB.addListenerForSingleValueEvent(
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
    }

    private void getTrees(DataSnapshot dataSnapshot, View root) {
        listView = root.findViewById(R.id.resultList);

        if (dataSnapshot.getValue() != null) {
            resultMessage.setText("");

            try {
                ArrayList<HashMap<String, Object>> catalogResult = new ArrayList<>();
                HashMap<String, Object> trees = (HashMap<String, Object>) dataSnapshot.getValue();
                //ArrayList<String> listTrees = (HashMap<String, Object>) dataSnapshot.getValue();
                /*for (Object tree : listTrees) {
                    if (tree != null) {
                        catalogResult.add((HashMap<String, Object>) tree);
                    }
                    //Log.e("class:", tree.getClass().toString());
                }*/
                //listTrees.forEach((key,value) -> Log.e("Item #"+key, "Values: "+value.toString()));

                trees.forEach((key,value) -> catalogResult.add((HashMap<String, Object>) value));

                adapter = new ListItemAdapter(root.getContext(), catalogResult);
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
            } catch (Exception e) {
                Log.e("Error:", e.getMessage());
                return;
            }
        } else {
            resultMessage.setText("The search did not return any results.");
            adapter = new ListItemAdapter(root.getContext(), new ArrayList<>());
            listView.setAdapter(adapter);
            return;
        }
    }

    class ListItemAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String, Object>> data;

        public ListItemAdapter(Context context, ArrayList<HashMap<String, Object>> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder.imgIcon = convertView.findViewById(R.id.imgIcon);
                holder.txtCommonName = convertView.findViewById(R.id.txtCommonName);
                holder.txtLatinName = convertView.findViewById(R.id.txtLatinName);
                //holder.txtDescription = convertView.findViewById(R.id.txtDescription);
                holder.txtMinHeight = convertView.findViewById(R.id.txtMinHeight);
                holder.txtSpaceBetween = convertView.findViewById(R.id.txtMinDistance);
                holder.txtMaxHeight = convertView.findViewById(R.id.txtMaxHeight);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                //holder.imgIcon.setImageResource((Integer) data.get(position).get("Image"));
                holder.txtCommonName.setText(data.get(position).get("common_name").toString());
                holder.txtLatinName.setText(data.get(position).get("latin_name").toString());
                //holder.txtDescription.setText(data.get(position).get("Descriptions").toString());
                holder.txtMinHeight.setText(data.get(position).get("min_height").toString() + "m");
                holder.txtSpaceBetween.setText(data.get(position).get("max_height").toString() + "m");
                holder.txtMaxHeight.setText(data.get(position).get("space_between").toString() + "m");
                Picasso.get().load(data.get(position).get("photo").toString()).into(holder.imgIcon);
            } catch (Exception e) {
                Log.e("Error:", e.getMessage());
                //Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return convertView;
        }

        class ViewHolder {
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