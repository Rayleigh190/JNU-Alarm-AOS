package com.jnu_alarm.android;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
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
import com.jnu_alarm.android.api.ApiService;
import com.jnu_alarm.android.api.response.AppInfoApiResponse;
import com.jnu_alarm.android.databinding.ActivityMainBinding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    private AdView bottomAdView;
    private static final String PREF_KEY_LAST_UPDATE_DIALOG_SHOWN = "last_update_dialog_shown";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 네트워크가 연결되어 있지 않은 경우
        if (!NetworkManager.checkNetworkState(this)) {
            // AlertDialog를 통해 사용자에게 네트워크 연결 상태를 알립니다.
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("네트워크 연결 확인")
                    .setMessage("네트워크에 연결되어 있지 않습니다. 앱을 종료합니다.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // '확인' 버튼을 클릭하면 앱을 종료합니다.
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // 알림 권한 요청 Start
        askNotificationPermission();
        // 알림 권한 요청 End

        // 알림 채널 생성 Start
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("100", "모든 알림", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            notificationChannel.enableVibration(true);
            notificationChannel.enableLights(true);
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
        // 알림 채널 생성 End

        // 배터리 최적화 권한이 허용되어 있는지 확인
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager.isIgnoringBatteryOptimizations(getPackageName()) == false) {
                Log.d(TAG, "배터리 최적화 제외 허용 안 됨");
                requestBatteryOptimizationPermission();
            } else {
                Log.d(TAG, "배터리 최적화 허용 됨");
            }
        }

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
//                        Toast.makeText(MainActivity.this, "FCM 등록 완료", Toast.LENGTH_SHORT).show();
                    }
                });

        setAdmob();
    }

    private void requestBatteryOptimizationPermission() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("배터리 설정")
                .setMessage("정상적인 알림 수신을 위해 해당 어플을 \"배터리 사용량 최적화\" 목록에서 \"제외\"해야 합니다.\n\n[확인] 버튼을 누른 후 시스템 알림 대화 상자가 뜨면 [허용]을 선택해 주세요.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // '확인' 버튼을 클릭하면 앱을 종료합니다.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        latestVersionCheck();
    }

    public Void setAdmob() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        bottomAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bottomAdView.loadAd(adRequest);
        bottomAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.d(TAG, "배너광고가 클릭 되었습니다.");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.d(TAG, "배너광고가 닫혔습니다.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.d(TAG, "배내광고 로드가 실패 되었습니다.");
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
                Log.d(TAG, "배너광고가 보여졌습니다.");
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "배너광고가 로드 되었습니다.");
                ImageView backgroundBannerImage = findViewById(R.id.backgroud_banner);
                backgroundBannerImage.setVisibility(View.GONE);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d(TAG, "배너광고가 오픈 되었습니다.");
            }
        });
        return null;
    }

    // navigate up 버튼으로 뒤로가기 설정
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    // 설정 값이 변했을 때 실행
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        // 네트워크가 연결되어 있지 않은 경우
        if (!NetworkManager.checkNetworkState(this)) {
            // AlertDialog를 통해 사용자에게 네트워크 연결 상태를 알립니다.
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle("네트워크 연결 확인")
                    .setMessage("네트워크에 연결되어 있지 않습니다. 연결 후 다시 시도해 주세요.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().putBoolean(
                                    key,
                                    !sharedPreferences.getBoolean(key, true)
                            ).apply();
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        if(key == PREF_KEY_LAST_UPDATE_DIALOG_SHOWN) {return;} // 무시하고 넘어가야 하는 key들 입니다.
        if (sharedPreferences==getSharedPreferences("subscribed_topics", Context.MODE_PRIVATE)) {
            Log.v(TAG, "변경된 설정: " + sharedPreferences);
            Log.v(TAG, "선택한 키: " + key);
            return;
        }
        Log.v(TAG, "변경된 설정: " + sharedPreferences);
        Log.v(TAG, "선택한 키: " + key);
        if (key != null && sharedPreferences.getBoolean(key, false)) {
            subscribeFCMTopic(sharedPreferences, key);
        } else {
            unsubscribeFCMTopic(sharedPreferences, key);
        }
    }

    // SharedPreferences에 리스트 데이터를 저장하는 메서드
    private void saveListToSharedPreferences(Context context, List<String> myList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("subscribed_topics", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(myList);

        editor.putString("data", json);
        editor.apply();
    }

    // SharedPreferences에서 리스트 데이터를 불러오는 메서드
    private ArrayList<String> getListFromSharedPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("subscribed_topics", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("data", null);

        Type type = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();

        ArrayList<String> list = gson.fromJson(json, type);
        if (list == null) {
            list = new ArrayList<>(); // 기본값으로 빈 ArrayList를 생성
        }
        return list;
    }

    private void subscribeFCMTopic(SharedPreferences sharedPreferences, String key) {

        FirebaseMessaging.getInstance().subscribeToTopic(key)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "구독 완료";
                        if (!task.isSuccessful()) {
                            msg = "구독 실패";
                            sharedPreferences.edit().putBoolean(key, false).apply();
                        } else {
                            // 구독한 key를 SharedPreferences에 저장 합니다.
                            ArrayList subscribedList = getListFromSharedPreferences(getApplicationContext());
                            subscribedList.add(key);
                            saveListToSharedPreferences(getApplicationContext(), subscribedList);
                        }
                        Log.d(TAG, "전체 설정" + getListFromSharedPreferences(getApplicationContext()).toString());
                        Log.d(TAG, msg);
                        if (!key.equals("basic") && !key.equals("aos")) {
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void unsubscribeFCMTopic(SharedPreferences sharedPreferences, String key) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(key)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "구독 취소";
                        if (!task.isSuccessful()) {
                            msg = "구독 취소 실패";
                            sharedPreferences.edit().putBoolean(key, true).apply();
                        } else {
                            // 구독 취소한 key를 SharedPreferences에서 제거 합니다.
                            ArrayList subscribedList = getListFromSharedPreferences(getApplicationContext());
                            if (subscribedList.contains(key)) {
                                subscribedList.remove(key);
                            }
                            saveListToSharedPreferences(getApplicationContext(), subscribedList);
                        }
                        Log.d(TAG, "전체 설정" + getListFromSharedPreferences(getApplicationContext()).toString());
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //리스너 해지
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void latestVersionCheck() {
        fetchAppInfo();
    }

    private void fetchAppInfo() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<AppInfoApiResponse> call = apiService.getAppInfo();

        call.enqueue(new Callback<AppInfoApiResponse>() {
            @Override
            public void onResponse(Call<AppInfoApiResponse> call, Response<AppInfoApiResponse> response) {
                if (response.isSuccessful()) {
                    AppInfoApiResponse appInfoResponse = response.body();
                    // 성공적인 응답 처리
                    // appInfoResponse.getResponseData()를 사용하여 앱 정보 데이터에 액세스합니다.
                    if (!appInfoResponse.getResponseData().getIsAvailable()) { return; }
                    String latestVersion = appInfoResponse.getResponseData().getAosLatestVersion();
                    String currentVersion = BuildConfig.VERSION_NAME;
                    String[] splitedLatestVersion = latestVersion.split("\\.");
                    String[] splitedCurrentVersion = currentVersion.split("\\.");
                    Log.d(TAG, Arrays.toString(splitedLatestVersion));
                    Log.d(TAG, Arrays.toString(splitedCurrentVersion));
                    if(Integer.parseInt(splitedLatestVersion[0]) > Integer.parseInt(splitedCurrentVersion[0])
                    || (Integer.parseInt(splitedLatestVersion[1]) > Integer.parseInt(splitedCurrentVersion[1]))) {
                        Log.d(TAG, "강제 업데이트 대상");
                        showForceUpdateDialog();
                    } else if (Integer.parseInt(splitedLatestVersion[2]) > Integer.parseInt(splitedCurrentVersion[2])) {
                        Log.d(TAG, "권장 업데이트 대상");
                        showRecommendUpdateDialog();
                    } else {
                        Log.d(TAG, "아무것도 아님 대상");
                    }

                } else {
                    // 실패한 응답 처리
                    // response.errorBody()를 사용하여 에러 메시지에 액세스합니다.
                    Log.d(TAG, "AppInfoApi 실패");
                }
            }

            @Override
            public void onFailure(Call<AppInfoApiResponse> call, Throwable t) {
                // 실패 처리
            }
        });
    }

    private void showForceUpdateDialog() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("필수 업데이트 알림");
        builder.setMessage("더 나은 서비스를 위해 새 버전이 나왔습니다! 업데이트를 해주세요.");
        builder.setCancelable(false);
        builder.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 업데이트 다이얼로그에서 업데이트 버튼을 클릭한 경우 Play Store로 이동하여 앱 업데이트를 시작합니다.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showRecommendUpdateDialog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastShownTime = sharedPreferences.getLong(PREF_KEY_LAST_UPDATE_DIALOG_SHOWN, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastShownTime);

        // 마지막으로 알림을 표시한 시간이 00:00 이후인지 확인
        if (!isSameDay(calendar, Calendar.getInstance())) {
            // 액티비티가 종료되었거나 종료 중인지 확인
            if (isFinishing() || isDestroyed()) {
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
            builder.setTitle("권장 업데이트 알림");
            builder.setMessage("안정적인 서비스 이용을 위해 업데이트를 권장합니다!");
            builder.setCancelable(false);
            builder.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 업데이트 다이얼로그에서 업데이트 버튼을 클릭한 경우 Play Store로 이동하여 앱 업데이트를 시작합니다.
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("나중에", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 나중에 버튼을 클릭한 경우 다이얼로그를 닫고 마지막으로 알림을 표시한 시간을 저장합니다.
                    sharedPreferences.edit().putLong(PREF_KEY_LAST_UPDATE_DIALOG_SHOWN, System.currentTimeMillis()).apply();
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // 두 Calendar 객체가 같은 날인지 확인하는 메서드
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

}