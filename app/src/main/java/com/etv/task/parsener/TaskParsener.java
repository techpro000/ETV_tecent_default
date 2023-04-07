package com.etv.task.parsener;

import android.content.Context;

import com.etv.config.ApiInfo;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.TaskWorkService;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.model.TaskCheckLimitListener;
import com.etv.task.model.TaskModelmpl;
import com.etv.task.model.TaskMudel;
import com.etv.task.model.TaskRequestListener;
import com.etv.task.view.TaskView;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.SharedPerManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

public class TaskParsener {

    Context context;
    TaskView taskView;
    TaskMudel taskMudel;

    public TaskParsener(Context context, TaskView taskView) {
        this.taskView = taskView;
        this.context = context;
        taskMudel = new TaskModelmpl();
    }

    public void requestTaskUrl(String printTag) {
        if (!NetWorkUtils.isNetworkConnected(context)) {
            parsenerJsonOverFromWeb("requestTaskUrl", null);   //没有网络去加载本地的素材
            return;
        }
        taskMudel.requestTaskInfo(taskRequestListener, printTag);
    }

    private TaskRequestListener taskRequestListener = new TaskRequestListener() {

        @Override
        public void finishMySelf(String errorDesc) {
            MyLog.task("=========finishMySelf========" + errorDesc);
            TaskWorkService.setCurrentTaskType(TaskWorkService.TASK_TYPE_DEFAULT, "解析完成，这里恢复原始状态");
            if (taskView == null) {
                return;
            }
            taskView.finishMyShelf(errorDesc);
        }

        @Override
        public void parserJsonOver(String tag, List<TaskWorkEntity> list) {
            parsenerJsonOverFromWeb(tag, list);
        }

        @Override
        public void modifyTxtInfoStatues(boolean isSuccess, String desc) {

        }

        @Override
        public void playNextProgram(boolean isBack, List<SceneEntity> sceneEntities, int tag) {

        }

    };

    /***
     * 解析完成，回调这里
     * @param tag
     * @param list
     */
    private void parsenerJsonOverFromWeb(String tag, List<TaskWorkEntity> list) {
        MyLog.task("解析完毕==" + tag + " /list is null =  " + (list == null));
        TaskWorkService.setCurrentTaskType(TaskWorkService.TASK_TYPE_DEFAULT, "解析完成，这里恢复状态");
        if (taskView == null) {
            return;
        }
        if (list == null || list.size() < 1) {
            MyLog.task("========parserJsonOver=======没有获取到任务=====1");
            taskView.finishMyShelf("没有合适的任务");
            return;
        }
        MyLog.task("========parserJsonOver=======准备去播放===== " + tag + " / " + list.size());
        taskView.backTaskList(list, tag);
    }

    /**
     * 获取内存阀值
     * 以及下载限制
     */
    public void getSystemSettingInfoTask(String printMsg) {
        if (AppInfo.getDevDownLevel) {
            return;
        }
        if (SharedPerManager.getWorkModel() != AppInfo.WORK_MODEL_NET) {
            return;
        }
        String requestUrl = ApiInfo.getSdManagerCheckUrl();
        OkHttpUtils
                .post()
                .url(requestUrl)
                .addParams("userName", SharedPerManager.getUserName())
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, String errorDesc, int id) {
                        MyLog.cdl("获取内存阀值=====" + printMsg + " / " + errorDesc);
                    }

                    @Override
                    public void onResponse(String json, int id) {
                        MyLog.cdl("获取内存阀值==" + printMsg + " / " + json);
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                String data = jsonObject.getString("data");
                                JSONObject jsonData = new JSONObject(data);
                                int riLevel = Integer.parseInt(jsonData.getString("riLevel"));
                                String riSpeedLimit = jsonData.getString("riSpeedLimit");  //下载速度
                                String riDownNumLimit = jsonData.getString("riDownNumLimit"); //下载台数限制
                                if (riDownNumLimit == null || riDownNumLimit.length() < 1) {
                                    riDownNumLimit = "200";
                                }
                                if (riSpeedLimit == null || riSpeedLimit.length() < 1) {
                                    riSpeedLimit = "5000";
                                }
                                int limitSpeed = Integer.parseInt(riSpeedLimit);
                                int limitNumLine = Integer.parseInt(riDownNumLimit);
                                SharedPerManager.setLimitDevNum(limitNumLine);
                                SharedPerManager.setLimitSpeed(limitSpeed);
                                SharedPerManager.setSdcardManagerAuthor(riLevel);
                                AppInfo.getDevDownLevel = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 检查当前是否有资格去下载
     */
    public void checkDownLimit(TaskCheckLimitListener limitListener) {
        if (!NetWorkUtils.isNetworkConnected(context)) {
            limitListener.checkDownLimitSpeed(false, 100, "网络异常或者未连接服务器");
            return;
        }
        taskMudel.checkDownLimit(limitListener);
    }

}
