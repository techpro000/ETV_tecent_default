package com.etv.util.system;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.EtvApplication;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.ScreenEntity;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/***
 * 双频异县工具类
 */
public class DoubleScreenManager {

    private static DoubleScreenManager manager;
    private Context context;

    public static DoubleScreenManager getInstance(Context context) {
        if (manager == null) {
            manager = new DoubleScreenManager(context);
        }
        return manager;
    }

    private DoubleScreenManager(Context context) {
        this.context = context;
    }

    int screenNumAdd = 0;

    /**
     * 获取屏幕的个数，
     * 每10分钟执行一次，防止android清理缓存，丢失数据
     */
    List<ScreenEntity> screenEntityList = new ArrayList<ScreenEntity>();

    public void getDevScreenNumFroSys() {
        try {
            screenNumAdd++;
            if (screenNumAdd > 2) {
                return;
            }
            screenEntityList.clear();
            DisplayManager mDisplayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Display[] displays = mDisplayManager.getDisplays();
            int screenNum = displays.length;
            //添加主屏到集合
            ScreenEntity screenEntity = new ScreenEntity(AppInfo.PROGRAM_POSITION_MAIN, SharedPerUtil.getScreenWidth(), SharedPerUtil.getScreenHeight(), displays[0]);
            screenEntityList.add(screenEntity);
            if (screenNum < 2) { //单个屏幕
                EtvApplication.getInstance().setListScreen(screenEntityList);
                //防止一次获取失败，这里再次获取一次
                getDevScreenNumFroSys();
                return;
            }
            for (int i = 0; i < displays.length; i++) {
                int widthCache = displays[i].getWidth();
                int heightCache = displays[i].getHeight();
                MyLog.screen("屏幕分辨率====i =" + i + " / " + widthCache + " / " + heightCache);
            }
            getSecondScreenSize(screenEntityList, displays[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 系统默认得获取双屏得方法
     *
     * @param screenEntityList
     * @param display
     */
    private void getSecondScreenSize(List<ScreenEntity> screenEntityList, Display display) {
        //5.0的使用方法
        int widthCache = display.getWidth();
        int heightCache = display.getHeight();
        //7.0的使用方法
//        int width = display.getMode().getPhysicalWidth();
//        int height = display.getMode().getPhysicalHeight();
//        MyLog.screen("=====7.0的使用方法======" + width + " / " + height);
        mathScreenSizeToLocalSave(widthCache, heightCache, screenEntityList, display);
    }

    //     * 1  横屏
    //     * -1 竖屏
    //     * 0  方屏
    private void mathScreenSizeToLocalSave(int widthCache, int heightCache, List<ScreenEntity> screenEntityList, Display display) {
        int screenRoate = 0;
        //RK  获取副屏得旋转角度
        String cpuModel = CpuModel.getMobileType();
        if (cpuModel.contains(CpuModel.CPU_MODEL_RK_3399)) {
            screenRoate = SystemManagerUtil.getScreenRoate(RootCmd.PROOERTY_OTHER_INFO_3399, widthCache, heightCache);
        } else if (cpuModel.contains(CpuModel.CPU_MODEL_PX30)) {
            screenRoate = SystemManagerUtil.getScreenRoate(RootCmd.PROOERTY_OTHER_INFO_PX30_DOUBLE, widthCache, heightCache);
        } else {
            screenRoate = SystemManagerUtil.getScreenRoate(RootCmd.PROOERTY_OTHER_INFO, widthCache, heightCache);
        }
        int mainScreenRoate = SystemManagerUtil.getMainScreenRoate();
        MyLog.screen("====屏幕得旋转角度=主屏=" + mainScreenRoate + " / " + screenRoate);
        //去判断屏幕类型
        if (mainScreenRoate == screenRoate) {
            SharedPerManager.setScreen_type(SharedPerManager.SCREEN_TYPE_DEFAULT);
        } else {
            SharedPerManager.setScreen_type(SharedPerManager.SCREEN_TYPE_DOUBLE_VER_OTHER_HRO);
        }

        MyLog.screen("========设置副屏分尺寸==副屏=" + screenRoate + " / " + widthCache + "/ " + heightCache);
        int width = widthCache;
        int height = heightCache;
        int screenType = SharedPerManager.getScreen_type();
        MyLog.screen("========主副屏方向是否一致=" + screenType);
        if (screenType == SharedPerManager.SCREEN_TYPE_DOUBLE_VER_OTHER_HRO) {
            if (screenRoate > 0) {   // 横屏
                width = Math.max(widthCache, heightCache);
                height = Math.min(widthCache, heightCache);
            } else if (screenRoate < 0) {   //竖屏
                width = Math.min(widthCache, heightCache);
                height = Math.max(widthCache, heightCache);
            } else if (screenRoate == 0) {   //方屏
                width = widthCache;
                height = heightCache;
            }
        } else if (screenType == SharedPerManager.SCREEN_TYPE_DEFAULT) {
            //两个屏幕是一个方向
            boolean isHroOrVerScreen = SystemManagerUtil.isScreenHorOrVer(context, AppInfo.PROGRAM_POSITION_MAIN);
            //判断主屏方向
            if (isHroOrVerScreen) {
                //横屏
                width = Math.max(widthCache, heightCache);
                height = Math.min(widthCache, heightCache);
            } else {
                //竖屏
                width = Math.min(widthCache, heightCache);
                height = Math.max(widthCache, heightCache);
            }
        }
        MyLog.screen("========设置副屏分尺寸==11=" + screenRoate + " / " + width + "/ " + height);
        if (AppConfig.APP_TYPE != AppConfig.APP_TYPE_JIANGJUN_YUNCHENG) {
            screenEntityList.add(new ScreenEntity(AppInfo.PROGRAM_POSITION_SECOND, width, height, display));
        }
        EtvApplication.getInstance().setListScreen(screenEntityList);
    }

    public void openMainScreenLight() {
        try {
            Utils.writeIOFile("1", "/sys/devices/fb.8/graphics/fb0/pwr_bl");
        } catch (IOException var2) {
            var2.printStackTrace();
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }
    }

    public void closeMainScreenLight() {
        try {
            Utils.writeIOFile("0", "/sys/devices/fb.8/graphics/fb0/pwr_bl");
        } catch (IOException var2) {
            var2.printStackTrace();
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

    }

    public void openSecondaryScreenLight() {
        try {
            Utils.writeIOFile("1", "/sys/class/display/HDMI/enable");
        } catch (IOException var2) {
            var2.printStackTrace();
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }
    }

    public void closeSecondaryScreenLight() {
        try {
            Utils.writeIOFile("0", "/sys/class/display/HDMI/enable");
        } catch (IOException var2) {
            var2.printStackTrace();
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }
    }

    public boolean isMainScreenOpen() {
        return "1".equals(Utils.readGpioPG("/sys/devices/fb.8/graphics/fb0/pwr_bl"));
    }

    public boolean isSecondaryScreenOpen() {
        return "1".equals(Utils.readGpioPG("/sys/class/display/HDMI/enable"));
    }

    public void rotateMainScreen(String degree) {
        Utils.setValueToProp("persist.sys.displayrot", degree);
        this.reboot();
    }

    public void rotateSecondaryScreen(String degree) {
        if ("90".equals(degree) || "270".equals(degree)) {
            Utils.setValueToProp("persist.sys.hdmiscaler", "1");
        }

        Utils.setValueToProp("persist.sys.hdmirot", degree);
        this.reboot();
    }

    private void reboot() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.reboot");
        this.context.sendBroadcast(intent);
    }
}
