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
import com.google.firebase.storage.StorageReference;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.utility.LoadingDialog;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class DpAndName extends AppCompatActivity {

    private CircleImageView imageView;
    private EditText name;
    private Button finish;
    private LoadingDialog loadingDialog;
    private Uri dpUri = null;
    private String phoneNum;
    public static final int REQ_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dp_and_name);
        imageView = (CircleImageView) findViewById(R.id.dp);
        name = (EditText) findViewById(R.id.name);
        finish = (Button) findViewById(R.id.button);
        loadingDialog = new LoadingDialog(this);
        imageView.setOnClickListener(new View.OnClickListener() {
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
                if (name.getText() == null)
                    return;
                if (name.getText().toString().trim().equals(""))
                    return;
                loadingDialog.start();
                phoneNum = getIntent().getStringExtra(MainActivity.PHONE);
                storeName();
                storeDP();
            }
        });
    }

    private void storeName() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(MainActivity.USERS)
                .child(Objects.requireNonNull(MainActivity.mAuth.getUid()));
        HashMap<String, Object> userDetail = new HashMap<>();
        userDetail.put(MainActivity.NAME, name.getText().toString().trim());
        userDetail.put(MainActivity.PHONE, phoneNum);
        userDetail.put(MainActivity.STATUS, MainActivity.INITIAL_STATUS);
        reference.updateChildren(userDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingDialog.stop();
                if (!task.isSuccessful()) {
                    MainActivity.mAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    private void storeDP() {
        if (dpUri == null)
            return;
        StorageReference reference = FirebaseStorage.getInstance().getReference().child(MainActivity.DP);
        reference.child(MainActivity.mAuth.getUid()).putFile(dpUri);
    }

//    private String getExtension() {
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//        return  mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(dpUri));
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            if (data == null)
                return;
            if (data.getData() != null) {
                dpUri = data.getData();
                Glide.with(this).load(dpUri).into(imageView);
            }
        }
    }

    @Override
    public void onBackPressed() {
        MainActivity.mAuth.signOut();
        super.onBackPressed();
    }
}