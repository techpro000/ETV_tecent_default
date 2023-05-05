package com.etv.view.layout.web;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.etv.config.AppInfo;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.TimerDealUtil;
import com.etv.view.layout.Generator;
import com.ys.etv.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 使用系统内核--加载网页
 */
public class ViewWebViewGenerate extends Generator {

    View view_web;
    private WebView webview;
    Context context;
    String webUrl;
    boolean showTimeRuduce = SharedPerManager.getWebShowReduce();  //显示倒计时

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    public ViewWebViewGenerate(Context context, int x, int y, int width, int height, String webUrl) {
        super(context, x, y, width, height);
        this.context = context;
        hookWebView();   //防止系统签名之后webView无法使用的问题
        this.webUrl = webUrl;
        view_web = LayoutInflater.from(context).inflate(R.layout.view_web, null);
        initView("初始化View");
        initListener();
        TimerDealUtil.getInstance().addGeneratorToList(this);
    }

    RelativeLayout rela_bgg_stream;
    TextView tv_desc, tv_time_reduce;
    Button btn_back;
    Button btn_refresh;

    private void initListener() {
        btn_back = (Button) view_web.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(clickListener);
        btn_refresh = (Button) view_web.findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(clickListener);
        boolean isShowButton = SharedPerManager.getWebShowButton();
        if (!isShowButton) {
            btn_back.setVisibility(View.GONE);
            btn_refresh.setVisibility(View.GONE);
        }
    }

    RelativeLayout mFrameLayout;

    private void initView(String printTag) {
        if (view_web == null) {
            MyLog.task("刷新网页==initView=view_web == null=" + printTag);
            return;
        }
        MyLog.task("刷新网页==initView==" + printTag);
        if (rela_bgg_stream == null) {
            rela_bgg_stream = (RelativeLayout) view_web.findViewById(R.id.rela_bgg_stream);
        }
        rela_bgg_stream.setVisibility(View.GONE);
        if (tv_desc == null) {
            tv_desc = (TextView) view_web.findViewById(R.id.tv_desc);
        }
        if (mFrameLayout == null) {
            mFrameLayout = (RelativeLayout) view_web.findViewById(R.id.mFrameLayout);
        }
        if (tv_time_reduce == null) {
            tv_time_reduce = (TextView) view_web.findViewById(R.id.tv_time_reduce);
        }
        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) {
            rela_bgg_stream.setVisibility(View.VISIBLE);
            tv_desc.setText("非网络模式,不加载网页");
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(context)) {
            rela_bgg_stream.setVisibility(View.VISIBLE);
            tv_desc.setText("网络异常,请检查");
            return;
        }
        if (webview == null) {
            webview = (WebView) view_web.findViewById(R.id.webview);
        }
        WebSettings mysettings = webview.getSettings();
        mysettings.setTextZoom(100);  //设置字体得显示比例
        mysettings.setLoadWithOverviewMode(true);
        mysettings.setJavaScriptCanOpenWindowsAutomatically(true);
        mysettings.setAllowFileAccess(true);
        mysettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mysettings.setUseWideViewPort(true);
        mysettings.setBlockNetworkImage(false);//解决图片不显示
        mysettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        //支持windows=============================================
        MyLog.cdl("==========setSupportMultipleWindows======");
//        mysettings.setSupportMultipleWindows(true);  //会拦截 点击跳转
//        mysettings.setUserAgentString("Windows");
        //支持windows=============================================
        mysettings.setAppCacheEnabled(true);
        mysettings.setDomStorageEnabled(true);
        mysettings.setGeolocationEnabled(true);
        mysettings.setAppCacheMaxSize(Long.MAX_VALUE);
        mysettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        mysettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mysettings.setSupportZoom(true);
        mysettings.setBuiltInZoomControls(true);
        mysettings.setJavaScriptEnabled(true);

