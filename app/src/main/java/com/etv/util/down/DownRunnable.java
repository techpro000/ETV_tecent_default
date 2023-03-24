package com.etv.util.down;

import android.os.Handler;

import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.ys.http.download.DownloadTask;
import com.ys.http.download.OnDownloadListener;
import com.ys.http.network.HttpUtils;

import java.io.File;

public class DownRunnable implements Runnable {

    String downUrl;
    String saveUrl;
    DownStateListener listener;
    boolean isFalse = false;
    int LimitDownSpeed = -1;
    String taskId;
    private DownloadTask downTask;

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setLimitDownSpeed(int LimitDownSpeed) {
        this.LimitDownSpeed = LimitDownSpeed;
    }

    public void setIsDelFile(boolean isDelFile) {
        this.isFalse = isDelFile;
    }

    public DownRunnable(String downUrl, String saveUrl, DownStateListener listener) {
        MyLog.down("======下载地址= " + downUrl + "\n保存的地址==" + saveUrl);
        this.downUrl = downUrl;
        this.saveUrl = saveUrl;
        this.listener = listener;
        FileUtil.creatPathNotExcit("下载线程");
    }

    public DownRunnable() {
    }

    public void setDownInfo(String downUrl, String saveUrl, DownStateListener listener) {
        MyLog.down("======下载地址= " + downUrl + "\n保存的地址==" + saveUrl);
        this.downUrl = downUrl;
        this.saveUrl = saveUrl;
        this.listener = listener;
        FileUtil.creatPathNotExcit("下载线程");
    }


    @Override
    public void run() {
        //第一个参数:下载地址
        //第二个参数:文件存储路径
        //第四个参数:是否重命名
        //第五个参数:请求回调
        File fileDown = new File(saveUrl);
        //如果不需要断点续传，这里可以删除
        if (isFalse) {
            if (fileDown.exists()) {
                MyLog.down("======文件存在，删除文件");
                fileDown.delete();
            }
        }
        if (downUrl.startsWith("https:")) {
            downUrl = downUrl.replace("https:", "http:");
        }
        downTask = HttpUtils.downloadFile(saveUrl, downUrl, new OnDownloadListener() {
            @Override
            public void onProgress(int progress, long speed) {
                speed = speed / 1024;
                backState("下载中", DownFileEntity.DOWN_STATE_PROGRESS, progress, true, downUrl, saveUrl, (int) speed, taskId);
            }

            @Override
            public void onSuccess(String filePath) {
                backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);
            }

            @Override
            public void onFailure(Throwable e) {
                backState(e.getMessage(), DownFileEntity.DOWN_STATE_FAIED, 0, false, downUrl, saveUrl, -1, taskId);
            }
        });
    }

    public void stopDown() {
        if (downTask != null) {
            downTask.stop();
        }
    }

    private Handler handler = new Handler();

    private void backState(final String state, final int downState, final int progress, final boolean b,
                           final String downUrl, final String saveUrl, final int speed, final String taskId) {
        DownFileEntity entity = new DownFileEntity();
        entity.setDesc(state);
        entity.setDownState(downState);
        entity.setDownSpeed(speed);
        entity.setProgress(progress);
        entity.setTaskId(taskId);
        entity.setDown(b);
        entity.setDownPath(downUrl);
        entity.setSavePath(saveUrl);
        listener.downStateInfo(entity);

    }


}
