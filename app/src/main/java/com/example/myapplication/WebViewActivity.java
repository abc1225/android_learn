package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {



    View mLadingView;
    CustomActionWebView mCustomActionWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mLadingView = findViewById(R.id.loadingView);
        mCustomActionWebView = (CustomActionWebView)findViewById(R.id.customActionWebView);


        List<String> list = new ArrayList<>();
        list.add("加红色");
//        list.add("Item2");
//        list.add("APIWeb");

        mCustomActionWebView.setWebViewClient(new CustomWebViewClient());

        //设置item
        mCustomActionWebView.setActionList(list);

        //链接js注入接口，使能选中返回数据
        mCustomActionWebView.linkJSInterface();

        mCustomActionWebView.getSettings().setBuiltInZoomControls(true);
        mCustomActionWebView.getSettings().setDisplayZoomControls(false);
        //使用javascript
        mCustomActionWebView.getSettings().setJavaScriptEnabled(true);
        mCustomActionWebView.getSettings().setDomStorageEnabled(true);


        //增加点击回调
        mCustomActionWebView.setActionSelectListener(new CustomActionWebView.ActionSelectListener() {
            @Override
            public void onClick(String title, String selectText, String seq) {
                if(title.equals("APIWeb")) {
                    Intent intent = new Intent(WebViewActivity.this, APIWebViewActivity.class);
                    startActivity(intent);
                    return;
                }
                Toast.makeText(WebViewActivity.this, "Click Item: " + title + "。\n\nValue: " + selectText+ "。\n\nSeq: " + seq, Toast.LENGTH_LONG).show();
            }
        });

        //加载url
        mCustomActionWebView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCustomActionWebView.loadUrl("https://www.baidu.com/");
            }
        }, 200);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mCustomActionWebView != null) {
            mCustomActionWebView.dismissAction();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private class CustomWebViewClient extends WebViewClient {

        private boolean mLastLoadFailed = false;

        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            if (!mLastLoadFailed) {
                CustomActionWebView customActionWebView = (CustomActionWebView) webView;
                customActionWebView.linkJSInterface();
                mLadingView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//            super.onReceivedError(view, request, error);
            mLastLoadFailed = true;
            mLadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(null != mCustomActionWebView && mCustomActionWebView.canGoBack()){
            mCustomActionWebView.goBack();
        }
    }
}
