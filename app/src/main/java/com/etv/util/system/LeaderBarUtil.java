package com.etv.util.system;

import static com.youth.banner.util.LogUtils.TAG;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.etv.config.AppConfig;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.ys.rkapi.Utils.VersionUtils;

import java.util.List;

public class LeaderBarUtil {

    /**
     * 判断APP是否运行在前台
     * true 后台
     * false 前台
     *
     * @param context
     * @return
     */
    public static boolean isAppRunBackground(Context context, String tag) {
        MyLog.guardian("==========判断软件是否在前台==tag = " + tag);
        try {
            if (context == null) {
                MyLog.guardian("==========判断软件是否在前台==null");
                return true;
            }
            ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
            if (appProcesses == null || appProcesses.size() < 1) {
                MyLog.guardian("==========获取是否在前台得列表==null==");
                return false;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_JIANGJUN_YUNCHENG) {
                    if (appProcess.processName.equals(SharedPerManager.getPackageNameByYuncheng())) {
                        return false;
                    } else {
                        return true;
                    }
                }
                if (appProcess.processName.equals(SharedPerManager.getPackageNameBySp())) {
                    if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /***
     * 调用系统层，关闭隐藏导航栏
     * @param context
     */
    public static void hiddleLeaderBar(Activity context) {
        try {
            if (context == null) {
                return;
            }
            boolean isShowNavbar = getNavBarHideState(context);
            Log.e(TAG, "hiddleLeaderBar=========== " + isShowNavbar);
            if (isShowNavbar) {
            } else {
                hideNavBar(false, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hideNavBar(boolean hide, Activity context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent();
        if (hide) {
            Log.e("TAG", "hideNavBar: " + 565656);
            intent.setAction("android.action.adtv.showNavigationBar");
            context.sendBroadcast(intent);
        } else {
            Log.e("TAG", "hideNavBar: " + 787878);
            intent.setAction("android.action.adtv.hideNavigationBar");
            context.sendBroadcast(intent);
        }

    }


    private static boolean getNavBarHideState(Activity context) {
        String mode = VersionUtils.getAndroidModle();
        return !"rk3368".equals(mode) && !"rk3328".equals(mode) ? ("rk3399".equals(mode) ? Utils.getValueFromProp("persist.sys.statebarstate").equals("0") : Settings.System.getInt(context.getContentResolver(), "hidden_state_bar", 0) == 1) : Utils.getValueFromProp("persist.sys.sb.hide").equals("1");
    }
}
