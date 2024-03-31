package com.jnu_alarm.android.api.response;
import com.google.gson.annotations.SerializedName;
import com.jnu_alarm.android.data.NotificationData;

import java.util.ArrayList;

public class NotificationApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("response")
    private ArrayList<NotificationData> notifications;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public ArrayList<NotificationData> getNotifications() {
        return notifications;
    }

    public String getError() {
        return error;
    }
}