package com.hitesh.pigeon.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.activities.GroupChatActivity;
import com.hitesh.pigeon.activities.GroupInfoActivity;
import com.hitesh.pigeon.activities.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.CustomVH> {

    private final Context context;
    private final List<Group> groups;
    private final HashMap<String, String> lastMessage = new HashMap<>();
    private final HashMap<String, String> timeOfMessage = new HashMap<>();
    private final HashMap<String, String> nameOfGroup = new HashMap<>();
    private final Set<String> lastMessageListenerSet = new HashSet<>();

    public GroupsAdapter(Context context, List<Group> groups) {
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view_of_chatfragment, parent, false);
        return new CustomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        if (nameOfGroup.containsKey(groups.get(position).groupId)) {
            holder.name.setText(nameOfGroup.get(groups.get(position).groupId));
        } else {
            setNameListenerFor(groups.get(position).groupId, position);
        }
        if (groups.get(position).dpUri != null) {
            Glide.with(context).load(groups.get(position).dpUri).into(holder.dp);
        } else {
            Glide.with(context).load(R.drawable.img).into(holder.dp);
        }
        if (lastMessage.containsKey(groups.get(position).groupId)) {
            holder.lastMsg.setText(lastMessage.get(groups.get(position).groupId));
        } else {
            setLastMsgListenerFor(groups.get(position).groupId, position);
        }
        if (timeOfMessage.containsKey(groups.get(position).groupId)) {
            holder.time.setText(timeOfMessage.get(groups.get(position).groupId));
        } else {
            setLastMsgListenerFor(groups.get(position).groupId, position);
        }
    }

    private void setNameListenerFor(final String groupId, final int position) {
        if (nameOfGroup.containsKey(groupId))
            return;
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.GROUPS)
                .child(groupId)
                .child(MainActivity.NAME)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            nameOfGroup.put(groupId, Objects.requireNonNull(dataSnapshot.getValue()).toString());
                            notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setLastMsgListenerFor(final String groupId, final int poition) {
        if (lastMessageListenerSet.contains(groupId))
            return;
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.GROUPS)
                .child(groupId)
                .child(MainActivity.MESSAGES)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (dataSnapshot.exists()) {
                            lastMessage.put(groupId, Objects.requireNonNull(dataSnapshot.child(MainActivity.MESSAGE).getValue()).toString());
                            timeOfMessage.put(groupId, getDate((Long) dataSnapshot.child(MainActivity.TIME).getValue()));
                            notifyItemChanged(poition);
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
        lastMessageListenerSet.add(groupId);
    }

    private String getDate(Long value) {
        Date date = new Date(value);
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm MMM d", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return groups.size();
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
            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGroupChat(getAdapterPosition());
                }
            });
            dp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGroupInfo(getAdapterPosition());
                }
            });
        }
    }

    public static class Group {
        public String groupId;
        public Boolean isAdmin;
        public Uri dpUri;

        public Group(String groupId, Boolean isAdmin) {
            this.groupId = groupId;
            this.isAdmin = isAdmin;
        }
    }

    private void showGroupInfo(int adapterPosition) {
        Intent intent = new Intent(context, GroupInfoActivity.class);
        intent.putExtra(MainActivity.GROUP_ID, groups.get(adapterPosition).groupId);
        intent.putExtra(MainActivity.IS_ADMIN, groups.get(adapterPosition).isAdmin);
        context.startActivity(intent);
    }

    private void startGroupChat(int adapterPosition) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(MainActivity.NAME, nameOfGroup.get(groups.get(adapterPosition).groupId));
        intent.putExtra(MainActivity.DP, groups.get(adapterPosition).dpUri.toString());
        intent.putExtra(MainActivity.GROUP_ID, groups.get(adapterPosition).groupId);
        intent.putExtra(MainActivity.IS_ADMIN, groups.get(adapterPosition).isAdmin);
        context.startActivity(intent);
    }
}
