package com.jnu_alarm.android.api.response;

import com.google.gson.annotations.SerializedName;
import com.jnu_alarm.android.data.AppInfoData;

public class AppInfoApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("response")
    private AppInfoData responseData;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public AppInfoData getResponseData() {
        return responseData;
    }

    public String getError() {
        return error;
    }


}
