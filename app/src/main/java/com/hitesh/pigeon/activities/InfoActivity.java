package com.hitesh.pigeon.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hitesh.pigeon.R;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoActivity extends AppCompatActivity {

    private String uid;
    private CircleImageView dp;
    private TextView phone, name, status;
    private ImageButton nameEdit;
    private ImageButton statusEdit;
    private String x = null; //store new value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setReferences();
        fetchData();
    }

    private void fetchData() {
        FirebaseStorage.getInstance()
                .getReference()
                .child(MainActivity.DP)
                .child(uid)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(InfoActivity.this).load(uri).into(dp);
                    }
                });
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.USERS)
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            name.setText(Objects.requireNonNull(dataSnapshot.child(MainActivity.NAME).getValue()).toString());
                            phone.setText(Objects.requireNonNull(dataSnapshot.child(MainActivity.PHONE).getValue()).toString());
                            status.setText(Objects.requireNonNull(dataSnapshot.child(MainActivity.STATUS).getValue()).toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setReferences() {
        Intent mIntent = getIntent();
        uid = mIntent.getStringExtra(MainActivity.UID);
        boolean editable = mIntent.getBooleanExtra(MainActivity.EDITABLE, false);
        dp = findViewById(R.id.dp);
        ImageView changeDp = findViewById(R.id.change_dp);
        phone = findViewById(R.id.number);
        name = findViewById(R.id.name);
        status = findViewById(R.id.status);
        ImageButton phoneCall = findViewById(R.id.phoneCall);
        nameEdit = findViewById(R.id.nameEdit);
        statusEdit = findViewById(R.id.statusEdit);
        Button dltDp = findViewById(R.id.dlt_dp);
        phone.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("Phone", phone.getText().toString());
                manager.setPrimaryClip(data);
                Toast.makeText(InfoActivity.this, "Phone number copied", Toast.LENGTH_LONG).show();
                return true;
            }
        });
        if (editable) {
            phoneCall.setVisibility(View.GONE);
            changeDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, DpAndName.REQ_CODE);
                }
            });
            dltDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseStorage.getInstance()
                            .getReference()
                            .child(MainActivity.DP)
                            .child(uid)
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
                                    dp.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.img, null));
                                }
                            });
                }
            });
            makeNameStatusEditable();
        } else {
            dltDp.setVisibility(View.GONE);
            changeDp.setVisibility(View.GONE);
            nameEdit.setVisibility(View.GONE);
            statusEdit.setVisibility(View.GONE);
            phoneCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    makeCall(phone.getText().toString());
                }
            });
        }
    }

    private void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void makeNameStatusEditable() {
        View view = LayoutInflater.from(this).inflate(R.layout.new_name_status, null, false);
        final EditText newVal = view.findViewById(R.id.newVal);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setView(view)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        x = newVal.getText().toString();
                        if (x.trim().equals(""))
                            return;
                        if (MainActivity.NAME.contentEquals(newVal.getHint())) {
                            updateValOf(MainActivity.NAME, x.trim());
                        } else {
                            updateValOf(MainActivity.STATUS, x.trim());
                        }
                        newVal.setText("");
                    }
                })
                .create();
        nameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setTitle("Enter new Name");
                dialog.setIcon(R.drawable.ic_baseline_edit_24);
                newVal.setHint(MainActivity.NAME);
                dialog.show();
            }
        });
        statusEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setTitle("Enter new Status");
                dialog.setIcon(R.drawable.ic_baseline_edit_24);
                newVal.setHint(MainActivity.STATUS);
                dialog.show();
            }
        });
    }

    private void updateValOf(final String attr, final String val) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.USERS)
                .child(uid)
                .child(attr)
                .setValue(val)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (attr.equals(MainActivity.NAME)) {
                                name.setText(val);
                            } else {
                                status.setText(val);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DpAndName.REQ_CODE && resultCode == RESULT_OK) {
            if (data == null)
                return;
            if (data.getData() != null) {
                uploadNewDp(data.getData());
            }
        }
    }

    private void uploadNewDp(final Uri data) {
        FirebaseStorage.getInstance()
                .getReference()
                .child(MainActivity.DP)
                .child(uid)
                .putFile(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Some error occurred", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Glide.with(InfoActivity.this).load(data).into(dp);
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
                MainActivity.setLoginStatus(true);
            }
        }, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainActivity.setLoginStatus(false);
    }
}