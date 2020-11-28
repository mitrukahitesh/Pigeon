package com.hitesh.whatssappclone;

public class Contacts {

    private String name, number, uid;

    public Contacts(String name, String number, String uid) {
        this.name = name;
        this.number = number;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
