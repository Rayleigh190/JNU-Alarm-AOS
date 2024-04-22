package com.jnu_alarm.android.api;

import com.jnu_alarm.android.api.response.AppInfoApiResponse;
import com.jnu_alarm.android.api.response.ContactApiResponse;
import com.jnu_alarm.android.api.response.NotificationApiResponse;
import com.jnu_alarm.android.data.AppInfoData;
import com.jnu_alarm.android.data.ContactData;
import com.jnu_alarm.android.data.SubscriptionData;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/alarm/notification/")
    Call<NotificationApiResponse> postNotification(@Body SubscriptionData requestBody);

    @POST("/api/alarm/question/")
    Call<ContactApiResponse> postContact(@Body ContactData requestBody);

    @GET("/api/alarm/app-info/")
    Call<AppInfoApiResponse> getAppInfo();
}
