package com.etv.task.model;

import com.etv.task.entity.TaskWorkEntity;
import com.etv.util.MyLog;
import com.etv.util.SimpleDateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务工具类
 */
public class TaskModelUtil_1029 {

    public TaskModelUtil_1029() {

    }

    /**
     * 删除过期任务
     *
     * @param taskWorkCacheList
     * @param tag
     * 这里事获取真实的任务
     * 1 : 表示删除过期任务，并且移除时间没有到的任务
     * <p>
     * 这里是获取下载任务
     * -1：表示仅仅删除过期任务
     * @return
     */
    public static final int DEL_LASTDATE_AND_AFTER_NOW = 1;
    public static final int DEL_LASTDATE_ONLY = -1;

    public List<TaskWorkEntity> delOverdueTask(List<TaskWorkEntity> taskWorkCacheList, int tag) {
        if (taskWorkCacheList == null || taskWorkCacheList.size() < 1) {
            MyLog.del("====0000==删除掉过期得任务，taskWorkCacheList == nul===");
            return null;
        }
        List<TaskWorkEntity> backList = new ArrayList<TaskWorkEntity>();
        //删除过期的任务
        for (int i = 0; i < taskWorkCacheList.size(); i++) {
            MyLog.del("===删除过期任务==" + taskWorkCacheList.get(i).toString() + " /" + tag);
            TaskWorkEntity taskWorkEntityCurrent = taskWorkCacheList.get(i);
            String taskId = taskWorkEntityCurrent.getTaskId();
            String endDateString = taskWorkEntityCurrent.getEndDate();
            String endTimeString = taskWorkEntityCurrent.getEndTime();
            if (endTimeString.endsWith(":00")) {
                endTimeString = endTimeString.substring(0, endTimeString.lastIndexOf(":") + 1) + "59";
            }
            String startDateString = taskWorkEntityCurrent.getStartDate();
            long startDate = SimpleDateUtil.formatStringtoDate(startDateString);
            String startTimeString = taskWorkEntityCurrent.getStartTime();
            long startTime = SimpleDateUtil.formatStringTime(startTimeString);
            long endDate = SimpleDateUtil.formatStringtoDate(endDateString);
            long endTime = SimpleDateUtil.formatStringTime(endTimeString);
            MyLog.del("====0000开始时间==startTimeString==" + startTime + " / " + endTime);
            long currentDate = SimpleDateUtil.getCurrentDateLong();
            //默认是 + 10 秒。但是凌晨会误判,暂时注释
            // long currentHoMin = SimpleDateUtil.getCurrentHourMinSecond() + 10;
            long currentHoMin = SimpleDateUtil.getCurrentHourMinSecond();
            MyLog.del("====0000开始时间==" + startDate + "/ " + startTime + " /curent== " + currentDate + "/ " + currentHoMin + " /" + tag);
            MyLog.del("====0000结束时间==" + endDate + "/ " + endTime + " /curent== " + currentDate + "/ " + currentHoMin + " /" + tag);
            if (tag == DEL_LASTDATE_AND_AFTER_NOW) {  //过滤过期的，和没有开始的
                if (startDate > currentDate) {
                    //过滤开始日期大于现在的日期的
                    MyLog.del("====0000==del===过滤开始日期大于现在的日期的" + " /" + tag);
                    continue;
                }
                if (endDate < currentDate) {                       //任务已经过期了
                    MyLog.del("====0000==del===删除还没有开始任务" + " /" + tag);
                    delTaskByIdOrParsenError("删除过期任务,日期小于当前", taskId);
                    continue;                   //从集合中移除这个数据
                }
                MyLog.del("====0000==开始时间和今天一致，去比对时间" + " /" + tag);
                //这里分两种情况考虑，正常和跨天得=======================================
                boolean isBreak = false;
                if (startTime < endTime) {
                    if (startTime > currentHoMin || endTime < currentHoMin) {
                        MyLog.del("====0000==del===当前日期何今天一致，开始时间还没到这里先移除111" + " /" + tag
                                + "\n" + startTime + "/ " + currentHoMin + " / " + endTime);
                        isBreak = true;
                    }
                } else if (startTime > endTime) { //跨天操作
                    MyLog.del("====0000==del===当前日期何今天一致，巧合了" + " /" + tag);
                    if (currentHoMin > endTime && currentHoMin < startTime) {
                        isBreak = true;
                    }
                } else if (startTime == endTime) {
                    MyLog.del("====0000==del===当前日期何今天一致，你玩我呢" + " /" + tag);
                    isBreak = true;
                }
                if (isBreak) {
                    continue;
                }
            } else if (tag == DEL_LASTDATE_ONLY) { //仅仅删除过期的
                if (endDate < currentDate) {                                  //任务已经过期了
                    delTaskByIdOrParsenError("删除过期任务,日期小于当前", taskId);
                    continue;                   //从集合中移除这个数据
                }
                if (endDate == currentDate) {
                    if (endTime < currentHoMin) {
                        continue;
                    }
                }
            }
            MyLog.del("====0000==添加到集合===" + taskWorkEntityCurrent.toString());
            backList.add(taskWorkEntityCurrent);
        }
        if (taskWorkCacheList == null || taskWorkCacheList.size() < 1) {
            MyLog.del("====0000==删除掉过期得任务，还剩下得人物个数===0");
        } else {
            MyLog.del("====0000==删除掉过期得任务，还剩下得人物个数===" + taskWorkCacheList.size());
            String taskInfo = taskWorkCacheList.get(0).toString();
            MyLog.del("====0000==删除掉过期得任务，taskInfo===" + taskInfo);
        }
        return backList;
    }

    /**
     * 删除数据库，提交删除信息给服务器
     *
     * @param taskid
     */
    private void delTaskByIdOrParsenError(String desc, String taskid) {
//        MyLog.del("======删除数据库任务，删除服务器信息==" + desc + " / " + taskid, true);
//        long currentTime = SimpleDateUtil.formatBig(System.currentTimeMillis());
//        if (currentTime < AppConfig.TIME_CHECK_POWER_REDUCE) {
//            MyLog.del("=======系统时间不对，不删除数据库过期任务====" + currentTime, true);
//            return;
//        }
//        DBTaskUtil.delTaskById(taskid, desc);
//        EtvService.getInstance().deleteEquipmentTaskById(desc, taskid);
    }
}
