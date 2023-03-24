package com.etv.util.poweronoff;

import android.content.Context;
import android.os.Handler;

import com.etv.config.AppConfig;
import com.etv.service.listener.EtvServerListener;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;

/**
 * 定时开关机检测时间得线程
 * 保存上一次得开关机时间
 * 然后开机比对上一次得开关机时间
 */
public class CheckTimeRunnable implements Runnable {

    EtvServerListener listener;
    Context context;

    public CheckTimeRunnable(Context context, EtvServerListener listener) {
        this.context = context;
        this.listener = listener;
    }

    private final long TIME_ADD_ON = 300L;
    private final long TIME_ADD_OFF = 100L;

    @Override
    public void run() {
        //当前开机时间小于关机时间
        //判断当前时间只要在 开关机时间之内就可以
        //这里提前三分钟，防止不开机，关机后一分钟的时间误差
        long powerOnTime = SharedPerManager.getPowerOnTime() - TIME_ADD_ON;
        long powerOffTime = SharedPerManager.getPowerOffTime() + TIME_ADD_OFF;
        if (powerOnTime < 0) {
            powerOnTime = 0L;
        }
        if (powerOffTime < 101) {
            powerOffTime = 0L;
        }
        long currentTimeLong = SimpleDateUtil.getCurrentTimelONG();
        if (currentTimeLong < AppConfig.TIME_CHECK_POWER_REDUCE) {
            MyLog.powerOnOff("系统时间不对，默认开机时间", true);
            backStatues(true);
            return;
        }
        MyLog.powerOnOff("===上一次设定得开关机时间==" + powerOffTime + " / " + currentTimeLong + " / " + powerOnTime);
        if (currentTimeLong > powerOffTime && currentTimeLong < powerOnTime) {
            backStatues(false);   //关机时间
            MyLog.powerOnOff("===当前是关机时间");
        } else {
            MyLog.powerOnOff("===当前是开机机时间");
            backStatues(true);   //当前是开机机时间
        }
    }

    private void backStatues(final boolean b) {
        try {
            if (listener == null) {
                return;
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.jujleCurrentIsShutDownTime(b);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler();

}
