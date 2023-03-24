package com.etv.util.system;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.ys.rkapi.Constant;
import com.ys.rkapi.MyManager;

public class SystemManagerInstance {

    Context context;
    public static SystemManagerInstance Instance;

    public static SystemManagerInstance getInstance(Context context) {
        if (Instance == null) {
            synchronized (SystemManagerInstance.class) {
                Instance = new SystemManagerInstance(context);
            }
        }
        return Instance;
    }

    MyManager myManager;

    public SystemManagerInstance(Context context) {
        this.context = context;
        if (myManager == null) {
            myManager = MyManager.getInstance(context);
            myManager.bindAIDLService(context);
        }
    }

    public boolean screenShot(String savePath) {
        return myManager.takeScreenshot(savePath);
    }

    /***
     * 关机
     */
    public void shoutDownDev() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.shutdown");
            if (Integer.parseInt(Build.VERSION.SDK) > 25) {
                intent.setPackage(Constant.YSRECEIVER_PACKAGE_NAME);
            }
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 重启设备
     */
    public void rebootDev() {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.reboot");
            if (Integer.parseInt(Build.VERSION.SDK) > 25) {
                intent.setPackage(Constant.YSRECEIVER_PACKAGE_NAME);
            }
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 背光灯是否亮起来
     * @return
     */
    public boolean getBackLightTtatues(String tag) {
        boolean isBackOn = true;
        try {
            isBackOn = myManager.isBacklightOn();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyLog.cdl("==========判断当前灯光的状态====" + tag + " /isBackOn=" + isBackOn);
        return isBackOn;
    }

    /***
     * 开关背光灯
     * @param isOn
     */
    public void turnBackLightTtatues(boolean isOn) {
        MyLog.sleep("===========turnBackLightTtatues===" + isOn + " / " + CpuModel.getMobileType());
        if (isOn) {
            turnOnBacklight();
        } else {
            turnOffBacklight();
        }
    }

    /***
     * 修改屏幕亮度
     * @param progress
     */
    public void setScreenLightProgress(int progress) {
        try {
            if (progress < 1) {
                progress = 1;
            }
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
                Intent intent1 = new Intent("com.ys.set_screen_bright").setPackage("com.ys.ys_receiver");
                intent1.putExtra("brightValue", progress);
                context.sendBroadcast(intent1);
                return;
            }
            myManager.changeScreenLight(progress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getScreenBrightness() {
        /*if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            Intent intent1 = new Intent("com.ys.set_screen_bright").setPackage("com.ys.ys_receiver");
            intent1.putExtra("brightValue", progress);
            context.sendBroadcast(intent1);
            return;
        }*/
        return myManager.getSystemBrightness() + 1;
    }

    /**
     * 打开背光
     */
    private void turnOnBacklight() {
        try {
            MyLog.sleep("===========00000执行唤醒程序");
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
                Intent intent = new Intent("com.ys.set_screen_on").setPackage("com.ys.ys_receiver");
                intent.putExtra("screen_on", 1);
                context.sendBroadcast(intent);
                return;
            }
            if (CpuModel.isGTCPU()) {
                //这里是高通得系统
                String lightNum = SharedPerManager.getScreenLightNum();
                String turnOnCmd = "echo " + lightNum + " > sys/class/leds/lcd-backlight/brightness";
                RootCmd.exusecmd(turnOnCmd, "高通开启主屏背光");
                MyLog.sleep("===========00000执行休眠程序===高通开启主屏背光  ");
                return;
            }
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_RK_3399)) {
                MyLog.sleep("===========00000执行休眠程序===06  ");
//                String lightNum = SharedPerManager.getScreenLightNum();
//                MyLog.sleep("===========开启背光===" + lightNum);
////                String turnOnCmd = "echo " + lightNum + " > sys/class/leds/lcd-backlight/brightness";
//                String turnOnCmd = "echo 0 > sys/class/backlight/backlight/bl_power";
//                RootCmd.exusecmd(turnOnCmd, "高通开启主屏背光");
                myManager.turnOnBackLight();
                MyLog.sleep("===========00000执行休眠程序===07 =" + (myManager == null));
                return;
            }
            MyLog.sleep("===========执行关屏操作==");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //4.4 M1
                RootCmd.setProperty(RootCmd.BRINT_LIGHT_3128_44, "1");
                return;
            }
            myManager.turnOnBackLight();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭背光
     */
    private void turnOffBacklight() {
        try {
            MyLog.sleep("===========00000执行休眠程序===");
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
                Intent intent = new Intent("com.ys.set_screen_on").setPackage("com.ys.ys_receiver");
                intent.putExtra("screen_on", 0);
                context.sendBroadcast(intent);
                return;
            }
            if (CpuModel.isGTCPU()) {
                MyLog.sleep("===========00000执行休眠程序===1");
                //这里是高通得系统
                String rootCmdLight = "cat sys/class/leds/lcd-backlight/brightness";
                String lightLightNum = RootCmd.execRootCmdBackInfo(rootCmdLight);
                SharedPerManager.setScreenLightNum(lightLightNum);
                String turnOnCmd = "echo 0 > sys/class/leds/lcd-backlight/brightness";
                RootCmd.exusecmd(turnOnCmd, "高通关闭主屏背光");
                MyLog.sleep("===========00000执行休眠程序===高通关闭主屏背光 ");
                return;
            }
            if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_RK_3399)) {
                MyLog.sleep("===========00000执行休眠程序===3");
//                String rootCmdLight = "cat sys/class/backlight/backlight/bl_power";
//                String lightLightNum = RootCmd.execRootCmdBackInfo(rootCmdLight);
//                SharedPerManager.setScreenLightNum(lightLightNum);
//                String turnOnCmd = "echo 1 > sys/class/backlight/backlight/bl_power";
//                RootCmd.exusecmd(turnOnCmd, "高通关闭主屏背光");
//                Log.e("light", "===========关闭背光===" + lightLightNum);
                myManager.turnOffBackLight();
                MyLog.sleep("===========00000执行休眠程序===4 " +  (myManager == null));
                return;
            }
            MyLog.sleep("==========执行关屏操作===========");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //4.4 M1
                RootCmd.setProperty(RootCmd.BRINT_LIGHT_3128_44, "0");

                return;
            }
            MyLog.sleep("===========00000执行休眠程序===");
            myManager.turnOffBackLight();
        } catch (Exception e) {
            MyLog.sleep("==========执行关屏操作======Exception=====5" + e.toString());
            e.printStackTrace();
        }
    }

    /***
     * 是否自动同步服务器时间
     * @param open
     * @param tag
     */
    public void switchAutoTime(boolean open, String tag) {
        MyLog.cdl("==========switchAutoTime======Exception=====" + tag);
        try {
            Intent intent = new Intent("com.ys.switch_auto_set_time");
            intent.putExtra("switch_auto_time", open);
            intent.setPackage(Constant.YSRECEIVER_PACKAGE_NAME);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rotateScreen(Context context, String s) {
        try {
            myManager.rotateScreen(context, s);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    public void updateImageOrZipSystem(String savePath) {
        try {
            MyLog.update("===========升级固件的文件是否存在==" + savePath, true);
            myManager.setUpdateSystemWithDialog(false);  //true=点击确认升级   false=自动升级
            myManager.upgradeSystem(savePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getGpioInfoByIoNum(int gpioNum) {
        int goioNum = -1;
        try {
            String goioNumInfo = myManager.getGpioValue(gpioNum);
            MyLog.cdl("===========goioNumInfo==" + goioNumInfo);
            goioNum = Integer.parseInt(goioNumInfo);
        } catch (Exception E) {
            E.printStackTrace();
        }
        return goioNum;
    }

    //HDMI比例拉伸
    public void setHDMIProportion() {
        myManager.getHdmiinStatus();
    }

    //HDMI全景拉伸
    public void setHDMIFullScreen(){
        myManager.turnOffHDMI();
    }


//    /***
//     * 获取当前定时开关机的开机时间
//     * @return
//     */
//    public String getPowerOnTime() {
//        String backPowerOnTime = "";
//        try {
//            backPowerOnTime = myManager.getPowerOnTime();
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//        return backPowerOnTime;
//    }
//
//    /***
//     * 获取当前定时开关机的关机时间
//     * @return
//     */
//    public String getPowerOffTime() {
//        String backPowerOffTime = "";
//        try {
//            backPowerOffTime = myManager.getPowerOffTime();
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//        return backPowerOffTime;
//    }
//
//    /***
//     * 获取设备上一次的定时开关机的开机时间
//     * @return
//     */
//    public String getLastestPowerOnTime() {
//        String backPowerOnTime = "";
//        try {
//            backPowerOnTime = myManager.getLastestPowerOnTime();
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//        return backPowerOnTime;
//    }
//
//    /***
//     * 获取设备上一次的定时开关机的关机机时间
//     * @return
//     */
//    public String getLastestPowerOffTime() {
//        String backPowerOffTime = "";
//        try {
//            backPowerOffTime = myManager.getLastestPowerOffTime();
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//        return backPowerOffTime;
//    }

}
