package com.hitesh.pigeon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.adapters.GroupCreationAdapter;
import com.hitesh.pigeon.model.Contacts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupCreationActivity extends AppCompatActivity {

    private GroupCreationAdapter adapter;
    private final List<Contacts> contacts = new ArrayList<>();
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    public static ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();
        actionBar.setTitle("0 selected");
        setReferences();
        setContactList();
    }

    private void setReferences() {
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupCreationAdapter(this, contacts);
        recycler.setAdapter(adapter);
        Button next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selected = adapter.getSelectedContacts();
                if (selected.size() < 2) {
                    Toast.makeText(GroupCreationActivity.this, "Select at least 2", Toast.LENGTH_LONG).show();
                    return;
                }
                final Intent intent = new Intent(GroupCreationActivity.this, GroupDpAndNameActivity.class);
                intent.putExtra(MainActivity.PARTICIPANTS, selected);
                final DatabaseReference reference = FirebaseDatabase.getInstance()
                        .getReference()
                        .child(MainActivity.GROUPS)
                        .push();
                reference.setValue(true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                intent.putExtra(MainActivity.GROUP_ID, reference.getKey());
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GroupCreationActivity.this, "Some error occurred", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void setContactList() {
        for (Map.Entry<String, String> contact : MainActivity.numberName.entrySet()) {
            addIfUserExists(contact.getValue(), contact.getKey());
        }
    }

    private void addIfUserExists(final String name, final String number) {
        Query query = db.getReference().child(MainActivity.USERS).orderByChild(MainActivity.PHONE).equalTo(number);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        contacts.add(new Contacts(name, number, childSnapshot.getKey()));
                        adapter.notifyItemInserted(contacts.size() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}