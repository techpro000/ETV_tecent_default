package com.etv.util.guardian;

import static com.youth.banner.util.LogUtils.TAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.EtvApplication;
import com.etv.config.ApiInfo;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.RawSourceEntity;
import com.etv.http.util.FileRawWriteRunnable;
import com.etv.listener.WriteSdListener;
import com.etv.service.EtvService;
import com.etv.util.APKUtil;
import com.etv.util.CodeUtil;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.ys.etv.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 安装守护进程方法
 * InstallGuardianUtil
 */
public class GuardianUtil {

    Context context;

    public GuardianUtil(Context context) {
        this.context = context;
    }

    /***
     * 设置所有的触发IO操作
     * @param context
     * @param enable
     * 是否打开GPIO操作
     * @param ioNum
     * IO选择
     *0  IO-1
     *1  IO-2
     *2  IO-3
     *3  IO-4
     * @param isBack
     * 是否开启消息反向
     * @param speed
     * 提交返回的速度
     *0   慢
     *1   中
     *2   快
     *  @param openPermission
     *  是否开启权限检查
     */
    public static void setIoTriggleAllInfo(Context context, boolean enable, int ioNum, boolean isBack, int speed, boolean openPermission) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_PERSON_INFO);
        intent.putExtra(AppInfo.SET_TRIGGLE_SPEED, speed);
        intent.putExtra(AppInfo.SET_LISTENER_MESSAGE_BACK, isBack);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_IO_CHOOICE, ioNum);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_OPEN, enable);
        intent.putExtra(AppInfo.SET_LISTENER_IO_PERMISSION, openPermission);
        context.sendBroadcast(intent);
    }

    /***
     *0   慢
     *1   中
     *2   快
     ** 设置IO触发响应速度
     * @param context
     * @param speed
     */
    public static void setIoTriggleSpeed(Context context, int speed) {
        Intent intent = new Intent(AppInfo.SET_TRIGGLE_SPEED);
        intent.putExtra(AppInfo.SET_TRIGGLE_SPEED, speed);
        context.sendBroadcast(intent);
    }

    /***
     * 设置触发通知反向
     * @param context
     * @param isBack
     */
    public static void modifyIoMessageBack(Context context, boolean isBack) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_MESSAGE_BACK);
        intent.putExtra(AppInfo.SET_LISTENER_MESSAGE_BACK, isBack);
        context.sendBroadcast(intent);
    }

    /***
     * 修改触发得IO
     * @param context
     * @param ioNum
     *0  IO-1
     *1  IO-2
     *2  IO-3
     *3  IO-4
     */
    public static void modifyIoChooiceNum(Context context, int ioNum) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_PERSON_IO_CHOOICE);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_IO_CHOOICE, ioNum);
        context.sendBroadcast(intent);
    }

    /***
     * 修改人体感应开关
     * @param context
     * @param enable
     */
    public static void modifyIoCheckStatues(Context context, boolean enable) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_PERSON_OPEN);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_OPEN, enable);
        context.sendBroadcast(intent);
    }

    /**
     * 修改守护进程得状态
     *
     * @param context
     * @param enable
     */
    public static void modifyGuardianStatues(Context context, boolean enable) {
        Intent intent = new Intent(AppInfo.CHANGE_GUARDIAN_STATUES);
        intent.putExtra(AppInfo.CHANGE_GUARDIAN_STATUES, enable);
        context.sendBroadcast(intent);
    }

    /***
     * 修改守护进程的包名
     */
    public void setGuardianPackageName() {
        String packageName = SharedPerManager.getPackageNameBySp();
        MyLog.guardian("======packageName===" + packageName);
        Intent intent = new Intent(AppInfo.MODIFY_GUARDIAN_PACKAGENAME);
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
    }

    /***
     * 设置守护进程得时间
     * @param context
     * @param daemonProcessTime
     * 单位  秒
     */
    public static void setGuardianProjectTime(Context context, String daemonProcessTime) {
        if (context == null) {
            return;
        }
        if (daemonProcessTime == null || daemonProcessTime.contains("null")) {
            return;
        }
        if (daemonProcessTime.length() < 1) {
            return;
        }
        try {
            int guardianTime = Integer.parseInt(daemonProcessTime);
            if (guardianTime < 15) {
                return;
            }
            Intent intent = new Intent(AppInfo.MODIFY_GUARDIAN_TIME);
            intent.putExtra(AppInfo.MODIFY_GUARDIAN_TIME, guardianTime * 1000);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getFisrt() {
        boolean aa = (boolean) EtvApplication.getInstance().getData("firstChoice", true);
        return aa;
    }

    public static void setFirst(boolean firstChoice) {
        EtvApplication.getInstance().saveData("firstChoice", firstChoice);
    }

    /**
     * 修改守护进程的状态
     * true 打开
     * false 关闭
     *
     * @param b
     */
    public static void setGuardianStaues(Context context, boolean b) {
        boolean aa = getFisrt();
        if (aa) {
            /// 执行关闭守护
            Intent intent = new Intent(AppInfo.CHANGE_GUARDIAN_STATUES);
            intent.putExtra(AppInfo.CHANGE_GUARDIAN_STATUES, false);
            context.sendBroadcast(intent);
            setFirst(false);
            return;
        }
        Intent intent = new Intent(AppInfo.CHANGE_GUARDIAN_STATUES);
        intent.putExtra(AppInfo.CHANGE_GUARDIAN_STATUES, b);
        context.sendBroadcast(intent);

    }

    /***
     * 修改开机启动开关
     * @param context
     * @param isOpen
     */
    public static void setGuardianPowerOnStart(Context context, boolean isOpen) {
        Intent intent = new Intent(AppInfo.MODIFY_GUARDIAN_POWER_START_ETV);
        intent.putExtra(AppInfo.MODIFY_GUARDIAN_POWER_START_ETV, isOpen);
        context.sendBroadcast(intent);
    }


    /***
     * 通知守护进程，自己是那个公司的
     * @param context
     */
    public static void sendBroadToGuardianConpany(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(AppInfo.CHANGE_ORDER_COMPANY);
            intent.putExtra(AppInfo.CHANGE_ORDER_COMPANY, AppConfig.APP_TYPE);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startInstallGuardian() {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            MyLog.guardian("MTK 不安装守护进程");
            return;
        }
        boolean isApkInstall = APKUtil.ApkState(context, AppInfo.GUARDIAN_PACKAGE_NAME);
        MyLog.guardian("守护进程是否安装: " + isApkInstall);
        RawSourceEntity rawSourceEntity = getResourceGuardianEntity();
        MyLog.guardian("===rawSourceEntity==" + rawSourceEntity);
        if (rawSourceEntity == null) {
            return;
        }
        if (isApkInstall) {  //已经安装就去检查版本号
            checkGuardianAppVersion(rawSourceEntity);
        } else {   //没有安装，直接安装
            installGUardianApp(rawSourceEntity);
            Log.e(TAG, "startInstallGuardian: " + "需要安装");
        }
    }

    /***
     * 检查版本号，需不需要升级
     */
    private void checkGuardianAppVersion(RawSourceEntity rawSourceEntity) {
        try {
            MyLog.guardian("==程序已经安装===");
            String guardianPackName = AppInfo.GUARDIAN_PACKAGE_NAME;
            int guardianCode = APKUtil.getOtherAppVersion(context, guardianPackName);
            int updateCode = rawSourceEntity.getApkVersion();
            if (updateCode > guardianCode) {
                installGUardianApp(rawSourceEntity);
            }
            MyLog.guardian("==程序已经安装00===" + guardianCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * anzhuang
     */
    private void installGUardianApp(RawSourceEntity rawSourceEntity) {
        MyLog.guardian("==获取的raw守护信息===" + rawSourceEntity.toString());
        int rourseId = rawSourceEntity.getRawId();
        long fileLength = rawSourceEntity.getFileLength();
        String savePath = AppInfo.BASE_APK() + "/" + AppInfo.GUARDIAN_APP_NAME;
        FileRawWriteRunnable runnable = new FileRawWriteRunnable(context, rourseId, savePath, fileLength, new WriteSdListener() {
            @Override
            public void writeProgress(int progress) {
                MyLog.guardian("===progress===" + progress);
            }

            @Override
            public void writeSuccess(String savePath) {
                File file = new File(savePath);
                MyLog.guardian("守护进程写入成功suucess==" + savePath + " / " + (file.exists()));
                new Thread(() -> {
                    RootCmd.writeFileToSystemApp(savePath, "/system/app/guardian.apk");
                }).start();
            }

            @Override
            public void writrFailed(String errorDesc) {
                MyLog.guardian("writrFailed==" + errorDesc);
            }
        });
        runnable.setIdDelOldFile(true);
        EtvService.getInstance().executor(runnable);
    }

    private RawSourceEntity getResourceGuardianEntity() {
        String cpuModel = CpuModel.getMobileType();
        if (cpuModel.contains(CpuModel.CPU_MODEL_T982)) {
            //T-982 守护进程是内置到固件，所以要升级固件才能使用
//                rawSourceEntity = new RawSourceEntity(R.raw.guardian_982, 3211268, "android-982", 79);
//                return rawSourceEntity;
            return null;
        }
        RawSourceEntity rawSourceEntity = new RawSourceEntity(R.raw.guardian_71, 3395008, "7.0通用版本", 77);
        try {
            MyLog.guardian("=====获取守护进程Raw id==" + cpuModel);
            if (cpuModel.contains(CpuModel.CPU_MODEL_MLOGIC)) {
                //PX30主板
                rawSourceEntity = new RawSourceEntity(R.raw.guardian_mlogic91, 3365824, "mlogic9.0", 47);
                return rawSourceEntity;
            }

            if (cpuModel.contains(CpuModel.CPU_MODEL_3568_11)) {
                //rk-3568 android 11
                rawSourceEntity = new RawSourceEntity(R.raw.guardian_3568, 3394812, "3568-android-3568", 76);
                return rawSourceEntity;
            }
            if (cpuModel.contains(CpuModel.CPU_MODEL_PX30)) {
                //PX-30 8.0系统
                rawSourceEntity = new RawSourceEntity(R.raw.guardian_81, 3389505, "8.1通用版本", 48);
                return rawSourceEntity;
            }
            if (cpuModel.contains(CpuModel.CPU_MODEL_RK_DEFAULT)) {
                int sdkCode = Build.VERSION.SDK_INT;
                MyLog.guardian("==当前SDK 得版本===" + sdkCode);
                if (sdkCode > Build.VERSION_CODES.Q) {
                    //RK-11.0
                    rawSourceEntity = new RawSourceEntity(R.raw.guardian_11_3588, 3394988, "3588-android-11", 78);
                    return rawSourceEntity;
                }
                if ((sdkCode > Build.VERSION_CODES.P || sdkCode == Build.VERSION_CODES.P) && sdkCode < Build.VERSION_CODES.Q) {
                    //RK_9.0	28	Pie (Android P)
                    rawSourceEntity = new RawSourceEntity(R.raw.guardian_91, 3397722, "9.0", 74);
                    return rawSourceEntity;
                }
                if (sdkCode > Build.VERSION_CODES.O) {
                    // 8.0
                    rawSourceEntity = new RawSourceEntity(R.raw.guardian_81, 3389505, "8.1通用版本", 48);
                    return rawSourceEntity;
                }
                if (sdkCode > Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    // 7.0系统需要系统签名
                    rawSourceEntity = new RawSourceEntity(R.raw.guardian_71, 3395008, "7.0通用版本", 77);
                    return rawSourceEntity;
                }
                if (sdkCode > Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // 5<Code<6.0  //需要系统签名
                    rawSourceEntity = new RawSourceEntity(R.raw.guardian_51, 1624725, "5.1通用版本", 43);
                    return rawSourceEntity;
                }
                if (sdkCode > Build.VERSION_CODES.JELLY_BEAN && sdkCode < Build.VERSION_CODES.LOLLIPOP) {
                    //4.0~5.0
                    rawSourceEntity = new RawSourceEntity(R.raw.guardian_44, 3359565, "4.4通用版本", 55);
                    return rawSourceEntity;
                }
            }
        } catch (Exception e) {
            MyLog.guardian("=====获取守护进程Raw id error==" + e.toString());
            e.printStackTrace();
        }
        return rawSourceEntity;
    }

    /***
     * 启动守护进程
     * @param contextOnly
     */
    public static void startGuardianService(Context contextOnly) {
        try {
            boolean isApkInstall = APKUtil.ApkState(contextOnly, AppInfo.GUARDIAN_PACKAGE_NAME);
            if (!isApkInstall) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction("com.guardian.service.GuardianService");  //应用在清淡文件中注册的action
            intent.setPackage(AppInfo.GUARDIAN_PACKAGE_NAME); //应用程序的包名
            contextOnly.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 截图代码
     */
    public static void getCaptureImage(Context context, int screenWidth, int screenHeight, String tag) {

        try {
            String code = CodeUtil.getUniquePsuedoID();
            String requestUrl = ApiInfo.MONITOR_IMAGE_UPDATE() + "?clientNo=" + code;
            Log.e(TAG, "getCaptureImage: " + requestUrl);
            Intent intent = new Intent();
            intent.putExtra("screenWidth", screenWidth);
            intent.putExtra("screenHeight", screenHeight);
            intent.putExtra("tag", tag);
            intent.putExtra("requestUrl", requestUrl);
            intent.setAction(AppInfo.CAPTURE_IMAGE_RECEIVE);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotoGuardianApp(Context context) {
        String packageName = "com.guardian";
        boolean isInstall = APKUtil.ApkState(context, packageName);
        if (!isInstall) {
            return;
        }
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName("com.guardian", "com.guardian.ui.MainActivity");
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(cmp);
        context.startActivity(intent);
    }

}
