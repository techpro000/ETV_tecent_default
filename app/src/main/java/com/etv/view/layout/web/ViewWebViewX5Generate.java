package com.etv.view.layout.web;

import static com.etv.config.AppConfig.APP_TYPE_TD_SERVICE_CENTER;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.EtvApplication;
import com.etv.config.AppConfig;
import com.etv.task.entity.MediAddEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TextInfo;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.TimerDealUtil;
import com.etv.util.system.CpuModel;
import com.etv.view.layout.Generator;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.ys.etv.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 使用腾讯内核--加载网页
 */
public class ViewWebViewX5Generate extends Generator {

    View view_web;
    TextView tv_time_reduce;
    WebView mWb;
    private Button btn_back, btn_refresh;
    private Handler handler = new Handler();
    String strPath;
    Context contextView;
    ProgressBar pb_load;
    long webRefreshTime = 5;  //分钟
    boolean showTimeRuduce = SharedPerManager.getWebShowReduce();  //显示倒计时

    @Override
    public int getVideoPlayCurrentDuartion() {
        return 0;
    }

    public ViewWebViewX5Generate(Context context, int x, int y, int width, int height, String strPath) {
        super(context, x, y, width, height);
        hookWebView();   //防止系统签名之后webView无法使用的问题
        this.strPath = strPath;
        this.contextView = context;
        view_web = LayoutInflater.from(context).inflate(R.layout.view_web_x5, null);
        initBtnListenser();
        TimerDealUtil.getInstance().addGeneratorToList(this);
    }

    @Override
    public void updateView(Object object, boolean isShowBtn) {
        //这里表示 H5节目
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
        MyLog.d("webView", "===action===fintSize=====" + webRefreshTime);
        if (webRefreshTime < 1) {
            webRefreshTime = 600;
        }
    }

    @Override
    public void updateTextInfo(Object object) {
        MyLog.d("webView", "===action===更新action=====");
        initWebInfo();
    }

    long addTime = 0;

    @Override
    public void timeChangeToUpdateView() {
        long freshTimeNext = webRefreshTime * 60;
        addTime++;
        MyLog.cdl("=====timeChangeToUpdateView======" + addTime + " / " + freshTimeNext);
        if (addTime > freshTimeNext) {
            addTime = 0;
            updateTextInfo(null);
        }
        if (tv_time_reduce != null && showTimeRuduce) {
            tv_time_reduce.setText(freshTimeNext - addTime + " S");
        }
    }

    RelativeLayout mFrameLayout;
    RelativeLayout rela_bgg_stream;

    private void initWebInfo() {
        if (rela_bgg_stream == null) {
            rela_bgg_stream = (RelativeLayout) view_web.findViewById(R.id.rela_bgg_stream);
        }
        if (!NetWorkUtils.isNetworkConnected(contextView)) {
            rela_bgg_stream.setVisibility(View.VISIBLE);
            return;
        }
        rela_bgg_stream.setVisibility(View.GONE);
        if (!strPath.startsWith("http")) {
            strPath = "http://" + strPath;
        }
        if (mWb == null) {
            mWb = (WebView) view_web.findViewById(R.id.wb);
        }
        if (tv_time_reduce == null) {
            tv_time_reduce = (TextView) view_web.findViewById(R.id.tv_time_reduce);
        }
        if (mFrameLayout == null) {
            mFrameLayout = (RelativeLayout) view_web.findViewById(R.id.mFrameLayout);
        }
        WebSettings settings = mWb.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(true); //隐藏原生的缩放控件
        settings.setBlockNetworkImage(false);//解决图片不显示
        settings.setSupportMultipleWindows(false);//这里一定得是false,不然打开的网页中，不能在点击打开了
        settings.setLoadsImagesAutomatically(true); //支持自动加载图片
//        settings.setUserAgentString("Windows");
        settings.setDefaultTextEncodingName("utf-8");//设置编码格式
        settings.setDomStorageEnabled(true);   //加载不出资源问题
//        settings.setAppCacheEnabled(true);
        settings.setPluginsEnabled(true);      //支持插件
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);     //设置可以访问文件
        settings.setLoadsImagesAutomatically(true);    //支持自动加载图片

