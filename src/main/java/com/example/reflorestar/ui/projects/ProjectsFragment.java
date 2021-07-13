package com.example.reflorestar.ui.projects;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ProjectsFragment extends Fragment {

    private ProjectsViewModel projectsViewModel;
    private View root;
    private ListView listView;
    private ListItemAdapter adapter;
    private DatabaseReference mDatabase, projectsDB, usersDB;
    private SharedPreferences sharedPreferences;
    private TextView emptyProjectsMessage, pageTitle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        projectsViewModel =
                new ViewModelProvider(this).get(ProjectsViewModel.class);
        root = inflater.inflate(R.layout.fragment_projects, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        projectsDB = mDatabase.child("projects");
        usersDB = mDatabase.child("users");

        sharedPreferences = root.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String authUser = sharedPreferences.getString("username", null);

        pageTitle = root.findViewById(R.id.projectsTitle);
        emptyProjectsMessage = root.findViewById(R.id.emptyProjects);

        if (authUser != null) {
            usersDB.child(sharedPreferences.getString("username", "null")).child("projects").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                    HashMap<String, Object> userProjects = (HashMap<String, Object>) dataSnapshot.getValue();

                    if (userProjects != null) {
                        ArrayList<HashMap<String, Object>> projectsList = new ArrayList<>();

                        userProjects.forEach((key, value) -> projectsDB.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                                HashMap<String, Object> project = (HashMap<String, Object>) dataSnapshot.getValue();
                                if (project != null) {
                                    projectsList.add(project);
                                }
                                getProjects(projectsList);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        }));
                    } else {
                        emptyProjectsMessage.setVisibility(View.VISIBLE);
                        emptyProjectsMessage.setText(getString(R.string.empty_projects));
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        } else {
            emptyProjectsMessage.setVisibility(View.VISIBLE);
            emptyProjectsMessage.setText(getString(R.string.login_projects));
        }


        return root;
    }

    private void getProjects(ArrayList<HashMap<String, Object>> projectsList) {
        listView = root.findViewById(R.id.resultList);

        if (!projectsList.isEmpty()) {
            pageTitle.setVisibility(View.VISIBLE);
            emptyProjectsMessage.setVisibility(View.GONE);

            try {
                adapter = new ListItemAdapter(root.getContext(), projectsList);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    String projectName = projectsList.get(position).get("name").toString();
                    String description = projectsList.get(position).get("description").toString();
                    String status = projectsList.get(position).get("status").toString();
                    String size = projectsList.get(position).get("size").toString();

                    DatabaseReference projectOwner = usersDB.child(projectsList.get(position).get("username_owner").toString());
                    projectOwner.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap<String, Object> user = (HashMap<String, Object>) dataSnapshot.getValue();

                            String ownerName = user.get("name").toString();
                            String username = user.get("username").toString();
                            String email = user.get("email").toString();
                            String photo = user.get("photo").toString();

                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            fm.beginTransaction().replace(R.id.projects_container, new ProjectsItemFragment(projectName, description, size, status, ownerName, username, email, photo)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).addToBackStack(null).commit();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                });
            } catch (Exception e) {
                Log.e("Error:", e.getMessage());
                return;
            }
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
                convertView = LayoutInflater.from(context).inflate(R.layout.project_list_item, null);
                holder.txtFullName = convertView.findViewById(R.id.txtFullName);
                holder.txtDescription = convertView.findViewById(R.id.txtDescription);
                holder.txtAvailability = convertView.findViewById(R.id.txtAvailability);
                holder.txtStatus = convertView.findViewById(R.id.txtStatus);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            try {
                holder.txtFullName.setText(data.get(position).get("name").toString());
                holder.txtDescription.setText(data.get(position).get("description").toString());
                holder.txtAvailability.setText(data.get(position).get("availability").toString());
                holder.txtStatus.setText(data.get(position).get("status").toString());
            } catch (Exception e) {
                Log.e("Error:", e.getMessage());
            }

            return convertView;
        }

        class ViewHolder {
            TextView txtFullName;
            TextView txtDescription;
            TextView txtAvailability;
            TextView txtStatus;
        }
    }

}