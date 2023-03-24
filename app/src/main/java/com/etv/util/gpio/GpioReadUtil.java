package com.etv.util.gpio;

import android.content.Context;
import android.util.Log;

import com.etv.service.EtvService;
import com.etv.util.MyLog;

/***
 * 通过读取IO操作
 */
public class GpioReadUtil {

    Context context;
    public static boolean isRunGpio = false;
    public static final int gpio_num_one = 139;
    public static final int gpio_num_two = 138;
    public static final int gpio_num_three = 68;
    int gpIO = 68;  //索引值

    public GpioReadUtil(Context context, int gpIO) {
        this.context = context;
        this.gpIO = gpIO;
        GpioUtils.upgradeRootPermissionForExport();
    }

    public void startGetValues() {
        stopGpioUtil();
        if (listener == null) {
            return;
        }
        boolean isOpen = checkIoPermission();
        Log.e("initGpio", "========initGpio=====isOpen =" + isOpen);
        if (!isOpen) {
            listener.openFailed("打开失败");
            return;
        }
        isRunGpio = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (isRunGpio) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String desc = GpioUtils.getGpioValue(gpIO);
                    backHasPerson(desc);
                }
            }
        };
        EtvService.getInstance().executor(runnable);
    }

    boolean lastPersonStates = false;

    private void backHasPerson(String desc) {
        // 0 是短接
        // 1 是断开
        MyLog.phone("===GPIO得值===" + desc);
        boolean ifHasPerson = false;
        if (desc.contains("0")) {
            ifHasPerson = true;
        } else {
            ifHasPerson = false;
        }
        if (listener != null && (ifHasPerson != lastPersonStates)) {
            listener.backGpioStringInfoAction(ifHasPerson);
        }
        lastPersonStates = ifHasPerson;
    }


    public void stopGpioUtil() {
        isRunGpio = false;
    }

    private boolean checkIoPermission() {
        isRunGpio = false;
        if (GpioUtils.exportGpio(gpIO)) {
            GpioUtils.upgradeRootPermissionForGpio(gpIO);
            String status = GpioUtils.getGpioDirection(gpIO);
            if ("" == status) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    GpioInfoBackListener listener;

    public void setGpioInfoListener(GpioInfoBackListener listener) {
        this.listener = listener;
    }


}
