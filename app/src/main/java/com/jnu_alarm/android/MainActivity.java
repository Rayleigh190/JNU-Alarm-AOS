package com.jnu_alarm.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.jnu_alarm.android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                // 바텀 네비게이션이 표시되는 Fragment
                if (destination.getId() == R.id.navigation_settings || destination.getId() == R.id.navigation_notifications) {
                    binding.navView.setVisibility(View.VISIBLE);
                }
                // 바텀 네비게이션이 표시되지 않는 Fragment
                else {
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
                        Toast.makeText(MainActivity.this, "FCM 등록 완료", Toast.LENGTH_SHORT).show();
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
                            }
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
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}