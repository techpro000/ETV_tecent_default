//package com.etv.task.util;
//
//import android.os.Handler;
//
//import com.EtvApplication;
//import com.etv.config.AppInfo;
//import com.etv.task.db.DBTaskUtil;
//import com.etv.task.entity.TaskWorkEntity;
//import com.etv.task.model.TaskGetDbListener;
//import com.etv.task.model.TaskModelUtil;
//import com.etv.util.MyLog;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TaskGetFromDbRunnableBack implements Runnable {
//
//    TaskGetDbListener listener;
//    private Handler handler = new Handler();
//    private String printTag;
//    private int delTag;
//
//    public TaskGetFromDbRunnableBack(TaskGetDbListener listener, String printTag, int delTag) {
//        this.listener = listener;
//        this.printTag = printTag;
//        this.delTag = delTag;
//    }
//
//    @Override
//    public void run() {
//        getNextPlayTaskListFromDb();
//    }
//
//    TaskModelUtil taskModelUtil;
//
//    public void getNextPlayTaskListFromDb() {
//        try {
//            //从数据库中获取所有的任务
//            List<TaskWorkEntity> taskWorkCacheList = DBTaskUtil.getTaskInfoList();
//            if (taskWorkCacheList == null || taskWorkCacheList.size() < 1) {
//                MyLog.task("=====00000====从数据库中获取的任务集合个数==0=====" + printTag);
//                DBTaskUtil.clearAllDbInfo("从数据库中获取的任务集合个数==0=" + printTag);   //清理所有的数据库信息
//                backTaskList(null, "从数据库中获取的任务集合个数==0");
//                return;
//            }
//            MyLog.task("=====00000====从数据库中获取的任务集合个数======" + taskWorkCacheList.size() + " / " + printTag, true);
//            //删除过期任务,以及还没有开始的任务
//            if (taskModelUtil == null) {
//                taskModelUtil = new TaskModelUtil();
//            }
//            taskWorkCacheList = taskModelUtil.delOverdueTask(taskWorkCacheList, delTag);
//            MyLog.task("=====00000====删除过期的任务，剩余任务==jujle=====" + (taskWorkCacheList == null) + " / " + (taskWorkCacheList.size() < 1));
//            if (taskWorkCacheList == null || taskWorkCacheList.size() < 1) {
//                MyLog.task("=====00000====删除过期的任务，剩余任务==0=====" + printTag);
//                DBTaskUtil.clearAllDbInfo("删除过期任务,剩余任务==0=" + printTag);
//                backTaskList(null, "删除过期任务,剩余任务==0");
//                return;
//            }
//            MyLog.task("=====00000====删除过期的任务==剩余任务====" + taskWorkCacheList.size() + printTag);
//            //判断任务是否包含今天播放
//            List<TaskWorkEntity> taskWorkEntityList = new ArrayList<TaskWorkEntity>();
//            for (int i = 0; i < taskWorkCacheList.size(); i++) {
//                TaskWorkEntity taskWorkEntityCurrent = taskWorkCacheList.get(i);
//                String taskId = taskWorkEntityCurrent.getTaskId();
//                boolean isHasWorkToday = TaskDealUtil.ifMathTodayTask(taskWorkEntityCurrent);
//                MyLog.task("====判断任务没有今天的数据==" + isHasWorkToday + " /taskId= " + taskId + " / " + printTag);
//                if (isHasWorkToday) {
//                    taskWorkEntityList.add(taskWorkCacheList.get(i));
//                }
//            }
//            //  表示数据库中没有今天的任务了，终端操作
//            if (taskWorkEntityList == null || taskWorkEntityList.size() < 1) {
//                backTaskList(null, "表示数据库中没有今天的任务了，终端操作");
//                return;
//            }
//            List<TaskWorkEntity> taskDefaultList = new ArrayList<TaskWorkEntity>();  //替换-----追加
//            List<TaskWorkEntity> taskWaitList = new ArrayList<TaskWorkEntity>();     //插播模式
//            List<TaskWorkEntity> taskSametList = new ArrayList<TaskWorkEntity>();    //同步模式
//
//            TaskWorkEntity taskWorkEntityInsert = null;                //插播消息，单独封装
//            for (int i = 0; i < taskWorkEntityList.size(); i++) {
//                String taskType = taskWorkEntityList.get(i).getEtTaskType();
//                if (taskType.contains(AppInfo.TASK_TYPE_INSERT_TXT)) {
//                    //插播消息,单独处理
//                    taskWorkEntityInsert = taskWorkEntityList.get(i);
//                    MyLog.task("====筛选任务==字幕插播消息");
//                } else {
//                    String taskLevel = taskWorkEntityList.get(i).getEtLevel();
//                    if (taskLevel.contains(AppInfo.TASK_PLAY_CALL_WAIT)) {   //插播任务
//                        MyLog.task("====筛选任务==插播任务");
//                        taskWaitList.add(taskWorkEntityList.get(i));
//                    }
//                    if (taskLevel.contains(AppInfo.TASK_PLAY_PLAY_SAME)) {  //同步播放
//                        MyLog.task("====筛选任务==同步播放");
//                        taskSametList.add(taskWorkEntityList.get(i));
//                    }
//                    if (taskLevel.contains(AppInfo.TASK_PLAY_ADD_TASK)) {  // 追加.
//                        MyLog.task("====筛选任务==追加.");
//                        taskDefaultList.add(taskWorkEntityList.get(i));
//                    }
//                    if (taskLevel.contains(AppInfo.TASK_PLAY_REPLACE)) {  //替换
//                        MyLog.task("====筛选任务==替换");
//                        taskDefaultList.add(taskWorkEntityList.get(i));
//                    }
//                    if (taskLevel.contains(AppInfo.TASK_PLAY_SAME_SCREEN)) {  //同屏
//                        MyLog.task("====筛选任务==替换");
//                        taskDefaultList.add(taskWorkEntityList.get(i));
//                    }
//                }
//            }
//            //保存插播消息
//            EtvApplication.getInstance().setTaskWorkEntityInsert(taskWorkEntityInsert);
//            MyLog.task("====剩下筛选插播数据==" + taskWaitList.size() + " /普通任务==" + taskDefaultList.size() + " / " + printTag);
//            if (taskWaitList.size() > 0) {   //如果有插播任务
//                backTaskList(taskWaitList, "插播任务");
//                return;
//            }
//            if (taskSametList.size() > 0) {   //如果有同步任务
//                backTaskList(taskSametList, "同步任务");
//                return;
//            }
//            if (taskDefaultList.size() > 0) {   //如果有普通任务
//                backTaskList(taskDefaultList, "普通任务");
//                return;
//            }
//        } catch (Exception e) {
//            MyLog.ExceptionPrint("解析任务error: " + e.toString() + " / " + printTag);
//            e.printStackTrace();
//        }
//        backTaskList(null, "最后解析");
//    }
//
//    private void backTaskList(final List<TaskWorkEntity> list, String tag) {
//        if (handler == null) {
//            return;
//        }
//        if (listener == null) {
//            return;
//        }
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                if (list == null || list.size() < 1) {
//                    MyLog.task("=====这里返回给UI界面得数据==null=" + tag);
//                } else {
//                    MyLog.task("=====这里返回给UI界面得数据===" + (list.size()) + " /" + tag);
//                }
//                listener.getTaskFromDb(list);
//            }
//        });
//    }
//
//}
