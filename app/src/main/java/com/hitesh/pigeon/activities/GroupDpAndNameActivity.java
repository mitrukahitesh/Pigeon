package com.hitesh.pigeon.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.utility.LoadingDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hitesh.pigeon.activities.MainActivity.setLoginStatus;

public class GroupDpAndNameActivity extends AppCompatActivity {

    private CircleImageView dp;
    private EditText name;
    private ArrayList<String> uids;
    private String groupId;
    private LoadingDialog dialog;
    public static final int REQ_CODE = 1;
    private Uri dpUri = null;
    private boolean dontMakeOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dp_and_name);
        setReferences();
    }

    private void setReferences() {
        Intent mIntent = getIntent();
        uids = mIntent.getStringArrayListExtra(MainActivity.PARTICIPANTS);
        groupId = mIntent.getStringExtra(MainActivity.GROUP_ID);
        dp = findViewById(R.id.dp);
        name = findViewById(R.id.name);
        Button finish = findViewById(R.id.button);
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
                name.setEnabled(false);
                dialog = new LoadingDialog(GroupDpAndNameActivity.this);
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
                .putFile(dpUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        dialog.stop();
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        dialog.stop();
                    }
                });
    }

    private void storeGroupData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(MainActivity.USERS);
        HashMap<String, Object> participants = new HashMap<>();
        participants.put(MainActivity.mAuth.getUid(), true);
        reference.child(Objects.requireNonNull(MainActivity.mAuth.getUid())).child(MainActivity.GROUPS).child(groupId).setValue(true);
        while (uids.contains(MainActivity.mAuth.getUid()))
            uids.remove(MainActivity.mAuth.getUid());
        for (String user : uids) {
            participants.put(user, false);
            reference.child(user).child(MainActivity.GROUPS).child(groupId).setValue(false);
        }
        reference = FirebaseDatabase.getInstance().getReference().child(MainActivity.GROUPS).child(groupId);
        reference.child(MainActivity.NAME).setValue(name.getText().toString());
        reference.child(MainActivity.DESCRIPTION).setValue(name.getText().toString());
        reference.child(MainActivity.PARTICIPANTS).updateChildren(participants).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
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