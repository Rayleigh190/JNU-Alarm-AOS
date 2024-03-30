package com.jnu_alarm.android.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jnu_alarm.android.R;
import com.jnu_alarm.android.api.ApiClient;
import com.jnu_alarm.android.api.ApiResponse;
import com.jnu_alarm.android.api.ApiService;
import com.jnu_alarm.android.data.NotificationData;
import com.jnu_alarm.android.data.SubscriptionData;
import com.jnu_alarm.android.databinding.FragmentNotificationsBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    private FragmentNotificationsBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ApiService apiService;
    private ArrayList<NotificationData> notificationDataList = new ArrayList<NotificationData>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        swipeRefreshLayout = root.findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /* swipe 시 진행할 동작 */
                fetchNotifications();
                /* 업데이트가 끝났음을 알림 */
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // [리사이클러뷰 설정 START]
        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = root.findViewById(R.id.recycler_view) ;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        NotificationsListAdapter adapter = new NotificationsListAdapter(notificationDataList) ;
        recyclerView.setAdapter(adapter) ;
        // [리사이클러뷰 설정 END]

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        fetchNotifications();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void fetchNotifications() {
        Log.d(TAG, "알림내역 가져오기 시작");
        apiService = ApiClient.getClient().create(ApiService.class);

        // 예시 데이터 생성
        String deviceId = "001";
        List<String> subscribedTopics = getListFromSharedPreferences(getContext());
        SubscriptionData subscriptionData = new SubscriptionData(deviceId, subscribedTopics);

        // POST 요청 보내기
        Call<ApiResponse> call = apiService.postData(subscriptionData);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null && apiResponse.isSuccess()) {
                        List<NotificationData> notifications = apiResponse.getNotifications();
                        if (notifications != null && !notifications.isEmpty()) {
                            Log.d(TAG, "알림내역: " + notifications.size());
                            // 리사이클러뷰에 응답 데이터 적용
                            notificationDataList = (ArrayList<NotificationData>) notifications;
                            NotificationsListAdapter adapter = new NotificationsListAdapter(notificationDataList);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.d(TAG, "알림이 없습니다.");
                        }
                    } else {
                        Log.e(TAG, "응답 처리 실패");
                    }
                } else {
                    Log.e(TAG, "POST 요청 실패");
                    // 요청 실패 처리
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "네트워크 오류: " + t.getMessage());
                // 네트워크 오류 등 요청 실패 시 처리
            }
        });
    }

    // SharedPreferences에서 리스트 데이터를 불러오는 메서드
    private ArrayList<String> getListFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("data", null);

        Type type = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }
}