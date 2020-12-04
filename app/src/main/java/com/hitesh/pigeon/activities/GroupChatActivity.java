package com.hitesh.pigeon.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.adapters.GroupChatAdapter;
import com.hitesh.pigeon.model.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hitesh.pigeon.activities.MainActivity.DP;
import static com.hitesh.pigeon.activities.MainActivity.GROUPS;
import static com.hitesh.pigeon.activities.MainActivity.IS_ADMIN;
import static com.hitesh.pigeon.activities.MainActivity.MESSAGE;
import static com.hitesh.pigeon.activities.MainActivity.NAME;
import static com.hitesh.pigeon.activities.MainActivity.SENDER;
import static com.hitesh.pigeon.activities.MainActivity.TIME;
import static com.hitesh.pigeon.activities.MainActivity.TYPE;
import static com.hitesh.pigeon.activities.MainActivity.setLoginStatus;

public class GroupChatActivity extends AppCompatActivity {

    private String name, groupId;
    private Boolean isAdmin;
    private Uri dpUri;
    private Intent intent;
    private TextView nameGroup, numOfMembers;
    private CircleImageView dp;
    private EditText msgBox;
    private ImageButton send;
    private FirebaseDatabase db;
    private final List<Messages> messages = new ArrayList<>();
    private GroupChatAdapter adapter;
    private LinearLayout nameAndLastSeen;
    private RecyclerView recyclerView;
    private boolean dontMakeOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setReferences();
        setValues();
        setListeners();
        fetchMessages();
    }

    private void setReferences() {
        db = FirebaseDatabase.getInstance();
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupChatAdapter(this, messages);
        recyclerView.setAdapter(adapter);
        nameGroup = findViewById(R.id.name);
        numOfMembers = findViewById(R.id.lastSeen);
        dp = findViewById(R.id.dp);
        msgBox = findViewById(R.id.message);
        send = findViewById(R.id.send);
        intent = getIntent();
        getIntentContent();
        nameAndLastSeen = findViewById(R.id.nameAndLastSeen);
    }

    private void fetchMessages() {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.GROUPS)
                .child(groupId)
                .child(MainActivity.MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            String sender = null, msg = null;
                            Long time = null;
                            Long type = null;
                            boolean nullExits = false;
                            if (dataSnapshot.child(SENDER).exists()) {
                                sender = Objects.requireNonNull(dataSnapshot.child(SENDER).getValue()).toString();
                            } else {
                                nullExits = true;
                            }
                            if (dataSnapshot.child(MESSAGE).exists()) {
                                msg = Objects.requireNonNull(dataSnapshot.child(MESSAGE).getValue()).toString();
                            } else {
                                nullExits = true;
                            }
                            if (dataSnapshot.child(TIME).exists()) {
                                time = (Long) dataSnapshot.child(TIME).getValue();
                            } else {
                                nullExits = true;
                            }
                            if (dataSnapshot.child(TYPE).exists()) {
                                type = (Long) dataSnapshot.child(TYPE).getValue();
                            } else {
                                nullExits = true;
                            }
                            if (!nullExits) {
                                messages.add(new Messages(sender, msg, type, time));
                                adapter.notifyItemInserted(messages.size() - 1);
                                recyclerView.smoothScrollToPosition(messages.size() - 1);
                            }
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

    private void setListeners() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        nameAndLastSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intent.putExtra(MainActivity.GROUP_ID, groupId);
                intent.putExtra(MainActivity.IS_ADMIN, isAdmin);
                startActivity(intent);
            }
        });
        db.getReference()
                .child(GROUPS)
                .child(groupId)
                .child(NAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            nameGroup.setText(Objects.requireNonNull(dataSnapshot.getValue()).toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage() {
        String message = msgBox.getText().toString().trim();
        msgBox.setText("");
        if (!message.equals("")) {
            HashMap<String, Object> content = new HashMap<>();
            content.put(MainActivity.SENDER, MainActivity.mAuth.getUid());
            content.put(MESSAGE, message);
            content.put(TYPE, 0);
            content.put(TIME, System.currentTimeMillis());
            db.getReference().child(MainActivity.GROUPS).child(groupId).child(MainActivity.MESSAGES).push().updateChildren(content);
        }
    }

    private void getIntentContent() {
        name = intent.getStringExtra(MainActivity.NAME);
        if (intent.getStringExtra(MainActivity.DP) != null)
            dpUri = Uri.parse(intent.getStringExtra(MainActivity.DP));
        groupId = intent.getStringExtra(MainActivity.GROUP_ID);
        isAdmin = intent.getBooleanExtra(IS_ADMIN, false);
    }

    private void setValues() {
        nameGroup.setText(name);
        if (dpUri == null) {
            dp.setImageResource(R.drawable.img);
            tryToGetDp();
        } else
            Glide.with(this).load(dpUri).into(dp);
        setNumberOfMembers();
    }

    private void tryToGetDp() {
        FirebaseStorage.getInstance()
                .getReference()
                .child(DP)
                .child(groupId)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(GroupChatActivity.this).load(uri).into(dp);
                    }
                });
    }

    private void setNumberOfMembers() {
        db.getReference()
                .child(MainActivity.GROUPS)
                .child(groupId)
                .child(MainActivity.PARTICIPANTS)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            numOfMembers.setText(dataSnapshot.getChildrenCount() + " members");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!dontMakeOnline)
                    setLoginStatus(true);
                dontMakeOnline = false;
            }
        }, 1000);
    }

    @Override
    protected void onStop() {
        dontMakeOnline = true;
        super.onStop();
        setLoginStatus(false);
    }
}