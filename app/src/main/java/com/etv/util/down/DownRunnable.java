package com.etv.util.down;

import android.os.Handler;

import com.etv.listener.FileMd5CompaireListener;
import com.etv.util.FileUtil;
import com.etv.util.Md5Util;
import com.etv.util.MyLog;
import com.ys.http.download.DownloadTask;
import com.ys.http.download.OnDownloadListener;
import com.ys.http.network.HttpUtils;

import java.io.File;

public class DownRunnable implements Runnable {

    String downUrl;
    String saveUrl;
    long needDownFileLength;
    DownStateListener listener;
    boolean isFalse = false;
    int LimitDownSpeed = -1;
    String taskId;
    String fileMd5;
    private DownloadTask downTask;
    private String type = "";

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setLimitDownSpeed(int LimitDownSpeed) {
        this.LimitDownSpeed = LimitDownSpeed;
    }

    public void setIsDelFile(boolean isDelFile) {
        this.isFalse = isDelFile;
    }

    public DownRunnable(String type, String downUrl, String saveUrl, DownStateListener listener) {
        MyLog.down("======下载地址= " + downUrl + "\n保存的地址==" + saveUrl);
        this.downUrl = downUrl;
        this.saveUrl = saveUrl;
        this.listener = listener;
        this.type = type;
        FileUtil.creatPathNotExcit("下载线程");
    }

    public DownRunnable() {
    }

    public void setDownInfo(long needDownFileLength, String downUrl, String saveUrl, DownStateListener listener) {
        MyLog.down("======下载地址= " + downUrl + "\n保存的地址==" + saveUrl);
        this.needDownFileLength = needDownFileLength;
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
                if (type.equals("UPDATE_APK")) { //升级apk
                    backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);
                    return;
                }
                //任务文件下载成功
                File file = new File(filePath);
                Md5Util.compireFileMd5Info(fileMd5, file, new FileMd5CompaireListener() {
                    @Override
                    public void fileMd5CompaireStatues(boolean isSuccess, String errorDesc) {
                        MyLog.down("Md5比对完成状态=" + isSuccess + " / " + errorDesc);
                        if (isSuccess) {
                            backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);
                            return;
                        }
                        compaireFileLength(file);
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                MyLog.down("下载文件失败原因： " + e.getMessage());
                backState(e.getMessage(), DownFileEntity.DOWN_STATE_FAIED, 0, false, downUrl, saveUrl, -1, taskId);
            }
        });
    }

    private void compaireFileLength(File file) {
        if (!file.exists()) {
            MyLog.down("下载完成==文件不存在");
            backState("下载成功文件不存在", DownFileEntity.DOWN_STATE_FAIED, 0, false, downUrl, saveUrl, 0, taskId);
            return;
        }
        //下载资源文件
        if (file.length() == needDownFileLength) { //判断下载大小跟，服务器文件大小一致。 返回下载成功的状态
            backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);
            MyLog.down("下载成功==文件大小一致");
            return;
        }
        if (file.length() > needDownFileLength) { //如果下载大小超过，服务器文件大小， 返回下载失败状态
            MyLog.down("下载成功==文件大小不一致=" + needDownFileLength + " / " + file.length() + " / " + downUrl);
            FileUtil.deleteDirOrFile(file, "文件大小不一致");
            backState("下载文件大小超过服务器文件大小", DownFileEntity.DOWN_STATE_FAIED, 0, false, downUrl, saveUrl, -1, taskId);
            return;
        }
        backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);
        MyLog.down("下载异常==文件小于服务器文件=", true);
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
