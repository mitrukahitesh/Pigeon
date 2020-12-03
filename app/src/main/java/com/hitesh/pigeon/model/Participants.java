package com.hitesh.pigeon.model;

public class Participants {
    private String uid;
    private Boolean isAdmin;

    public Participants(String uid, Boolean isAdmin) {
        this.uid = uid;
        this.isAdmin = isAdmin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
