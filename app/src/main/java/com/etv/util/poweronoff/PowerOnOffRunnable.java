package com.etv.util.poweronoff;


import android.text.TextUtils;

import com.etv.config.AppConfig;
import com.etv.entity.ScheduleRecord;
import com.etv.entity.TimeEntity;
import com.etv.listener.ObjectChangeListener;
import com.etv.util.SharedPerManager;
import com.etv.util.poweronoff.db.PowerDbManager;
import com.etv.util.poweronoff.entity.PoOnOffLogEntity;
import com.etv.util.MyLog;
import com.etv.util.SimpleDateUtil;
import com.etv.util.poweronoff.entity.TimerDbEntity;
import com.etv.util.poweronoff.entity.TSchedule;
import com.etv.util.rxjava.AppStatuesListener;

import java.util.ArrayList;
import java.util.List;

public class PowerOnOffRunnable implements Runnable {

    List<TimerDbEntity> listsCache;   //用来缓存，传入得数据集合
    List<ScheduleRecord> listPowerOnOff = new ArrayList<ScheduleRecord>();
    String action;

    public static final String TIMER_ACTION_SAVE_POWERONOFF_TIME = "savePowerOnOffTime";
    public static final String TIMER_ACTION_GET_POWERONOFF_TIME = "getPowerOnOffFromDb";
    public static final String TIMER_ACTION_SET_POWERONOFF_TIME = "setPowerOnOffTime";
    ObjectChangeListener objectChangeListener = null;

    public PowerOnOffRunnable() {

    }

    public void setLists(String action, List<TimerDbEntity> lists) {
        this.listsCache = lists;
        this.action = action;
    }

    public void setObjectChangeListener(ObjectChangeListener objectChangeListener) {
        this.objectChangeListener = objectChangeListener;
    }

    @Override
    public void run() {
        if (action.equals(TIMER_ACTION_SAVE_POWERONOFF_TIME)) {
            savePowerOnOffTime();
        } else if (action.equals(TIMER_ACTION_GET_POWERONOFF_TIME)) {
            getPowerOnOffFromDb();
        } else if (action.equals(TIMER_ACTION_SET_POWERONOFF_TIME)) {
            setPowerOnOffTime(listsCache);
        }
    }

    /***
     * 保存数据到本地数据库
     */
    public void savePowerOnOffTime() {
        listPowerOnOff.clear();
        List<TimerDbEntity> timerDbEntityList = PowerDbManager.queryTimerList();
        if (timerDbEntityList == null || timerDbEntityList.size() < 1) {
            logpowerInfo("数据库没有数据，直接保存");
            savePowerListToDb(listsCache, "数据库没有数据，直接保存");
            return;
        }
        //判断本地的数量和 服务器是否数量上保持一致
        int sameTimeNum = 0;
        for (TimerDbEntity timerServer : listsCache) {
            String timeIdServer = timerServer.getTimneId();
            for (TimerDbEntity timerDbEntity : timerDbEntityList) {
                String timeIdLocal = timerDbEntity.getTimneId();
                if (timeIdServer.equals(timeIdLocal)) {
                    sameTimeNum++;
                }
                logpowerInfo("比对数据=====" + timeIdServer + " / " + timeIdLocal);
            }
        }
        logpowerInfo("比对数据，sameTimeNum = " + sameTimeNum + " /服务器=" + listsCache.size() + " /local = " + timerDbEntityList.size());
        if (sameTimeNum != listsCache.size()) {
            logpowerInfo("比对数据，数据不对称，需要添加");
            savePowerListToDb(listsCache, "服务器，本地数据不对称，直接添加");
            return;
        }
        //这里开始解析，比对数据库
        boolean needSaveLocalDb = false;
        for (TimerDbEntity timerServer : listsCache) {
            String timeIdServer = timerServer.getTimneId();
            String ttOffTimeServer = timerServer.getTtOffTime();
            String ttOnTimeServer = timerServer.getTtOnTime();
            String ttMonServer = timerServer.getTtMon();
            String ttTueServer = timerServer.getTtTue();
            String ttWedServer = timerServer.getTtWed();
            String ttThuServer = timerServer.getTtThu();
            String ttFriServer = timerServer.getTtFri();
            String ttSatServer = timerServer.getTtSat();
            String ttSunServer = timerServer.getTtSun();
            for (TimerDbEntity timerDbEntity : timerDbEntityList) {
                String timeIdLocal = timerDbEntity.getTimneId();
                String ttOffTimeLocal = timerDbEntity.getTtOffTime();
                String ttOnTimeLocal = timerDbEntity.getTtOnTime();
                String ttMonLocal = timerDbEntity.getTtMon();
                String ttTueLocal = timerDbEntity.getTtTue();
                String ttWedLocal = timerDbEntity.getTtWed();
                String ttThuLocal = timerDbEntity.getTtThu();
                String ttFriLocal = timerDbEntity.getTtFri();
                String ttSatLocal = timerDbEntity.getTtSat();
                String ttSunLocal = timerDbEntity.getTtSun();
                if (listsCache.size() == timerDbEntityList.size()){
                if (timeIdServer.equals(timeIdLocal)) {
                    if (ttOffTimeServer.equals(ttOffTimeLocal)
                            && ttOnTimeServer.equals(ttOnTimeLocal)
                            && ttMonServer.equals(ttMonLocal)
                            && ttTueServer.equals(ttTueLocal)
                            && ttWedServer.equals(ttWedLocal)
                            && ttThuServer.equals(ttThuLocal)
                            && ttFriServer.equals(ttFriLocal)
                            && ttSatServer.equals(ttSatLocal)
                            && ttSunServer.equals(ttSunLocal)) {
                    } else {
                        needSaveLocalDb = true;
                        logpowerInfo("比对数据===数据不一致。需要同步==" + timerServer.toString() + "\n " + timerDbEntity.toString());
                        break;
                    }
                }
                }else {
                    needSaveLocalDb = true;
                    break;
                }
            }
        }
        if (!needSaveLocalDb) {
            logpowerInfo("比对数据==数据一致，不用数据同步");
            return;
        }
        logpowerInfo("比对数据==数据不一致，开始同步数据");
        savePowerListToDb(listsCache, "比对完成，开始同步数据");
    }

