package com.jnu_alarm.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.UUID;

public class AgreeActivity extends AppCompatActivity {
    // [START declare_analytics]
    private FirebaseAnalytics mFirebaseAnalytics;
    // [END declare_analytics]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agree);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // [START shared_app_measurement]
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (BuildConfig.DEBUG) {
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(false);
        } else {
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
        }
        // [END shared_app_measurement]

        // 다크모드 금지
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // 이용약관 동의 했는지 확인
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("agree", Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("terms_of_service", false)) {
            // 동의 했으면 메인 화면으로 이동
            Intent intent = new Intent(AgreeActivity.this, MainActivity.class);

            intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            // 안 했으면 이용약관 동의 화면 보여주기
            CardView termCard = findViewById(R.id.term_card);
            termCard.setVisibility(View.VISIBLE);
        }

        getSupportActionBar().hide();

        TextView serviceButton = findViewById(R.id.service_text);
        serviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("https://wackitlab.notion.site/81bc7df4763144beaec4610b39811529");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        TextView privacyButton = findViewById(R.id.privacy_text);
        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("https://wackitlab.notion.site/d6483585330d47cf8c3927c018d9075e");
                intent.setData(uri);
                startActivity(intent);
            }
        });

        Button agreeButton = findViewById(R.id.agree_button);
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("agree", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("terms_of_service", true).apply();

                initUUID();

                Intent intent = new Intent(AgreeActivity.this, MainActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        });

        Button disagreeButton = findViewById(R.id.disagree_button);
        disagreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AgreeActivity.this, MainActivity.class);
                finish();
            }
        });

        // 푸시 알림 클릭하면 바로 웹뷰로 열리도록 합니다.
        if (getIntent().getStringExtra("link") != null) {
            String title = getIntent().getStringExtra("title");
            String link = getIntent().getStringExtra("link");

            Intent intent = new Intent(getApplicationContext(), WebActivity.class);
            intent.putExtra("link", link);
            intent.putExtra("title", title);
            startActivity(intent);
        }
    }

    // UUID 초기화
    private void initUUID() {
        SharedPreferences mPref = getSharedPreferences("agree", MODE_PRIVATE);
        String uuid = mPref.getString("uuid", null);
        if(uuid == null){
            uuid = UUID.randomUUID().toString();
            mPref.edit().putString("uuid", uuid).apply();
        }
    }
}