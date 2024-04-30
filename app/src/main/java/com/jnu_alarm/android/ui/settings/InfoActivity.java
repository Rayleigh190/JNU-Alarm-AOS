package com.jnu_alarm.android.ui.settings;
import com.jnu_alarm.android.BuildConfig;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jnu_alarm.android.R;
import com.jnu_alarm.android.WebActivity;

public class InfoActivity extends AppCompatActivity {
    TextView appVersionText;
    LinearLayout upDateHistoryLayout;
    LinearLayout privacyPolicyLayout;
    LinearLayout homepageLayout;
    LinearLayout supportLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportActionBar().setTitle("정보");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appVersionText = findViewById(R.id.app_version_text);
        appVersionText.setText(BuildConfig.VERSION_NAME);

        upDateHistoryLayout = findViewById(R.id.update_history_layout);
        upDateHistoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("업데이트 내역", "https://wackitlab.notion.site/AOS-e884e53ccdba439f8d5fab146bb4b7f6");
            }
        });

        privacyPolicyLayout = findViewById(R.id.privacy_policy_layout);
        privacyPolicyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("개인정보 처리방침", "https://wackitlab.notion.site/d6483585330d47cf8c3927c018d9075e");
            }
        });

        homepageLayout = findViewById(R.id.homepage_layout);
        homepageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("공식페이지", "https://wackitlab.notion.site/469d2c23433c48cca6965c3573058397");
            }
        });

        supportLayout = findViewById(R.id.support_layout);
        supportLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("후원하기", "https://wackitlab.notion.site/1c5a516070804fa5a0dd30d43a486979");
            }
        });
    }

    public void openWebView(String title, String link) {
        Intent intent = new Intent(InfoActivity.this, WebActivity.class);
        intent.putExtra("link", link);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) { // ActionBar의 Home/Up 버튼 클릭 시
            finish(); // 뒤로가기 처리
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}