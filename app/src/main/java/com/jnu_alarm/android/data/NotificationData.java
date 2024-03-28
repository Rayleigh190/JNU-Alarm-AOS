package com.jnu_alarm.android.data;

import com.google.gson.annotations.SerializedName;

public class NotificationData {
    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("link")
    private String link;

    @SerializedName("created_at")
    private String createdAt;

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getLink() {
        return link;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
