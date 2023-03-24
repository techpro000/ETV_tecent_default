package com.etv.util;

import static com.youth.banner.util.LogUtils.TAG;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.EtvApplication;
import com.etv.entity.ScreenEntity;
import com.etv.util.guardian.GuardianUtil;
import com.etv.util.rxjava.AppStatuesListener;
import com.etv.util.system.CpuModel;
import com.etv.util.system.SystemManagerInstance;

import java.io.File;
import java.util.List;

public class ScreenUtil {

    public static boolean screenShot(Context context, String savePath) {
        MyLog.update("=========截图成功-------正在截图------",true);
        return SystemManagerInstance.getInstance(context).screenShot(savePath);
    }

    /**
     * 智能截图
     *
     * @param context
     * @param tag
     */
    public static void getScreenImage(Context context, String tag) {
        int screenWidth = SharedPerUtil.getScreenWidth();
        int screenHeight = SharedPerUtil.getScreenHeight();
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11) && (screenWidth < screenHeight)) {
            //M11 竖屏，调用这里
            AppStatuesListener.getInstance().objectLiveDate.postValue(AppStatuesListener.LIVE_DATA_SCREEN_CATPTURE);
            return;
        }
        String cpuMudel = CpuModel.getMobileType();
        //判断是否是4.4的3128
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (cpuMudel.contains("rk312x")) {
                deal312844ScreenInfo(context, tag);
                return;
            }
        }
        if (cpuMudel.contains("smd") || cpuMudel.contains("msm")) {
            deal312844ScreenInfo(context, tag);
            return;
        }
        GuardianUtil.getCaptureImage(context, screenWidth, screenHeight, tag);
    }

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
        /*if (screenWidth > screenHeight) {
            return "3840x2160";
        } else {
            return "2160x3840";
        }*/
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
