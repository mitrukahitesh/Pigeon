package com.hitesh.pigeon.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.adapters.ParticipantsAdapter;
import com.hitesh.pigeon.model.Participants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hitesh.pigeon.activities.MainActivity.DESCRIPTION;
import static com.hitesh.pigeon.activities.MainActivity.DP;
import static com.hitesh.pigeon.activities.MainActivity.GROUPS;
import static com.hitesh.pigeon.activities.MainActivity.GROUP_ID;
import static com.hitesh.pigeon.activities.MainActivity.IS_ADMIN;
import static com.hitesh.pigeon.activities.MainActivity.NAME;
import static com.hitesh.pigeon.activities.MainActivity.PARTICIPANTS;
import static com.hitesh.pigeon.activities.MainActivity.setLoginStatus;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private Boolean isAdmin;
    private CircleImageView dp;
    private ImageView dpEdit;
    private TextView name, des;
    private ImageButton nameEdit, desEdit;
    private Button dltDp;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final List<Participants> participants = new ArrayList<>();
    private ParticipantsAdapter adapter;
    private String x = null; //store new value
    private boolean dontMakeOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        Intent intent = getIntent();
        groupId = intent.getStringExtra(GROUP_ID);
        isAdmin = intent.getBooleanExtra(IS_ADMIN, false);
        setReferences();
        getParticipants();
        setValues();
        setListeners();
    }

    private void getParticipants() {
        db.getReference()
                .child(GROUPS)
                .child(groupId)
                .child(PARTICIPANTS)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            participants.add(new Participants(dataSnapshot.getKey(), (Boolean) dataSnapshot.getValue()));
                            adapter.notifyItemInserted(participants.size() - 1);
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
        dp = findViewById(R.id.dp);
        dpEdit = findViewById(R.id.change_dp);
        name = findViewById(R.id.name);
        des = findViewById(R.id.des);
        nameEdit = findViewById(R.id.nameEdit);
        desEdit = findViewById(R.id.desEdit);
        dltDp = findViewById(R.id.dlt_dp);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ParticipantsAdapter(this, participants);
        recycler.setAdapter(adapter);
    }

    private void setValues() {
        if (!isAdmin) {
            dpEdit.setVisibility(View.GONE);
            nameEdit.setVisibility(View.GONE);
            desEdit.setVisibility(View.GONE);
            dltDp.setVisibility(View.GONE);
        }
        db.getReference()
                .child(GROUPS)
                .child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            name.setText(Objects.requireNonNull(dataSnapshot.child(NAME).getValue()).toString());
                            des.setText(Objects.requireNonNull(dataSnapshot.child(DESCRIPTION).getValue()).toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        FirebaseStorage.getInstance().getReference().child(DP).child(groupId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(GroupInfoActivity.this).load(uri).into(dp);
            }
        });
    }

    private void setListeners() {
        dltDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseStorage.getInstance()
                        .getReference()
                        .child(DP)
                        .child(groupId)
                        .delete()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dp.setImageResource(R.drawable.img);
                            }
                        });
            }
        });

        dpEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        View view = LayoutInflater.from(this).inflate(R.layout.new_name_status, null, false);
        final EditText newVal = view.findViewById(R.id.newVal);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        x = newVal.getText().toString();
                        if (x.trim().equals(""))
                            return;
                        if (NAME.contentEquals(newVal.getHint())) {
                            updateValOf(NAME, x.trim());
                        } else {
                            updateValOf(DESCRIPTION, x.trim());
                        }
                        newVal.setText("");
                    }

                    private void updateValOf(String attr, String val) {
                        db.getReference()
                                .child(GROUPS)
                                .child(groupId)
                                .child(attr)
                                .setValue(val);

                    }
                })
                .setCancelable(true)
                .setView(view)
                .create();

        nameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setTitle("Enter new name");
                dialog.setIcon(R.drawable.ic_baseline_edit_24);
                newVal.setHint(NAME);
                dialog.show();
            }
        });

        desEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setTitle("Enter new description");
                dialog.setIcon(R.drawable.ic_baseline_edit_24);
                newVal.setHint(DESCRIPTION);
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                FirebaseStorage.getInstance().getReference().child(DP).child(groupId).putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Glide.with(GroupInfoActivity.this).load(data.getData()).into(dp);
                    }
                });
            }
        }
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