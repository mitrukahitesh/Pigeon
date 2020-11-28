package com.hitesh.whatssappclone.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hitesh.whatssappclone.fragments.ChatsFragment;
import com.hitesh.whatssappclone.fragments.ContactsFragment;
import com.hitesh.whatssappclone.fragments.GroupsFragment;

public class TabsAccessorAdapter extends FragmentStateAdapter {

    public TabsAccessorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChatsFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new ContactsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
