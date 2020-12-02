package com.hitesh.pigeon.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hitesh.pigeon.R;
import com.hitesh.pigeon.activities.GroupCreationActivity;
import com.hitesh.pigeon.activities.MainActivity;
import com.hitesh.pigeon.model.Contacts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupCreationAdapter extends RecyclerView.Adapter<GroupCreationAdapter.CustomVH> {

    private final Context context;
    private final List<Contacts> contacts;
    private final ArrayList<String> selected = new ArrayList<>();
    private final HashMap<String, Uri> dpUri = new HashMap<>();

    public GroupCreationAdapter(Context context, List<Contacts> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_selection_for_group_view, parent, false);
        return new CustomVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.name.setText(contacts.get(position).getName());
        String number = contacts.get(position).getNumber();
        holder.number.setText(number);
        if (dpUri.containsKey(number)) {
            Glide.with(context).load(dpUri.get(number)).into(holder.dp);
        } else {
            Glide.with(context).load(R.drawable.img).into(holder.dp);
            getDpUri(position);
        }
        holder.checkBox.setChecked(selected.contains(contacts.get(position).getUid()));
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

    public ArrayList<String> getSelectedContacts() {
        return selected;
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        CircleImageView dp;
        TextView name, number;
        CheckBox checkBox;
        LinearLayout ll;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            checkBox = itemView.findViewById(R.id.checkBox);
            ll = itemView.findViewById(R.id.ll);
            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.performClick();
                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkBox.isChecked()) {
                        selected.add(contacts.get(getAdapterPosition()).getUid());
                    } else {
                        selected.remove(contacts.get(getAdapterPosition()).getUid());
                    }
                    GroupCreationActivity.actionBar.setTitle(selected.size() + " selected");
                }
            });
        }
    }
}
