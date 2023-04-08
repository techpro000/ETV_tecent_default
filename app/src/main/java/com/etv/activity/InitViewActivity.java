package com.etv.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.etv.activity.pansener.InitPansener;
import com.etv.activity.view.InitView;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.udp.util.KeyControl;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.ViewSizeChange;
import com.etv.util.image.ImageUtil;
import com.etv.util.permission.PermissionUtil;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerInstance;
import com.ys.etv.R;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.OridinryDialogClick;
import com.ys.model.util.ActivityCollector;
import com.ys.model.util.KeyBoardUtil;
import com.ys.rkapi.MyManager;

/**
 * 用来初始化APP
 * 同步时间
 * 判断定时开关机操作
 */
public class InitViewActivity extends BaseActivity implements InitView {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_splash_low);
        initHdmiInService();
        updateTopBottomViewStatues();
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            KeyControl.sendCodeToDev(InitViewActivity.this, 7004);
            KeyControl.sendCodeToDev(InitViewActivity.this, 7005);
        }
    }

    /***
     * 初始化Hdmi_In得功能
     */
    private void initHdmiInService() {
        boolean isSuportHdmi = SharedPerManager.getIfHdmiInSuport();
        if (!isSuportHdmi) {
            return;
        }
        String packageName = AppInfo.HDMI_IN_PACKAGE_NAME;
        boolean isInstall = APKUtil.ApkState(InitViewActivity.this, packageName);
        if (!isInstall) {
            MyLog.hdmi("Hdmi in软件未安装", true);
            return;
        }
        try {
            Intent intent = new Intent();
            ComponentName cmp = new ComponentName("teaonly.rk.droidipcam", "teaonly.rk.droidipcam.CameraHdmiActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            startActivity(intent);
        } catch (Exception e) {
            MyLog.hdmi("Hdmi in软件打开异常:" + e.toString(), true);
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MLOGIC)) {
            MyLog.cdl("======设置HDMI-IN 的属性参数===");
            RootCmd.setProperty("sys.meida.omx.vr", "1");
            RootCmd.setProperty("media.omx.display_mode", "0");
        }
    }

    /**
     * 为什么要再onresume里面初始化
     * 因为需要检测一个悬浮窗权限，检查完毕，直接进入界面
     */
    @Override
    protected void onResume() {
        super.onResume();
        SystemManagerInstance.getInstance(InitViewActivity.this);
        boolean isPermission = PermissionUtil.checkFloatPermission(InitViewActivity.this);
        MyLog.cdl("==检查悬浮窗权限==" + isPermission, true);
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_RK_3288) && !isPermission) {
            //除了 3288 需要检查 悬浮窗权限，其他得不用检查
            checkPermissionPopWinds();
        } else {
            //设置第一次登录为false
            SharedPerManager.setFirstComeIn(false);
            initView();
        }
    }

    /**
     * 检查悬浮窗权限
     */
    private void checkPermissionPopWinds() {
        OridinryDialog oridinryDialog = new OridinryDialog(InitViewActivity.this);
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                PermissionUtil.openWinPopToast(InitViewActivity.this);
            }

            @Override
            public void noSure() {
                ActivityCollector.finishAll();
                finish();
            }
        });
        String content = getString(R.string.pop_window_content);
        oridinryDialog.show(content, getString(R.string.ok), getString(R.string.cancel));
    }

    /**
     * 防止APK升级。
     * 程序误判下载完成而造成
     * 这里得删除一下
     */
    private void delApkPath() {
//        //删除APK文件
//        String apkPath = "/sdcard/etv/apk";
//        boolean isDelApk = FileUtil.deleteDirOrFilePath(apkPath, "下载失败得问题==delApkPath");
//        MyLog.e("delete", "========删除APK目录==" + isDelApk);
    }

    InitPansener initPansener;

    private void initView() {
        AppInfo.startCheckTaskTag = false;
        ImageView iv_logo_show = (ImageView) findViewById(R.id.iv_logo_show);
        ViewSizeChange.setLogoPosition(iv_logo_show);
        int showBggImage = ImageUtil.getShowBggLogo();
        iv_logo_show.setBackgroundResource(showBggImage);
        TextView tv_show_deac = (TextView) findViewById(R.id.tv_show_deac);
        tv_show_deac.setText(getString(R.string.check_timer));
        initPansener = new InitPansener(InitViewActivity.this, this);
        initPansener.checkMainPowerOnOff();   //检查定时开关机
        initPansener.queryNickName();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        initPansener.onDestroy();
    }

    @Override
    public void jumpActivity() {
        delApkPath();
        Intent intent = new Intent();
        MainActivity.IS_ORDER_REQUEST_TASK = true;
        intent.setClass(InitViewActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 关闭滑出下拉菜单
     */
    private void updateTopBottomViewStatues() {
        //如果不是默认版本，不操作此指令
    }


}
