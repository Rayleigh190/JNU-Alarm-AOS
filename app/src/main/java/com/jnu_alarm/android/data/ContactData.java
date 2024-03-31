package com.jnu_alarm.android.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContactData {
    @SerializedName("email")
    private String email;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    public ContactData(String email, String title, String content) {
        this.email = email;
        this.title = title;
        this.content = content;
    }
}
