package com.etv.task.util;

import android.os.Handler;

import com.EtvApplication;
import com.etv.config.AppInfo;
import com.etv.task.db.DBTaskUtil;
import com.etv.task.db.DbTaskManager;
import com.etv.task.entity.CpListEntity;
import com.etv.task.entity.PmListEntity;
import com.etv.task.entity.SceneEntity;
import com.etv.task.entity.TaskWorkEntity;
import com.etv.task.entity.TextInfo;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.SimpleDateUtil;

import java.util.List;

/**
 * 获取插播消息控件信息
 */
public class TaskGetTxtInsertRunnable implements Runnable {

    private Handler handler = new Handler();
    GetTaskTxtInsertListener listener;

    public TaskGetTxtInsertRunnable(GetTaskTxtInsertListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        TaskWorkEntity taskWorkEntityInsert = EtvApplication.getInstance().getTaskWorkEntityInsert();
        MyLog.cdl("TaskGetTxtInsertRunnable====" + (taskWorkEntityInsert == null));
        if (taskWorkEntityInsert == null) {
            backCpEntityToView("", null, "TaskWorkEntity==null");
            return;
        }
        String taskId = taskWorkEntityInsert.getTaskId();
        List<TextInfo> textInfoList = DBTaskUtil.getTxtParsentListInfoByTaskId(taskId);
        if (textInfoList == null || textInfoList.size() < 1) {
            backCpEntityToView("", null, "txList=null");
            return;
        }
        TextInfo textInfo = textInfoList.get(0);
        if (textInfo == null) {
            backCpEntityToView("", null, "textInfo==null");
            return;
        }
        String textSize = textInfo.getTaFontSize();
        if (textSize == null || textSize.length() < 1) {
            textSize = "25";
        }
        int textSizeShow = Integer.parseInt(textSize);
        if (textSizeShow < 10) {
            textSizeShow = 10;
        }
        //计算控件得高度
        int viewHeight = textSizeShow * 5 / 4;
        String alineMent = textInfo.getTaAlignment();
        if (alineMent == null || alineMent.length() < 1) {
            alineMent = "2";
        }
        int topMarginSize = 0;
        if (alineMent.contains("2")) {
            topMarginSize = 2;
        } else if (alineMent.contains("5")) {
            topMarginSize = SharedPerUtil.getScreenHeight() / 2 - (viewHeight / 2);
        } else if (alineMent.contains("8")) {
            topMarginSize = SharedPerUtil.getScreenHeight() - viewHeight;
        }
        CpListEntity cpListEntity = new CpListEntity();
        cpListEntity.setCoLeftPosition("0");
        cpListEntity.setCoRightPosition(topMarginSize + "");
        cpListEntity.setCoWidth(SharedPerUtil.getScreenWidth() + "");
        cpListEntity.setCoHeight(viewHeight + "");
        cpListEntity.setTxList(textInfoList);
        backCpEntityToView(taskId, cpListEntity, "获取字母插播消息");
    }


    private void backCpEntityToView(String taskId, final CpListEntity cpListEntity, final String errorDesc) {
        if (listener == null) {
            return;
        }
        if (handler == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.backCpEntity(taskId, cpListEntity, errorDesc);
            }
        });
    }


    public interface GetTaskTxtInsertListener {
        void backCpEntity(String taskId, CpListEntity cpListEntity, String errorDesc);
    }


}
