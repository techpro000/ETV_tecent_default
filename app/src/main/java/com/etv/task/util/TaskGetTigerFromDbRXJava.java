package com.etv.task.util;

import com.etv.config.AppInfo;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.db.DbTaskManager;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.util.MyLog;

import java.util.List;

/***
 * 替换 TaskGetTigerFromDbRunnable 存在的
 *
 */
public class TaskGetTigerFromDbRXJava {

    public TaskWorkEntity getTaskTigerFromDb() {
        List<TaskWorkEntity> taskWorkCacheList = DBTaskUtil.getTaskInfoList();
        if (taskWorkCacheList == null || taskWorkCacheList.size() < 1) {
            MyLog.task("从数据库获取得数据==null");
            return null;
        }
        MyLog.playTask("==backTaskList===便利数据库得数量==" + taskWorkCacheList.size());
        TaskWorkEntity taskBack = null;
        for (TaskWorkEntity taskWorkEntity : taskWorkCacheList) {
            if (taskWorkEntity == null) {
                continue;
            }
            String etLevel = taskWorkEntity.getEtLevel();
            MyLog.playTask("==backTaskList===便利数据库==" + taskWorkEntity.toString());
            if (etLevel.contains(AppInfo.TASK_PLAY_TRIGGER)) {
                taskBack = taskWorkEntity;
                break;
            }
        }
        if (taskBack == null) {
            MyLog.task("便利数集合，没有数据==");
            return null;
        }
        return searchTaskInfo(taskBack);
    }

    /***
     *获取任务信息
     * @param taskBack
     */
    private TaskWorkEntity searchTaskInfo(TaskWorkEntity taskBack) {
        TaskWorkEntity taskWorkEntity = DbTaskManager.getTaskEntityFormDb(taskBack);
        return taskWorkEntity;
    }
}
