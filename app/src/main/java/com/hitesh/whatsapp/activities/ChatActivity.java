package com.hitesh.whatsapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hitesh.whatsapp.R;
import com.hitesh.whatsapp.adapters.ContactsAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hitesh.whatsapp.activities.MainActivity.setLoginStatus;

public class ChatActivity extends AppCompatActivity {

    private Intent mIntent;
    private CircleImageView dp;
    private TextView name, lastSeen;
    private RecyclerView recyclerView;
    private EditText msg;
    private ImageButton send;
    public static final String ONLINE = "online";
    public static final String LAST_SEEN = "last seen ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("");
        setReferences();
        setInfo();
    }

    private void setReferences() {
        mIntent = getIntent();
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        lastSeen = findViewById(R.id.lastSeen);
        lastSeen.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recycler);
        msg = findViewById(R.id.message);
        send = findViewById(R.id.send);
    }

    private void setInfo() {
        name.setText(mIntent.getStringExtra(ContactsAdapter.RECEIVER_NAME));
        String uri = mIntent.getStringExtra(ContactsAdapter.RECEIVER_DP_URI);
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
        st.child(mIntent.getStringExtra(ContactsAdapter.RECEIVER_UID)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ChatActivity.this).load(uri).into(dp);
            }
        });
    }

    private void setLastSeen() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.LAST_SEEN)
                .child(mIntent.getStringExtra(ContactsAdapter.RECEIVER_UID));
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    lastSeen.setVisibility(View.VISIBLE);
                    if (((Long) snapshot.getValue()) == 0)
                        lastSeen.setText(ONLINE);
                    else
                        lastSeen.setText(LAST_SEEN + getDate(snapshot.getValue()));
                }
            }

            private String getDate(Object value) {
                String date ;
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

    @Override
    protected void onResume() {
        super.onResume();
        setLoginStatus(true);
    }
}