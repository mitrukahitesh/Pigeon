package com.hitesh.pigeon.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.utility.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends AppCompatActivity {

    private CircleImageView dp;
    private EditText name;
    private Button finish;
    private Intent mIntent;
    private ArrayList<String> uids;
    private String groupId;
    private LoadingDialog dialog;
    public static final int REQ_CODE = 1;
    private Uri dpUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        setReferences();
    }

    private void setReferences() {
        mIntent = getIntent();
        uids = mIntent.getStringArrayListExtra(MainActivity.PARTICIPANTS);
        groupId = mIntent.getStringExtra(MainActivity.GROUP_ID);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        finish = findViewById(R.id.button);
        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQ_CODE);
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().trim().equals(""))
                    return;
                dialog = new LoadingDialog(GroupInfoActivity.this);
                dialog.start();
                storeDp();
                storeGroupData();
            }
        });
    }

    private void storeDp() {
        if (dpUri == null)
            return;
        FirebaseStorage.getInstance()
                .getReference()
                .child(MainActivity.DP)
                .child(groupId)
                .putFile(dpUri);
    }

    private void storeGroupData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(MainActivity.USERS);
        HashMap<String, Object> participants = new HashMap<>();
        for (String user : uids) {
            participants.put(user, true);
            reference.child(user).child(MainActivity.GROUPS).child(groupId).setValue(true);
        }
        reference = FirebaseDatabase.getInstance().getReference().child(MainActivity.GROUPS).child(groupId);
        reference.child(MainActivity.NAME).setValue(name.getText().toString());
        reference.child(MainActivity.PARTICIPANTS).updateChildren(participants).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(GroupInfoActivity.this, "Group created successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(GroupInfoActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                }
                finish();
                dialog.stop();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) {
            if (data != null && resultCode == RESULT_OK) {
                dpUri = data.getData();
                Glide.with(this).load(dpUri).into(dp);
            }
        }
    }
}