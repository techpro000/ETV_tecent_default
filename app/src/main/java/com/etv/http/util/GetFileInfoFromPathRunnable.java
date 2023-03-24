package com.etv.http.util;

import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单得获取文件得属性
 */
public class GetFileInfoFromPathRunnable implements Runnable {

    FileInfoGetListener fileInfoGetListener;
    private Handler handler = new Handler();
    String filePth;

    public GetFileInfoFromPathRunnable(String filePth, FileInfoGetListener fileInfoGetListener) {
        this.filePth = filePth;
        this.fileInfoGetListener = fileInfoGetListener;
    }

    List<File> liftFile = new ArrayList<File>();

    @Override
    public void run() {
        liftFile.clear();
        getFilesFromPath(filePth);
        backFileListInfoToView();
    }

    /**
     * 从Single文件夹里面获取所有的文件
     *
     * @param path
     */
    public void getFilesFromPath(String path) {
        File file = new File(path);
        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File value : files) {
                    if (value.isDirectory()) {
                        getFilesFromPath(value.getPath());
                    } else {
                        liftFile.add(value);
                    }
                }
            } else {
                liftFile.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void backFileListInfoToView() {
        if (fileInfoGetListener == null) {
            return;
        }
        if (handler == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                fileInfoGetListener.backFileList(liftFile);
            }
        });
    }

    public interface FileInfoGetListener {
        void backFileList(List<File> list);
    }


}
