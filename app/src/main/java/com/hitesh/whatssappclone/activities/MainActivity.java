package com.hitesh.whatssappclone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hitesh.whatssappclone.R;
import com.hitesh.whatssappclone.adapters.TabsAccessorAdapter;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    public static FirebaseUser mUser;
    public static FirebaseAuth mAuth;
    public static final String USERS = "USERS";
    public static final String PHONE = "PHONE";
    public static final String NAME = "NAME";
    public static final String DP = "DP";
    public static final String LAST_SEEN = "LAST SEEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        setReferences();
        setTabLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        checkLoginStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null)
            setLoginStatus();
    }

    public static void setLoginStatus() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(LAST_SEEN).child(mUser.getUid());
        reference.setValue(0);
        reference.onDisconnect().setValue(System.currentTimeMillis());
    }

    private void checkLoginStatus() {
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            setUser();
            mUser = mAuth.getCurrentUser();
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
        switch (item.getItemId()) {
            case R.id.search:

            case R.id.setting:

            default:
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(LAST_SEEN).child(mUser.getUid());
                reference.setValue(System.currentTimeMillis()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAuth.signOut();
                            checkLoginStatus();
                        } else {
                            Toast.makeText(context, "Same error occurred", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        }
        return true;
    }

    private void setReferences() {
        context = this;
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

    private void setUser() {
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }
}