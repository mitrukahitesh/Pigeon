package com.hitesh.pigeon.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.activities.InfoActivity;
import com.hitesh.pigeon.activities.MainActivity;
import com.hitesh.pigeon.model.Messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.CustomVH> {

    private final Context context;
    private final List<Messages> messages;
    public static final HashMap<String, String> participants = new HashMap<>();

    public GroupChatAdapter(Context context, List<Messages> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_message_view, parent, false);
        return new CustomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        TextView msg, time;
        if (messages.get(position).getSENDER().equals(MainActivity.mAuth.getUid())) {
            holder.boxReceived.setVisibility(View.GONE);
            holder.boxSent.setVisibility(View.VISIBLE);
            msg = holder.msgSent;
            time = holder.timeSent;
        } else {
            holder.boxSent.setVisibility(View.GONE);
            holder.boxReceived.setVisibility(View.VISIBLE);
            msg = holder.msgReceived;
            time = holder.timeReceived;
            if (participants.containsKey(messages.get(position).getSENDER())) {
                if (MainActivity.numberName.containsKey(participants.get(messages.get(position).getSENDER()))) {
                    holder.sender.setText(MainActivity.numberName.get(participants.get(messages.get(position).getSENDER())));
                } else {
                    holder.sender.setText(participants.get(messages.get(position).getSENDER()));
                }
            } else {
                getSenderDetails(position);
            }
        }
        msg.setText(messages.get(position).getMESSAGE());
        time.setText(getTime(messages.get(position).getTIME()));
    }

    private void getSenderDetails(final int position) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.USERS)
                .child(messages.get(position).getSENDER())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            participants.put(messages.get(position).getSENDER(), Objects.requireNonNull(dataSnapshot.child(MainActivity.PHONE).getValue()).toString());
                            notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private String getTime(Long time) {
        TimeZone timeZone = TimeZone.getDefault();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
        sdf.setTimeZone(timeZone);
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        LinearLayout boxSent, boxReceived;
        TextView msgSent, timeSent, msgReceived, timeReceived, sender;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            boxSent = itemView.findViewById(R.id.boxSent);
            msgSent = itemView.findViewById(R.id.messageSent);
            timeSent = itemView.findViewById(R.id.timeSent);
            boxReceived = itemView.findViewById(R.id.boxReceived);
            msgReceived = itemView.findViewById(R.id.messageReceived);
            timeReceived = itemView.findViewById(R.id.timeReceived);
            sender = itemView.findViewById(R.id.sender);
            sender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDescription(getAdapterPosition());
                }
            });
        }
    }

    private void showDescription(int position) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(MainActivity.UID, messages.get(position).getSENDER());
        intent.putExtra(MainActivity.EDITABLE, false);
        context.startActivity(intent);
    }
}
