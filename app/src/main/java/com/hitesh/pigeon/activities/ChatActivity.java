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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.adapters.ChatAdapter;
import com.hitesh.pigeon.adapters.ContactsAdapter;
import com.hitesh.pigeon.model.Messages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hitesh.pigeon.activities.MainActivity.LAST_SEEN;
import static com.hitesh.pigeon.activities.MainActivity.LAST_SEEN_SMALL;
import static com.hitesh.pigeon.activities.MainActivity.MESSAGE;
import static com.hitesh.pigeon.activities.MainActivity.ONLINE;
import static com.hitesh.pigeon.activities.MainActivity.SENDER;
import static com.hitesh.pigeon.activities.MainActivity.TIME;
import static com.hitesh.pigeon.activities.MainActivity.TYPE;
import static com.hitesh.pigeon.activities.MainActivity.mAuth;
import static com.hitesh.pigeon.activities.MainActivity.setLoginStatus;

public class ChatActivity extends AppCompatActivity {

    private Intent mIntent;
    private CircleImageView dp;
    private TextView name, lastSeen;
    private EditText msg;
    private FirebaseDatabase database;
    private String _name;
    private String uid;
    private String uri;
    private String chatId;
    private ChatAdapter adapter;
    private final List<Messages> messages = new ArrayList<>();
    private RecyclerView recycler;
    private boolean dontMakeOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        setReferences();
        extractIntentData();
        setInfo();
        createChatIdIfNotCreated();
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

    private void extractIntentData() {
        _name = mIntent.getStringExtra(ContactsAdapter.RECEIVER_NAME);
        uid = mIntent.getStringExtra(ContactsAdapter.RECEIVER_UID);
        uri = mIntent.getStringExtra(ContactsAdapter.RECEIVER_DP_URI);
    }

    private void createChatIdIfNotCreated() {
        DatabaseReference reference =
                database.getReference()
                        .child(MainActivity.USERS)
                        .child(Objects.requireNonNull(mAuth.getUid()))
                        .child(MainActivity.CHATS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (Objects.equals(childSnapshot.getKey(), uid)) {
                            chatId = (String) childSnapshot.getValue();
                            fetchChat();
                            return;
                        }
                    }
                }
                createId();
            }

            private void createId() {
                DatabaseReference reference = database.getReference()
                        .child(MainActivity.CHATS)
                        .push();
                reference.setValue(true);
                chatId = reference.getKey();
                database.getReference()
                        .child(MainActivity.USERS)
                        .child(MainActivity.mAuth.getUid())
                        .child(MainActivity.CHATS)
                        .child(uid)
                        .setValue(chatId);
                database.getReference()
                        .child(MainActivity.USERS)
                        .child(uid)
                        .child(MainActivity.CHATS)
                        .child(MainActivity.mAuth.getUid())
                        .setValue(chatId);
                fetchChat();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchChat() {
        if (chatId == null) {
            Toast.makeText(this, "Some error occurred", Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseReference reference = database.getReference()
                .child(MainActivity.CHATS)
                .child(chatId);
        reference.addChildEventListener(new ChildEventListener() {
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
                        recycler.scrollToPosition(messages.size() - 1);
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

    private void setReferences() {
        database = FirebaseDatabase.getInstance();
        mIntent = getIntent();
        dp = findViewById(R.id.dp);
        LinearLayout nameAndLastSeen = findViewById(R.id.nameAndLastSeen);
        nameAndLastSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDescription();
            }
        });
        name = findViewById(R.id.name);
        lastSeen = findViewById(R.id.lastSeen);
        lastSeen.setVisibility(View.GONE);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, messages);
        recycler.setAdapter(adapter);
        msg = findViewById(R.id.message);
        ImageButton send = findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void showDescription() {
        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra(MainActivity.UID, uid);
        intent.putExtra(MainActivity.EDITABLE, false);
        startActivity(intent);
    }

    private void sendMessage() {
        String message = msg.getText().toString();
        msg.setText("");
        if (!message.trim().equals("")) {
            HashMap<String, Object> msgInfo = new HashMap<>();
            msgInfo.put(SENDER, mAuth.getUid());
            msgInfo.put(MESSAGE, message);
            msgInfo.put(TYPE, 0);
            msgInfo.put(TIME, System.currentTimeMillis());
            database.getReference().child(MainActivity.CHATS).child(chatId).push().updateChildren(msgInfo);
        }
    }

    private void setInfo() {
        name.setText(_name);
        if (uri != null) {
            if (!uri.equals(""))
                Glide.with(this).load(Uri.parse(uri)).into(dp);
            else
                getDpUri();
        } else {
            getDpUri();
        }
        setLastSeen();
    }

    private void getDpUri() {
        StorageReference st = FirebaseStorage.getInstance().getReference().child(MainActivity.DP);
        st.child(uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this).load(uri).into(dp);
            }
        });
    }

    private void setLastSeen() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child(LAST_SEEN)
                .child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    lastSeen.setVisibility(View.VISIBLE);
                    if (snapshot.getValue() == null)
                        return;
                    if (((Long) snapshot.getValue()) == 0)
                        lastSeen.setText(ONLINE);
                    else
                        lastSeen.setText(LAST_SEEN_SMALL + getDate(snapshot.getValue()));
                }
            }

            private String getDate(Object value) {
                String date;
                TimeZone timeZone = TimeZone.getDefault();
                Date date1 = new Date((Long) value);
                SimpleDateFormat sdf = new SimpleDateFormat("H:mm EEE, MMM d", Locale.getDefault());
                sdf.setTimeZone(timeZone);
                date = sdf.format(date1);
                return date;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                lastSeen.setVisibility(View.GONE);
            }
        });
    }
}