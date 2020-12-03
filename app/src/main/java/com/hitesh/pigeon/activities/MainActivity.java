package com.hitesh.pigeon.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.adapters.TabsAccessorAdapter;
import com.hitesh.pigeon.utility.CountryToPhonePrefix;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    public static FirebaseAuth mAuth;
    public static HashMap<String, String> numberName = new HashMap<>();
    public static String iso;
    public static final String USERS = "USERS";
    public static final String PHONE = "PHONE";
    public static final String NAME = "NAME";
    public static final String STATUS = "STATUS";
    public static final String INITIAL_STATUS = "Hey there! I am using WhatsApp Clone.";
    public static final String DP = "DP";
    public static final String LAST_SEEN = "LAST SEEN";
    public static final String CHATS = "CHATS";
    public static final String GROUPS = "GROUPS";
    public static final String GROUP_ID = "GROUP_ID";
    public static final String PARTICIPANTS = "PARTICIPANTS";
    public static final String MESSAGES = "MESSAGES";
    public static final String EDITABLE = "EDITABLE";
    public static final String UID = "UID";
    public static final String SENDER = "SENDER";
    public static final String MESSAGE = "MESSAGE";
    public static final String TYPE = "TYPE";
    public static final String TIME = "TIME";
    public static final String ONLINE = "online";
    public static final String LAST_SEEN_SMALL = "last seen ";
    public static final int TEXT_MSG = 0;
    public static final int MEDIA_MSG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fetchContacts();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setReferences();
        setTabLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth = FirebaseAuth.getInstance();
        checkLoginStatus();
        if (mAuth.getCurrentUser() != null) {
            Handler handler = new Handler(Looper.myLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setLoginStatus(true);
                }
            }, 1000);
        }
    }

    @Override
    protected void onStop() {
        if (mAuth.getCurrentUser() != null)
            setLoginStatus(false);
        super.onStop();
    }

    public static void setLoginStatus(boolean online) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(LAST_SEEN).child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        if (online)
            reference.setValue(0);
        else
            reference.setValue(System.currentTimeMillis());
    }

    private void checkLoginStatus() {
        if (mAuth.getCurrentUser() == null) {
            setUser();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.new_group) {
            Intent intent = new Intent(this, GroupCreationActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.my_info) {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra(UID, mAuth.getUid());
            intent.putExtra(EDITABLE, true);
            startActivity(intent);
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(LAST_SEEN).child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            reference.setValue(System.currentTimeMillis()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mAuth.signOut();
                        checkLoginStatus();
                    } else {
                        Toast.makeText(MainActivity.this, "Same error occurred", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        return true;
    }

    private void setReferences() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setTabLayout() {
        viewPager.setAdapter(new TabsAccessorAdapter(this));
        TabLayoutMediator mediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Chats");
                        break;
                    case 1:
                        tab.setText("Groups");
                        break;
                    case 2:
                        tab.setText("Contacts");
                }
            }
        });
        mediator.attach();
    }

    private void fetchContacts() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            setContactList();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getISO();
                setContactList();
            }
        }
    }

    private void setContactList() {
        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
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
                    numberName.put(n, cursor.getString(nameID));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private void getISO() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getNetworkCountryIso() != null)
            if (!manager.getNetworkCountryIso().equals("")) {
                iso = manager.getNetworkCountryIso();
                iso = CountryToPhonePrefix.getPhone(iso);
            }
    }

    private void setUser() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}