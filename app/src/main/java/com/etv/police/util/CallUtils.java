package com.etv.police.util;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.etv.util.MyLog;

import java.lang.reflect.Method;

public class CallUtils {


    /**
     * 是否有SIM卡
     */
    public static boolean hasSimCard(Context context) {
        if (context == null) {
            return false;
        }
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false; // 没有SIM卡
                break;
        }
        return result;
    }

//    /**
//     * 挂断电话
//     */
//    public static void handUpCallPhone(Context context) {
//        try {
////            context.sendBroadcast(new Intent("PHONE_HAND_UP_RECEIVER"));
//            // 首先拿到TelephonyManager
//            TelephonyManager telMag = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//            Class<TelephonyManager> c = TelephonyManager.class;
//            // 再去反射TelephonyManager里面的私有方法 getITelephony 得到 ITelephony对象
//            Method mthEndCall = c.getDeclaredMethod("getITelephony", (Class[]) null);
//            //允许访问私有方法
//            mthEndCall.setAccessible(true);
//            final Object obj = mthEndCall.invoke(telMag, (Object[]) null);
//            // 再通过ITelephony对象去反射里面的endCall方法，挂断电话
//            Method mt = obj.getClass().getMethod("endCall");
//            //允许访问私有方法
//            mt.setAccessible(true);
//            mt.invoke(obj);
//            MyLog.phone("挂断电话");
//        } catch (Exception e) {
//            MyLog.phone("挂断电话 error :" + e.toString());
//            e.printStackTrace();
//        }
//    }

    //自动挂断
    public static void handUpCallPhone(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
            ITelephony iTelephony;
            Method getITelephonyMethod = TelephonyManager.class
                    .getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            iTelephony = (ITelephony) getITelephonyMethod.invoke(tm,
                    (Object[]) null);
            iTelephony.endCall();
            MyLog.phone("挂断电话");
        } catch (Exception e) {
            MyLog.phone("挂断电话 error :" + e.toString());
            e.printStackTrace();
        }
    }


}
