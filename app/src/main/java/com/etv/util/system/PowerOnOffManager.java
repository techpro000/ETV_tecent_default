//package com.etv.util.system;
//
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//import com.etv.config.AppConfig;
//import com.etv.config.AppInfo;
//import com.etv.entity.ScheduleRecord;
//import com.etv.listener.ObjectChangeListener;
//import com.etv.service.EtvService;
//import com.etv.util.MyLog;
//import com.etv.util.SharedPerManager;
//import com.etv.util.poweronoff.PowerOnOffRunnable;
//import com.etv.util.poweronoff.entity.TimerDbEntity;
//import com.etv.util.poweronoff.db.PowerDbManager;
//import com.etv.util.poweronoff.util.TimerChangeUtil;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class PowerOnOffManager {
//
//    private static PowerOnOffManager intsnace;
//    private Context mContext;
//
//    public static PowerOnOffManager getInstance() {
//        if (intsnace == null) {
//            synchronized (PowerOnOffManager.class) {
//                if (intsnace == null) {
//                    intsnace = new PowerOnOffManager();
//                }
//            }
//        }
//        return intsnace;
//    }
//
//    /***
//     * 根据工作模式来设定定时开关机
//     */
//    public void changePowerOnOffByWorkModel(String printTag) {
//        if (AppConfig.APP_TYPE == AppConfig.APP_TYPE_KING_LAM) {
//            MyLog.powerOnOff("0000==============金浪漫客户，不修改定时开关机");
//            return;
//        }
//        MyLog.powerOnOff("修改定时开关机时间: " + printTag, true);
//        clearPowerOnOffTime("准备设定定时开关机数据");
//        List<TimerDbEntity> lists = PowerDbManager.queryTimerList();
//        if (lists == null || lists.size() < 1) {
//            return;
//        }
//        setPowerOnOffTime(lists);
//    }
//
//    private void setPowerOnOffTime(List<TimerDbEntity> timeListSet) {
//        PowerOnOffRunnable runnable = new PowerOnOffRunnable();
//        runnable.setLists(PowerOnOffRunnable.TIMER_ACTION_SET_POWERONOFF_TIME, timeListSet);
//        EtvService.getInstance().executor(runnable);
//    }
//
//    /***
//     * 获取定时开机时间
//     * @return
//     */
//    public static String getPowerOnTime() {
//        return Utils.getValueFromProp("persist.sys.powerontime");
//    }
//
//    /***
//     * 获取关机的时间
//     * @return
//     */
//    public static String getPowerOffTime() {
//        return Utils.getValueFromProp("persist.sys.powerofftime");
//    }
//
//    /***
//     * 把星期 开关机时间转化成时间戳形式
//     * @param timerList
//     * @return
//     */
//    public static List<ScheduleRecord> getPowerDateLocalList(List<TimerDbEntity> timerList) {
//        List<ScheduleRecord> listBack = new ArrayList<ScheduleRecord>();
//        try {
//            listBack = TimerChangeUtil.addTimerInfoToList(timerList);
//        } catch (Exception E) {
//            E.printStackTrace();
//        }
//        return listBack;
//    }
//
//    public static List<ScheduleRecord> getPowerDateNetList(List<TimerDbEntity> timerList) {
//        if (timerList == null || timerList.size() < 1) {
//            return null;
//        }
//        List<ScheduleRecord> listBack = TimerChangeUtil.addTimerInfoToList(timerList);
//        return listBack;
//    }
//
//    public void savePowerOnOffTime(List<TimerDbEntity> timedTaskList, String printTag, ObjectChangeListener objectChangeListener) {
//        PowerOnOffRunnable runnable = new PowerOnOffRunnable();
//        runnable.setLists(PowerOnOffRunnable.TIMER_ACTION_SAVE_POWERONOFF_TIME, timedTaskList);
//        runnable.setObjectChangeListener(objectChangeListener);
//        EtvService.getInstance().executor(runnable);
//    }
//
//    public void getPowerOnOffFromDb() {
//        PowerOnOffRunnable runnable = new PowerOnOffRunnable();
//        runnable.setLists(PowerOnOffRunnable.TIMER_ACTION_GET_POWERONOFF_TIME, null);
//        EtvService.getInstance().executor(runnable);
//    }
//
//    /****
//     * ==================================================================================
//     * 祖传代码
//     * @param context
//     */
//
//    public void initPowerOnOffManager(Context context) {
//        this.mContext = context;
//    }
//
//    public void clearPowerOnOffTime(String printTag) {
//        MyLog.powerOnOff("clearPowerOnOffTime = " + printTag);
//        Intent intent = new Intent("android.intent.ClearOnOffTime");
//        this.mContext.sendBroadcast(intent);
//    }
//
//    public static String getLastestPowerOnTime() {
//        return Utils.getValueFromProp("persist.sys.powerontimeper");
//    }
//
//    public void setPowerOnOff(int[] powerOnTime, int[] powerOffTime) {
//        if (mContext == null) {
//            return;
//        }
//        Intent intent = new Intent("android.intent.action.setpoweronoff");
//        intent.putExtra("timeon", powerOnTime);
//        intent.putExtra("timeoff", powerOffTime);
//        intent.putExtra("enable", true);
//        intent.setPackage("com.adtv");                //添加定时开关机的包名
//        mContext.sendBroadcast(intent);
//    }
//
//    private void setLangguoCpuPowerOnOff(int[] timeonArray, int[] timeoffArray) {
//        try {
//            String SET_POWER_ON_OFF = "android.intent.action.setpoweronoff";
//            //携带的数据格式为：
//            Intent intent = new Intent();
//            intent.setAction(SET_POWER_ON_OFF);
//            intent.putExtra("timeon", timeonArray);
//            intent.putExtra("timeoff", timeoffArray);
//            intent.putExtra("enable", true); //使能开关机，true为打开，false为关闭
//            mContext.sendBroadcast(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void cancelPowerOnOff(int[] powerOnTime, int[] powerOffTime) {
//        Intent intent = new Intent("android.intent.action.setpoweronoff");
//        intent.putExtra("timeon", powerOnTime);
//        intent.putExtra("timeoff", powerOffTime);
//        intent.putExtra("enable", false);
//        this.mContext.sendBroadcast(intent);
//        Log.d("PowerOnOffManager", "poweron:" + Arrays.toString(powerOnTime) + "/ poweroff:" + Arrays.toString(powerOffTime));
//    }
//
//    public String getVersion() {
//        return Utils.getValueFromProp("persist.sys.poweronoffversion");
//    }
//}
