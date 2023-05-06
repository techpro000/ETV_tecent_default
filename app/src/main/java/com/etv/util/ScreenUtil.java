package com.etv.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.EtvApplication;
import com.etv.config.AppInfo;
import com.etv.entity.ScreenEntity;
import com.etv.listener.BitmapCaptureListener;
import com.etv.service.EtvService;
import com.etv.util.guardian.GuardianUtil;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerInstance;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class ScreenUtil {

    private static Handler mHandler = new Handler();

    /**
     * 智能截图
     *
     * @param context
     * @param tag
     */
    public static void getScreenImage(Context context, String tag, BitmapCaptureListener listener) {
        int screenWidth = SharedPerUtil.getScreenWidth();
        int screenHeight = SharedPerUtil.getScreenHeight();
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11) && (screenWidth < screenHeight)) {
            //M11 竖屏，调用这里
            AppStatuesListener.getInstance().objectLiveDate.postValue(AppStatuesListener.LIVE_DATA_SCREEN_CATPTURE);
            return;
        }
        String cpuMudel = CpuModel.getMobileType();
        if (cpuMudel.startsWith(CpuModel.CPU_MODEL_RK_3128)) {
            //3128 截图
            deal312844ScreenInfo(context, tag);
            return;
        }
        if (cpuMudel.startsWith(CpuModel.CPU_RK_3566)) {
            //3566软件截图
            get356xCatptureImage(context, listener);
            return;
        }
        //旧版本使用守护进程截图
        GuardianUtil.getCaptureImage(context, screenWidth, screenHeight, tag);
    }

    private static void get356xCatptureImage(Context context, BitmapCaptureListener listener) {
        FileUtil.creatPathNotExcit("开始截图");
        MyLog.update("=截图=3566=开始截图==");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean isSuccess = SystemManagerInstance.getInstance(context).screenShot(AppInfo.CAPTURE_MAIN);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.backCaptureImage(isSuccess, AppInfo.CAPTURE_MAIN);
                    }
                });
            }
        };
        EtvService.getInstance().executor(runnable);
    }

    /***
     * 3128截图
     * @param context
     * @param tag
     */
    private static void deal312844ScreenInfo(Context context, String tag) {
        int cacheWidth = SharedPerUtil.getScreenWidth();
        int cacheHeight = SharedPerUtil.getScreenHeight();
        int screenWidth = cacheWidth;
        int screenHeight = cacheHeight;
        if (cacheWidth > cacheHeight) {
            screenWidth = cacheWidth;
            screenHeight = cacheHeight;
        } else {
            screenWidth = cacheHeight;
            screenHeight = cacheWidth;
        }
        GuardianUtil.getCaptureImage(context, screenWidth, screenHeight, tag);
    }

    public static String getScreenNum() {
        String screenNum = "1";
        List<ScreenEntity> screenEntityList = EtvApplication.getInstance().getListScreen();
        if (screenEntityList == null || screenEntityList.size() < 1) {
            screenNum = "1";
        } else {
            screenNum = screenEntityList.size() + "";
        }
        return screenNum;
    }

    public static String getresolution() {
        if (CpuModel.isMLogic()) {
            return getMlogicResolution();
        }
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            return getM11Resolution();
        }
        StringBuilder screenNumSize = new StringBuilder();
        List<ScreenEntity> screenEntityList = EtvApplication.getInstance().getListScreen();
        if (screenEntityList != null && screenEntityList.size() > 0) {
            for (int i = 0; i < screenEntityList.size(); i++) {
                ScreenEntity screenEntity = screenEntityList.get(i);
                int width = screenEntity.getScreenWidth();
                int height = screenEntity.getScreenHeight();
                if (i != 0) {
                    screenNumSize.append("(" + width + "x" + height + ")");
                } else {
                    screenNumSize.append(width + "x" + height);
                }
            }
        }
        return screenNumSize.toString();
    }

    /***
     * 获取M11-分辨率
     * @return
     */
    private static String screenSize = "";

    public static String getM11Resolution() {
        if (screenSize != null && screenSize.length() > 3) {
            MyLog.cdl("========屏幕信息000===" + screenSize);
            return screenSize;
        }
        screenSize = RootCmd.readFile(new File(RootCmd.M11_SCREEN_SIZE));
        if (screenSize != null && screenSize.length() > 2 && screenSize.contains(",")) {
            screenSize = screenSize.replace(",", "x");
        }
        MyLog.cdl("========屏幕信息111===" + screenSize, true);
        return screenSize;
    }

    private static String getMlogicResolution() {
        int screenWidth = SharedPerUtil.getScreenWidth();
        int screenHeight = SharedPerUtil.getScreenHeight();
        Shell.Result result = Shell.exeCommandForResult("cat /sys/class/display/mode");
        Shell.Result rotation = Shell.exeCommandForResult("getprop persist.sys.displayrot");
        String mode = result.msg.trim();
        String rot = rotation.msg.trim();
        if (mode.startsWith("1080p")) {
            if ("0".equals(rot) || "180".equals(rot)) {
                return "1920x1080";
            }
            return "1080x1920";
        }
        if (mode.startsWith("2160p")) {
            if ("0".equals(rot) || "180".equals(rot)) {
                return "3840x2160";
            }
            return "2160x3840";
        }
        if (screenWidth > screenHeight) {
            return screenWidth + "x" + screenHeight;
        }
        return screenHeight + "x" + screenWidth;
    }
}
