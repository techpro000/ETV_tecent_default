package com.etv.task.shiwei;

import android.os.Handler;
import android.util.Log;

import com.ys.model.entity.FileEntity;
import com.ys.model.util.FileMatch;
import com.etv.task.entity.MediAddEntity;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GetMediaShiWeiRunnable implements Runnable {

    String path;
    GetMediaFileListener listener;

    public GetMediaShiWeiRunnable(String path, GetMediaFileListener listener) {
        this.path = path;
        this.listener = listener;
    }

    @Override
    public void run() {
        getMediaFileListFromPath(path);
    }

    public void getMediaFileListFromPath(String path) {
        File root = new File(path);
        if (!root.exists()) {
            backListFileInfo("single文件不存在", false, null);
            return;
        }
        File[] files = root.listFiles();
        if (files == null || files.length < 1) {
            backListFileInfo("改目录下没有文件", false, null);
            return;
        }
        List<MediAddEntity> list_image = new ArrayList<MediAddEntity>();
        List<MediAddEntity> list_video = new ArrayList<MediAddEntity>();
        List<MediAddEntity> listsEntity = new ArrayList<MediAddEntity>();
        long fileSize;
        for (File file : files) {
            if (file.isDirectory()) {//过滤文件夹
                continue;
            }
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();
            fileSize = file.length();
            MediAddEntity entity = new MediAddEntity();
            int style_file = FileMatch.fileMatch(fileName);
            if (style_file == FileEntity.STYLE_FILE_IMAGE) {
                entity.setFileType(FileEntity.STYLE_FILE_IMAGE);
            } else if (style_file == FileEntity.STYLE_FILE_VIDEO) {
                entity.setFileType(FileEntity.STYLE_FILE_VIDEO);
            }
            entity.setUrl(filePath);
            int cartonAnim = SharedPerManager.getSinglePicAnimiType();
            int videoNum = SharedPerManager.getSingleVideoVoiceNum();
            int picDistanceTime = SharedPerManager.getPicDistanceTime();

            entity.setCartoon(cartonAnim + "");
            entity.setPlayParam(picDistanceTime + "");
            entity.setVolNum(videoNum + "");
            entity.setFileSize(fileSize);
            int fileType = entity.getFileType();
//            MyLog.media("==========遍历数据=====" + entity.toString());
            listsEntity.add(entity);
            if (fileType == FileEntity.STYLE_FILE_IMAGE) {
//                MyLog.media("==========遍历数据=====添加图片");
                list_image.add(entity);
            } else if (fileType == FileEntity.STYLE_FILE_VIDEO) {
//                MyLog.media("==========遍历数据=====添加视频");
                list_video.add(entity);
            }
        }
        listsEntity = sortFile(listsEntity);
        SingleTaskShiWeiEntity singleTaskShiWeiEntity = new SingleTaskShiWeiEntity();
        singleTaskShiWeiEntity.setList_image(list_image);
        singleTaskShiWeiEntity.setList_video(list_video);
        singleTaskShiWeiEntity.setListsEntity(listsEntity);
        backListFileInfo("添加完成", true, singleTaskShiWeiEntity);
    }

    /**
     * 根据命名排序
     *
     * @param fileList
     */
    private List<MediAddEntity> sortFile(List<MediAddEntity> fileList) {
        if (fileList == null || fileList.size() < 1) {
            return null;
        }
        Collections.sort(fileList, new Comparator<MediAddEntity>() {
            @Override
            public int compare(MediAddEntity o1, MediAddEntity o2) {
                String o1Path = o1.getUrl();
                String o2Path = o2.getUrl();
                String name1 = o1Path.substring(o1Path.lastIndexOf("/") + 1, o1Path.length());
                String name2 = o2Path.substring(o2Path.lastIndexOf("/") + 1, o2Path.length());
                return name1.compareTo(name2);
            }
        });
        return fileList;
    }


    private Handler handler = new Handler();

    private void backListFileInfo(String tag, final boolean isSuccess, SingleTaskShiWeiEntity singleTaskShiWeiEntity) {
        MyLog.cdl("========bianli wenjian ===" + tag + " / " + isSuccess);
        if (listener == null) {
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                MyLog.cdl("========返回的数据=== " + isSuccess);
                listener.backMediaFileList(isSuccess, singleTaskShiWeiEntity);
            }
        });
    }
}
