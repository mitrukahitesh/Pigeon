package com.hitesh.pigeon.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hitesh.pigeon.model.Contacts;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.activities.ChatActivity;
import com.hitesh.pigeon.activities.MainActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.CustomVH> {

    private final Context context;
    private final List<Contacts> contacts;
    private final HashMap<String, Uri> dpUri = new HashMap<>();
    public static final String RECEIVER_NAME = "RECEIVER_NAME";
    public static final String RECEIVER_NUMBER = "RECEIVER_NUMBER";
    public static final String RECEIVER_DP_URI = "RECEIVER_DP_URI";
    public static final String RECEIVER_UID = "RECEIVER_UID";
    public static final String CHAT_ID = "CHAT_ID";

    public ContactsAdapter(Context context, List<Contacts> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_view, parent, false);
        return new CustomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.name.setText(contacts.get(position).getName());
        holder.number.setText(contacts.get(position).getNumber());
        if (dpUri.containsKey(contacts.get(position).getNumber())) {
            Glide.with(context).load(dpUri.get(contacts.get(position).getNumber())).into(holder.imageView);
        } else {
            Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.img)).into(holder.imageView);
            getDpUri(position);
        }
    }

    private void getDpUri(final int pos) {
        StorageReference st = FirebaseStorage.getInstance().getReference().child(MainActivity.DP);
        st.child(contacts.get(pos).getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                dpUri.put(contacts.get(pos).getNumber(), uri);
                notifyItemChanged(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        TextView name, number;
        CircleImageView imageView;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            imageView = itemView.findViewById(R.id.dp);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startChat(getAdapterPosition());
                }
            });
        }
    }

    private void startChat(final int adapterPosition) {
        final Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(RECEIVER_NAME, contacts.get(adapterPosition).getName());
        intent.putExtra(RECEIVER_NUMBER, contacts.get(adapterPosition).getNumber());
        intent.putExtra(RECEIVER_UID, contacts.get(adapterPosition).getUid());
        if (dpUri.containsKey(contacts.get(adapterPosition).getNumber())) {
            intent.putExtra(RECEIVER_DP_URI, Objects.requireNonNull(dpUri.get(contacts.get(adapterPosition).getNumber())).toString());
        } else {
            intent.putExtra(RECEIVER_DP_URI, "");
        }
        context.startActivity(intent);
    }
}
