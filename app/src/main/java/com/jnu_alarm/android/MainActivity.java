package com.jnu_alarm.android;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.common.reflect.TypeToken;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.jnu_alarm.android.api.ApiClient;
import com.jnu_alarm.android.api.ApiResponse;
import com.jnu_alarm.android.api.ApiService;
import com.jnu_alarm.android.data.NotificationData;
import com.jnu_alarm.android.data.SubscriptionData;
import com.jnu_alarm.android.databinding.ActivityMainBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ApiService apiService;
    private ArrayList<String> subscribedList = new ArrayList<>();
    private Boolean onResumeFlag = false;
    @Override
    protected void onResume() {
        super.onResume();
        if (onResumeFlag) {
            // 백그라운드에서 포그라운드로 왔을때만 실행합니다.
            Log.d(TAG, "onResume()");
            fetchNotifications();
            onResumeFlag = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 알림 권한 요청 Start
        askNotificationPermission();
        // 알림 권한 요청 End

        // 구독한 토픽(key) 목록을 가져옵니다.
        subscribedList = getListFromSharedPreferences(getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_notifications, R.id.navigation_settings
        ).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 세부 화면에서는 BottomNavigationView를 숨긴다.
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.d(TAG, String.valueOf(destination.getLabel()));
                if (destination.getId() == R.id.navigation_settings || destination.getId() == R.id.navigation_notifications) {
                    // 바텀 네비게이션이 표시되는 Fragment
                    binding.navView.setVisibility(View.VISIBLE);
                }
                else {
                    // 바텀 네비게이션이 표시되지 않는 Fragment
                    binding.navView.setVisibility(View.GONE);
                }

                if (destination.getId() == R.id.navigation_notifications && onResumeFlag == false) {
                    // 첫 실행과 navigation 이동 시에만 실행합니다.
                    Log.d(TAG, "navigation_notifications");
                    fetchNotifications();
                }

            }
        });

        // onSharedPreferenceChanged를 사용하기 위한 기본 설정
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // FCM 등록
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//
//                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
//                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, "FCM 등록 완료", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        onResumeFlag = true;
    }

    public void fetchNotifications() {
        apiService = ApiClient.getClient().create(ApiService.class);

        // 예시 데이터 생성
        String deviceId = "001";
        List<String> subscribedTopics = getListFromSharedPreferences(getApplicationContext());
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
                            // 첫 번째 알림 정보 가져오기
                            NotificationData firstNotification = notifications.get(0);
                            String title = firstNotification.getTitle();
                            String body = firstNotification.getBody();
                            String link = firstNotification.getLink();
                            String createdAt = firstNotification.getCreatedAt();

                            // 출력하거나 처리하기
                            Log.d("MainActivity", "알림 제목: " + title);
                            Log.d("MainActivity", "알림 내용: " + body);
                            Log.d("MainActivity", "알림 링크: " + link);
                            Log.d("MainActivity", "알림 생성일: " + createdAt);
                        } else {
                            Log.d("MainActivity", "알림이 없습니다.");
                        }
                    } else {
                        Log.e("MainActivity", "응답 처리 실패");
                    }
                } else {
                    Log.e("MainActivity", "POST 요청 실패");
                    // 요청 실패 처리
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("MainActivity", "네트워크 오류: " + t.getMessage());
                // 네트워크 오류 등 요청 실패 시 처리
            }
        });
    }

    // navigate up 버튼으로 뒤로가기 설정
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    // 설정 값이 변했을 때 실행
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (key=="notifications") {return;}
        Log.v(TAG, key);
        if (key != null && sharedPreferences.getBoolean(key, false)) {
            FirebaseMessaging.getInstance().subscribeToTopic(key)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Subscribed";
                            if (!task.isSuccessful()) {
                                msg = "Subscribe failed";
                                sharedPreferences.edit().putBoolean(key, false).apply();
                            } else {
                                // 구독한 key를 ArrayList와 SharedPreferences에 저장 합니다.
                                subscribedList.add(key);
                                saveListToSharedPreferences(getApplicationContext(), subscribedList);
                            }
                            Log.d(TAG, subscribedList.toString());
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            // 여기에서 새로고침하고싶어.
                        }
                    });
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(key)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Unsubscribed";
                            if (!task.isSuccessful()) {
                                msg = "Unsubscribe failed";
                                sharedPreferences.edit().putBoolean(key, true).apply();
                            } else {
                                // 구독 취소한 key를 ArrayList와 SharedPreferences에서 제거 합니다.
                                if (subscribedList.contains(key)) {
                                    subscribedList.remove(key);
                                }
                                // 변경된 리스트를 SharedPreferences에 저장합니다.
                                saveListToSharedPreferences(getApplicationContext(), subscribedList);
                            }
                            Log.d(TAG, subscribedList.toString());
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // SharedPreferences에 리스트 데이터를 저장하는 메서드
    private void saveListToSharedPreferences(Context context, List<String> myList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(myList);

        editor.putString("data", json);
        editor.apply();
    }

    // SharedPreferences에서 리스트 데이터를 불러오는 메서드
    private ArrayList<String> getListFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("data", null);

        Type type = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    // [START ask_post_notifications]
    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    // [END ask_post_notifications]
}