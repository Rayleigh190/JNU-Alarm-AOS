package com.jnu_alarm.android.api.response;
import com.google.gson.annotations.SerializedName;
import com.jnu_alarm.android.data.NotificationData;

import java.util.ArrayList;

public class ContactApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}