    /***
     * 保存数据到数据库
     * @param lists
     */
    private void savePowerListToDb(List<TimerDbEntity> lists, String printTag) {
        PowerDbManager.clearTimeDb("服务器数据保存之前，清理一次数据");
        logpowerInfo("比对数据=== " + printTag, true);
        for (int i = 0; i < lists.size(); i++) {
            TimerDbEntity timerDbEntity = lists.get(i);
            PowerDbManager.addTimerDb(timerDbEntity);
        }
        getPowerOnOffFromDb();
    }

    /***
     * 从数据库中获取数据
     */
    public void getPowerOnOffFromDb() {
        logpowerInfo("======冲本地数据库中查询数", true);
        List<TimerDbEntity> lists = PowerDbManager.queryTimerList();
        if (lists == null || lists.size() < 1) {
            clearPowerOnOffTime();
            return;
        }
        setPowerOnOffTime(lists);
    }

    /**
     * 设置系统定时开关机
     * 亿晟定时开关机规律
     * 1：分组智能设定一组
     * 2：采取获取当前最近的一组，设定，
     * 3：下次开机重新获取离下次最近的一组
     *
     * @param lists
     */
    public void setPowerOnOffTime(List<TimerDbEntity> lists) {
        try {
            listPowerOnOff.clear();
            if (lists.size() < 1) {
                clearPowerOnOffTime();    //===清理定时开关机====
                logpowerInfo("获取的定时开关机 < 1 ,用户删除定时开关机任务，终止操作", true);
                return;
            }
            for (int i = 0; i < lists.size(); i++) {
                int dayOfWeek = -1;
                TimerDbEntity entity = lists.get(i);
                String powerOnTime = entity.getTtOnTime();
                String powerOffTime = entity.getTtOffTime();
                if (TextUtils.isEmpty(powerOnTime) || TextUtils.isEmpty(powerOffTime)) {
                    //如果本次的数据为null，跳过本次循环
                    continue;
                }
                logpowerInfo("=====获取的开机关机时间===" + powerOnTime + " / " + powerOffTime, true);
                int onHour = Integer.parseInt(powerOnTime.substring(0, powerOnTime.indexOf(":")));
                int onMin = Integer.parseInt(powerOnTime.substring(powerOnTime.indexOf(":") + 1, powerOnTime.length()));
                int offHour = Integer.parseInt(powerOffTime.substring(0, powerOffTime.indexOf(":")));
                int offMin = Integer.parseInt(powerOffTime.substring(powerOffTime.indexOf(":") + 1, powerOffTime.length()));
                logpowerInfo("=====获取的开关机机小时==" + onHour + " / " + onMin + "  /关机时间==" + offHour + " / " + offMin, true);
                boolean ttMon = Boolean.parseBoolean(entity.getTtMon());
                if (ttMon) {
                    dayOfWeek = 2;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
                boolean ttTue = Boolean.parseBoolean(entity.getTtTue());
                if (ttTue) {
                    dayOfWeek = 3;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
                boolean ttWed = Boolean.parseBoolean(entity.getTtWed());
                if (ttWed) {
                    dayOfWeek = 4;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
                boolean ttThu = Boolean.parseBoolean(entity.getTtThu());
                if (ttThu) {
                    dayOfWeek = 5;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
                boolean ttFri = Boolean.parseBoolean(entity.getTtFri());
                if (ttFri) {
                    dayOfWeek = 6;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
                boolean ttSat = Boolean.parseBoolean(entity.getTtSat());
                if (ttSat) {
                    dayOfWeek = 7;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
                boolean ttSun = Boolean.parseBoolean(entity.getTtSun());
                if (ttSun) {
                    dayOfWeek = 1;
                    saveTimerTask(dayOfWeek, onHour, onMin, offHour, offMin);
                }
            }
            long powerOffTime = TSchedule.getLastPowerOnOffTime(listPowerOnOff, false);
            long powerOnTime = TSchedule.getLastPowerOnOffTime(listPowerOnOff, true);
            logpowerInfo("===获取的关机时间===" + powerOffTime + " /最早的开机时间==" + powerOnTime, true);
            if (powerOffTime < 0 && powerOnTime < 0) {
                //这里可能获取时间异常了 ，不设置定时开关机
                logpowerInfo("===获取的开关机时间异常了，这里清理定时开关机", true);
                clearPowerOnOffTime();
                return;
            }
            if (powerOffTime > 0 && powerOnTime < 0) {
                //这里表示设置的开关时间已经超过一周了，这里需要做一个预判。第二天这个时候开机i一次，然后重新获取
                powerOnTime = TSchedule.jujleNoOnTime(powerOffTime);
            }
            logpowerInfo("=====最终的时间===" + powerOnTime + " / " + powerOffTime, true);
            long currentTimeSimple = SimpleDateUtil.getCurrentTimelONG(); //獲取當前的時間
            //如果下一次的定时开关机時間大於當前3分鐘，才能設定，不然就區下一次的定時開關機
            if (powerOnTime < powerOffTime && (powerOnTime - currentTimeSimple) > 300) {
                //如果最近的一次开机时间《关机时间，以最近的一次为准.倒退两分钟开机
                long onTimeLong = SimpleDateUtil.StringToLongTimePower(powerOnTime + "");
                long onTimeLongOff = onTimeLong - (1000 * 120);  //关机时间在此时间前减去2分钟再去换算时间
                logpowerInfo("===onTimeLongOff===" + onTimeLongOff);
                powerOffTime = SimpleDateUtil.formatBig(onTimeLongOff);
                logpowerInfo("===合理的时间===" + powerOnTime + " / " + powerOffTime);
                setToPowerOnOffTime(powerOffTime, powerOnTime);
                return;
            }
            powerOnTime = TSchedule.getLastPowerOnTime(listPowerOnOff, powerOffTime);
            logpowerInfo("===获取的开机时间===" + powerOnTime);
            if (powerOffTime < 0 || powerOnTime < 0) {
                //这里可能获取时间异常了 ，不设置定时开关机
                logpowerInfo("===获取的开关机时间异常了，这里清理定时开关机");
                clearPowerOnOffTime();
                return;
            }
            setToPowerOnOffTime(powerOffTime, powerOnTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setToPowerOnOffTime(long powerOffTime, long powerOnTime) {
        SharedPerManager.setPowerOnTime(powerOnTime, "设定定时开关机");
        SharedPerManager.setPowerOffTime(powerOffTime, "设定定时开关机");
        MyLog.powerOnOff("保存数据库的时间setToPowerOnOffTime==" + powerOffTime + " / " + powerOnTime);
        TimeEntity entityOff = SimpleDateUtil.getFormatLongTime(powerOffTime);
        TimeEntity entityOn = SimpleDateUtil.getFormatLongTime(powerOnTime);
        String onTimeSave = entityOn.getYear() + "-" + entityOn.getMonth() + "-" + entityOn.getDay() + " " + entityOn.getHour() + ":" + entityOn.getMinite() + ":00";
        String offTimeSave = entityOff.getYear() + "-" + entityOff.getMonth() + "-" + entityOff.getDay() + " " + entityOff.getHour() + ":" + entityOff.getMinite() + ":00";
        String createTime = SimpleDateUtil.formatTaskTimeShow(System.currentTimeMillis());
        MyLog.powerOnOff("保存数据库的时间==" + offTimeSave + " / " + onTimeSave + " / " + createTime);
        int[] timeoffArray = new int[]{entityOff.getYear(), entityOff.getMonth(), entityOff.getDay(), entityOff.getHour(), entityOff.getMinite()};
        int[] timeonArray = new int[]{entityOn.getYear(), entityOn.getMonth(), entityOn.getDay(), entityOn.getHour(), entityOn.getMinite()};
        PowerOnOffManager.getInstance().setPowerOnOff(timeonArray, timeoffArray);
        PoOnOffLogEntity poOnOffLogEntity = new PoOnOffLogEntity(offTimeSave, onTimeSave, createTime);
        boolean isSave = PowerDbManager.savePowerOnOffToWeb(poOnOffLogEntity);
        MyLog.powerOnOff("设定的定时开关机数据状态==" + isSave + " /createTime =  " + createTime, true);
//        AppStatuesListener.getInstance().objectLiveDate.postValue(AppStatuesListener.LIVE_DATA_SAVE_POWERONOFF_LOG);
    }

    private void saveTimerTask(int dayOfWeek, int onHour, int onMin, int offHour, int offMin) {
        ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
        listPowerOnOff.add(scheduleRecord);
    }

    /***
     * 清理定时开关机任务
     */
    public void clearPowerOnOffTime() {
        try {
            //删除数据库中的数据
            PowerOnOffManager.getInstance().clearPowerOnOffTime("runnable ");
        } catch (Exception e) {
            logpowerInfo("清理定时开关机error =" + e.toString(), true);
        }
    }

    private void logpowerInfo(String s) {
        logpowerInfo(s, false);
    }

    private void logpowerInfo(String s, boolean b) {
        MyLog.powerOnOff(s, b);
    }
}
