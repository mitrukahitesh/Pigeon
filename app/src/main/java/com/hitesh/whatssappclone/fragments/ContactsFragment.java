package com.hitesh.whatssappclone.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hitesh.whatssappclone.Contacts;
import com.hitesh.whatssappclone.CountryToPhonePrefix;
import com.hitesh.whatssappclone.R;
import com.hitesh.whatssappclone.activities.MainActivity;
import com.hitesh.whatssappclone.adapters.ContactsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private RecyclerView recyclerView;
    ContactsAdapter adapter;
    private List<Contacts> contacts = new ArrayList<>();
    private String iso = null;
    private final int REQ_CODE = 1;


    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getISO();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getContactList();
        adapter = new ContactsAdapter(getContext(), contacts);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void getContactList() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQ_CODE);
        } else {
            setContactList();
        }
    }

    private void setContactList() {
        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameID = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberID = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                do {
                    String n = cursor.getString(numberID);
                    if (n.charAt(0) != '+') {
                        n = iso + n;
                    }
                    n = n.replace(" ", "");
                    n = n.replace("-", "");
                    n = n.replace("(", "");
                    n = n.replace(")", "");
                    addIfUserExists(cursor.getString(nameID), n);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private void addIfUserExists(final String name, final String number) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(MainActivity.USERS);
        Query query = reference.orderByChild(MainActivity.PHONE).equalTo(number);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        contacts.add(new Contacts(name, number, childSnapshot.getKey()));
                        adapter.notifyItemInserted(contacts.size() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getISO() {
        TelephonyManager manager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getNetworkCountryIso() != null)
            if (!manager.getNetworkCountryIso().equals("")) {
                iso = manager.getNetworkCountryIso();
                iso = CountryToPhonePrefix.getPhone(iso);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContactList();
            }
        }
    }
}