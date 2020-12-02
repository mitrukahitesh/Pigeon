package com.hitesh.pigeon.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hitesh.pigeon.fragments.ChatsFragment;
import com.hitesh.pigeon.fragments.ContactsFragment;
import com.hitesh.pigeon.fragments.GroupsFragment;

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
            default:
                return new ContactsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
