package com.etv.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.ViewSizeChange;
import com.etv.util.image.ImageUtil;
import com.etv.util.rxjava.RxLifecycle;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.ys.etv.R;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.OridinryDialogClick;
import com.ys.rkapi.MyManager;

import java.util.Calendar;

import io.reactivex.functions.Consumer;


/***
 * 程序启动界面
 * 里面涉及到自动登录相关功能
 */
public class StartActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_splash_low);
        MyLog.cdl("===软件启动了===", true);
        AppConfig.isOnline = false;
        initView();
    }


    private void initView() {
        int currentTime = SimpleDateUtil.getHourMin();
        if (currentTime > 234 && currentTime < 330) {
            //进入splash界面自主息屏处理
        } else {
            SharedPerManager.setSleepStatues(false);
        }
        ImageView iv_logo_show = (ImageView) findViewById(R.id.iv_logo_show);
        ViewSizeChange.setLogoPosition(iv_logo_show);
        int showBggImage = ImageUtil.getShowBggLogo();
        iv_logo_show.setBackgroundResource(showBggImage);
        AppInfo.startCheckTaskTag = false;
        SharedPerManager.setExitDefault(false);
        //RK的主板才检测定时开关机
        checkCpuModelInfo();
    }

    private void checkCpuModelInfo() {
        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_START_ON_POWER) {
            checkAppIsSafe();
            return;
        }
        String powerOnOffPackageName = "com.adtv";  //定时开关机
        boolean isInstall = APKUtil.ApkState(StartActivity.this, powerOnOffPackageName);
        if (!isInstall) {
            showCpuErrorDialog();
            return;
        }
        checkAppIsSafe();
    }

    /***
     * 检查APP得合法性
     */
    private void checkAppIsSafe() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {  // <6.0
            MyLog.cdl("====检查动态权限");
            checkVideoPermission();
        } else {
            MyLog.cdl("====版本过低不用检查权限==权限请求通过");
            startGoToView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Android - 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    //REQUEST_INSTALL_PACKAGES  获取未知应用权限
    private String[] permissionsGroup = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public void checkVideoPermission() {
        RxPermissions rxPermissions = new RxPermissions(StartActivity.this);
        rxPermissions.request(permissionsGroup)
                .compose(RxLifecycle.bindRxLifecycle(this))
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isTrue) throws Exception {
                        MyLog.cdl("======权限请求通过==" + isTrue);
                        if (isTrue) {
                            startGoToView();
                        } else {
                            finish();
                        }
                    }
                });
    }

    private void startGoToView() {
        AppInfo.PERMISSION_COMPLAIY = true;
        startActivity(new Intent(StartActivity.this, SplashLowActivity.class));
        finish();
//        boolean islocationEnable = isLocServiceEnable(StartActivity.this);
//        MyLog.location("判断设备是否有定位权限==" + islocationEnable);
//        if (!islocationEnable) {
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivityForResult(intent, 3);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED && requestCode == 3) {
            if (isLocServiceEnable(StartActivity.this)) {
                MyLog.location("位置信息已打开，做接下来的操作");
            } else {
                MyLog.location("未开启GPS或定位服务，无法进入");
            }
        }
    }

    /**
     * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
     */
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        // 有一个为true，就代表定位服务已经打开
        if (gps || network) {
            return true;
        }
        return false;
    }


    /**
     * 提示用户
     */
    private void showCpuErrorDialog() {
        OridinryDialog oridinryDialog = new OridinryDialog(StartActivity.this);
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                finish();
            }

            @Override
            public void noSure() {
                finish();
            }
        });
        oridinryDialog.show(getString(R.string.broad_not_allow),
                getString(R.string.close), getString(R.string.close));
    }

}
