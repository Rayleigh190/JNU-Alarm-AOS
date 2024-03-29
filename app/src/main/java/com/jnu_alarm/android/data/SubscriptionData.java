package com.jnu_alarm.android.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubscriptionData {
    @SerializedName("device_id")
    private String deviceId;

    @SerializedName("subscribed_topics")
    private List<String> subscribedTopics;

    @SerializedName("is_iOS")
    private Boolean is_iOS;

    public SubscriptionData(String deviceId, List<String> subscribedTopics) {
        this.deviceId = deviceId;
        this.subscribedTopics = subscribedTopics;
        this.is_iOS = false;
    }
}
