package com.jnu_alarm.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jnu_alarm.android.utils.ClipboardUtil;

public class WebActivity extends AppCompatActivity {
    private static final String TAG = "WebActivity";
    private WebView webView;
    private WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_web);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String link = intent.getStringExtra("link");
        String title = intent.getStringExtra("title");

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = findViewById(R.id.web_view);
        webView.loadUrl(link);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
//        webSettings.setDisplayZoomControls(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // http 이미지 안 뜨는 문제 해결

        // 뒤로가기 동작을 처리하는 콜백 등록
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_top_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) { // ActionBar의 Home/Up 버튼 클릭 시
            finish(); // 뒤로가기 처리
            return true;
        } else if (itemId == R.id.url_copy_menu) {
            // 현재 웹뷰로 보고 있는 사이트의 주소를 클립보드에 복사
            String currentUrl = webView.getUrl();
            ClipboardUtil.copyToClipboard(this, "URL", currentUrl);
            // 사용자에게 알림 또는 토스트 메시지 등을 통해 복사되었음을 알릴 수도 있음
            Toast.makeText(this, "URL이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.url_share_menu) {
            // 현재 웹뷰로 보고 있는 사이트의 주소를 공유
            String currentUrl = webView.getUrl();
            String extraText = "전대알림에서 링크를 공유했어요!\n어떤 링크인지 들어가서 확인해볼까요?\n\n";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, extraText + currentUrl);
            startActivity(Intent.createChooser(shareIntent, "URL 공유하기"));
            return true;
        } else if (itemId == R.id.url_open_menu) {
            String currentUrl = webView.getUrl();
            Intent openBrowserIntent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(currentUrl);
            openBrowserIntent.setData(uri);
            startActivity(openBrowserIntent);
        } else if (itemId == R.id.reload_menu) {
            webView.reload();
        }
        return super.onOptionsItemSelected(item);
    }
}