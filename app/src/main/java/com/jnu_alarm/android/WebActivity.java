package com.jnu_alarm.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.jnu_alarm.android.utils.ClipboardUtil;

import java.net.URISyntaxException;

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

        // WebView와 설정(webSettings)을 초기화한 후에
        // WebView에서 JavaScript 호출을 처리할 클래스 정의
        class WebViewJavaScriptInterface {
            @JavascriptInterface
            public void hideElement() {
                // JavaScript를 통해 버튼을 숨기는 메서드
                webView.post(() -> {
                    // 클래스 이름으로 요소를 찾아 숨깁니다
                    webView.evaluateJavascript(
                            "var elements = document.getElementsByClassName('btn-deco color2');" +
                                    "if (elements.length > 0) {" +
                                    "   elements[0].style.display = 'none';" + // 첫 번째 일치하는 요소 숨기기
                                    "}",
                            null);
                });
            }
        }

        // WebView에 JavaScript 인터페이스 추가
        webView.addJavascriptInterface(new WebViewJavaScriptInterface(), "Android");

        // WebViewClient를 설정하여 페이지 로딩 완료 이벤트를 감지
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // JavaScript 인터페이스 메서드를 호출하여 버튼 숨기기
                webView.loadUrl("javascript:window.Android.hideElement()");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                if (url.contains("kakao") || url.contains("market://") || url.contains("intent://") || url.contains("https://play.google.com/store/")) {
                    // kakao or market or intent가 포함된 주소는 새로운 Intent로 연결합니다.
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(WebActivity.this, "지원하지 않는 링크입니다.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    return true; // URL 로딩 중지
                }
                return false; // 계속 URL 로딩
            }
        });


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