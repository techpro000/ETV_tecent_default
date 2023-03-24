package com.etv.util.poweronoff.util;

import android.text.TextUtils;

import com.etv.entity.ScheduleRecord;
import com.etv.entity.TimeEntity;
import com.etv.util.MyLog;
import com.etv.util.SimpleDateUtil;
import com.etv.util.poweronoff.entity.TimerDbEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimerChangeUtil {
    public static List<ScheduleRecord> addTimerInfoToList(List<TimerDbEntity> timeListSet) {
        List<ScheduleRecord> listBack = new ArrayList<ScheduleRecord>();
        try {
            for (int i = 0; i < timeListSet.size(); i++) {
                int dayOfWeek = -1;
                TimerDbEntity entity = timeListSet.get(i);
                String powerOnTime = entity.getTtOnTime();
                String powerOffTime = entity.getTtOffTime();
                if (TextUtils.isEmpty(powerOnTime) || TextUtils.isEmpty(powerOffTime)) { //如果本次的数据为null，跳过本次循环
                    continue;
                }
                int onHour = Integer.parseInt(powerOnTime.substring(0, powerOnTime.indexOf(":")));
                int onMin = Integer.parseInt(powerOnTime.substring(powerOnTime.indexOf(":") + 1, powerOnTime.length()));
                int offHour = Integer.parseInt(powerOffTime.substring(0, powerOffTime.indexOf(":")));
                int offMin = Integer.parseInt(powerOffTime.substring(powerOffTime.indexOf(":") + 1, powerOffTime.length()));
                boolean ttMon = Boolean.parseBoolean(entity.getTtMon());
                if (ttMon) {
                    dayOfWeek = 2;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
                boolean ttTue = Boolean.parseBoolean(entity.getTtTue());
                if (ttTue) {
                    dayOfWeek = 3;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
                boolean ttWed = Boolean.parseBoolean(entity.getTtWed());
                if (ttWed) {
                    dayOfWeek = 4;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
                boolean ttThu = Boolean.parseBoolean(entity.getTtThu());
                if (ttThu) {
                    dayOfWeek = 5;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
                boolean ttFri = Boolean.parseBoolean(entity.getTtFri());
                if (ttFri) {
                    dayOfWeek = 6;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
                boolean ttSat = Boolean.parseBoolean(entity.getTtSat());
//                MyLog.powerOnOff("遍历数据==周六==" + ttSat);
                if (ttSat) {
                    dayOfWeek = 7;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
                boolean ttSun = Boolean.parseBoolean(entity.getTtSun());
//                MyLog.powerOnOff("遍历数据==周六==" + ttSun);
                if (ttSun) {
                    dayOfWeek = 1;
                    ScheduleRecord scheduleRecord = new ScheduleRecord(dayOfWeek, onHour, onMin, offHour, offMin);
                    listBack.add(scheduleRecord);
                }
            }
            if (listBack == null || listBack.size() < 1) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsenerTimerListSch(listBack);
    }

    /***
     * @param listSch
     * 定时开关机列表
     * on   表示开机时间
     * off  表示关机时间
     * @return
     */
    private static List<ScheduleRecord> parsenerTimerListSch(List<ScheduleRecord> listSch) {
        if (listSch == null || listSch.size() < 1) {
            return null;
        }
        List<ScheduleRecord> listBack = new ArrayList<ScheduleRecord>();
        try {
            Calendar calendar = Calendar.getInstance();
            int currentDayWeek = calendar.get(Calendar.DAY_OF_WEEK);  //今天的工作星期
            Long currentTimeMillis = System.currentTimeMillis();
            long simpleTime = SimpleDateUtil.formatBig(currentTimeMillis);
            //以今天基准，遍历所有日期，补全所有的时间数据
            TimeEntity entity = SimpleDateUtil.getFormatLongTime(simpleTime);
            for (int i = 0; i < listSch.size(); i++) {
                ScheduleRecord scheduleRecord = listSch.get(i);
                int weekList = scheduleRecord.getDayOfWeek();
                int distanceDay = 0;
                int year = entity.getYear();
                int month = entity.getMonth();
                int day = entity.getDay();

                if (weekList < currentDayWeek) {
                    distanceDay = 7 - currentDayWeek + weekList;
                } else {  //星期属大于今天
                    distanceDay = weekList - currentDayWeek;
                }
                day = day + distanceDay;
                if (month == 12 && day > 31) {  //跨年了
                    year = year + 1;
                    month = 1;
                    day = 1;
                } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {   //判断31天月份
                    if (day > 31) {
                        day = day - 31;
                        month = month + 1;
                    }
                } else if (month == 4 || month == 6 || month == 9 || month == 11) {           //判断30天的月份
                    if (day > 30) {
                        day = day - 30;
                        month = month + 1;
                    }
                } else if (month == 2) {                            //判断二月
                    if (year % 4 == 0 && year % 100 != 0) {//闰年
                        if (day > 29) {
                            day = day - 29;
                            month = month + 1;
                        }
                    } else if (year % 400 == 0) { //闰年
                        if (day > 29) {
                            day = day - 29;
                            month = month + 1;
                        }
                    } else {  //平年
                        if (day > 28) {
                            day = day - 28;
                            month = month + 1;
                        }
                    }
                }
                scheduleRecord.setYear(year);
                scheduleRecord.setMonth(month);
                scheduleRecord.setDay(day);
                listBack.add(scheduleRecord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listBack;
    }


}
