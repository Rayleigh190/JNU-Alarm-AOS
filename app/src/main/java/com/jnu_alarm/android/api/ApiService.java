package com.jnu_alarm.android.api;

import com.jnu_alarm.android.data.SubscriptionData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/alarm/notification/")
    Call<ApiResponse> postData(@Body SubscriptionData requestBody);
}
