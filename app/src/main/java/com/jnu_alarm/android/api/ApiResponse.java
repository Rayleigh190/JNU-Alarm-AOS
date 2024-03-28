package com.jnu_alarm.android.api;
import com.google.gson.annotations.SerializedName;
import com.jnu_alarm.android.data.NotificationData;

import java.util.List;

public class ApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("response")
    private List<NotificationData> notifications;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public List<NotificationData> getNotifications() {
        return notifications;
    }

    public String getError() {
        return error;
    }
}

