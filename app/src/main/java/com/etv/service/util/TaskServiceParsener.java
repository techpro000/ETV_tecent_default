package com.etv.service.util;

import android.content.Context;

import com.etv.service.model.TaskWorkModel;
import com.etv.service.model.TaskWorkModelmpl;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;

import java.io.File;
import java.util.List;

public class TaskServiceParsener {

    TaskWorkModel taskWorkModel;
    Context context;

    public TaskServiceParsener(Context context) {
        this.context = context;
        taskWorkModel = new TaskWorkModelmpl();
    }

    /***
     * 上传录像文件到服务器
     * @param fileList
     */
    public void updateVideoFileToWeb(List<File> fileList) {
        if (context == null) {
            return;
        }
        taskWorkModel.updateVideoFileToWeb(context, fileList);
    }

    /***
     * 检测流量统计得
     */
    public void checkTrafficstatistics() {
        if (context == null) {
            return;
        }
        boolean isUpdateTraff = SharedPerManager.getIfUpdateTraffToWeb();
        if (!isUpdateTraff) {
            MyLog.traff( "====流量上传拦截===开关没有打开=");
            return;
        }
        taskWorkModel.checkTrafficstatistics(context);
    }

    public void startToCheckBggImage() {
        if (context == null) {
            return;
        }
        taskWorkModel.startToCheckBggImage(context);
    }


}
