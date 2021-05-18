package com.example.reflorestar.ui.projects;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.reflorestar.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectsFragment extends Fragment {

    private ProjectsViewModel projectsViewModel;
    private ListView listView;
    private CustomAdapter adapter;
    DatabaseReference mDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        projectsViewModel =
                new ViewModelProvider(this).get(ProjectsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_projects, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference projects = mDatabase.child("projects");

        projects.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of trees in datasnapshot
                        getProjects(dataSnapshot, root);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

        return root;
    }

    private void getProjects(DataSnapshot dataSnapshot, View root) {
        listView = root.findViewById(R.id.resultList);

        ArrayList<String> projects = (ArrayList<String>) dataSnapshot.getValue();

        final ArrayList<HashMap<String,Object>> projectsResult = new ArrayList<>();
        for (Object project : projects) {
            projectsResult.add((HashMap<String, Object>) project);
        }

        adapter = new CustomAdapter(root.getContext(),projectsResult);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String projectName = projectsResult.get(position).get("full_name").toString();
            String description = projectsResult.get(position).get("description").toString();
            String availability = projectsResult.get(position).get("availability").toString();
            String status = projectsResult.get(position).get("status").toString();
            String projectId = projectsResult.get(position).get("id").toString();

            DataSnapshot projectOwner = dataSnapshot.child("users").child(projectsResult.get(position).get("id_owner").toString());
            HashMap<String, Object> user = (HashMap<String, Object>) projectOwner.getValue();
            Log.e("user:", user.toString());

            //String ownerName = user.get("full_name").toString();
            String ownerName = "";
            //String email = user.get("email").toString();
            String email = "";
            //Picasso.get().load(user.get("photo").toString()).into(paramUserImage);

            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.projects_container, new ProjectsItemFragment(projectName, description, availability, status, projectId, ownerName, email)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
        });
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
                convertView = LayoutInflater.from(context).inflate(R.layout.project_list_item, null);
                holder.txtFullName = convertView.findViewById(R.id.txtFullName);
                holder.txtDescription = convertView.findViewById(R.id.txtDescription);
                holder.txtAvailability = convertView.findViewById(R.id.txtAvailability);
                holder.txtStatus = convertView.findViewById(R.id.txtStatus);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }

            holder.txtFullName.setText(data.get(position).get("full_name").toString());
            holder.txtDescription.setText(data.get(position).get("description").toString());
            holder.txtAvailability.setText(data.get(position).get("availability").toString());
            holder.txtStatus.setText(data.get(position).get("status").toString());


            return convertView;
        }

        class ViewHolder{
            TextView txtFullName;
            TextView txtDescription;
            TextView txtAvailability;
            TextView txtStatus;
        }
    }

}