package com.hitesh.pigeon.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.activities.MainActivity;
import com.hitesh.pigeon.adapters.GroupsAdapter;
import com.hitesh.pigeon.model.AvailableGroups;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    private final List<AvailableGroups> groups = new ArrayList<>();
    private GroupsAdapter adapter;
    private FirebaseDatabase database;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupsAdapter(getContext(), groups);
        recycler.setAdapter(adapter);
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return view;
        initialSetup();
        return view;
    }

    private void initialSetup() {
        database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference()
                .child(MainActivity.USERS)
                .child(MainActivity.mAuth.getUid())
                .child(MainActivity.GROUPS);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    AvailableGroups group = new AvailableGroups(dataSnapshot.getKey());
                    getGroupDp(group);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getGroupDp(final AvailableGroups group) {
        FirebaseStorage.getInstance().getReference()
                .child(MainActivity.DP)
                .child(group.groupId)
                .getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful())
                            group.dpUri = task.getResult();
                        getNameOfGroup(group);
                    }
                });
    }

    private void getNameOfGroup(final AvailableGroups group) {
        database.getReference()
                .child(MainActivity.GROUPS)
                .child(group.groupId)
                .child(MainActivity.NAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            group.name = dataSnapshot.getValue().toString();
                            groups.add(group);
                            adapter.notifyItemInserted(groups.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}