        if (SharedPerManager.getWebCache()) {
            settings.setDatabaseEnabled(true);
            settings.setAppCacheEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }
        boolean isInitStatues = EtvApplication.getInstance().isInitWebX5Statues;
        MyLog.playTask("========浏览器初始化状态==isInitStatues==" + isInitStatues);
        if (!isInitStatues && isNeedUseX5Web()) {
            strPath = "http://debugtbs.qq.com/";
        }
        mWb.loadUrl(strPath);
        //该界面打开更多链接
        mWb.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (url == null) return false;
                try {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        //使用外部浏览器打开
                        return true;
                    }
                } catch (Exception e) {//防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                webView.loadUrl(url);
                return true;
            }
        });
        mWb.setWebChromeClient(new MyWebChromeClient());
    }

    public boolean isNeedUseX5Web() {
        String cpuModel = CpuModel.getMobileType();
        if (cpuModel.startsWith(CpuModel.CPU_MODEL_RK_3288)) {
            return true;
        }
        if (cpuModel.startsWith(CpuModel.CPU_MODEL_RK_3128)) {
            return true;
        }
        return false;
    }


    private class MyWebChromeClient extends WebChromeClient {
        private View mCustomView;
        private IX5WebChromeClient.CustomViewCallback mCustomViewCallback;

        @Override
        public void onProgressChanged(WebView webView, int i) {
            Log.e("webProgress", "===webProgress====" + i);
            if (i == 100) {
                pb_load.setVisibility(View.GONE);
            } else {
                pb_load.setVisibility(View.VISIBLE);
            }
            pb_load.setProgress(i);
            changGoForwardButton(webView);
        }

        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            MyLog.cdl("=========999999===这里加载全屏逻辑============");
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            mCustomView = view;
            mFrameLayout.addView(mCustomView);
            mCustomViewCallback = callback;
            mWb.setVisibility(View.GONE);
//          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        public void onHideCustomView() {
            MyLog.cdl("======999999======这里隐藏全屏逻辑============");
            mWb.setVisibility(View.VISIBLE);
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


    private void initBtnListenser() {
        pb_load = (ProgressBar) view_web.findViewById(R.id.pb_load);
        btn_back = (Button) view_web.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MyLog.d("keyCode", "========点击了回退=====");
                if (mWb != null && mWb.canGoBack())
                    mWb.goBack();
            }
        });

        btn_refresh = (Button) view_web.findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWebView();
            }
        });
//        if (mWebView != null && mWebView.canGoForward())
//            mWebView.goForward();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initWebInfo();
            }
        }, 1500);

        boolean isShowButton = SharedPerManager.getWebShowButton();
        if (!isShowButton) {
            btn_back.setVisibility(View.GONE);
            btn_refresh.setVisibility(View.GONE);
        }
    }


    private void refreshWebView() {
        MyLog.playTask("=========网页开始刷新=====");
        if (mWb != null && mWb.canGoBack()) {
            mWb.onResume();
            //恢复pauseTimers状态
            mWb.resumeTimers();
            mWb.reload();
        }
    }

    private void changGoForwardButton(WebView view) {
        if (view.canGoBack()) {
            btn_back.setClickable(true);
            btn_refresh.setClickable(true);
        } else {
            btn_back.setClickable(false);
            btn_refresh.setClickable(false);
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
            if (mWb != null) {
                mWb.getSettings().setBuiltInZoomControls(true);
                mWb.stopLoading();
                mWb.setVisibility(View.GONE);
                mWb.destroy();
                mWb = null;
            }
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    @Override
    public void removeCacheView(String tag) {
        MyLog.cdl("=========网页执行==removeCacheView==");
        clearMemory();
        if (mWb != null) {
//            mWb.clearCache(true);
//            mWb.clearFormData();
//            mWb.clearHistory();
//            mWb.clearMatches();
//            mWb.clearSslPreferences();
            mWb.destroy();
        }
    }

    @Override
    public void playComplet() {

    }

    @Override
    public void playPositionScenProgram(int position, List<MediAddEntity> mediAddEntities, SceneEntity currentScentity) {

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
