package com.hitesh.whatsapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hitesh.whatsapp.R;
import com.hitesh.whatsapp.activities.MainActivity;
import com.hitesh.whatsapp.adapters.ContactsAdapter;
import com.hitesh.whatsapp.model.Contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsFragment extends Fragment {

    private ContactsAdapter adapter;
    private final List<Contacts> contacts = new ArrayList<>();


    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setContactList();
        adapter = new ContactsAdapter(getContext(), contacts);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void setContactList() {
        for (Map.Entry<String, String> entry : MainActivity.numberName.entrySet()) {
            addIfUserExists(entry.getValue(), entry.getKey());
        }
    }

    private void addIfUserExists(final String name, final String number) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(MainActivity.USERS);
        Query query = reference.orderByChild(MainActivity.PHONE).equalTo(number);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        contacts.add(new Contacts(name, number, childSnapshot.getKey()));
                        adapter.notifyItemInserted(contacts.size() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}