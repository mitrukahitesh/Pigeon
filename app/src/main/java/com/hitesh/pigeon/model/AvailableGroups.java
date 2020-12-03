package com.hitesh.pigeon.model;

import android.net.Uri;

public class AvailableGroups {
    public String groupId;
    public Uri dpUri = null;
    public String name;

    public AvailableGroups(String groupId) {
        this.groupId = groupId;
    }
}
