package com.hitesh.whatsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.hitesh.whatsapp.AvailableChats;
import com.hitesh.whatsapp.CountryToPhonePrefix;
import com.hitesh.whatsapp.R;
import com.hitesh.whatsapp.activities.ChatActivity;
import com.hitesh.whatsapp.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.CustomVH> {

    private Context context;
    private List<AvailableChats> availableChats = new ArrayList<>();
    private HashMap<String, String> numberName = new HashMap<>();
    private HashMap<String, String> lastMessage = new HashMap<>();
    private HashMap<String, String> timeOfMessage = new HashMap<>();
    private String iso = null;

    public ChatsAdapter(Context context, List<AvailableChats> availableChats) {
        this.context = context;
        this.availableChats = availableChats;
        getISO();
        setContactList();
    }

    private void setContactList() {
        String[] projection = new String[]{
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null, null);
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
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getNetworkCountryIso() != null)
            if (!manager.getNetworkCountryIso().equals("")) {
                iso = manager.getNetworkCountryIso();
                iso = CountryToPhonePrefix.getPhone(iso);
            }
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view_of_chatfragment, parent, false);
        return new CustomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        if (numberName.containsKey(availableChats.get(position).number)) {
            holder.name.setText(numberName.get(availableChats.get(position).number));
        } else {
            holder.name.setText(availableChats.get(position).number);
        }
        if (lastMessage.containsKey(availableChats.get(position).number)) {
            holder.box.setVisibility(View.VISIBLE);
            holder.lastMsg.setVisibility(View.VISIBLE);
            holder.lastMsg.setText(lastMessage.get(availableChats.get(position).number));
        } else {
            holder.box.setVisibility(View.VISIBLE);
            fetchLastMessage(position);
            holder.lastMsg.setVisibility(View.GONE);
        }
        if (timeOfMessage.containsKey(availableChats.get(position).number)) {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(timeOfMessage.get(availableChats.get(position).number));
        } else {
            fetchLastMessage(position);
            holder.time.setVisibility(View.GONE);
        }
        if (availableChats.get(position).dpUri != null) {
            Glide.with(context).load(availableChats.get(position).dpUri).into(holder.dp);
        } else {
            Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.img)).into(holder.dp);
        }
    }

    private void fetchLastMessage(final int position) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.CHATS)
                .child(availableChats.get(position).chatId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            lastMessage.put(availableChats.get(position).number, dataSnapshot.child(ChatActivity.MESSAGE).getValue().toString());
                            timeOfMessage.put(availableChats.get(position).number, getDate((Long) dataSnapshot.child(ChatActivity.TIME).getValue()));
                            notifyItemChanged(position);
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

    private String getDate(Long value) {
        Date date = new Date(value);
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm MMM d", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return availableChats.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        CircleImageView dp;
        TextView name, lastMsg, time;
        LinearLayout box;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            lastMsg = itemView.findViewById(R.id.lastMsg);
            time = itemView.findViewById(R.id.time);
            box = itemView.findViewById(R.id.box);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startChat(getAdapterPosition());
                }
            });
        }
    }

    private void startChat(int adapterPosition) {
        final Intent intent = new Intent(context, ChatActivity.class);
        if (numberName.containsKey(availableChats.get(adapterPosition).number)) {
            intent.putExtra(ContactsAdapter.RECEIVER_NAME, numberName.get(availableChats.get(adapterPosition).number));
        } else {
            intent.putExtra(ContactsAdapter.RECEIVER_NAME, availableChats.get(adapterPosition).number);
        }
        intent.putExtra(ContactsAdapter.RECEIVER_NUMBER, availableChats.get(adapterPosition).number);
        intent.putExtra(ContactsAdapter.RECEIVER_UID, availableChats.get(adapterPosition).uid);
        intent.putExtra(ContactsAdapter.CHAT_ID, availableChats.get(adapterPosition).chatId);
        if (availableChats.get(adapterPosition).dpUri != null) {
            intent.putExtra(ContactsAdapter.RECEIVER_DP_URI, availableChats.get(adapterPosition).dpUri.toString());
        } else {
            intent.putExtra(ContactsAdapter.RECEIVER_DP_URI, "");
        }
        context.startActivity(intent);
    }
}
