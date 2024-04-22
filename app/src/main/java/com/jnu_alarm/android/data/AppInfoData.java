package com.jnu_alarm.android.data;

import com.google.gson.annotations.SerializedName;

public class AppInfoData {
    @SerializedName("ios_latest_version")
    private String iosLatestVersion;

    @SerializedName("aos_latest_version")
    private String aosLatestVersion;

    public String getIosLatestVersion() {
        return iosLatestVersion;
    }

    public String getAosLatestVersion() {
        return aosLatestVersion;
    }
}