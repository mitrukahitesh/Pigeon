package com.hitesh.whatsapp.model;

import android.net.Uri;

public class AvailableChats {
    public String uid, chatId, number;
    public Uri dpUri = null;

    public AvailableChats(String uid, String chatId) {
        this.uid = uid;
        this.chatId = chatId;
    }
}
