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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.activities.InfoActivity;
import com.hitesh.pigeon.activities.MainActivity;
import com.hitesh.pigeon.model.Participants;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.CustomVH> {

    private final Context context;
    private final List<Participants> participants;
    private final HashMap<String, User> users = new HashMap<>();

    public ParticipantsAdapter(Context context, List<Participants> participants) {
        this.context = context;
        this.participants = participants;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participant_view, parent, false);
        return new CustomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        if (users.containsKey(participants.get(position).getUid())) {
            if (MainActivity.numberName.containsKey(Objects.requireNonNull(users.get(participants.get(position).getUid())).number)) {
                holder.name.setText(MainActivity.numberName.get(Objects.requireNonNull(users.get(participants.get(position).getUid())).number));
            } else {
                holder.name.setText(Objects.requireNonNull(users.get(participants.get(position).getUid())).number);
            }
            if (Objects.requireNonNull(users.get(participants.get(position).getUid())).dpUri == null) {
                Glide.with(context).load(R.drawable.img).into(holder.dp);
            } else {
                Glide.with(context).load(Objects.requireNonNull(users.get(participants.get(position).getUid())).dpUri).into(holder.dp);
            }
            holder.status.setText(Objects.requireNonNull(users.get(participants.get(position).getUid())).status);
        } else {
            fetchData(position);
        }
        if (participants.get(position).getAdmin()) {
            holder.admin.setVisibility(View.VISIBLE);
        } else {
            holder.admin.setVisibility(View.GONE);
        }
    }

    private void fetchData(final int position) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(MainActivity.USERS)
                .child(participants.get(position).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                            return;
                        fetchDp(Objects.requireNonNull(dataSnapshot.child(MainActivity.PHONE).getValue()).toString(),
                                Objects.requireNonNull(dataSnapshot.child(MainActivity.STATUS).getValue()).toString(),
                                position);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void fetchDp(final String number, final String status, final int position) {
        FirebaseStorage.getInstance()
                .getReference()
                .child(MainActivity.DP)
                .child(participants.get(position).getUid())
                .getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Uri uri = null;
                        if (task.isSuccessful()) {
                            uri = task.getResult();
                        }
                        users.put(participants.get(position).getUid(), new User(number, status, uri));
                        notifyItemChanged(position);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        CircleImageView dp;
        TextView admin, name, status;
        LinearLayout box;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            admin = itemView.findViewById(R.id.admin);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
            box = itemView.findViewById(R.id.box);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInfo(getAdapterPosition());
                }
            });
        }
    }

    private class User {
        public String number;
        public String status;
        public Uri dpUri;

        public User(String number, String status, Uri dpUri) {
            this.number = number;
            this.status = status;
            this.dpUri = dpUri;
        }
    }

    private void showInfo(int adapterPosition) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(MainActivity.UID, participants.get(adapterPosition).getUid());
        if (participants.get(adapterPosition).getUid().equals(MainActivity.mAuth.getUid())) {
            intent.putExtra(MainActivity.EDITABLE, true);
        } else {
            intent.putExtra(MainActivity.EDITABLE, false);
        }
        context.startActivity(intent);
    }
}
