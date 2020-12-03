package com.hitesh.pigeon.model;

import android.net.Uri;

public class AvailableGroups {
    public String groupId;
    public Uri dpUri = null;
    public String name;
    public Boolean isAdmin;

    public AvailableGroups(String groupId, Boolean isAdmin) {
        this.groupId = groupId;
        this.isAdmin = isAdmin;
    }
}
