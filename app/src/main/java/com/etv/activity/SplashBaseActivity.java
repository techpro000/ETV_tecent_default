package com.etv.activity;


import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.setting.GuardianBaseActivity;
import com.etv.task.util.BubbleUtil;
import com.etv.util.APKUtil;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.ProjectorUtil;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.guardian.GuardianUtil;
import com.etv.util.system.CpuModel;
import com.etv.util.system.LeaderBarUtil;
import com.etv.util.system.SystemManagerInstance;
import com.etv.util.system.VoiceManager;
import com.ys.model.config.DialogConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplashBaseActivity extends GuardianBaseActivity {

    @Override
    public void updateGuardianView() {
//        //守护进程打开状态
//        //这里主要是用户用户手动点击的APK，判断缓存的状态
//        boolean isOpenStatues = getGuardianOpenStatues();
//        SharedPerManager.setGuardianStatues(isOpenStatues);
    }


    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        initApp();
        closeBluth();
    }

    /**
     * 解决设备WIFI信号差的问题
     */
    private void closeBluth() {
//        if (SharedPerManager.getBluetooth()) {
//            MyLog.cdl("======打开蓝牙===========");
//            return;
//        }
//        MyLog.cdl("======关闭蓝牙===========");
//        try {
//            BluetoothAdapter mBluetooth = BluetoothAdapter.getDefaultAdapter();
//            if (mBluetooth != null && mBluetooth.isEnabled()) {
//                mBluetooth.disable();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void initApp() {
        delCrashLogFile();
        installGuardianApp();
        setProjectorSavePath();
        String PackageName = APKUtil.getPackageName(SplashBaseActivity.this);
        SharedPerManager.setPackageName(PackageName, "程序启动，保存包名");
        setM11MediaPlayerVoiceNum();
    }

    private void setM11MediaPlayerVoiceNum() {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            int lastVoiceNUm = SharedPerManager.getLastMediaVoiceNum();
            MyLog.cdl("===check358M11CurrentMediaVoiceNum=设置音量==" + lastVoiceNUm);
            VoiceManager.getInstance(SplashBaseActivity.this).setMediaVoiceNum(lastVoiceNUm);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getScreenSize();
        //隐藏虚拟按键，并且全屏
        LeaderBarUtil.hiddleLeaderBar(SplashBaseActivity.this);
        //清理U盘升级缓存得APK文件，防止缓存沾满得问题
        RootCmd.clearApkCache();
        SystemManagerInstance.getInstance(SplashBaseActivity.this).switchAutoTime(true, "ETV 默认打开自动同步系统时间");
    }

    /***
     * 删除多余的日志
     */
    private int DAT_LOG_SAVE = 20;

    private void delCrashLogFile() {
//        系统时间不对的话，不往下执行
        long currentTime = SimpleDateUtil.formatBig(System.currentTimeMillis());
        if (currentTime < AppConfig.TIME_CHECK_POWER_REDUCE) {
            MyLog.cdl("===============crashlog=======系统时间不对，不检测运行日志====");
            return;
        }
        try {
            String url = AppInfo.BASE_CRASH_LOG();
            File fileDir = new File(url);
            if (!fileDir.exists()) {
                MyLog.cdl("===============crashlog=======文件不存在====");
                return;
            }
            File[] fileList = fileDir.listFiles();
            if (fileList == null || fileList.length < 2) {
                return;
            }
            List<File> fileListArray = new ArrayList<File>();
            for (File f : fileList) {
                fileListArray.add(f);
            }
            // 按照时间先后顺序，进行排序
            fileListArray = BubbleUtil.sortByFileName(fileListArray, true);
            int delNum = 0;
            if (fileListArray.size() > DAT_LOG_SAVE) {
                delNum = fileListArray.size() - DAT_LOG_SAVE;
            }
            if (delNum < 1) {
                MyLog.cdl("===============crashlog=======没有需要删除的日志");
                return;
            }
            for (int i = 0; i < delNum; i++) {
                File file = fileListArray.get(i);
                String filePath = file.getPath();
                MyLog.cdl("===============crashlog=======需要删除的文件路径====" + filePath);
                FileUtil.deleteDirOrFilePath(filePath, "===delCrashLogFile==");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 设置节目的保存地址
     */
    private void setProjectorSavePath() {
        ProjectorUtil.setProjectorSavePath(SplashBaseActivity.this, "SplashBaseActivity--程序启动，保存节目路径");
    }

    //安装系统保活进程
    public void installGuardianApp() {
        GuardianUtil installUtil = new GuardianUtil(SplashBaseActivity.this);
        installUtil.startInstallGuardian();
        boolean isOpenPower = SharedPerManager.getOpenPower();
        MyLog.guardian("开机设置开机启动==" + isOpenPower);
        GuardianUtil.setGuardianPowerOnStart(SplashBaseActivity.this, isOpenPower);
        //修改守护进程的包名
        boolean isGuardian = SharedPerManager.getGuardianStatues();
        GuardianUtil.setGuardianStaues(SplashBaseActivity.this, isGuardian);

        installUtil.setGuardianPackageName();
        switch (AppConfig.APP_TYPE) {
            case AppConfig.APP_TYPE_KING_LAM:
                GuardianUtil.setGuardianProjectTime(SplashBaseActivity.this, "360");
                break;
            case AppConfig.APP_TYPE_JIANGJUN_YUNCHENG:
                GuardianUtil.setGuardianStaues(SplashBaseActivity.this, true);
                GuardianUtil.setGuardianProjectTime(SplashBaseActivity.this, "31");
                break;
        }
    }

    private void getScreenSize() {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(localDisplayMetrics);
        int width = localDisplayMetrics.widthPixels;
        int height = localDisplayMetrics.heightPixels;
        MyLog.cdl("=======屏幕得尺寸==" + width + " / " + height, true);
        int densityDpi = localDisplayMetrics.densityDpi;
        SharedPerManager.setDensityDpi(densityDpi);
        SharedPerManager.setScreenWidth(width);
        SharedPerManager.setScreenHeight(height);
        DialogConfig.screenWidth = width;
        DialogConfig.screenHeight = height;
    }
}