        if (SharedPerManager.getWebCache()) {
            mysettings.setDatabaseEnabled(true);
            mysettings.setAppCacheEnabled(true);
            mysettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });
        webview.setWebChromeClient(new MyWebChromeClient());
        if (!webUrl.startsWith("http")) {
            webUrl = "http://" + webUrl;
        }
        //背景透明
        webview.setBackgroundColor(Color.parseColor("#00000000"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webview.loadUrl(webUrl);
    }

    private class MyWebChromeClient extends WebChromeClient {
        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            MyLog.cdl("=========刷新网页===这里加载全屏逻辑============");
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            mFrameLayout.addView(mCustomView);
            mCustomViewCallback = callback;
            webview.setVisibility(View.GONE);
//          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        public void onHideCustomView() {
            MyLog.cdl("======刷新网页======这里隐藏全屏逻辑============");
            webview.setVisibility(View.VISIBLE);
            if (mCustomView == null) {
                return;
            }
            mCustomView.setVisibility(View.GONE);
            mFrameLayout.removeView(mCustomView);
            mCustomViewCallback.onCustomViewHidden();
            mCustomView = null;
//          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            super.onHideCustomView();
        }
    }


    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_refresh: //刷新按钮
                    refreshWebView();
                    break;
                case R.id.btn_back:
                    if (webview != null) {
                        webview.goBack();
                    }
                    break;
            }
        }
    };

    private void refreshWebView() {
        MyLog.playTask("===刷新网页======网页开始刷新=====");
        if (webview != null) {
            webview.onResume();
            //恢复pauseTimers状态
            webview.resumeTimers();
            webview.reload();
        }
    }

    @Override
    public View getView() {
        return view_web;
    }

    @Override
    public void clearMemory() {
        TimerDealUtil.getInstance().removeGeneratorToList(this);
        try {
            if (webview != null) {
                webview.getSettings().setBuiltInZoomControls(true);
                webview.stopLoading();
                webview.setVisibility(View.GONE);
                webview = null;
            }
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    @Override
    public void removeCacheView(String tag) {
        clearMemory();
        if (webview != null) {
            webview.destroy();
        }
    }

    long webRefreshTime = 5;  //分钟

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        if (!isShowBtn) {
            if (btn_back != null) {
                btn_back.setVisibility(View.GONE);
            }
            if (btn_refresh != null) {
                btn_refresh.setVisibility(View.GONE);
            }
        }
        TextInfo textInfo = (TextInfo) object;
        if (textInfo == null) {
            return;
        }
        String refreshTime = textInfo.getTaFontSize();
        MyLog.d("webView", "===action===fintSize=====" + refreshTime);
        if (refreshTime == null || refreshTime.length() < 1) {
            return;
        }
        try {
            webRefreshTime = Long.parseLong(refreshTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (webRefreshTime < 1) {
            webRefreshTime = 600;
        }
    }

    @Override
    public void updateTextInfo(Object object) {
        MyLog.d("webView", "===action===更新action=====");
        initView("updateTextInfo");
    }

    long addTime = 0;

    @Override
    public void timeChangeToUpdateView() {
        long freshTimeNext = webRefreshTime * 60;
        addTime++;
        MyLog.d("webView", "===timeChangeToUpdateView======" + addTime);
        if (addTime > freshTimeNext) {
            addTime = 0;
            updateTextInfo(null);
        }
        if (tv_time_reduce != null && showTimeRuduce) {
            tv_time_reduce.setText(freshTimeNext - addTime + " S");
        }
    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

    }

    @Override
    public void playComplet() {

    }

    public static void hookWebView() {
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                MyLog.cdl("sProviderInstance isn't null");
                return;
            }
            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
                MyLog.cdl("Don't need to Hook WebView");
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> providerConstructor = providerClass.getConstructor(delegateClass);
            if (providerConstructor != null) {
                providerConstructor.setAccessible(true);
                Constructor<?> declaredConstructor = delegateClass.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                sProviderInstance = providerConstructor.newInstance(declaredConstructor.newInstance());
                field.set("sProviderInstance", sProviderInstance);
            }
            MyLog.cdl("Hook done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moveViewForward(boolean b) {

    }

    @Override
    public void pauseDisplayView() {

    }

    @Override
    public void resumePlayView() {

    }
}
