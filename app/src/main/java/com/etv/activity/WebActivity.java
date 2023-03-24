package com.etv.activity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.ys.etv.R;

public class WebActivity extends AppCompatActivity {

    WebView mWb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWb = findViewById(R.id.wb);
        mWb.getSettings().setJavaScriptEnabled(true);
        mWb.getSettings().setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        mWb.getSettings().setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        mWb.getSettings().setDisplayZoomControls(true); //隐藏原生的缩放控件
        mWb.getSettings().setBlockNetworkImage(false);//解决图片不显示
        mWb.getSettings().setLoadsImagesAutomatically(true); //支持自动加载图片
        mWb.getSettings().setDefaultTextEncodingName("utf-8");//设置编码格式
        mWb.getSettings().setDomStorageEnabled(true);   //加载不出资源问题
//        mWb.getSettings().setPluginsEnabled(true);//支持插件
        mWb.getSettings().setAllowFileAccess(true);//设置可以访问文件
        mWb.getSettings().setLoadsImagesAutomatically(true);//支持自动加载图片
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWb.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        String url = "https://ahcz.cntimo.com/manager/screen/index.html?UnitID=1";
        mWb.loadUrl(url);
        Log.d("WebActivity", "监控界面加载的url为: " + url);

        //该界面打开更多链接
        mWb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (url == null) return false;
                try {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        //使用外部浏览器打开
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {//防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }

                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                webView.loadUrl(url);
                return true;
            }

//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
////                super.onReceivedSslError(view, handler, error);
//            }
        });
        //监听网页的加载进度
        mWb.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView webView, int i) {
                /*if (i < 100 && MainTaskFragment.this.isVisible()) {
                    tvTaskProgress.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                } else {
                    if (MainTaskFragment.this.isVisible()) {
                        tvTaskProgress.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                    }
                }*/
            }
        });

    }
}
