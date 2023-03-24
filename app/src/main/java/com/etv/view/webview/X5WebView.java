package com.etv.view.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class X5WebView extends WebView {

    private WebViewClient client = new WebViewClient() {
        //防止加载网页时调起系统浏览器
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        //防止加载Hppts加载不出来的问题
//        @Override
//        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//            handler.proceed();
//        }
    };

    public X5WebView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public X5WebView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.setWebViewClient(client);
        initWebViewSettings();
        this.getView().setClickable(true);
    }


    private void initWebViewSettings() {
        //设置默认显示区域
        this.getSettings().setTextZoom(100);  //设置字体得显示比例
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setLoadWithOverviewMode(true);
        //缩放倍数比例====================================
//        this.setInitialScale(100);  //为25%，最小缩放等级
//        this.setScaleX(SharedPerUtil.getScreenWidth());
//        this.setScaleY(SharedPerUtil.getScreenHeight());
        //基本设置-======================================================
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        // 1、LayoutAlgorithm.NARROW_COLUMNS,适应内容大小
        // LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);            //支持屏幕缩放
        webSetting.setBuiltInZoomControls(true);  //放大缩小按钮
        webSetting.setUseWideViewPort(true);
//      webSetting.setSupportMultipleWindows(true);  //会拦截跳转标签
//      webSetting.setUserAgentString("Windows");
        webSetting.setAppCacheEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);  //不使用缓存，只从网络获取数据.
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean ret = super.drawChild(canvas, child, drawingTime);
        return ret;
    }

    public X5WebView(Context arg0) {
        super(arg0);
        setBackgroundColor(85621);
    }

}
