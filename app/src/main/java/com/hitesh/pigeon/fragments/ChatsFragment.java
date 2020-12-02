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

import com.google.android.gms.tasks.OnCanceledListener;
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
import com.hitesh.pigeon.adapters.ChatsAdapter;
import com.hitesh.pigeon.model.AvailableChats;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ChatsFragment extends Fragment {

    private RecyclerView recycler;
    private ChatsAdapter adapter;
    private FirebaseDatabase database;
    private final List<AvailableChats> availableChats = new ArrayList<>();


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        recycler = view.findViewById(R.id.recycler);
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            return view;
        initialSetup();
        return view;
    }

    private void initialSetup() {
        database = FirebaseDatabase.getInstance();
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatsAdapter(getContext(), availableChats);
        recycler.setAdapter(adapter);
        getChatList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recycler.getAdapter() == null) {
            initialSetup();
        }
    }

    private void getChatList() {
        DatabaseReference reference =
                database
                        .getReference()
                        .child(MainActivity.USERS)
                        .child(Objects.requireNonNull(MainActivity.mAuth.getUid()))
                        .child(MainActivity.CHATS);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    String uid = dataSnapshot.getKey();
                    String chatId = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                    getNumberFromUidIfMessagesExist(new AvailableChats(uid, chatId));
                }
            }

            private void getNumberFromUidIfMessagesExist(final AvailableChats availableChats) {
                database.getReference()
                        .child(MainActivity.CHATS)
                        .child(availableChats.chatId)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChildren()) {
                                        if (availableChats.number == null)
                                            getNumberFromUid(availableChats);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            private void getNumberFromUid(final AvailableChats chat) {
                DatabaseReference ref = database.getReference().child(MainActivity.USERS).child(chat.uid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            chat.number = Objects.requireNonNull(dataSnapshot.child(MainActivity.PHONE).getValue()).toString();
                            FirebaseStorage.getInstance()
                                    .getReference()
                                    .child(MainActivity.DP)
                                    .child(chat.uid)
                                    .getDownloadUrl()
                                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful())
                                                chat.dpUri = task.getResult();
                                            addThisToChatList(chat);
                                        }
                                    })
                                    .addOnCanceledListener(new OnCanceledListener() {
                                        @Override
                                        public void onCanceled() {
                                            addThisToChatList(chat);
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

    private void addThisToChatList(AvailableChats chat) {
        availableChats.add(chat);
        adapter.notifyItemInserted(availableChats.size() - 1);
    }
}