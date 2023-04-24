package com.etv.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.EtvService;
import com.etv.service.TcpService;
import com.etv.service.TcpSocketService;
import com.etv.setting.InterestActivity;
import com.etv.util.CodeUtil;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.ViewSizeChange;
import com.etv.util.image.ImageUtil;
import com.ys.etv.BuildConfig;
import com.ys.etv.R;

/***
 * 程序启动界面
 * 里面涉及到自动登录相关功能
 */
public class SplashLowActivity extends SplashBaseActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_splash_low);
        initView();
    }

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        ImageView iv_logo_show = (ImageView) findViewById(R.id.iv_logo_show);
        ViewSizeChange.setLogoPosition(iv_logo_show);
        int showBggImage = ImageUtil.getShowBggLogo();
        iv_logo_show.setBackgroundResource(showBggImage);
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            startService(new Intent(SplashLowActivity.this, TcpService.class));
        } else {
            startService(new Intent(SplashLowActivity.this, TcpSocketService.class));
        }
        lineToWeb();
    }

    private static final int CHECK_LOGIN_STATUS = 908;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case CHECK_LOGIN_STATUS:
                    startToMainView();
                    break;
            }
        }
    };

    private void lineToWeb() {
        if (!NetWorkUtils.isNetworkConnected(SplashLowActivity.this)) {
            MyLog.login("没有网络，打断操作");
            return;
        }
        if (AppConfig.isOnline) {
            MyLog.login("登陆socket success 停止操作");
            return;
        }
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            TcpService.getInstance().lineSockeyWebServer();
        } else {
            TcpSocketService.getInstance().lineSockeyWebServer("SplashLowActivity 进行服务器连接");
        }
    }


    /***
     * 开始进入界面
     */
    private void startToMainView() {
        boolean isSlppeStatues = SharedPerManager.getSleepStatues();
        Intent intent = new Intent();
        if (isSlppeStatues) {
            MyLog.sleep("======当前是休眠状态，关屏操作", true);
            intent.setClass(this, InterestActivity.class);
        } else {
            MyLog.sleep("======当前不是休眠状态，开屏操作", true);
            intent.setClass(this, InitViewActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FileUtil.deleteDirOrFilePath(AppInfo.BASE_CACHE(), "===清理缓存目录==SplashLow");
        String clVersion = CodeUtil.getSystCodeVersion(SplashLowActivity.this);
        EtvService.getInstance().updateDevInfoToAuthorServer(clVersion);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            MyLog.cdl("========未知权限===" + b);
            if (!b) {
                Uri packageURI = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                startActivityForResult(intent, 1);
            } else {
                handler.removeMessages(CHECK_LOGIN_STATUS);
                handler.sendEmptyMessageDelayed(CHECK_LOGIN_STATUS, 6000);
            }
        } else {
            handler.removeMessages(CHECK_LOGIN_STATUS);
            handler.sendEmptyMessageDelayed(CHECK_LOGIN_STATUS, 6000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeMessages(CHECK_LOGIN_STATUS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
