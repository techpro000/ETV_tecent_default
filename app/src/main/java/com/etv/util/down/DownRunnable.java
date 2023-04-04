package com.etv.util.down;

import android.os.Handler;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.etv.util.FileUtil;
import com.etv.util.FileUtils;
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
    private DownloadTask downTask;
    private String type = "";

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
                MyLog.d("liujk", "下载文件成功： " + filePath + " 下载类型type： " + type);
                File file = new File(filePath);

                if(type.equals("UPDATE_APK")) { //升级apk

                    backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);
                }else  { //下载资源文件
                    if(file.length() == needDownFileLength) { //判断下载大小跟，服务器文件大小一致。 返回下载成功的状态
                        backState("下载成功", DownFileEntity.DOWN_STATE_SUCCESS, 100, false, downUrl, saveUrl, 0, taskId);

                    }else if(file.length() > needDownFileLength) { //如果下载大小超过，服务器文件大小， 返回下载失败状态

                        MyLog.d("liujk", "下载文件大小： " + file.length() + " 需要下载的大小: " + needDownFileLength);
                        FileUtils.deleteSingleFile(filePath);
                        backState("下载文件大小超过服务器文件大小", DownFileEntity.DOWN_STATE_FAIED, 0, false, downUrl, saveUrl, -1, taskId);
                    }
                }


            }

            @Override
            public void onFailure(Throwable e) {
                MyLog.d("liujk", "下载文件失败原因： " + e.getMessage());
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
