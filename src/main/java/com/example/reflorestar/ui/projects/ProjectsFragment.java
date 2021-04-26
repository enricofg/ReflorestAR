package com.example.reflorestar.ui.projects;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectsFragment extends Fragment {

    private ProjectsViewModel projectsViewModel;
    private TextInputLayout searchText;
    private Button searchButton;
    private ListView listView;
    private CustomAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        projectsViewModel =
                new ViewModelProvider(this).get(ProjectsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_projects, container, false);

        listView = root.findViewById(R.id.resultList);

        final ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String, Object>>();

        HashMap<String,Object> map1 = new HashMap<String,Object>();
        map1.put("Image",R.drawable.ic_launcher_background);
        map1.put("FirstLastName", "FirstLastName 1");
        map1.put("Descriptions","Descriptions 1 Descriptions 1 Descriptions 1 Descriptions 1 Descriptions 1 Descriptions 1 Descriptions 1 Descriptions 1");
        map1.put("Params1","Params1 1");
        map1.put("Params2","Params2 1");
        map1.put("Params3","Params3 1");
        list.add(map1);

        HashMap<String,Object> map2 = new HashMap<String,Object>();
        map2.put("Image",R.drawable.ic_launcher_foreground);
        map2.put("FirstLastName","FirstLastName 2");
        map2.put("Descriptions","Descriptions 2 Descriptions 2 Descriptions 2 Descriptions 2 Descriptions 2 Descriptions 2 Descriptions 2 Descriptions 2");
        map2.put("Params1","Params1 2");
        map2.put("Params2","Params2 2");
        map2.put("Params3","Params3 2");
        list.add(map2);

        HashMap<String,Object> map3 = new HashMap<String,Object>();
        map3.put("Image",R.drawable.ic_home_black_24dp);
        map3.put("FirstLastName","FirstLastName 3");
        map3.put("Descriptions","Descriptions 3 Descriptions 3 Descriptions 3 Descriptions 3 Descriptions 3 Descriptions 3 Descriptions 3 Descriptions 3");
        map3.put("Params1","Params1 3");
        map3.put("Params2","Params2 3");
        map3.put("Params3","Params3 3");
        list.add(map3);

        HashMap<String,Object> map4 = new HashMap<String,Object>();
        map4.put("Image",R.drawable.baseline_camera_alt_24);
        map4.put("FirstLastName","FirstLastName 4");
        map4.put("Descriptions","Descriptions 4 Descriptions 4 Descriptions 4 Descriptions 4 Descriptions 4 Descriptions 4 Descriptions 4 Descriptions 4");
        map4.put("Params1","Params1 3");
        map4.put("Params2","Params2 3");
        map4.put("Params3","Params3 3");
        list.add(map4);

        adapter = new CustomAdapter(root.getContext(),list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            /*Fragment catalogItemFragment = new CatalogItemFragment();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, catalogItemFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();
*/
            Toast.makeText(root.getContext(),list.get(position).get("FirstLastName").toString(),Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    class CustomAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String,Object>> data;

        public CustomAdapter(Context context,ArrayList<HashMap<String,Object>> data){
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
                holder.txtFirstLastName = convertView.findViewById(R.id.txtFirstLastName);
                holder.txtDescription = convertView.findViewById(R.id.txtDescription);
                holder.txtParams1 = convertView.findViewById(R.id.txtParams1);
                holder.txtParams2 = convertView.findViewById(R.id.txtParams2);
                holder.txtParams3 = convertView.findViewById(R.id.txtParams3);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            holder.imgIcon.setImageResource((Integer) data.get(position).get("Image"));
            holder.txtFirstLastName.setText(data.get(position).get("FirstLastName").toString());
            holder.txtDescription.setText(data.get(position).get("Descriptions").toString());
            holder.txtParams1.setText(data.get(position).get("Params1").toString());
            holder.txtParams2.setText(data.get(position).get("Params2").toString());
            holder.txtParams3.setText(data.get(position).get("Params3").toString());


            return convertView;
        }

        class ViewHolder{
            ImageView imgIcon;
            TextView txtFirstLastName;
            TextView txtDescription;
            TextView txtParams1;
            TextView txtParams2;
            TextView txtParams3;
        }
    }

}