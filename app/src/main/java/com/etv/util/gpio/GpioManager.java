package com.etv.util.gpio;

import com.etv.service.EtvService;
import com.etv.util.MyLog;

public class GpioManager {

    GpioHelper gpioHelper;

    public GpioManager(String sysPath) {
        gpioHelper = new GpioHelper(sysPath);
    }

    boolean isRun = false;

    public void startReadGpioInfo() {
        isRun = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (isRun) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String backInfo = gpioHelper.readGpioInfo();
                    backInToView(backInfo);
                }
            }
        };
        EtvService.getInstance().executor(runnable);
    }

    /***
     * 这里处理IO-1 得操作
     * @param desc
     */
    boolean lastPersonStates = false;

    private void backInToView(String desc) {
//        MyLog.cdl("===获取得属性得===" + desc);
        if (desc == null || desc.length() < 1) {
            desc = "1";
        }
        boolean ifHasPerson = false;
        if (desc.contains("0")) {
            //东西丢了
            ifHasPerson = true;
        } else {
            //东西还在
            ifHasPerson = false;
        }
        if (listener != null && (ifHasPerson != lastPersonStates)) {
            listener.backGpioStringInfoAction(ifHasPerson);
        }
        lastPersonStates = ifHasPerson;

    }


    public void stopReadGpioNum() {
        isRun = false;
        gpioHelper.releaseGpio();
    }

    GpioInfoBackListener listener;

    public void setGpioInfoListener(GpioInfoBackListener listener) {
        this.listener = listener;
    }


}
